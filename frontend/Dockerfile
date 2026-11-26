# syntax=docker/dockerfile:1.7
FROM node:22-alpine AS build
ARG NPM_REGISTRY_URL=https://registry.npmmirror.com
WORKDIR /build
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --registry="$NPM_REGISTRY_URL"
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /build/dist /usr/share/nginx/html
EXPOSE 80 443
