import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint: {
    // 本番ビルド時にESLintエラーを無視
    ignoreDuringBuilds: true,
  },
  typescript: {
    // 本番ビルド時にTypeScriptエラーを無視(警告のみ)
    ignoreBuildErrors: false,
  },
  // OneDrive上でのファイルアクセスエラー対策
  webpack: (config, { isServer }) => {
    // ファイルシステムキャッシュを完全に無効化
    config.cache = false;

    // スナップショットを無効化（OneDrive対策）
    config.snapshot = {
      managedPaths: [],
      immutablePaths: [],
      buildDependencies: {
        hash: false,
        timestamp: false,
      },
      module: {
        hash: false,
        timestamp: false,
      },
      resolve: {
        hash: false,
        timestamp: false,
      },
      resolveBuildDependencies: {
        hash: false,
        timestamp: false,
      },
    };

    // ファイル監視の設定を調整（ポーリング方式）
    config.watchOptions = {
      poll: 1000,
      aggregateTimeout: 300,
      ignored: /node_modules/,
    };

    // ファイルシステムの設定
    config.infrastructureLogging = {
      level: 'error',
    };

    return config;
  },
  // 実験的機能の設定
  experimental: {
    // ファイルシステムキャッシュを無効化
    webpackBuildWorker: false,
  },
};

export default nextConfig;
