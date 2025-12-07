import React, { useState, useEffect } from 'react';
import {
  PostRequest,
  CreatePostRequestDto,
  postRequestService,
  OrganizationMember,
  organizationManagementService,
} from '../services/api';

interface PostRequestManagerProps {
  currentOrganizationId: number;
  currentUserId: number;
  canManageRequests: boolean;
}

const PostRequestManager: React.FC<PostRequestManagerProps> = ({
  currentOrganizationId,
  currentUserId,
  canManageRequests,
}) => {
  const [pendingRequests, setPendingRequests] = useState<PostRequest[]>([]);
  const [myCreatedRequests, setMyCreatedRequests] = useState<PostRequest[]>([]);
  const [members, setMembers] = useState<OrganizationMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 新規リクエスト作成フォーム
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [dueDate, setDueDate] = useState('');
  const [message, setMessage] = useState('');

  // タブ選択
  const [activeTab, setActiveTab] = useState<'pending' | 'created'>('pending');

  useEffect(() => {
    loadData();
  }, [currentOrganizationId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [pending, created, memberList] = await Promise.all([
        postRequestService.getMyPendingRequests(),
        canManageRequests ? postRequestService.getMyCreatedRequests() : Promise.resolve([]),
        canManageRequests ? organizationManagementService.getMembers(currentOrganizationId) : Promise.resolve([]),
      ]);
      setPendingRequests(pending);
      setMyCreatedRequests(created);
      setMembers(memberList);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.error || 'データの取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUserId || !dueDate) {
      alert('対象ユーザーと期限日を選択してください');
      return;
    }

    try {
      const data: CreatePostRequestDto = {
        organizationId: currentOrganizationId,
        targetUserId: selectedUserId,
        dueDate,
        message,
      };
      await postRequestService.createPostRequest(data);
      setShowCreateForm(false);
      setSelectedUserId(null);
      setDueDate('');
      setMessage('');
      alert('投稿リクエストを作成しました');
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.error || '投稿リクエストの作成に失敗しました');
    }
  };

  const handleCancelRequest = async (requestId: number) => {
    if (!confirm('このリクエストをキャンセルしますか?')) {
      return;
    }

    try {
      await postRequestService.cancelRequest(requestId);
      alert('リクエストをキャンセルしました');
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.error || 'キャンセルに失敗しました');
    }
  };

  const getStatusBadge = (status: string) => {
    const badges = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      FULFILLED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-gray-100 text-gray-800',
      OVERDUE: 'bg-red-100 text-red-800',
    };
    const labels = {
      PENDING: '保留中',
      FULFILLED: '完了',
      CANCELLED: 'キャンセル',
      OVERDUE: '期限超過',
    };
    return (
      <span className={`px-2 py-1 text-xs rounded ${badges[status as keyof typeof badges]}`}>
        {labels[status as keyof typeof labels]}
      </span>
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ja-JP');
  };

  if (loading) {
    return <div className="p-4">読み込み中...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-600">エラー: {error}</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-2xl font-bold mb-4">投稿リクエスト管理</h2>

        {/* タブ */}
        <div className="flex border-b mb-4">
          <button
            onClick={() => setActiveTab('pending')}
            className={`px-4 py-2 -mb-px ${
              activeTab === 'pending'
                ? 'border-b-2 border-blue-600 text-blue-600 font-medium'
                : 'text-gray-600 hover:text-gray-800'
            }`}
          >
            自分宛て ({pendingRequests.length})
          </button>
          {canManageRequests && (
            <button
              onClick={() => setActiveTab('created')}
              className={`px-4 py-2 -mb-px ${
                activeTab === 'created'
                  ? 'border-b-2 border-blue-600 text-blue-600 font-medium'
                  : 'text-gray-600 hover:text-gray-800'
              }`}
            >
              作成したリクエスト ({myCreatedRequests.length})
            </button>
          )}
        </div>

        {/* 自分宛てのリクエスト */}
        {activeTab === 'pending' && (
          <div>
            {pendingRequests.length === 0 ? (
              <p className="text-gray-500">投稿リクエストはありません</p>
            ) : (
              <div className="space-y-3">
                {pendingRequests.map((request) => (
                  <div key={request.id} className="p-4 border rounded hover:bg-gray-50">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          {getStatusBadge(request.status)}
                          <span className="text-sm text-gray-600">
                            期限: {formatDate(request.dueDate)}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600">
                          依頼者: {request.requesterDisplayName || request.requesterUsername}
                        </p>
                      </div>
                    </div>
                    {request.message && (
                      <p className="text-sm mb-2 text-gray-700">{request.message}</p>
                    )}
                    <p className="text-xs text-gray-500">
                      作成日: {formatDate(request.createdAt)}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* 作成したリクエスト */}
        {activeTab === 'created' && canManageRequests && (
          <div>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">作成したリクエスト</h3>
              <button
                onClick={() => setShowCreateForm(!showCreateForm)}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                {showCreateForm ? 'キャンセル' : '新規リクエスト作成'}
              </button>
            </div>

            {showCreateForm && (
              <form onSubmit={handleCreateRequest} className="mb-6 p-4 bg-gray-50 rounded space-y-3">
                <div>
                  <label className="block text-sm font-medium mb-1">対象メンバー</label>
                  <select
                    value={selectedUserId || ''}
                    onChange={(e) => setSelectedUserId(Number(e.target.value))}
                    className="w-full px-3 py-2 border rounded"
                    required
                  >
                    <option value="">選択してください</option>
                    {members.map((member) => (
                      <option key={member.userId} value={member.userId}>
                        {member.displayName || member.username} (@{member.username})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">投稿期限</label>
                  <input
                    type="date"
                    value={dueDate}
                    onChange={(e) => setDueDate(e.target.value)}
                    className="w-full px-3 py-2 border rounded"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">メッセージ（任意）</label>
                  <textarea
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    className="w-full px-3 py-2 border rounded"
                    rows={3}
                    placeholder="リクエストの詳細や背景を記入"
                  />
                </div>
                <button
                  type="submit"
                  className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                  リクエスト作成
                </button>
              </form>
            )}

            {myCreatedRequests.length === 0 ? (
              <p className="text-gray-500">作成したリクエストはありません</p>
            ) : (
              <div className="space-y-3">
                {myCreatedRequests.map((request) => (
                  <div key={request.id} className="p-4 border rounded hover:bg-gray-50">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          {getStatusBadge(request.status)}
                          <span className="text-sm text-gray-600">
                            期限: {formatDate(request.dueDate)}
                          </span>
                        </div>
                        <p className="text-sm text-gray-700">
                          対象: {request.targetDisplayName || request.targetUsername}
                        </p>
                      </div>
                      {request.status === 'PENDING' && (
                        <button
                          onClick={() => handleCancelRequest(request.id)}
                          className="px-3 py-1 text-sm text-red-600 border border-red-600 rounded hover:bg-red-50"
                        >
                          キャンセル
                        </button>
                      )}
                    </div>
                    {request.message && (
                      <p className="text-sm mb-2 text-gray-700">{request.message}</p>
                    )}
                    <p className="text-xs text-gray-500">
                      作成日: {formatDate(request.createdAt)}
                      {request.fulfilledAt && ` | 完了日: ${formatDate(request.fulfilledAt)}`}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default PostRequestManager;
