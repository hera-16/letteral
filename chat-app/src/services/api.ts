import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// JWTトークンをリクエストヘッダーに追加するインターセプター
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  displayName?: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  displayName?: string;
}

export interface ChatMessage {
  id?: number;
  content: string;
  senderUsername: string;
  senderDisplayName?: string;
  roomId: string;
  messageType: string;
  timestamp?: string;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/signin', credentials);
    return response.data;
  },

  async signup(userData: SignupRequest): Promise<string> {
    const response = await api.post('/auth/signup', userData);
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!localStorage.getItem('token');
  },
};

export const chatService = {
  async getMessages(roomId: string): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/messages/${roomId}`);
    return response.data;
  },
};

export default api;