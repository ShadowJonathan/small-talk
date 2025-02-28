package app.dapk.st.domain

import app.dapk.db.DapkDb
import app.dapk.st.core.CoroutineDispatchers
import app.dapk.st.core.extensions.ErrorTracker
import app.dapk.st.core.extensions.unsafeLazy
import app.dapk.st.domain.eventlog.EventLogPersistence
import app.dapk.st.domain.localecho.LocalEchoPersistence
import app.dapk.st.domain.profile.ProfilePersistence
import app.dapk.st.domain.sync.OverviewPersistence
import app.dapk.st.domain.sync.RoomPersistence
import app.dapk.st.matrix.common.CredentialsStore
import app.dapk.st.matrix.message.LocalEchoStore
import app.dapk.st.matrix.room.MemberStore
import app.dapk.st.matrix.room.ProfileStore
import app.dapk.st.matrix.sync.FilterStore
import app.dapk.st.matrix.sync.OverviewStore
import app.dapk.st.matrix.sync.RoomStore
import app.dapk.st.matrix.sync.SyncStore

class StoreModule(
    private val database: DapkDb,
    private val databaseDropper: DatabaseDropper,
    private val preferences: Preferences,
    private val credentialPreferences: Preferences,
    private val errorTracker: ErrorTracker,
    private val coroutineDispatchers: CoroutineDispatchers,
) {

    fun overviewStore(): OverviewStore = OverviewPersistence(database, coroutineDispatchers)
    fun roomStore(): RoomStore = RoomPersistence(database, OverviewPersistence(database, coroutineDispatchers), coroutineDispatchers)
    fun credentialsStore(): CredentialsStore = CredentialsPreferences(credentialPreferences)
    fun syncStore(): SyncStore = SyncTokenPreferences(preferences)
    fun filterStore(): FilterStore = FilterPreferences(preferences)
    val localEchoStore: LocalEchoStore by unsafeLazy { LocalEchoPersistence(errorTracker, database) }

    fun olmStore() = OlmPersistence(database, credentialsStore())
    fun knownDevicesStore() = DevicePersistence(database, KnownDevicesCache(), coroutineDispatchers)

    fun profileStore(): ProfileStore = ProfilePersistence(preferences)

    fun cacheCleaner() = StoreCleaner { cleanCredentials ->
        if (cleanCredentials) {
            credentialPreferences.clear()
        }
        preferences.clear()
        databaseDropper.dropAllTables(includeCryptoAccount = cleanCredentials)
    }

    fun eventLogStore(): EventLogPersistence {
        return EventLogPersistence(database, coroutineDispatchers)
    }

    fun memberStore(): MemberStore {
        return MemberPersistence(database, coroutineDispatchers)
    }
}
