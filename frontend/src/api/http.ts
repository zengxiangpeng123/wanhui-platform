import axios, { AxiosError } from 'axios';

export interface ApiFieldError {
  field: string;
  message: string;
}

export class ApiRequestError extends Error {
  constructor(
    message: string,
    readonly status: number | null,
    readonly code: string,
    readonly traceId?: string,
    readonly fieldErrors: ApiFieldError[] = []
  ) {
    super(message);
    this.name = 'ApiRequestError';
  }
}

function readCookie(name: string): string | null {
  if (typeof document === 'undefined') return null;
  const entry = document.cookie
    .split('; ')
    .find((item) => item.startsWith(`${name}=`));
  return entry ? decodeURIComponent(entry.slice(name.length + 1)) : null;
}

export const http = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: { Accept: 'application/json' }
});

http.interceptors.request.use((config) => {
  const csrfToken = readCookie('XSRF-TOKEN');
  if (csrfToken) {
    config.headers.set('X-XSRF-TOKEN', csrfToken);
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status ?? null;
    const payload = error.response?.data as
      | { error?: { code?: string; message?: string; fieldErrors?: ApiFieldError[] }; traceId?: string }
      | undefined;
    const message =
      payload?.error?.message ||
      (status === null ? '网络连接失败，请检查服务是否启动' : '请求失败，请稍后重试');
    return Promise.reject(
      new ApiRequestError(
        message,
        status,
        payload?.error?.code || 'NETWORK_ERROR',
        payload?.traceId,
        payload?.error?.fieldErrors || []
      )
    );
  }
);
