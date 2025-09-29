'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { io, Socket } from 'socket.io-client';
import api from '@/services/api';

interface Message {
  id: number;
  content: string;
  senderUsername: string;
  senderDisplayName: string;
  messageType: string;
  timestamp: string;
  roomId: string;
}

interface Group {
  id: number;
  name: string;
  description?: string;
  groupType: 'INVITE_ONLY' | 'PUBLIC_TOPIC';
}

interface AnonymousGroupChatProps {
  groupId: number;
}

const AnonymousGroupChat: React.FC<AnonymousGroupChatProps> = ({ groupId }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [group, setGroup] = useState<Group | null>(null);
  const [anonymousName, setAnonymousName] = useState<string>('');
  const socketRef = useRef<Socket | null>(null);
  const [loading, setLoading] = useState(true);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const extractErrorMessage = (err: unknown): string | null => {
    if (typeof err === 'string') {
      return err;
    }

    if (err && typeof err === 'object') {
      if ('message' in err && typeof (err as { message: unknown }).message === 'string') {
        return (err as { message: string }).message;
      }

      const response = (err as { response?: { data?: unknown } }).response;
      if (response?.data) {
        if (typeof response.data === 'string') {
          return response.data;
        }

        if (
          typeof response.data === 'object' &&
          response.data !== null &&
          'message' in response.data &&
          typeof (response.data as { message: unknown }).message === 'string'
        ) {
          return (response.data as { message: string }).message;
        }
      }
    }

    return null;
  };

  const fetchGroupInfo = useCallback(async () => {
    try {
      const response = await api.get<Group>(`/groups/${groupId}`);
      setGroup(response.data);
    } catch (error) {
      console.error('グループ情報取得エラー:', error);
    }
  }, [groupId]);

  const fetchMessages = useCallback(async () => {
    try {
      const response = await api.get<Message[]>(`/chat/groups/${groupId}/messages`);
      setMessages(response.data);
    } catch (error) {
      console.error('メッセージ取得エラー:', error);
      if (
        error &&
        typeof error === 'object' &&
        'response' in error &&
        (error as { response?: { status?: number } }).response?.status === 403
      ) {
        alert('このグループにアクセスする権限がありません');
      }
    } finally {
      setLoading(false);
    }
  }, [groupId]);

  const fetchAnonymousName = useCallback(async () => {
    try {
      const response = await api.get<string>(`/groups/${groupId}/anonymous-name`);
      setAnonymousName(response.data);
    } catch (error) {
      console.error('匿名名取得エラー:', error);
    }
  }, [groupId]);

  const initializeSocket = useCallback(() => {
    const newSocket = io('http://localhost:8080');

    newSocket.on('connect', () => {
      console.log('WebSocket connected');
      newSocket.emit('join', `group_${groupId}`);
    });

    newSocket.on(`group_${groupId}`, (message: Message) => {
      setMessages((prev) => [...prev, message]);
    });

    socketRef.current = newSocket;
    return newSocket;
  }, [groupId]);

  useEffect(() => {
    const socketInstance = initializeSocket();

    void fetchGroupInfo();
    void fetchMessages();
    void fetchAnonymousName();

    return () => {
      socketInstance.disconnect();
      socketRef.current = null;
    };
  }, [fetchAnonymousName, fetchGroupInfo, fetchMessages, initializeSocket]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const sendMessage = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      await api.post(`/chat/groups/${groupId}/anonymous`, {
        content: newMessage,
      });
      setNewMessage('');
    } catch (error) {
      console.error('メッセージ送信エラー:', error);
      const message = extractErrorMessage(error) ?? 'メッセージの送信に失敗しました';
      alert(message);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('ja-JP', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">読み込み中...</div>;
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-xl font-bold">{group?.name}</h1>
            {group?.description && (
              <p className="text-gray-600 text-sm">{group.description}</p>
            )}
          </div>
          <div className="text-right">
            <div className="text-sm text-gray-500">あなたの匿名名</div>
            <div className="font-mono text-lg text-blue-600">{anonymousName}</div>
            <div className="text-xs text-gray-400">
              {group?.groupType === 'INVITE_ONLY' ? '招待制グループ' : '公開グループ'}
            </div>
          </div>
        </div>
      </div>

      {/* メッセージエリア */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.length === 0 ? (
          <div className="text-center text-gray-500 mt-8">
            <p>まだメッセージがありません。</p>
            <p className="text-sm mt-2">最初のメッセージを投稿してみましょう！</p>
          </div>
        ) : (
          messages.map((message) => (
            <div key={message.id} className="bg-white rounded-lg p-3 shadow-sm">
              <div className="flex justify-between items-start mb-2">
                <span className="font-mono text-blue-600 font-medium">
                  {message.senderDisplayName}
                </span>
                <span className="text-xs text-gray-500">
                  {formatTime(message.timestamp)}
                </span>
              </div>
              <p className="text-gray-800 whitespace-pre-wrap">{message.content}</p>
              {message.messageType === 'ANONYMOUS_POST' && (
                <div className="mt-2 flex space-x-2">
                  <button className="text-xs text-gray-500 hover:text-yellow-600">
                    ⚠️ 通報
                  </button>
                  <button className="text-xs text-gray-500 hover:text-red-600">
                    ❌ 非表示
                  </button>
                </div>
              )}
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 入力エリア */}
      <div className="bg-white border-t border-gray-200 p-4">
        <form onSubmit={sendMessage} className="flex space-x-2">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="匿名でメッセージを投稿..."
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <button
            type="submit"
            disabled={!newMessage.trim()}
            className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            送信
          </button>
        </form>
        <div className="mt-2 text-xs text-gray-500">
          匿名名「{anonymousName}」として投稿されます • 毎日0:00に匿名名が変更されます
        </div>
      </div>
    </div>
  );
};

export default AnonymousGroupChat;