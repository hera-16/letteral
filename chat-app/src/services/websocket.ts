import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ChatMessage } from './api';

class WebSocketService {
  private stompClient: Client | null = null;
  private connected = false;

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
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

      this.stompClient.onConnect = (frame) => {
        this.connected = true;
        console.log('Connected: ' + frame);
        resolve();
      };

      this.stompClient.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
        reject(new Error('Connection failed'));
      };

      this.stompClient.activate();
    });
  }

  subscribe(roomId: string, onMessageReceived: (message: ChatMessage) => void) {
    if (this.stompClient && this.connected) {
      this.stompClient.subscribe(`/topic/${roomId}`, (message) => {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        onMessageReceived(chatMessage);
      });
    }
  }

  sendMessage(message: ChatMessage) {
    if (this.stompClient && this.connected) {
      this.stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message),
      });
    }
  }

  addUser(message: ChatMessage) {
    if (this.stompClient && this.connected) {
      this.stompClient.publish({
        destination: '/app/chat.addUser',
        body: JSON.stringify(message),
      });
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.connected = false;
    }
  }

  isConnected() {
    return this.connected;
  }
}

const webSocketService = new WebSocketService();

export { webSocketService };
export default webSocketService;