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

// フレンド関連の型定義
export interface Friend {
  id: number;
  requester: User;
  addressee: User;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'BLOCKED';
  requestedAt: string;
  respondedAt?: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  displayName?: string;
  createdAt: string;
}

export interface FriendStats {
  totalFriends: number;
  pendingRequests: number;
}

// グループ関連の型定義
export interface Group {
  id: number;
  name: string;
  description?: string;
  groupType: 'INVITE_ONLY' | 'PUBLIC_TOPIC';
  inviteCode?: string;
  maxMembers: number;
  creator: User;
  createdAt: string;
}

export interface GroupMember {
  id: number;
  user: User;
  role: 'ADMIN' | 'MEMBER';
  joinedAt: string;
}

export interface CreateGroupRequest {
  name: string;
  description?: string;
  maxMembers?: number;
}

// トピック関連の型定義
export interface Topic {
  id: number;
  name: string;
  description?: string;
  category: string;
  creator: User;
  createdAt: string;
  isActive: boolean;
}

export interface CreateTopicRequest {
  name: string;
  description?: string;
  category: string;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  async signup(userData: SignupRequest): Promise<string> {
    const response = await api.post('/auth/register', userData);
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

export const friendService = {
  async getFriends(): Promise<User[]> {
    const response = await api.get('/friends/list');
    return response.data;
  },

  async getFriendStats(): Promise<FriendStats> {
    const response = await api.get('/friends/stats');
    return response.data;
  },

  async getPendingRequests(): Promise<Friend[]> {
    const response = await api.get('/friends/requests/pending');
    return response.data;
  },

  async getSentRequests(): Promise<Friend[]> {
    const response = await api.get('/friends/requests/sent');
    return response.data;
  },

  async sendFriendRequest(username: string): Promise<Friend> {
    const response = await api.post(`/friends/request/${username}`);
    return response.data;
  },

  async acceptFriendRequest(friendshipId: number): Promise<Friend> {
    const response = await api.post(`/friends/accept/${friendshipId}`);
    return response.data;
  },

  async rejectFriendRequest(friendshipId: number): Promise<Friend> {
    const response = await api.post(`/friends/reject/${friendshipId}`);
    return response.data;
  },

  async removeFriend(friendId: number): Promise<void> {
    await api.delete(`/friends/${friendId}`);
  },

  async blockUser(userId: number): Promise<Friend> {
    const response = await api.post(`/friends/block/${userId}`);
    return response.data;
  },

  async resetAllFriendships(): Promise<string> {
    const response = await api.delete('/friends/reset');
    return response.data;
  },

  async getAllFriendshipsWithDetails(): Promise<any[]> {
    const response = await api.get('/friends/debug/all');
    return response.data;
  },
};

export const groupService = {
  async createInviteOnlyGroup(data: CreateGroupRequest): Promise<Group> {
    const response = await api.post('/groups/invite', data);
    return response.data;
  },

  async getMyInviteOnlyGroups(): Promise<Group[]> {
    const response = await api.get('/groups/invite/my');
    return response.data;
  },

  async joinGroupByInviteCode(inviteCode: string): Promise<Group> {
    const response = await api.post('/groups/invite/join', { inviteCode });
    return response.data;
  },

  async getGroup(groupId: number): Promise<Group> {
    const response = await api.get(`/groups/${groupId}`);
    return response.data;
  },

  async getGroupMembers(groupId: number): Promise<GroupMember[]> {
    const response = await api.get(`/groups/${groupId}/members`);
    return response.data;
  },

  async getGroupMemberCount(groupId: number): Promise<number> {
    const response = await api.get(`/groups/${groupId}/members/count`);
    return response.data;
  },

  async leaveGroup(groupId: number): Promise<void> {
    await api.delete(`/groups/${groupId}/leave`);
  },

  async promoteToAdmin(groupId: number, memberId: number): Promise<GroupMember> {
    const response = await api.put(`/groups/${groupId}/members/${memberId}/promote`);
    return response.data;
  },

  async regenerateInviteCode(groupId: number): Promise<Group> {
    const response = await api.put(`/groups/${groupId}/invite-code/regenerate`);
    return response.data;
  },

  async getPublicTopics(): Promise<Group[]> {
    const response = await api.get('/groups/public');
    return response.data;
  },

  async joinPublicTopic(groupId: number): Promise<GroupMember> {
    const response = await api.post(`/groups/public/${groupId}/join`);
    return response.data;
  },
};

export const topicService = {
  async getAllTopics(): Promise<Topic[]> {
    const response = await api.get('/topics');
    return response.data;
  },

  async createTopic(data: CreateTopicRequest): Promise<Topic> {
    const response = await api.post('/topics', data);
    return response.data;
  },

  async getTopic(topicId: number): Promise<Topic> {
    const response = await api.get(`/topics/${topicId}`);
    return response.data;
  },

  async getTopicsByCategory(category: string): Promise<Topic[]> {
    const response = await api.get(`/topics/category/${category}`);
    return response.data;
  },

  async getAllCategories(): Promise<string[]> {
    const response = await api.get('/topics/categories');
    return response.data;
  },

  async searchTopics(query: string): Promise<Topic[]> {
    const response = await api.get(`/topics/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },

  async updateTopic(topicId: number, data: Partial<CreateTopicRequest>): Promise<Topic> {
    const response = await api.put(`/topics/${topicId}`, data);
    return response.data;
  },

  async deactivateTopic(topicId: number): Promise<Topic> {
    const response = await api.put(`/topics/${topicId}/deactivate`);
    return response.data;
  },

  async reactivateTopic(topicId: number): Promise<Topic> {
    const response = await api.put(`/topics/${topicId}/reactivate`);
    return response.data;
  },

  async deleteTopic(topicId: number): Promise<void> {
    await api.delete(`/topics/${topicId}`);
  },

  async getMyTopics(): Promise<Topic[]> {
    const response = await api.get('/topics/my');
    return response.data;
  },
};

export const chatService = {
  async getMessages(roomId: string): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/messages/${roomId}`);
    return response.data;
  },

  async getGroupMessages(groupId: number): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/groups/${groupId}/messages`);
    return response.data;
  },

  async getTopicMessages(topicId: number): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/topics/${topicId}/messages`);
    return response.data;
  },

  async getFriendMessages(friendshipId: number): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/friends/${friendshipId}/messages`);
    return response.data;
  },
};

export default api;