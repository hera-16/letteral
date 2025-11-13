'use client';

// 旧CharaChalle機能 - Letteralでは使用しない
// このコンポーネントは互換性のために残されていますが、機能は無効化されています
export interface Group {
  id: number;
  name: string;
  description?: string;
  inviteCode?: string;
}

interface GroupListProps {
  onSelectGroup: (group: Group) => void;
}

export default function GroupList({ onSelectGroup }: GroupListProps) {
  return null; // Letteralでは匿名グループ機能は使用しません
}
