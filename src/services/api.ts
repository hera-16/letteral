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
    // ログイン・サインアップ時はトークンを送信しない
    const isAuthEndpoint = config.url?.includes('/auth/login') || config.url?.includes('/auth/register') || config.url?.includes('/auth/signup');
    
    if (!isAuthEndpoint) {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        console.log('🔐 Request to:', config.url, '| Token length:', token.length, '| First 20 chars:', token.substring(0, 20));
      } else {
        console.warn('⚠️ No token found for request to:', config.url);
      }
    } else {
      console.log('🔓 Auth endpoint - no token required:', config.url);
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 401エラー時に自動ログアウトするレスポンスインターセプター
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // トークンが無効または期限切れの場合、ログアウト
      const isAlreadyOnLoginPage = typeof window !== 'undefined' && window.location.pathname === '/';
      
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      if (typeof window !== 'undefined' && !isAlreadyOnLoginPage) {
        // ユーザーに通知してからログイン画面へ遷移
        console.warn('セッションが期限切れです。再度ログインしてください。');
        // 次のイベントループでリダイレクトすることで、エラーメッセージが先に表示される
        setTimeout(() => {
          window.location.href = '/';
        }, 100);
      }
    }
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

export interface FriendWithId {
  friendshipId: number;
  userId: number;
  username: string;
  displayName: string;
  email: string;
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

export interface ChallengeCompletionSummary {
  id: number;
  challenge: DailyChallengeSummary;
  note?: string;
  completedAt: string;
  pointsEarned: number;
}

export interface DailyChallengeSummary {
  id: number;
  title: string;
  description?: string;
  challengeType: string;
  difficultyLevel?: string;
  points: number;
}

export type ChallengeShareReactionType = 'ENCOURAGE' | 'EMPATHY' | 'AWESOME';

export interface ChallengeShareUserSummary {
  id: number;
  username: string;
  displayName?: string;
}

export interface ChallengeShareChallengeSummary {
  id: number;
  title: string;
  challengeType: string;
  points: number;
}

export interface ChallengeShare {
  id: number;
  user: ChallengeShareUserSummary;
  challenge: ChallengeShareChallengeSummary;
  comment: string;
  mood?: string;
  sharedAt: string;
  reactions: Partial<Record<ChallengeShareReactionType, number>>;
  userReaction?: ChallengeShareReactionType | null;
}

export interface PagedChallengeShares {
  shares: ChallengeShare[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

// バッジ関連の型定義
export interface Badge {
  id: number;
  name: string;
  description: string;
  badgeType: string;
  icon: string;
  requirementValue: number;
  createdAt: string;
}

export interface UserBadge {
  id: number;
  badge: Badge;
  earnedAt: string;
  isNew: boolean;
}

// グループ関連の型定義
export interface Group {
  id: number;
  name: string;
  description?: string;
  groupType: 'INVITE_ONLY';
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

  async getFriendsWithIds(): Promise<FriendWithId[]> {
    const response = await api.get('/friends/list/detailed');
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

  async updateGroup(groupId: number, data: { name: string; description: string }): Promise<Group> {
    const response = await api.put(`/groups/${groupId}`, data);
    return response.data;
  },

  async addGroupMember(groupId: number, username: string): Promise<User[]> {
    const response = await api.post(`/groups/${groupId}/members`, { username });
    return response.data;
  },

  async removeGroupMember(groupId: number, memberId: number): Promise<User[]> {
    const response = await api.delete(`/groups/${groupId}/members/${memberId}`);
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

  async getFriendMessages(friendshipId: number): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/friends/${friendshipId}/messages`);
    return response.data;
  },
};

export const anonymousService = {
  async getAnonymousNames(groupId: number): Promise<Record<number, string>> {
    const response = await api.get(`/anonymous/groups/${groupId}/names`);
    return response.data;
  },

  async resetAnonymousNames(groupId: number): Promise<void> {
    await api.delete(`/anonymous/groups/${groupId}/names`);
  },
};

export const badgeService = {
  async getUserBadges(): Promise<UserBadge[]> {
    const response = await api.get('/challenges/badges');
    return response.data.success ? response.data.data : [];
  },

  async getNewBadges(): Promise<UserBadge[]> {
    const response = await api.get('/challenges/badges/new');
    return response.data.success ? response.data.data : [];
  },

  async markBadgesAsRead(): Promise<void> {
    await api.post('/challenges/badges/mark-read');
  },
};

export const challengeServiceApi = {
  async getHistory(): Promise<ChallengeCompletionSummary[]> {
    const response = await api.get('/challenges/history');
    return response.data.success ? response.data.data : [];
  },
};

export const challengeShareService = {
  async createShare(payload: { challengeId: number; comment: string; mood?: string }): Promise<ChallengeShare> {
    const response = await api.post('/shares', payload);
    return response.data.data;
  },

  async getTimeline(page = 0, size = 10): Promise<PagedChallengeShares> {
    const response = await api.get('/shares', { params: { page, size } });
    return response.data.success ? response.data.data : {
      shares: [],
      page,
      size,
      totalElements: 0,
      totalPages: 0,
      hasNext: false,
    };
  },

  async addReaction(shareId: number, type: ChallengeShareReactionType): Promise<ChallengeShare> {
    const response = await api.post(`/shares/${shareId}/reactions`, { type });
    return response.data.data;
  },

  async removeReaction(shareId: number, type: ChallengeShareReactionType): Promise<ChallengeShare> {
    const response = await api.delete(`/shares/${shareId}/reactions/${type}`);
    return response.data.data;
  },

  async getUnreadCount(): Promise<number> {
    const response = await api.get('/shares/unread-count');
    return response.data.success ? response.data.data?.count ?? 0 : 0;
  },

  async markRead(): Promise<void> {
    await api.post('/shares/mark-read');
  },
};

export default api;