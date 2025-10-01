import { Client, IFrame } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ChatMessage } from './api';

class WebSocketService {
  private stompClient: Client | null = null;
  private connected = false;
  private connecting = false;
  private subscriptions: Map<string, any> = new Map();
  private connectionPromise: Promise<void> | null = null;

  connect(): Promise<void> {
    // 既に接続済みの場合
    if (this.connected && this.stompClient) {
      console.log('Already connected');
      return Promise.resolve();
    }

    // 接続中の場合は既存のPromiseを返す
    if (this.connecting && this.connectionPromise) {
      console.log('Connection in progress, waiting...');
      return this.connectionPromise;
    }

    // 新しい接続を開始
    this.connecting = true;
    this.connectionPromise = new Promise((resolve, reject) => {
      const socket = new SockJS('http://localhost:8080/ws');
      
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {},
        debug: (str) => {
          console.log('STOMP: ' + str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.onConnect = (frame: IFrame) => {
        this.connected = true;
        this.connecting = false;
        console.log('WebSocket connected successfully');
        resolve();
      };

      this.stompClient.onStompError = (frame: IFrame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
        this.connected = false;
        this.connecting = false;
        this.connectionPromise = null;
        reject(new Error('Connection failed'));
      };

      this.stompClient.onDisconnect = () => {
        this.connected = false;
        this.connecting = false;
        this.connectionPromise = null;
        this.subscriptions.clear();
        console.log('WebSocket disconnected');
      };

      this.stompClient.activate();
    });

    return this.connectionPromise;
  }

  subscribe(roomId: string, onMessageReceived: (message: ChatMessage) => void): () => void {
    if (!this.stompClient) {
      console.error('Cannot subscribe: stompClient is null');
      return () => {};
    }

    if (!this.connected) {
      console.error('Cannot subscribe: not connected yet. Connection state:', {
        connected: this.connected,
        connecting: this.connecting,
        stompClientActive: this.stompClient?.connected
      });
      return () => {};
    }

    // 既存のサブスクリプションがあれば解除
    if (this.subscriptions.has(roomId)) {
      console.log(`Unsubscribing existing subscription for /topic/${roomId}`);
      const oldSub = this.subscriptions.get(roomId);
      oldSub.unsubscribe();
    }

    console.log(`Subscribing to /topic/${roomId}`, {
      connected: this.connected,
      stompClientConnected: this.stompClient.connected
    });
    
    try {
      const subscription = this.stompClient.subscribe(`/topic/${roomId}`, (message) => {
        console.log(`Received message on /topic/${roomId}:`, message.body);
        try {
          const chatMessage: ChatMessage = JSON.parse(message.body);
          onMessageReceived(chatMessage);
        } catch (error) {
          console.error('Failed to parse message:', error);
        }
      });

      this.subscriptions.set(roomId, subscription);
      console.log(`Successfully subscribed to /topic/${roomId}`);

      // サブスクリプション解除用の関数を返す
      return () => {
        subscription.unsubscribe();
        this.subscriptions.delete(roomId);
        console.log(`Unsubscribed from /topic/${roomId}`);
      };
    } catch (error) {
      console.error(`Failed to subscribe to /topic/${roomId}:`, error);
      return () => {};
    }
  }

  sendMessage(message: ChatMessage) {
    if (this.stompClient && this.connected) {
      console.log('Sending message:', message);
      this.stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message),
      });
    } else {
      console.error('Cannot send message: not connected');
    }
  }

  addUser(message: ChatMessage) {
    if (this.stompClient && this.connected) {
      console.log('Adding user:', message);
      this.stompClient.publish({
        destination: '/app/chat.addUser',
        body: JSON.stringify(message),
      });
    } else {
      console.error('Cannot add user: not connected');
    }
  }

  disconnect() {
    if (this.stompClient) {
      // すべてのサブスクリプションを解除
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();
      
      this.stompClient.deactivate();
      this.connected = false;
      console.log('WebSocket service disconnected');
    }
  }

  isConnected() {
    return this.connected;
  }
}

export default new WebSocketService();