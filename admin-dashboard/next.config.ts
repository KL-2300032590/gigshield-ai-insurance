import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'standalone',
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8091',
    NEXT_PUBLIC_SIMULATOR_URL: process.env.NEXT_PUBLIC_SIMULATOR_URL || 'http://localhost:8091',
  },
};

export default nextConfig;
