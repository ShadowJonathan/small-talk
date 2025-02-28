package test

import io.mockk.MockKMatcherScope
import io.mockk.MockKVerificationScope
import io.mockk.coJustRun
import io.mockk.coVerify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext

fun runExpectTest(testBody: suspend ExpectTestScope.() -> Unit) {
    runTest { testBody(ExpectTest(coroutineContext)) }
}


class ExpectTest(override val coroutineContext: CoroutineContext) : ExpectTestScope {

    private val expects = mutableListOf<Pair<Int, suspend MockKVerificationScope.() -> Unit>>()

    override fun verifyExpects() = expects.forEach { (times, block) ->
        coVerify(exactly = times) { block.invoke(this) }
    }

    override fun <T> T.expectUnit(times: Int, block: suspend MockKMatcherScope.(T) -> Unit) {
        coJustRun { block(this@expectUnit) }.ignore()
        expects.add(times to { block(this@expectUnit) })
    }

}

private fun Any.ignore() = Unit

interface ExpectTestScope : CoroutineScope {
    fun verifyExpects()
    fun <T> T.expectUnit(times: Int = 1, block: suspend MockKMatcherScope.(T) -> Unit)
}