# チャットアプリケーション API リファレンス

最終更新: 2025年10月1日

## 概要

このドキュメントでは、チャットアプリケーションのバックエンドAPIの全エンドポイントを説明します。

---

## 認証 API (`/api/auth`)

### POST /api/auth/signup
ユーザー新規登録

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "displayName": "string"
}
```

### POST /api/auth/login
ログイン

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "string",
  "type": "Bearer",
  "id": "number",
  "username": "string",
  "email": "string",
  "displayName": "string"
}
```

---

## フレンド API (`/api/friends`)

### GET /api/friends
フレンド一覧取得

**Response:** `User[]`

### GET /api/friends/stats
フレンド統計取得

**Response:**
```json
{
  "friendCount": "number",
  "pendingRequestCount": "number"
}
```

### GET /api/friends/requests/pending
受信したフレンドリクエスト一覧

**Response:** `Friend[]`

### GET /api/friends/requests/sent
送信したフレンドリクエスト一覧

**Response:** `Friend[]`

### POST /api/friends/request/{username}
フレンドリクエスト送信

**Response:** `Friend`

### POST /api/friends/accept/{requestId}
フレンドリクエスト承認

**Response:** `Friend`

### POST /api/friends/reject/{requestId}
フレンドリクエスト拒否

**Response:** `Friend`

### DELETE /api/friends/{friendId}
フレンド削除

**Response:** `204 No Content`

### POST /api/friends/block/{targetUserId}
ユーザーブロック

**Response:** `Friend`

---

## グループ API (`/api/groups`)

### POST /api/groups/invite
招待制グループ作成

**Request Body:**
```json
{
  "name": "string",
  "description": "string",
  "maxMembers": "number (optional)"
}
```

**Response:** `Group`

### GET /api/groups/invite/my
参加中の招待制グループ一覧

**Response:** `Group[]`

### POST /api/groups/invite/join
招待コードでグループ参加

**Request Body:**
```json
{
  "inviteCode": "string"
}
```

**Response:** `Group`

### GET /api/groups/{groupId}
グループ詳細取得

**Response:** `Group`

### GET /api/groups/{groupId}/members
グループメンバー一覧

**Response:** `User[]`

### GET /api/groups/{groupId}/members/count
グループメンバー数取得

**Response:**
```json
{
  "count": "number"
}
```

### DELETE /api/groups/{groupId}/leave
グループ退出

**Response:** `204 No Content`

### PUT /api/groups/{groupId}/members/{memberId}/promote
メンバーを管理者に昇格

**Response:** `200 OK`

### PUT /api/groups/{groupId}/invite-code/regenerate
招待コード再生成

**Response:**
```json
{
  "inviteCode": "string"
}
```

### GET /api/groups/public
全公開トピック一覧

**Response:** `Group[]`

### POST /api/groups/public/{groupId}/join
公開トピック参加

**Response:** `Group`

---

## トピック API (`/api/topics`)

### GET /api/topics
全アクティブトピック一覧

**Response:** `Topic[]`

### POST /api/topics
トピック作成

**Request Body:**
```json
{
  "name": "string",
  "description": "string",
  "category": "string (optional)"
}
```

**Response:** `Topic`

### GET /api/topics/{topicId}
トピック詳細取得

**Response:** `Topic`

### GET /api/topics/category/{category}
カテゴリー別トピック一覧

**Response:** `Topic[]`

### GET /api/topics/categories
全カテゴリー一覧

**Response:** `string[]`

### GET /api/topics/search?q={searchTerm}
トピック名検索

**Response:** `Topic[]`

### PUT /api/topics/{topicId}
トピック更新

**Request Body:**
```json
{
  "name": "string (optional)",
  "description": "string (optional)",
  "category": "string (optional)"
}
```

**Response:** `Topic`

### PUT /api/topics/{topicId}/deactivate
トピック無効化

**Response:** `200 OK`

### PUT /api/topics/{topicId}/reactivate
トピック再有効化

**Response:** `200 OK`

### DELETE /api/topics/{topicId}
トピック削除

**Response:** `204 No Content`

### GET /api/topics/my
自分が作成したトピック一覧

**Response:** `Topic[]`

---

## チャットメッセージ API (`/api/chat`)

### GET /api/chat/messages/{roomId}
ルームのメッセージ履歴取得（汎用）

**Response:** `ChatMessageDto[]`

### GET /api/chat/groups/{groupId}/messages
グループチャットのメッセージ履歴

**Response:** `ChatMessageDto[]`

### GET /api/chat/topics/{topicId}/messages
トピックチャットのメッセージ履歴

**Response:** `ChatMessageDto[]`

### GET /api/chat/friends/{friendshipId}/messages
フレンドチャットのメッセージ履歴

**Response:** `ChatMessageDto[]`

---

## WebSocket エンドポイント

### 接続
```
ws://localhost:8080/ws
```

### メッセージ送信
**Destination:** `/app/chat.sendMessage`

**Payload:**
```json
{
  "content": "string",
  "senderUsername": "string",
  "roomId": "string",
  "messageType": "CHAT|JOIN|LEAVE"
}
```

### ユーザー参加通知
**Destination:** `/app/chat.addUser`

**Payload:**
```json
{
  "senderUsername": "string",
  "roomId": "string"
}
```

### メッセージ受信（購読）
- **通常ルーム:** `/topic/{roomId}`
- **グループチャット:** `/topic/group-{groupId}`
- **トピックチャット:** `/topic/topic-{topicId}`
- **フレンドチャット:** `/topic/friend-{friendshipId}`

---

## Room ID 命名規則

- **通常ルーム:** 任意の文字列（例: `"room1"`, `"lobby"`）
- **グループチャット:** `"group-{groupId}"` （例: `"group-1"`)
- **トピックチャット:** `"topic-{topicId}"` （例: `"topic-5"`)
- **フレンドチャット:** `"friend-{friendshipId}"` （例: `"friend-123"`)

---

## エラーレスポンス

すべてのエンドポイントで、エラー時には適切なHTTPステータスコードとエラーメッセージが返されます。

**例:**
```json
{
  "timestamp": "2025-10-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid invite code",
  "path": "/api/groups/invite/join"
}
```

---

## 認証ヘッダー

認証が必要なエンドポイント（`/api/auth/**`以外のすべて）では、以下のヘッダーが必要です：

```
Authorization: Bearer {token}
```

---

## データモデル

### User
```json
{
  "id": "number",
  "username": "string",
  "email": "string",
  "displayName": "string",
  "createdAt": "datetime"
}
```

### Friend
```json
{
  "id": "number",
  "requester": "User",
  "addressee": "User",
  "status": "PENDING|ACCEPTED|REJECTED|BLOCKED",
  "requestedAt": "datetime",
  "respondedAt": "datetime"
}
```

### Group
```json
{
  "id": "number",
  "name": "string",
  "description": "string",
  "groupType": "INVITE_ONLY|PUBLIC_TOPIC",
  "inviteCode": "string (INVITE_ONLY のみ)",
  "maxMembers": "number (optional)",
  "creator": "User",
  "createdAt": "datetime"
}
```

### Topic
```json
{
  "id": "number",
  "name": "string",
  "description": "string",
  "category": "string",
  "creator": "User",
  "createdAt": "datetime",
  "isActive": "boolean"
}
```

### ChatMessageDto
```json
{
  "id": "number",
  "content": "string",
  "senderUsername": "string",
  "senderDisplayName": "string",
  "roomId": "string",
  "messageType": "CHAT|JOIN|LEAVE",
  "timestamp": "string (yyyy-MM-dd HH:mm:ss)"
}
```
