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
      const requestUrl = error.config?.url || '';
      const isAuthEndpoint = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register') || requestUrl.includes('/auth/signup');
      
      // 認証エンドポイント自体の401エラーは通常のエラーとして扱う（自動ログアウトしない）
      if (isAuthEndpoint) {
        console.log('🔓 Auth endpoint returned 401 (invalid credentials) - not logging out');
        return Promise.reject(error);
      }
      
      // トークンが無効または期限切れの場合、ログアウト
      const isAlreadyOnLoginPage = typeof window !== 'undefined' && window.location.pathname === '/';
      
      console.warn('⚠️ 401 Unauthorized for:', requestUrl, '- Logging out');
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

export interface User {
  id: number;
  username: string;
  email: string;
  displayName?: string;
  createdAt: string;
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

export const userService = {
  async getCurrentUser(): Promise<User> {
    const response = await api.get('/users/me');
    return response.data;
  },

  async searchUsers(query: string): Promise<User[]> {
    const response = await api.get('/users/search', { params: { query } });
    return response.data;
  },

  async getUserByUsername(username: string): Promise<User> {
    const response = await api.get(`/users/username/${encodeURIComponent(username)}`);
    return response.data;
  },

  async getAllUsers(): Promise<User[]> {
    const response = await api.get('/users/all');
    return response.data;
  },
};


export const chatService = {
  async getMessages(roomId: string): Promise<ChatMessage[]> {
    const response = await api.get(`/chat/messages/${roomId}`);
    return response.data;
  },
};


// 進捗投稿関連の型定義
export type PostType = 'PROGRESS' | 'GOAL' | 'BLOCKER' | 'LEARNING' | 'QUESTION';
export type Visibility = 'PRIVATE' | 'TEAM' | 'DEPARTMENT' | 'ORGANIZATION' | 'COMPANY';

export interface ProgressPost {
  id?: number;
  tenantId: number;
  organizationId: number;
  authorId: number;
  isAnonymous: boolean;
  postType: PostType;
  title?: string;
  content: string;
  achievementRate?: number;
  blockers?: string;
  learnings?: string;
  nextAction?: string;
  visibility: Visibility;
  targetOrganizationId?: number;
  postDate: string;
  tags?: string[];
  attachments?: Array<{ type: string; url: string; name?: string }>;
  reactionCount?: number;
  commentCount?: number;
  viewCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateProgressPostRequest {
  tenantId: number;
  organizationId: number;
  authorId: number;
  isAnonymous?: boolean;
  postType?: PostType;
  title?: string;
  content: string;
  achievementRate?: number;
  blockers?: string;
  learnings?: string;
  nextAction?: string;
  visibility?: Visibility;
  postDate: string;
  tags?: string[];
}

export interface PagedProgressPosts {
  content: ProgressPost[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  number: number;
  size: number;
}

export const progressPostService = {
  async createPost(data: CreateProgressPostRequest): Promise<ProgressPost> {
    const response = await api.post('/progress-posts', data);
    return response.data;
  },

  async getPost(postId: number): Promise<ProgressPost> {
    const response = await api.get(`/progress-posts/${postId}`);
    return response.data;
  },

  async getTenantTimeline(tenantId: number, page = 0, size = 10): Promise<PagedProgressPosts> {
    const response = await api.get(`/progress-posts/tenant/${tenantId}/timeline`, {
      params: { page, size },
    });
    return response.data;
  },

  async getOrganizationTimeline(organizationId: number, page = 0, size = 10): Promise<PagedProgressPosts> {
    const response = await api.get(`/progress-posts/organization/${organizationId}/timeline`, {
      params: { page, size },
    });
    return response.data;
  },

  async getPostsByAuthor(authorId: number, page = 0, size = 10): Promise<PagedProgressPosts> {
    const response = await api.get(`/progress-posts/author/${authorId}`, {
      params: { page, size },
    });
    return response.data;
  },

  async updatePost(postId: number, data: Partial<ProgressPost>): Promise<ProgressPost> {
    const response = await api.put(`/progress-posts/${postId}`, data);
    return response.data;
  },

  async deletePost(postId: number): Promise<void> {
    await api.delete(`/progress-posts/${postId}`);
  },

  async incrementReaction(postId: number): Promise<void> {
    await api.post(`/progress-posts/${postId}/reactions/increment`);
  },

  async decrementReaction(postId: number): Promise<void> {
    await api.post(`/progress-posts/${postId}/reactions/decrement`);
  },

  async incrementView(postId: number): Promise<void> {
    await api.post(`/progress-posts/${postId}/views/increment`);
  },
};

// 組織管理関連の型定義
export type OrganizationRole =
  | 'ADMIN_ROOT'     // 最高権限（全テナント操作可）
  | 'ADMIN_CORE'     // コア管理者（テナント内全組織操作可）
  | 'ADMIN_LEAD'     // リード管理者（自組織と子組織操作可）
  | 'ADMIN_SUPER'    // スーパー管理者（自組織操作可）
  | 'MEMBER';        // 一般メンバー

export interface Organization {
  id: number;
  name: string;
  description?: string;
  type: string;
  parentId?: number;
  level: number;
  path: string;
  createdAt: string;
}

export interface OrganizationMember {
  id: number;
  userId: number;
  username: string;
  displayName?: string;
  role: OrganizationRole;
  joinedAt: string;
}

export interface AddMemberRequest {
  username: string;
  role: OrganizationRole;
}

export interface UpdateRoleRequest {
  role: OrganizationRole;
}

export interface CreateSubOrgRequest {
  name: string;
  description?: string;
}

export interface OrganizationPermissions {
  isMember: boolean;
  role: OrganizationRole | null;
  canAddMember: boolean;
  canRemoveMember: boolean;
  canCreateSubOrganization: boolean;
}

export const organizationManagementService = {
  async getMembers(organizationId: number): Promise<OrganizationMember[]> {
    const response = await api.get(`/organization-management/${organizationId}/members`);
    return response.data;
  },

  async addMember(organizationId: number, data: AddMemberRequest): Promise<OrganizationMember> {
    const response = await api.post(`/organization-management/${organizationId}/members`, data);
    return response.data;
  },

  async removeMember(organizationId: number, userId: number): Promise<void> {
    await api.delete(`/organization-management/${organizationId}/members/${userId}`);
  },

  async updateMemberRole(organizationId: number, userId: number, data: UpdateRoleRequest): Promise<OrganizationMember> {
    const response = await api.put(`/organization-management/${organizationId}/members/${userId}/role`, data);
    return response.data;
  },

  async createSubOrganization(parentOrganizationId: number, data: CreateSubOrgRequest): Promise<Organization> {
    const response = await api.post(`/organization-management/${parentOrganizationId}/sub-organizations`, data);
    return response.data;
  },

  async getPermissions(organizationId: number): Promise<OrganizationPermissions> {
    const response = await api.get(`/organization-management/${organizationId}/permissions`);
    return response.data;
  },
};

export default api;