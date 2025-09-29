import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
const TOKEN_STORAGE_KEY = 'token';
const USER_STORAGE_KEY = 'user';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// JWTトークンをリクエストヘッダーに追加するインターセプター
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (token) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
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

export interface StoredUser {
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

const toStoredUser = (payload: AuthResponse): StoredUser => ({
  id: payload.id,
  username: payload.username,
  email: payload.email,
  displayName: payload.displayName,
});

const persistSession = (payload: AuthResponse): StoredUser => {
  const user = toStoredUser(payload);
  localStorage.setItem(TOKEN_STORAGE_KEY, payload.accessToken);
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  return user;
};

const readStoredUser = (): StoredUser | null => {
  const raw = localStorage.getItem(USER_STORAGE_KEY);
  if (!raw) return null;

  try {
    const parsed = JSON.parse(raw) as StoredUser;
    if (typeof parsed.id === 'number' && typeof parsed.username === 'string' && typeof parsed.email === 'string') {
      return parsed;
    }
  } catch (error) {
    console.warn('Failed to parse stored user', error);
  }

  localStorage.removeItem(USER_STORAGE_KEY);
  return null;
};

export const authService = {
  async login(credentials: LoginRequest): Promise<StoredUser> {
    const response = await api.post<AuthResponse>('/auth/signin', credentials);
    return persistSession(response.data);
  },

  async signup(userData: SignupRequest): Promise<string> {
    const response = await api.post<string>('/auth/signup', userData);
    return response.data;
  },

  logout() {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
  },

  getCurrentUser(): StoredUser | null {
    return readStoredUser();
  },

  isAuthenticated(): boolean {
    return Boolean(localStorage.getItem(TOKEN_STORAGE_KEY));
  },
};

export const chatService = {
  async getMessages(roomId: string): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/messages/${roomId}`);
    return response.data;
  },
};

export default api;