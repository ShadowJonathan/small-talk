package internalfixture

import app.dapk.st.matrix.common.*
import app.dapk.st.matrix.sync.internal.request.*
import app.dapk.st.matrix.sync.internal.request.ApiTimelineEvent.RoomMember.Content.Membership
import fixture.*

internal fun anApiSyncRoom(
    timeline: ApiSyncRoomTimeline = anApiSyncRoomTimeline(),
    state: ApiSyncRoomState = anApiSyncRoomState(),
    accountData: ApiAccountData? = null,
    ephemeral: ApiEphemeral? = null,
) = ApiSyncRoom(timeline, state, accountData, ephemeral)

internal fun anApiSyncRoomTimeline(
    apiTimelineEvents: List<ApiTimelineEvent> = emptyList(),
) = ApiSyncRoomTimeline(apiTimelineEvents)

internal fun anApiSyncRoomState(
    stateEvents: List<ApiTimelineEvent> = emptyList(),
) = ApiSyncRoomState(stateEvents)

internal fun anApiEphemeral(
    events: List<ApiEphemeralEvent> = emptyList()
) = ApiEphemeral(events)

internal fun anEphemeralTypingEvent(
    userIds: List<UserId> = emptyList(),
) = ApiEphemeralEvent.Typing(ApiEphemeralEvent.Typing.Content(userIds))

internal fun anApiTimelineTextEvent(
    id: EventId = anEventId(),
    senderId: UserId = aUserId(),
    content: ApiTimelineEvent.TimelineText.Content = aTimelineTextEventContent(),
    utcTimestamp: Long = 0L,
    decryptionStatus: ApiTimelineEvent.DecryptionStatus? = null
) = ApiTimelineEvent.TimelineText(id, senderId, content, utcTimestamp, decryptionStatus)

internal fun aTimelineTextEventContent(
    body: String? = null,
    formattedBody: String? = null,
    type: String? = null,
    relation: ApiTimelineEvent.TimelineText.Relation? = null,
) = ApiTimelineEvent.TimelineText.Content(body, formattedBody, type, relation)

internal fun anEditRelation(originalId: EventId) = ApiTimelineEvent.TimelineText.Relation(
    relationType = "m.replace",
    inReplyTo = null,
    eventId = originalId,
)

internal fun aReplyRelation(originalId: EventId) = ApiTimelineEvent.TimelineText.Relation(
    relationType = null,
    eventId = null,
    inReplyTo = ApiTimelineEvent.TimelineText.InReplyTo(originalId),
)

internal fun anEncryptedApiTimelineEvent(
    senderId: UserId = aUserId(),
    encryptedContent: ApiEncryptedContent = aMegolmApiEncryptedContent(),
    eventId: EventId = anEventId(),
    utcTimestamp: Long = 0L,
) = ApiTimelineEvent.Encrypted(senderId, encryptedContent, eventId, utcTimestamp)

internal fun anEncryptionApiTimelineEvent(
    algorithm: AlgorithmName = AlgorithmName("an-algorithm"),
    rotationMs: Long? = null,
    rotationMessages: Long? = null,
) = ApiTimelineEvent.Encryption(ApiTimelineEvent.Encryption.Content(algorithm, rotationMs, rotationMessages))

internal fun aRoomAvatarApiTimelineEvent(
    eventId: EventId = anEventId(),
    url: MxUrl? = null
) = ApiTimelineEvent.RoomAvatar(eventId, ApiTimelineEvent.RoomAvatar.Content(url))

internal fun aRoomCreateApiTimelineEvent(
    eventId: EventId = anEventId(),
    utcTimestamp: Long = 0L,
    type: String? = null
) = ApiTimelineEvent.RoomCreate(eventId, utcTimestamp, ApiTimelineEvent.RoomCreate.Content(type))

internal fun aRoomMemberApiTimelineEvent(
    eventId: EventId = anEventId(),
    senderId: UserId = aUserId(),
    displayName: String? = null,
    membership: Membership = Membership("join"),
    avatarUrl: MxUrl? = null,
) = ApiTimelineEvent.RoomMember(eventId, ApiTimelineEvent.RoomMember.Content(displayName, membership, avatarUrl), senderId)

internal fun aRoomNameApiTimelineEvent(
    eventId: EventId = anEventId(),
    name: String = "a-room-name"
) = ApiTimelineEvent.RoomName(eventId, ApiTimelineEvent.RoomName.Content(name))

internal fun aRoomTopicApiTimelineEvent(
    eventId: EventId = anEventId(),
    topic: String = "a-room-topic"
) = ApiTimelineEvent.RoomTopic(eventId, ApiTimelineEvent.RoomTopic.Content(topic))

internal fun anIgnoredApiTimelineEvent() = ApiTimelineEvent.Ignored

internal fun aMegolmApiEncryptedContent(
    cipherText: CipherText = aCipherText(),
    deviceId: DeviceId = aDeviceId(),
    senderKey: String = "a-sender-key",
    sessionId: SessionId = aSessionId(),
    relation: ApiTimelineEvent.TimelineText.Relation? = null,
) = ApiEncryptedContent.MegOlmV1(cipherText, deviceId, senderKey, sessionId, relation)

internal fun anOlmApiEncryptedContent(
    cipherText: Map<Curve25519, ApiEncryptedContent.CipherTextInfo> = mapOf(aCurve25519() to aCipherTextInfo()),
    senderKey: Curve25519 = aCurve25519(),
) = ApiEncryptedContent.OlmV1(cipherText, senderKey)

internal fun aCipherTextInfo(
    body: CipherText = aCipherText(),
    type: Int = 0,
) = ApiEncryptedContent.CipherTextInfo(body, type)

internal fun anUnknownApiEncryptedContent() = ApiEncryptedContent.Unknown