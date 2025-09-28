'use client';

import { useState, useEffect, useRef } from 'react';
import { ChatMessage, chatService, authService } from '@/services/api';
import websocketService from '@/services/websocket';

interface ChatRoomProps {
  user: any;
  roomId: string;
  onLogout: () => void;
}

export default function ChatRoom({ user, roomId, onLogout }: ChatRoomProps) {
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

    // 過去のメッセージを取得
    const loadMessages = async () => {
      try {
        const pastMessages = await chatService.getMessages(roomId);
        setMessages(pastMessages); // サーバーから時系列順で取得
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
        await websocketService.connect((message: ChatMessage) => {
          // WebSocketで受信したメッセージのみをローカルステートに追加
          setMessages((prev) => {
            // 重複チェック（同じIDのメッセージが既に存在しないか確認）
            const exists = prev.some(m => m.id && m.id === message.id);
            if (exists) return prev;
            return [...prev, message];
          });
        });

        websocketService.subscribe(roomId, (message: ChatMessage) => {
          // ルーム固有のメッセージを受信
          setMessages((prev) => {
            const exists = prev.some(m => m.id && m.id === message.id);
            if (exists) return prev;
            return [...prev, message];
          });
        });

        // ユーザーが参加したことを通知
        websocketService.addUser({
          content: `${user.displayName || user.username} joined the chat`,
          senderUsername: user.username,
          roomId,
          messageType: 'JOIN',
        });

        setConnected(true);
      } catch (error) {
        console.error('Failed to connect WebSocket:', error);
        setConnected(false);
      }
    };

    loadMessages();
    connectWebSocket();

    return () => {
      websocketService.disconnect();
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
    <div className="flex flex-col h-screen bg-gray-100">
      {/* エラーメッセージ */}
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-md mx-6 mt-4">
          <div className="flex items-center justify-between">
            <span>{error}</span>
            <button
              onClick={() => setError(null)}
              className="text-red-700 hover:text-red-900"
            >
              ×
            </button>
          </div>
        </div>
      )}

      {/* ヘッダー */}
      <div className="bg-white shadow-sm border-b border-gray-200 px-6 py-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold text-gray-900">
              チャットルーム: {roomId}
            </h1>
            <p className="text-sm text-gray-600">
              {user.displayName || user.username} としてログイン中
            </p>
          </div>
          <div className="flex items-center space-x-4">
            <div className={`flex items-center space-x-2 ${connected ? 'text-green-600' : 'text-red-600'}`}>
              <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-400' : 'bg-red-400'}`}></div>
              <span className="text-sm">{connected ? '接続済み' : '未接続'}</span>
            </div>
            <button
              onClick={refreshMessages}
              className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              title="メッセージを更新"
            >
              🔄
            </button>
            <button
              onClick={onLogout}
              className="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
            >
              ログアウト
            </button>
          </div>
        </div>
      </div>

      {/* メッセージリスト */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        {messages.map((message, index) => (
          <div
            key={message.id || index}
            className={`${
              message.messageType === 'CHAT'
                ? message.senderUsername === user.username
                  ? 'flex justify-end'
                  : 'flex justify-start'
                : 'flex justify-center'
            }`}
          >
            {message.messageType === 'CHAT' ? (
              <div
                className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                  message.senderUsername === user.username
                    ? 'bg-blue-500 text-white'
                    : 'bg-white text-gray-900 border border-gray-200'
                }`}
              >
                <div className="text-xs opacity-75 mb-1">
                  {getMessageSenderName(message)}
                </div>
                <div>{message.content}</div>
                {message.timestamp && (
                  <div className="text-xs opacity-60 mt-1">
                    {message.timestamp}
                  </div>
                )}
              </div>
            ) : (
              <div className={`text-sm italic ${getMessageTypeColor(message.messageType)}`}>
                {message.content}
                {message.timestamp && (
                  <span className="ml-2 text-xs opacity-60">
                    {message.timestamp}
                  </span>
                )}
              </div>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* メッセージ入力フォーム */}
      <div className="bg-white border-t border-gray-200 px-6 py-4">
        <form onSubmit={sendMessage} className="flex space-x-4">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="メッセージを入力..."
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            disabled={!connected}
          />
          <button
            type="submit"
            disabled={!connected || !newMessage.trim()}
            className="px-6 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            送信
          </button>
        </form>
      </div>
    </div>
  );
}