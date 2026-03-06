const express = require("express");
const cors = require("cors");
const { createProxyMiddleware } = require("http-proxy-middleware");

const app = express();

const target = process.env.GATEWAY_TARGET || "http://localhost:8080";
const port = Number(process.env.GATEWAY_PORT || 8081);
const allowedOrigin = process.env.GATEWAY_ORIGIN || "http://localhost:5173";

app.use(cors({
  origin: allowedOrigin,
  credentials: true,
  exposedHeaders: ["Authorization", "X-Refresh-Token"]
}));

app.use("/api", createProxyMiddleware({
  target,
  changeOrigin: true,
  onProxyReq: (proxyReq, req) => {
    if (req.headers.authorization) {
      proxyReq.setHeader("Authorization", req.headers.authorization);
    }
    if (req.headers["x-refresh-token"]) {
      proxyReq.setHeader("X-Refresh-Token", req.headers["x-refresh-token"]);
    }
  },
  onProxyRes: (proxyRes) => {
    const expose = proxyRes.headers["access-control-expose-headers"];
    if (!expose) {
      proxyRes.headers["access-control-expose-headers"] = "Authorization, X-Refresh-Token";
    }
  }
}));

app.listen(port, () => {
  console.log(`Gateway listening on ${port}, proxy to ${target}`);
});
