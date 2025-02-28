package app.dapk.st.matrix.sync.internal.sync

import app.dapk.st.matrix.common.RoomId
import fake.FakeMatrixLogger
import fake.FakeRoomStore
import fixture.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import test.expect

private val A_ROOM_OVERVIEW = aRoomOverview()
private val A_ROOM_MESSAGE_FROM_OTHER = aRoomMessageEvent(
    eventId = anEventId("a-new-message-event"),
    author = aRoomMember(id = aUserId("a-different-user"))
)

internal class UnreadEventsUseCaseTest {

    private val fakeRoomStore = FakeRoomStore()

    private val unreadEventsUseCase = UnreadEventsUseCase(
        fakeRoomStore,
        FakeMatrixLogger()
    )

    @Test
    fun `given initial sync when processing unread then does mark any events as unread`() = runTest {
        unreadEventsUseCase.processUnreadState(
            isInitialSync = true,
            overview = aRoomOverview(),
            previousState = null,
            newEvents = emptyList(),
            selfId = aUserId()
        )

        fakeRoomStore.verifyNoUnreadChanges()
    }

    @Test
    fun `given read marker has changed when processing unread then marks room read`() = runTest {
        fakeRoomStore.expect { it.markRead(RoomId(any())) }

        unreadEventsUseCase.processUnreadState(
            isInitialSync = false,
            overview = A_ROOM_OVERVIEW.copy(readMarker = anEventId("an-updated-marker")),
            previousState = A_ROOM_OVERVIEW,
            newEvents = emptyList(),
            selfId = aUserId()
        )

        fakeRoomStore.verifyRoomMarkedRead(A_ROOM_OVERVIEW.roomId)
    }

    @Test
    fun `given new events from other users when processing unread then inserts events as unread`() = runTest {
        fakeRoomStore.expect { it.insertUnread(RoomId(any()), any()) }

        unreadEventsUseCase.processUnreadState(
            isInitialSync = false,
            overview = A_ROOM_OVERVIEW,
            previousState = null,
            newEvents = listOf(A_ROOM_MESSAGE_FROM_OTHER),
            selfId = aUserId()
        )

        fakeRoomStore.verifyInsertsEvents(A_ROOM_OVERVIEW.roomId, listOf(A_ROOM_MESSAGE_FROM_OTHER.eventId))
    }
}
