package app.dapk.st.notifications

import android.app.NotificationManager
import android.content.Context
import app.dapk.st.core.ProvidableModule
import app.dapk.st.imageloader.IconLoader
import app.dapk.st.matrix.common.CredentialsStore
import app.dapk.st.matrix.push.PushService
import app.dapk.st.matrix.sync.RoomStore
import app.dapk.st.matrix.sync.SyncService
import app.dapk.st.push.RegisterFirebasePushTokenUseCase

class NotificationsModule(
    private val pushService: PushService,
    private val syncService: SyncService,
    private val credentialsStore: CredentialsStore,
    private val firebasePushTokenUseCase: RegisterFirebasePushTokenUseCase,
    private val iconLoader: IconLoader,
    private val roomStore: RoomStore,
    private val context: Context,
) : ProvidableModule {

    fun pushUseCase() = pushService
    fun syncService() = syncService
    fun credentialProvider() = credentialsStore
    fun firebasePushTokenUseCase() = firebasePushTokenUseCase
    fun notificationsUseCase() = NotificationsUseCase(
        roomStore,
        NotificationRenderer(notificationManager(), NotificationFactory(iconLoader, context)),
        NotificationChannels(notificationManager()),
    )

    private fun notificationManager() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
