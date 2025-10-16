'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';

export default function NotFound() {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  return (
    <div 
      className="min-h-screen flex items-center justify-center px-4"
      style={{ backgroundColor: '#222831' }}
    >
      <div className="max-w-2xl w-full text-center">
        {/* アニメーション付き404 */}
        <div 
          className="mb-8 transition-all duration-1000"
          style={{
            opacity: mounted ? 1 : 0,
            transform: mounted ? 'translateY(0)' : 'translateY(-20px)'
          }}
        >
          <h1 
            className="text-9xl font-bold mb-4"
            style={{ 
              color: '#00ADB5',
              textShadow: '0 0 20px rgba(0, 173, 181, 0.5)'
            }}
          >
            404
          </h1>
          <div 
            className="text-6xl mb-4"
            style={{ filter: 'grayscale(0.3)' }}
          >
            🌸
          </div>
        </div>

        {/* メッセージ */}
        <div
          className="mb-8 transition-all duration-1000 delay-300"
          style={{
            opacity: mounted ? 1 : 0,
            transform: mounted ? 'translateY(0)' : 'translateY(20px)'
          }}
        >
          <h2 
            className="text-3xl font-bold mb-4"
            style={{ color: '#EEEEEE' }}
          >
            ページが見つかりません
          </h2>
          <p 
            className="text-lg mb-2"
            style={{ color: '#EEEEEE', opacity: 0.8 }}
          >
            お探しのページは存在しないか、移動した可能性があります。
          </p>
          <p 
            className="text-sm"
            style={{ color: '#00ADB5' }}
          >
            URLをご確認いただくか、ホームに戻ってください。
          </p>
        </div>

        {/* ボタン */}
        <div
          className="flex flex-col sm:flex-row gap-4 justify-center transition-all duration-1000 delay-500"
          style={{
            opacity: mounted ? 1 : 0,
            transform: mounted ? 'scale(1)' : 'scale(0.9)'
          }}
        >
          <Link
            href="/"
            className="px-8 py-4 rounded-lg font-semibold transition-all duration-300 hover:scale-105"
            style={{ 
              backgroundColor: '#00ADB5', 
              color: '#EEEEEE',
              boxShadow: '0 4px 6px rgba(0, 173, 181, 0.3)'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.boxShadow = '0 8px 12px rgba(0, 173, 181, 0.5)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.boxShadow = '0 4px 6px rgba(0, 173, 181, 0.3)';
            }}
          >
            🏠 ホームに戻る
          </Link>
          
          <button
            onClick={() => window.history.back()}
            className="px-8 py-4 rounded-lg font-semibold transition-all duration-300 hover:scale-105"
            style={{ 
              backgroundColor: '#393E46', 
              color: '#EEEEEE',
              border: '2px solid #00ADB5'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = '#00ADB5';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = '#393E46';
            }}
          >
            ← 前のページに戻る
          </button>
        </div>

        {/* 装飾的な背景要素 */}
        <div 
          className="absolute inset-0 overflow-hidden pointer-events-none"
          style={{ opacity: 0.1 }}
        >
          <div
            className="absolute top-1/4 left-1/4 w-64 h-64 rounded-full"
            style={{
              background: 'radial-gradient(circle, #00ADB5 0%, transparent 70%)',
              animation: 'pulse 3s ease-in-out infinite'
            }}
          />
          <div
            className="absolute bottom-1/4 right-1/4 w-96 h-96 rounded-full"
            style={{
              background: 'radial-gradient(circle, #00ADB5 0%, transparent 70%)',
              animation: 'pulse 4s ease-in-out infinite'
            }}
          />
        </div>
      </div>

      <style jsx>{`
        @keyframes pulse {
          0%, 100% {
            opacity: 0.3;
            transform: scale(1);
          }
          50% {
            opacity: 0.6;
            transform: scale(1.1);
          }
        }
      `}</style>
    </div>
  );
}
