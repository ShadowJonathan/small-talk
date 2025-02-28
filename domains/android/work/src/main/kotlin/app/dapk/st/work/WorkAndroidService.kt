package app.dapk.st.work

import android.app.job.JobParameters
import android.app.job.JobService
import android.app.job.JobWorkItem
import app.dapk.st.core.extensions.Scope
import app.dapk.st.core.extensions.unsafeLazy
import app.dapk.st.core.module
import app.dapk.st.work.TaskRunner.RunnableWorkTask
import app.dapk.st.work.WorkScheduler.WorkTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class WorkAndroidService : JobService() {

    private val module by unsafeLazy { module<TaskRunnerModule>() }
    private val serviceScope = Scope(Dispatchers.IO)
    private var currentJob: Job? = null

    override fun onStartJob(params: JobParameters): Boolean {
        currentJob = serviceScope.launch {
            val results = module.taskRunner().run(params.collectAllTasks())
            results.forEach {
                when (it) {
                    is TaskRunner.TaskResult.Failure -> {
                        if (!it.canRetry) {
                            params.completeWork(it.source)
                        }
                    }
                    is TaskRunner.TaskResult.Success -> {
                        params.completeWork(it.source)
                    }
                }
            }

            val shouldReschedule = results.any { it is TaskRunner.TaskResult.Failure && it.canRetry }
            jobFinished(params, shouldReschedule)
        }
        return true
    }

    private fun JobParameters.collectAllTasks(): List<RunnableWorkTask> {
        var work: JobWorkItem?
        val tasks = mutableListOf<RunnableWorkTask>()
        do {
            work = this.dequeueWork()
            work?.intent?.also { intent ->
                tasks.add(
                    RunnableWorkTask(
                        source = work,
                        task = WorkTask(
                            jobId = this.jobId,
                            type = intent.getStringExtra("task-type")!!,
                            jsonPayload = intent.getStringExtra("task-payload")!!,
                        )
                    )
                )
            }
        } while (work != null)
        return tasks
    }

    override fun onStopJob(params: JobParameters): Boolean {
        currentJob?.cancel()
        return true
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}