package fake

import app.dapk.st.matrix.common.RoomId
import app.dapk.st.matrix.sync.SyncService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import test.delegateReturn

class FakeSyncService : SyncService by mockk() {
    fun givenSyncs() {
        every { startSyncing() }.returns(flowOf(Unit))
    }

    fun givenRoom(roomId: RoomId) = every { room(roomId) }.delegateReturn()

    fun givenEvents(roomId: RoomId) = every { events() }.delegateReturn()

}
