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
};

export default nextConfig;
