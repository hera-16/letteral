'use client';

import React, { useState, useEffect } from 'react';
import { organizationService, Organization } from '../services/api';
import { Building2, ChevronDown } from 'lucide-react';

interface OrganizationSelectorProps {
  tenantId: number;
  selectedOrganizationId?: number | null;
  onSelectOrganization: (organizationId: number) => void;
  label?: string;
  required?: boolean;
}

const OrganizationSelector: React.FC<OrganizationSelectorProps> = ({
  tenantId,
  selectedOrganizationId,
  onSelectOrganization,
  label = '投稿先組織',
  required = false,
}) => {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadOrganizations();
  }, [tenantId]);

  const loadOrganizations = async () => {
    try {
      setLoading(true);
      const orgs = await organizationService.getOrganizationsByTenant(tenantId);
      setOrganizations(orgs);

      // デフォルト値が指定されていない場合、最初の組織を選択
      if (!selectedOrganizationId && orgs.length > 0) {
        onSelectOrganization(orgs[0].id);
      }

      setError(null);
    } catch (err: any) {
      console.error('組織一覧の読み込みエラー:', err);
      setError('組織一覧の読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-2">
        <label className="block text-sm font-medium text-gray-700">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
        <div className="text-sm text-gray-500">読み込み中...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-2">
        <label className="block text-sm font-medium text-gray-700">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
        <div className="text-sm text-red-600">{error}</div>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <label className="block text-sm font-medium text-gray-700">
        <div className="flex items-center gap-2">
          <Building2 className="w-4 h-4" />
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </div>
      </label>

      <div className="relative">
        <select
          value={selectedOrganizationId || ''}
          onChange={(e) => onSelectOrganization(Number(e.target.value))}
          required={required}
          className="w-full px-4 py-2 pr-10 border border-gray-300 rounded-lg appearance-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white"
        >
          {organizations.length === 0 && (
            <option value="">組織がありません</option>
          )}
          {organizations.map((org) => (
            <option key={org.id} value={org.id}>
              {org.name}
              {org.type && ` (${org.type})`}
            </option>
          ))}
        </select>

        <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">
          <ChevronDown className="w-5 h-5 text-gray-400" />
        </div>
      </div>

      {selectedOrganizationId && (
        <p className="text-xs text-gray-500">
          選択された組織のメンバーに投稿が表示されます
        </p>
      )}
    </div>
  );
};

export default OrganizationSelector;
