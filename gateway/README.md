# Dianping Gateway

Simple API gateway for frontend apps to proxy backend calls.

## Usage

```bash
npm install
npm start
```

### Environment variables

- `GATEWAY_PORT` (default: 8081)
- `GATEWAY_TARGET` (default: http://localhost:8080)
- `GATEWAY_ORIGINS` (comma-separated allowlist, optional)
```
