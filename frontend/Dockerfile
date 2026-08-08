# ─── Build Stage ─────────────────────────────────────────────
FROM node:20-alpine AS builder
WORKDIR /app

# Cache package dependencies layer
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build -- --configuration=production

# ─── Runtime Stage ────────────────────────────────────────────
FROM nginx:alpine

# Copy built app to nginx path
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
