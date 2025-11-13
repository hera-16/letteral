'use client';

import { useState, useEffect, useRef } from 'react';
import { ChatMessage, chatService, authService } from '@/services/api';
import websocketService from '@/services/websocket';

interface ChatRoomProps {
  user: any;
  roomId: string;
  chatType?: 'friend' | 'group' | 'topic' | 'general';
  chatId?: number;
  onLogout?: () => void; // オプショナルに変更
}

export default function ChatRoom({ user, roomId, chatType, chatId, onLogout }: ChatRoomProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    // 認証状態をチェック
    if (!authService.isAuthenticated()) {
      console.error('User not authenticated');
      setError('ログインが必要です');
      return;
    }

    let unsubscribe: (() => void) | null = null;

    // 過去のメッセージを取得
    const loadMessages = async () => {
      try {
        console.log('Loading messages for roomId:', roomId);
        const pastMessages = await chatService.getMessages(roomId);

        // JOIN/LEAVEメッセージをフィルタリング
        const filteredMessages = pastMessages.filter(
          (msg: ChatMessage) => msg.messageType !== 'JOIN' && msg.messageType !== 'LEAVE'
        );
        setMessages(filteredMessages); // サーバーから時系列順で取得
        console.log('Messages loaded:', filteredMessages.length, 'messages');
      } catch (err: any) {
        console.error('Failed to load messages:', err);
        if (err.response?.status === 401) {
          setError('認証が無効です。再度ログインしてください。');
        } else {
          setError('メッセージの読み込みに失敗しました');
        }
      }
    };

    // WebSocket接続を確立
    const connectWebSocket = async () => {
      try {
        console.log('Starting WebSocket connection...');
        
        // WebSocket接続を確立（引数なし）
        await websocketService.connect();
        
        console.log('WebSocket connected, checking connection state...');
        
        // 接続が確立されるまで少し待つ
        await new Promise(resolve => setTimeout(resolve, 100));

        // ルーム固有のメッセージを購読
        console.log(`Attempting to subscribe to room: ${roomId}`);
        unsubscribe = websocketService.subscribe(roomId, (message: ChatMessage) => {
          console.log('Received message in ChatRoom:', message);
          // JOIN/LEAVEメッセージは表示しない
          if (message.messageType === 'JOIN' || message.messageType === 'LEAVE') {
            return;
          }
          setMessages((prev) => {
            // 重複チェック（同じIDのメッセージが既に存在しないか確認）
            const exists = prev.some(m => m.id && m.id === message.id);
            if (exists) {
              console.log('Message already exists, skipping:', message.id);
              return prev;
            }
            console.log('Adding new message to state:', message.id);
            return [...prev, message];
          });
        });

        setConnected(true);
        console.log('WebSocket connection setup complete');
      } catch (error) {
        console.error('Failed to connect WebSocket:', error);
        setConnected(false);
        setError('WebSocket接続に失敗しました');
      }
    };

    loadMessages();
    connectWebSocket();

    return () => {
      // クリーンアップ: サブスクリプション解除
      if (unsubscribe) {
        unsubscribe();
      }
      // 注意: disconnect()は他のルームで使用している可能性があるため呼ばない
    };
  }, [user, roomId]);

  const sendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (newMessage.trim() && connected) {
      const message: ChatMessage = {
        content: newMessage.trim(),
        senderUsername: user.username,
        senderDisplayName: user.displayName,
        roomId,
        messageType: 'CHAT',
      };

      // メッセージをサーバーに送信（ローカルステートには追加しない）
      // WebSocketで戻ってきたメッセージをステートに追加する
      websocketService.sendMessage(message);
      setNewMessage('');
    }
  };

  // 定期的にサーバーからメッセージを同期（オプション）
  const refreshMessages = async () => {
    try {
      const freshMessages = await chatService.getMessages(roomId);
      setMessages(freshMessages); // サーバーから時系列順で取得
    } catch (error) {
      console.error('Failed to refresh messages:', error);
    }
  };

  const getMessageSenderName = (message: ChatMessage) => {
    return message.senderDisplayName || message.senderUsername;
  };

  const getMessageTypeColor = (messageType: string) => {
    switch (messageType) {
      case 'JOIN':
        return 'text-green-600';
      case 'LEAVE':
        return 'text-red-600';
      default:
        return 'text-gray-900';
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '384px', backgroundColor: '#393E46', border: '1px solid #00ADB5', borderRadius: '0.5rem' }}>
      {/* エラーメッセージ */}
      {error && (
        <div style={{ backgroundColor: '#393E46', border: '1px solid #ff6b6b', color: '#ff6b6b', padding: '1rem', borderRadius: '0.375rem', margin: '1rem' }}>
          <div className="flex items-center justify-between">
            <span>{error}</span>
            <button
              onClick={() => setError(null)}
              style={{ color: '#ff6b6b', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem' }}
            >
              ×
            </button>
          </div>
        </div>
      )}

      {/* ステータスバー */}
      <div style={{ backgroundColor: '#222831', borderBottom: '1px solid #00ADB5', padding: '0.5rem 1rem' }}>
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div className={`flex items-center space-x-2 ${connected ? 'text-green-600' : 'text-red-600'}`}>
              <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-400' : 'bg-red-400'}`}></div>
              <span style={{ fontSize: '0.875rem', color: '#EEEEEE' }}>{connected ? '接続済み' : '未接続'}</span>
            </div>
            <span style={{ fontSize: '0.875rem', color: '#00ADB5' }}>
              {user.displayName || user.username} としてログイン中
            </span>
          </div>
          <button
            onClick={refreshMessages}
            style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem', fontWeight: 500, color: '#EEEEEE', backgroundColor: '#00ADB5', border: 'none', borderRadius: '0.375rem', cursor: 'pointer' }}
            title="メッセージを更新"
          >
            🔄
          </button>
        </div>
      </div>

      {/* メッセージリスト */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '1rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {messages.map((message, index) => {
          const isMyMessage = message.senderUsername === user.username;
          
          return (
            <div
              key={message.id || index}
              className={`flex ${
                message.messageType === 'CHAT'
                  ? isMyMessage
                    ? 'justify-end'
                    : 'justify-start'
                  : 'justify-center'
              }`}
            >
              {message.messageType === 'CHAT' ? (
                <div
                  style={{
                    maxWidth: '75%',
                    padding: '0.5rem 1rem',
                    borderRadius: '0.5rem',
                    boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                    backgroundColor: isMyMessage ? '#00ADB5' : '#222831',
                    color: '#EEEEEE',
                    borderBottomRightRadius: isMyMessage ? '0' : '0.5rem',
                    borderBottomLeftRadius: isMyMessage ? '0.5rem' : '0'
                  }}
                >
                  {!isMyMessage && (
                    <div style={{ fontSize: '0.75rem', fontWeight: 600, marginBottom: '0.25rem', opacity: 0.75 }}>
                      {getMessageSenderName(message)}
                    </div>
                  )}
                  <div style={{ wordBreak: 'break-word' }}>{message.content}</div>
                  {message.timestamp && (
                    <div style={{ fontSize: '0.75rem', marginTop: '0.25rem', opacity: isMyMessage ? 0.7 : 0.5 }}>
                      {message.timestamp}
                    </div>
                  )}
                </div>
              ) : (
                <div style={{ fontSize: '0.875rem', fontStyle: 'italic' }} className={getMessageTypeColor(message.messageType)}>
                  {message.content}
                  {message.timestamp && (
                    <span style={{ marginLeft: '0.5rem', fontSize: '0.75rem', opacity: 0.6 }}>
                      {message.timestamp}
                    </span>
                  )}
                </div>
              )}
            </div>
          );
        })}
        <div ref={messagesEndRef} />
      </div>

      {/* メッセージ入力フォーム */}
      <div style={{ backgroundColor: '#222831', borderTop: '1px solid #00ADB5', padding: '1rem' }}>
        <form onSubmit={sendMessage} className="flex space-x-4">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="メッセージを入力..."
            style={{
              flex: 1,
              padding: '0.5rem 1rem',
              backgroundColor: '#393E46',
              color: '#EEEEEE',
              border: '1px solid #00ADB5',
              borderRadius: '0.5rem',
              outline: 'none'
            }}
            disabled={!connected}
          />
          <button
            type="submit"
            disabled={!connected || !newMessage.trim()}
            style={{
              padding: '0.5rem 1.5rem',
              fontSize: '0.875rem',
              fontWeight: 500,
              color: '#EEEEEE',
              backgroundColor: '#00ADB5',
              border: 'none',
              borderRadius: '0.5rem',
              cursor: connected && newMessage.trim() ? 'pointer' : 'not-allowed',
              opacity: connected && newMessage.trim() ? 1 : 0.5
            }}
          >
            送信
          </button>
        </form>
      </div>
    </div>
  );
}