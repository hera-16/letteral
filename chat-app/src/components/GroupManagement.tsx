'use client';

import React, { useState, useEffect } from 'react';
import axios from 'axios';

interface Group {
  id: number;
  name: string;
  description?: string;
  groupType: 'INVITE_ONLY' | 'PUBLIC_TOPIC';
  maxMembers?: number;
  inviteCode?: string;
  createdAt: string;
}

interface GroupCreateFormProps {
  onGroupCreated: (group: Group) => void;
  onCancel: () => void;
}

const GroupCreateForm: React.FC<GroupCreateFormProps> = ({ onGroupCreated, onCancel }) => {
  const [groupType, setGroupType] = useState<'INVITE_ONLY' | 'PUBLIC_TOPIC'>('INVITE_ONLY');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const endpoint = groupType === 'INVITE_ONLY' 
        ? '/api/groups/invite' 
        : '/api/groups/public';
      
      const response = await axios.post(`http://localhost:8080${endpoint}`, {
        name,
        description: groupType === 'PUBLIC_TOPIC' ? description : undefined
      }, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      onGroupCreated(response.data);
      setName('');
      setDescription('');
    } catch (error) {
      console.error('グループ作成エラー:', error);
      alert('グループの作成に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h2 className="text-xl font-bold mb-4">新しいグループを作成</h2>
        
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-2">グループタイプ</label>
            <div className="space-y-2">
              <label className="flex items-center">
                <input
                  type="radio"
                  value="INVITE_ONLY"
                  checked={groupType === 'INVITE_ONLY'}
                  onChange={(e) => setGroupType(e.target.value as 'INVITE_ONLY')}
                  className="mr-2"
                />
                招待制グループ（最大15名）
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  value="PUBLIC_TOPIC"
                  checked={groupType === 'PUBLIC_TOPIC'}
                  onChange={(e) => setGroupType(e.target.value as 'PUBLIC_TOPIC')}
                  className="mr-2"
                />
                公開トピックグループ
              </label>
            </div>
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium mb-2">グループ名</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              placeholder="グループ名を入力"
              required
            />
          </div>

          {groupType === 'PUBLIC_TOPIC' && (
            <div className="mb-4">
              <label className="block text-sm font-medium mb-2">トピック・説明</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full border border-gray-300 rounded px-3 py-2"
                placeholder="このグループのトピックや説明を入力"
                rows={3}
              />
            </div>
          )}

          <div className="flex space-x-2">
            <button
              type="submit"
              disabled={loading || !name.trim()}
              className="flex-1 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:opacity-50"
            >
              {loading ? '作成中...' : '作成'}
            </button>
            <button
              type="button"
              onClick={onCancel}
              className="flex-1 bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
            >
              キャンセル
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const GroupManagement: React.FC = () => {
  const [groups, setGroups] = useState<Group[]>([]);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [joinCode, setJoinCode] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMyGroups();
  }, []);

  const fetchMyGroups = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/groups/my', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      setGroups(response.data);
    } catch (error) {
      console.error('グループ取得エラー:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleGroupCreated = (newGroup: Group) => {
    setGroups([newGroup, ...groups]);
    setShowCreateForm(false);
  };

  const handleJoinByCode = async () => {
    if (!joinCode.trim()) return;

    try {
      await axios.post('http://localhost:8080/api/groups/join/invite', {
        inviteCode: joinCode
      }, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      setJoinCode('');
      fetchMyGroups(); // グループリストを更新
      alert('グループに参加しました！');
    } catch (error) {
      console.error('グループ参加エラー:', error);
      alert('招待コードが無効です');
    }
  };

  if (loading) {
    return <div className="p-6">読み込み中...</div>;
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Letteral - グループ管理</h1>
        <button
          onClick={() => setShowCreateForm(true)}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          新しいグループを作成
        </button>
      </div>

      {/* 招待コード入力 */}
      <div className="mb-6 p-4 bg-gray-50 rounded">
        <h3 className="font-medium mb-2">招待コードでグループに参加</h3>
        <div className="flex space-x-2">
          <input
            type="text"
            value={joinCode}
            onChange={(e) => setJoinCode(e.target.value)}
            placeholder="招待コードを入力"
            className="border border-gray-300 rounded px-3 py-2 flex-1"
          />
          <button
            onClick={handleJoinByCode}
            className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
          >
            参加
          </button>
        </div>
      </div>

      {/* グループ一覧 */}
      <div className="space-y-4">
        <h2 className="text-lg font-semibold">参加中のグループ</h2>
        {groups.length === 0 ? (
          <p className="text-gray-500">参加しているグループがありません</p>
        ) : (
          groups.map((group) => (
            <div key={group.id} className="border border-gray-200 rounded p-4">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="font-medium text-lg">{group.name}</h3>
                  {group.description && (
                    <p className="text-gray-600 mt-1">{group.description}</p>
                  )}
                  <div className="mt-2 text-sm text-gray-500">
                    <span className={`inline-block px-2 py-1 rounded text-xs ${
                      group.groupType === 'INVITE_ONLY' 
                        ? 'bg-blue-100 text-blue-800' 
                        : 'bg-green-100 text-green-800'
                    }`}>
                      {group.groupType === 'INVITE_ONLY' ? '招待制' : '公開'}
                    </span>
                    {group.maxMembers && (
                      <span className="ml-2">最大{group.maxMembers}名</span>
                    )}
                  </div>
                  {group.inviteCode && (
                    <div className="mt-2">
                      <span className="text-sm text-gray-500">招待コード: </span>
                      <code className="bg-gray-100 px-2 py-1 rounded text-sm font-mono">
                        {group.inviteCode}
                      </code>
                    </div>
                  )}
                </div>
                <button
                  onClick={() => window.location.href = `/groups/${group.id}`}
                  className="bg-blue-500 text-white px-3 py-1 rounded text-sm hover:bg-blue-600"
                >
                  チャットに参加
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {showCreateForm && (
        <GroupCreateForm
          onGroupCreated={handleGroupCreated}
          onCancel={() => setShowCreateForm(false)}
        />
      )}
    </div>
  );
};

export default GroupManagement;