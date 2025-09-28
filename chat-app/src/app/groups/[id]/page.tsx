'use client';

import { useParams } from 'next/navigation';
import AnonymousGroupChat from '@/components/AnonymousGroupChat';

export default function GroupChatPage() {
  const params = useParams();
  const groupId = parseInt(params.id as string);

  if (isNaN(groupId)) {
    return <div className="p-6">無効なグループIDです</div>;
  }

  return <AnonymousGroupChat groupId={groupId} />;
}