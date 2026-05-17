import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  // Must match frontend-service server.port (default 8088). Override in .env: VITE_UI_API_PROXY=http://127.0.0.1:8088
  const uiApiProxyTarget = env.VITE_UI_API_PROXY || "http://127.0.0.1:8088";

  const proxyToFrontendService = {
    target: uiApiProxyTarget,
    changeOrigin: true,
  };

  return {
    plugins: [react()],
    server: {
      proxy: {
        "/ui-api": proxyToFrontendService,
      },
    },
    preview: {
      proxy: {
        "/ui-api": proxyToFrontendService,
      },
    },
    build: {
      outDir: "../src/main/resources/static",
      emptyOutDir: true,
    },
  };
});
