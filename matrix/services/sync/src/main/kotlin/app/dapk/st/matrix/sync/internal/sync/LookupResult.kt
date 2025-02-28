package app.dapk.st.matrix.sync.internal.sync

import app.dapk.st.matrix.sync.RoomEvent
import app.dapk.st.matrix.sync.internal.request.ApiTimelineEvent

internal data class LookupResult(
    private val apiTimelineEvent: ApiTimelineEvent.TimelineText?,
    private val roomEvent: RoomEvent?,
) {

    inline fun <T> fold(
        onApiTimelineEvent: (ApiTimelineEvent.TimelineText) -> T?,
        onRoomEvent: (RoomEvent) -> T?,
        onEmpty: () -> T?,
    ): T? {
        return when {
            apiTimelineEvent != null -> onApiTimelineEvent(apiTimelineEvent)
            roomEvent != null -> onRoomEvent(roomEvent)
            else -> onEmpty()
        }
    }
}