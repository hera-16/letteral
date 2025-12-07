import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export interface CreateProgressPostRequest {
  tenantId: number;
  organizationId: number;
  postType: string;
  title?: string;
  content: string;
  achievementRate?: number;
  blockers?: string;
  learnings?: string;
  nextAction?: string;
  visibility: string;
  postDate: string;
  tags?: string[];
  targetOrganizationId?: number | null;
}

export interface ProgressPost {
  id: number;
  postType: string;
  title?: string;
  content: string;
  achievementRate?: number;
  blockers?: string;
  learnings?: string;
  nextAction?: string;
  visibility: string;
  postDate: string;
  tags?: string[];
  author: {
    id: number;
    displayName: string;
    isAnonymous: boolean;
  };
  organization: {
    id: number;
    name: string;
  };
  boxType?: {
    id: number;
    name: string;
    displayName: string;
  };
  commentCount: number;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface TimelineResponse {
  content: ProgressPost[];
  page: {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface TimelineFilters {
  tenantId?: number;
  organizationId?: number | null;
  postType?: string | null;
  visibilities?: string[];
  startDate?: string | null;
  endDate?: string | null;
  page?: number;
  size?: number;
  sort?: string[];
}

class ProgressPostService {
  private getAuthToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('token');
    }
    return null;
  }

  private getAuthHeaders() {
    const token = this.getAuthToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  /**
   * タイムライン取得
   */
  async getTimeline(filters: TimelineFilters): Promise<TimelineResponse> {
    const params: any = {
      page: filters.page || 0,
      size: filters.size || 20
    };

    if (filters.tenantId) params.tenantId = filters.tenantId;
    if (filters.organizationId) params.organizationId = filters.organizationId;
    if (filters.postType) params.postType = filters.postType;
    if (filters.startDate) params.startDate = filters.startDate;
    if (filters.endDate) params.endDate = filters.endDate;
    if (filters.sort && filters.sort.length > 0) {
      params.sort = filters.sort;
    }

    const response = await axios.get<TimelineResponse>(`${API_BASE_URL}/posts`, {
      params,
      headers: this.getAuthHeaders()
    });

    return response.data;
  }

  /**
   * 投稿詳細取得
   */
  async getPostById(id: number): Promise<ProgressPost> {
    const response = await axios.get<ProgressPost>(`${API_BASE_URL}/posts/${id}`, {
      headers: this.getAuthHeaders()
    });

    return response.data;
  }

  /**
   * 投稿作成
   */
  async createPost(data: CreateProgressPostRequest): Promise<ProgressPost> {
    const response = await axios.post<ProgressPost>(`${API_BASE_URL}/posts`, data, {
      headers: {
        ...this.getAuthHeaders(),
        'Content-Type': 'application/json'
      }
    });

    return response.data;
  }

  /**
   * 投稿更新
   */
  async updatePost(id: number, updates: Partial<CreateProgressPostRequest>): Promise<ProgressPost> {
    const response = await axios.patch<ProgressPost>(`${API_BASE_URL}/posts/${id}`, updates, {
      headers: {
        ...this.getAuthHeaders(),
        'Content-Type': 'application/json'
      }
    });

    return response.data;
  }

  /**
   * 投稿削除
   */
  async deletePost(id: number): Promise<void> {
    await axios.delete(`${API_BASE_URL}/posts/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

}

export const progressPostService = new ProgressPostService();
