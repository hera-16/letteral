# フロントエンド統合ガイド

このドキュメントは、フロントエンド開発チームがバックエンドAPIと統合するためのクイックスタートガイドです。

## 目次

1. [認証フロー](#認証フロー)
2. [API使用例](#api使用例)
3. [WebSocket統合](#websocket統合)
4. [ルームIDパターン](#ルームidパターン)
5. [エラーハンドリング](#エラーハンドリング)
6. [ベストプラクティス](#ベストプラクティス)

---

## 認証フロー

### 1. ユーザー登録

```javascript
// POST /api/auth/register
const registerUser = async (userData) => {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      username: userData.username,
      email: userData.email,
      password: userData.password,
      displayName: userData.displayName
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Registration failed');
  }
  
  return await response.json();
};
```

### 2. ログイン

```javascript
// POST /api/auth/login
const loginUser = async (credentials) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      username: credentials.username,
      password: credentials.password
    })
  });
  
  const data = await response.json();
  
  if (response.ok) {
    // JWTトークンをローカルストレージに保存
    localStorage.setItem('jwt_token', data.token);
    return data;
  } else {
    throw new Error(data.message || 'Login failed');
  }
};
```

### 3. 認証ヘッダーの使用

```javascript
// すべての認証が必要なリクエストにJWTトークンを含める
const authenticatedFetch = async (url, options = {}) => {
  const token = localStorage.getItem('jwt_token');
  
  const headers = {
    ...options.headers,
    'Authorization': `Bearer ${token}`,
  };
  
  if (options.body && typeof options.body === 'object') {
    headers['Content-Type'] = 'application/json';
  }
  
  return fetch(url, {
    ...options,
    headers
  });
};
```

---

## API使用例

### フレンド管理

#### フレンド一覧取得

```javascript
const getFriends = async () => {
  const response = await authenticatedFetch('http://localhost:8080/api/friends');
  return await response.json();
};
```

#### フレンドリクエスト送信

```javascript
const sendFriendRequest = async (username) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/friends/request/${username}`,
    { method: 'POST' }
  );
  return await response.json();
};
```

#### フレンドリクエスト承認

```javascript
const acceptFriendRequest = async (friendshipId) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/friends/accept/${friendshipId}`,
    { method: 'POST' }
  );
  return await response.json();
};
```

#### 保留中のリクエスト取得

```javascript
const getPendingRequests = async () => {
  const response = await authenticatedFetch(
    'http://localhost:8080/api/friends/requests/pending'
  );
  return await response.json();
};
```

### グループ管理

#### 招待制グループ作成

```javascript
const createInviteOnlyGroup = async (groupData) => {
  const response = await authenticatedFetch(
    'http://localhost:8080/api/groups/invite',
    {
      method: 'POST',
      body: JSON.stringify({
        name: groupData.name,
        description: groupData.description,
        maxMembers: groupData.maxMembers || 50
      })
    }
  );
  return await response.json();
};
```

#### 招待コードでグループに参加

```javascript
const joinGroupByInviteCode = async (inviteCode) => {
  const response = await authenticatedFetch(
    'http://localhost:8080/api/groups/invite/join',
    {
      method: 'POST',
      body: JSON.stringify({ inviteCode })
    }
  );
  return await response.json();
};
```

#### パブリックトピック一覧取得

```javascript
const getPublicTopics = async () => {
  const response = await authenticatedFetch(
    'http://localhost:8080/api/groups/public'
  );
  return await response.json();
};
```

#### グループメンバー取得

```javascript
const getGroupMembers = async (groupId) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/groups/${groupId}/members`
  );
  return await response.json();
};
```

### トピック管理

#### トピック作成

```javascript
const createTopic = async (topicData) => {
  const response = await authenticatedFetch(
    'http://localhost:8080/api/topics',
    {
      method: 'POST',
      body: JSON.stringify({
        name: topicData.name,
        description: topicData.description,
        category: topicData.category
      })
    }
  );
  return await response.json();
};
```

#### カテゴリー別トピック取得

```javascript
const getTopicsByCategory = async (category) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/topics/category/${category}`
  );
  return await response.json();
};
```

#### トピック検索

```javascript
const searchTopics = async (query) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/topics/search?q=${encodeURIComponent(query)}`
  );
  return await response.json();
};
```

### チャットメッセージ履歴取得

#### グループメッセージ取得

```javascript
const getGroupMessages = async (groupId) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/chat/groups/${groupId}/messages`
  );
  return await response.json();
};
```

#### トピックメッセージ取得

```javascript
const getTopicMessages = async (topicId) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/chat/topics/${topicId}/messages`
  );
  return await response.json();
};
```

#### フレンドメッセージ取得

```javascript
const getFriendMessages = async (friendshipId) => {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/chat/friends/${friendshipId}/messages`
  );
  return await response.json();
};
```

---

## WebSocket統合

### 1. STOMP over WebSocketのセットアップ

#### インストール (npm/yarn)

```bash
npm install sockjs-client stompjs
# または
yarn add sockjs-client stompjs
```

#### 接続の初期化

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class ChatWebSocket {
  constructor() {
    this.stompClient = null;
    this.subscriptions = new Map();
  }
  
  connect(onConnected, onError) {
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);
    
    // JWTトークンをヘッダーに追加
    const token = localStorage.getItem('jwt_token');
    const headers = {
      'Authorization': `Bearer ${token}`
    };
    
    this.stompClient.connect(
      headers,
      (frame) => {
        console.log('Connected: ' + frame);
        if (onConnected) onConnected();
      },
      (error) => {
        console.error('Connection error: ', error);
        if (onError) onError(error);
      }
    );
  }
  
  disconnect() {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        console.log('Disconnected');
      });
    }
  }
  
  // ルームに参加
  subscribeToRoom(roomId, onMessageReceived) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket not connected');
      return;
    }
    
    // 既存のサブスクリプションがあれば解除
    if (this.subscriptions.has(roomId)) {
      this.subscriptions.get(roomId).unsubscribe();
    }
    
    // 新しいサブスクリプション作成
    const subscription = this.stompClient.subscribe(
      `/topic/${roomId}`,
      (message) => {
        const chatMessage = JSON.parse(message.body);
        if (onMessageReceived) onMessageReceived(chatMessage);
      }
    );
    
    this.subscriptions.set(roomId, subscription);
  }
  
  // ルームから退出
  unsubscribeFromRoom(roomId) {
    if (this.subscriptions.has(roomId)) {
      this.subscriptions.get(roomId).unsubscribe();
      this.subscriptions.delete(roomId);
    }
  }
  
  // メッセージ送信
  sendMessage(roomId, content, messageType = 'CHAT') {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket not connected');
      return;
    }
    
    const chatMessage = {
      content: content,
      roomId: roomId,
      messageType: messageType
    };
    
    this.stompClient.send(
      '/app/chat.sendMessage',
      {},
      JSON.stringify(chatMessage)
    );
  }
  
  // ユーザーがルームに参加したことを通知
  addUser(roomId, username) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket not connected');
      return;
    }
    
    const chatMessage = {
      sender: username,
      roomId: roomId,
      messageType: 'JOIN'
    };
    
    this.stompClient.send(
      '/app/chat.addUser',
      {},
      JSON.stringify(chatMessage)
    );
  }
}

// シングルトンインスタンス
export const chatWebSocket = new ChatWebSocket();
```

### 2. React統合例

```jsx
import React, { useEffect, useState } from 'react';
import { chatWebSocket } from './ChatWebSocket';

const ChatRoom = ({ roomId }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [connected, setConnected] = useState(false);
  
  useEffect(() => {
    // WebSocket接続
    chatWebSocket.connect(
      () => {
        setConnected(true);
        // ルームに参加
        chatWebSocket.subscribeToRoom(roomId, handleNewMessage);
        chatWebSocket.addUser(roomId, 'CurrentUser'); // 実際のユーザー名を使用
      },
      (error) => {
        console.error('Connection failed:', error);
        setConnected(false);
      }
    );
    
    // 過去のメッセージ履歴を取得
    loadMessageHistory();
    
    // クリーンアップ
    return () => {
      chatWebSocket.unsubscribeFromRoom(roomId);
    };
  }, [roomId]);
  
  const handleNewMessage = (message) => {
    setMessages((prevMessages) => [...prevMessages, message]);
  };
  
  const loadMessageHistory = async () => {
    try {
      const response = await authenticatedFetch(
        `http://localhost:8080/api/chat/messages/${roomId}`
      );
      const history = await response.json();
      setMessages(history.reverse()); // 古い順に並べる
    } catch (error) {
      console.error('Failed to load message history:', error);
    }
  };
  
  const sendMessage = (e) => {
    e.preventDefault();
    if (newMessage.trim() && connected) {
      chatWebSocket.sendMessage(roomId, newMessage);
      setNewMessage('');
    }
  };
  
  return (
    <div className="chat-room">
      <div className="connection-status">
        {connected ? '✓ Connected' : '✗ Disconnected'}
      </div>
      
      <div className="messages">
        {messages.map((msg, index) => (
          <div key={index} className={`message ${msg.messageType}`}>
            <span className="sender">{msg.sender}</span>
            <span className="content">{msg.content}</span>
            <span className="time">
              {new Date(msg.createdAt).toLocaleTimeString()}
            </span>
          </div>
        ))}
      </div>
      
      <form onSubmit={sendMessage} className="message-input">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Type a message..."
          disabled={!connected}
        />
        <button type="submit" disabled={!connected}>Send</button>
      </form>
    </div>
  );
};

export default ChatRoom;
```

---

## ルームIDパターン

バックエンドは異なるチャットタイプに対して特定のルームIDパターンを使用します:

| チャットタイプ | ルームIDパターン | 例 | WebSocketトピック |
|-------------|---------------|----|--------------------|
| グループチャット | `group-{groupId}` | `group-123` | `/topic/group-123` |
| トピックチャット | `topic-{topicId}` | `topic-456` | `/topic/topic-456` |
| フレンドチャット | `friend-{friendshipId}` | `friend-789` | `/topic/friend-789` |
| 汎用ルーム | 任意の文字列 | `general-chat` | `/topic/general-chat` |

### ルームID生成ユーティリティ

```javascript
const RoomIdUtils = {
  // グループチャットのルームID生成
  forGroup: (groupId) => `group-${groupId}`,
  
  // トピックチャットのルームID生成
  forTopic: (topicId) => `topic-${topicId}`,
  
  // フレンドチャットのルームID生成
  forFriend: (friendshipId) => `friend-${friendshipId}`,
  
  // ルームIDからタイプを判別
  getType: (roomId) => {
    if (roomId.startsWith('group-')) return 'GROUP';
    if (roomId.startsWith('topic-')) return 'TOPIC';
    if (roomId.startsWith('friend-')) return 'FRIEND';
    return 'GENERAL';
  },
  
  // ルームIDからIDを抽出
  extractId: (roomId) => {
    const match = roomId.match(/^(group|topic|friend)-(\d+)$/);
    return match ? parseInt(match[2]) : null;
  }
};

export default RoomIdUtils;
```

### 使用例

```javascript
// グループチャットを開く
const openGroupChat = (groupId) => {
  const roomId = RoomIdUtils.forGroup(groupId);
  chatWebSocket.subscribeToRoom(roomId, handleMessage);
};

// トピックチャットを開く
const openTopicChat = (topicId) => {
  const roomId = RoomIdUtils.forTopic(topicId);
  chatWebSocket.subscribeToRoom(roomId, handleMessage);
};

// フレンドチャットを開く
const openFriendChat = (friendshipId) => {
  const roomId = RoomIdUtils.forFriend(friendshipId);
  chatWebSocket.subscribeToRoom(roomId, handleMessage);
};
```

---

## エラーハンドリング

### API エラーレスポンス

バックエンドは以下の形式でエラーを返します:

```json
{
  "message": "エラーメッセージ",
  "status": 400,
  "timestamp": "2024-01-20T10:30:00"
}
```

### エラーハンドリングユーティリティ

```javascript
const ApiError = class extends Error {
  constructor(message, status, response) {
    super(message);
    this.status = status;
    this.response = response;
  }
};

const handleApiResponse = async (response) => {
  if (!response.ok) {
    let errorMessage = 'An error occurred';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch (e) {
      // JSONパースエラーの場合、デフォルトメッセージを使用
    }
    throw new ApiError(errorMessage, response.status, response);
  }
  
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return await response.json();
  }
  return await response.text();
};

// 使用例
const createGroupWithErrorHandling = async (groupData) => {
  try {
    const response = await authenticatedFetch(
      'http://localhost:8080/api/groups/invite',
      {
        method: 'POST',
        body: JSON.stringify(groupData)
      }
    );
    return await handleApiResponse(response);
  } catch (error) {
    if (error instanceof ApiError) {
      if (error.status === 401) {
        // 認証エラー: ログインページへリダイレクト
        console.error('Unauthorized. Please log in.');
        // redirect to login
      } else if (error.status === 403) {
        // 権限エラー
        console.error('Forbidden. You do not have permission.');
      } else if (error.status === 404) {
        // リソースが見つからない
        console.error('Resource not found.');
      } else {
        console.error('API Error:', error.message);
      }
    } else {
      console.error('Network error:', error);
    }
    throw error;
  }
};
```

### WebSocketエラーハンドリング

```javascript
class RobustChatWebSocket extends ChatWebSocket {
  constructor() {
    super();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
  }
  
  connect(onConnected, onError) {
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);
    
    const token = localStorage.getItem('jwt_token');
    const headers = {
      'Authorization': `Bearer ${token}`
    };
    
    this.stompClient.connect(
      headers,
      (frame) => {
        console.log('Connected: ' + frame);
        this.reconnectAttempts = 0; // リセット
        if (onConnected) onConnected();
      },
      (error) => {
        console.error('Connection error: ', error);
        
        // 再接続を試みる
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          console.log(`Reconnect attempt ${this.reconnectAttempts}...`);
          setTimeout(() => {
            this.connect(onConnected, onError);
          }, this.reconnectDelay);
        } else {
          console.error('Max reconnect attempts reached');
          if (onError) onError(error);
        }
      }
    );
  }
}

export const robustChatWebSocket = new RobustChatWebSocket();
```

---

## ベストプラクティス

### 1. JWTトークンのライフサイクル管理

```javascript
// トークンの有効期限をチェック
const isTokenExpired = () => {
  const token = localStorage.getItem('jwt_token');
  if (!token) return true;
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  } catch (e) {
    return true;
  }
};

// トークンを自動更新する(必要に応じて)
const ensureValidToken = async () => {
  if (isTokenExpired()) {
    // トークンをリフレッシュするか、再ログインを促す
    console.warn('Token expired. Please log in again.');
    // ログインページへリダイレクト
    window.location.href = '/login';
    return false;
  }
  return true;
};

// 認証付きフェッチを改善
const authenticatedFetch = async (url, options = {}) => {
  if (!(await ensureValidToken())) {
    throw new Error('Authentication required');
  }
  
  const token = localStorage.getItem('jwt_token');
  // ... 残りのコード
};
```

### 2. リアクティブな状態管理 (React Context例)

```jsx
import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('jwt_token'));
  
  useEffect(() => {
    if (token) {
      // トークンからユーザー情報を取得
      fetchUserInfo();
    }
  }, [token]);
  
  const login = async (credentials) => {
    const data = await loginUser(credentials);
    setToken(data.token);
    localStorage.setItem('jwt_token', data.token);
    setUser(data.user);
  };
  
  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('jwt_token');
    chatWebSocket.disconnect();
  };
  
  const fetchUserInfo = async () => {
    try {
      const response = await authenticatedFetch(
        'http://localhost:8080/api/users/me'
      );
      const userData = await response.json();
      setUser(userData);
    } catch (error) {
      console.error('Failed to fetch user info:', error);
      logout();
    }
  };
  
  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### 3. メッセージのページネーション

```javascript
const ChatRoomWithPagination = ({ roomId }) => {
  const [messages, setMessages] = useState([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  
  const loadMoreMessages = async () => {
    if (loading || !hasMore) return;
    
    setLoading(true);
    try {
      // 最古のメッセージIDを取得
      const oldestMessageId = messages.length > 0 
        ? messages[0].id 
        : null;
      
      const url = oldestMessageId
        ? `http://localhost:8080/api/chat/messages/${roomId}?before=${oldestMessageId}`
        : `http://localhost:8080/api/chat/messages/${roomId}`;
      
      const response = await authenticatedFetch(url);
      const newMessages = await response.json();
      
      if (newMessages.length === 0) {
        setHasMore(false);
      } else {
        setMessages((prev) => [...newMessages.reverse(), ...prev]);
      }
    } catch (error) {
      console.error('Failed to load more messages:', error);
    } finally {
      setLoading(false);
    }
  };
  
  // ... 残りのコード
};
```

### 4. タイピングインジケーター実装 (オプション)

```javascript
// バックエンドにタイピングイベントを追加する必要がある場合
const ChatWithTypingIndicator = ({ roomId }) => {
  const [typingUsers, setTypingUsers] = useState(new Set());
  let typingTimeout;
  
  const handleTyping = () => {
    // タイピングイベントを送信
    chatWebSocket.sendTypingIndicator(roomId);
    
    // 一定時間後にタイピング状態をクリア
    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
      chatWebSocket.sendStopTypingIndicator(roomId);
    }, 3000);
  };
  
  useEffect(() => {
    // タイピングイベントをサブスクライブ
    chatWebSocket.subscribeToTyping(roomId, (data) => {
      setTypingUsers((prev) => new Set([...prev, data.username]));
      
      // 5秒後に自動的に削除
      setTimeout(() => {
        setTypingUsers((prev) => {
          const newSet = new Set(prev);
          newSet.delete(data.username);
          return newSet;
        });
      }, 5000);
    });
  }, [roomId]);
  
  return (
    <div>
      {typingUsers.size > 0 && (
        <div className="typing-indicator">
          {Array.from(typingUsers).join(', ')} is typing...
        </div>
      )}
      {/* ... 残りのUI */}
    </div>
  );
};
```

### 5. オフライン対応

```javascript
// オフライン時のメッセージキューイング
class OfflineMessageQueue {
  constructor() {
    this.queue = JSON.parse(localStorage.getItem('offline_messages') || '[]');
  }
  
  addMessage(message) {
    this.queue.push(message);
    localStorage.setItem('offline_messages', JSON.stringify(this.queue));
  }
  
  async flushQueue() {
    if (this.queue.length === 0) return;
    
    const messages = [...this.queue];
    this.queue = [];
    localStorage.setItem('offline_messages', JSON.stringify(this.queue));
    
    for (const message of messages) {
      try {
        await chatWebSocket.sendMessage(
          message.roomId,
          message.content,
          message.messageType
        );
      } catch (error) {
        console.error('Failed to send queued message:', error);
        // 失敗したメッセージをキューに戻す
        this.addMessage(message);
      }
    }
  }
}

const offlineQueue = new OfflineMessageQueue();

// オンライン/オフライン検出
window.addEventListener('online', () => {
  console.log('Connection restored. Flushing message queue...');
  offlineQueue.flushQueue();
});

window.addEventListener('offline', () => {
  console.log('Connection lost. Messages will be queued.');
});
```

---

## 環境設定

### 開発環境 (.env.development)

```env
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_WS_URL=http://localhost:8080/ws
```

### 本番環境 (.env.production)

```env
REACT_APP_API_BASE_URL=https://api.yourapp.com
REACT_APP_WS_URL=https://api.yourapp.com/ws
```

### 環境変数の使用

```javascript
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;
const WS_URL = process.env.REACT_APP_WS_URL;

const authenticatedFetch = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`;
  // ... 残りのコード
};

const connectWebSocket = () => {
  const socket = new SockJS(WS_URL);
  // ... 残りのコード
};
```

---

## トラブルシューティング

### CORS エラー

フロントエンドが別のドメイン/ポートで動作している場合、CORSエラーが発生する可能性があります。バックエンドの`WebSecurityConfig.java`でCORS設定を確認してください。

```java
// WebSecurityConfig.java の CorsConfigurationSource
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // フロントエンドのURL
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

### WebSocket接続失敗

1. バックエンドが起動していることを確認
2. WebSocketエンドポイント (`/ws`) が正しいか確認
3. JWTトークンが有効か確認
4. ブラウザのコンソールでエラーメッセージを確認

### 認証トークンの問題

- トークンが`localStorage`に保存されているか確認
- トークンの形式が正しいか確認 (`Bearer {token}`)
- トークンの有効期限が切れていないか確認

---

## 次のステップ

1. **UIコンポーネントライブラリの選定**: Material-UI, Ant Design, Chakra UI など
2. **状態管理ライブラリの導入**: Redux, MobX, Zustand など
3. **リアルタイム通知の実装**: トースト通知、デスクトップ通知
4. **ファイルアップロード機能**: 画像、動画、ドキュメントの共有
5. **検索機能の強化**: メッセージ検索、ユーザー検索
6. **テーマ切り替え**: ダーク/ライトモード
7. **国際化 (i18n)**: 多言語対応

---

## サポート

質問や問題がある場合は、バックエンドチームに連絡してください。

- **APIリファレンス**: `docs/API_REFERENCE.md`
- **機能要件**: `docs/FEATURE_REQUIREMENTS.md`
- **バックエンドセットアップ**: `backend/README.md`
