import { beforeEach, describe, expect, it } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { http, ApiRequestError } from './http';

function mockReject(status: number | null, data: unknown) {
  const config = { headers: new AxiosHeaders() };
  if (status === null) {
    return new AxiosError('Network Error', 'ERR_NETWORK', config as never);
  }
  return new AxiosError('Request failed', 'ERR_BAD_REQUEST', config as never, null, {
    data,
    status,
    statusText: '',
    headers: {},
    config: config as never
  });
}

async function trigger(status: number | null, data: unknown): Promise<ApiRequestError> {
  const error = mockReject(status, data);
  try {
    await http.get('/test-error-contract', {
      adapter: async () => {
        throw error;
      }
    });
  } catch (caught) {
    return caught as ApiRequestError;
  }
  throw new Error('expected request to fail');
}

describe('API error contract', () => {
  beforeEach(() => {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  it('maps 401 to a login-required message', async () => {
    const error = await trigger(401, {
      success: false,
      error: { code: 'AUTH_REQUIRED', message: '请先登录' },
      traceId: 'trace-401'
    });
    expect(error.status).toBe(401);
    expect(error.code).toBe('AUTH_REQUIRED');
    expect(error.message).toBe('请先登录');
    expect(error.traceId).toBe('trace-401');
  });

  it('maps 403 to access denied', async () => {
    const error = await trigger(403, {
      success: false,
      error: { code: 'ACCESS_DENIED', message: '没有权限执行此操作' },
      traceId: 'trace-403'
    });
    expect(error.status).toBe(403);
    expect(error.code).toBe('ACCESS_DENIED');
    expect(error.message).toBe('没有权限执行此操作');
  });

  it('maps 409 conflict', async () => {
    const error = await trigger(409, {
      success: false,
      error: { code: 'CONFLICT', message: '用户名或邮箱已注册' }
    });
    expect(error.code).toBe('CONFLICT');
    expect(error.message).toBe('用户名或邮箱已注册');
  });

  it('maps 410 deleted post', async () => {
    const error = await trigger(410, {
      success: false,
      error: { code: 'POST_DELETED', message: '帖子已删除' }
    });
    expect(error.status).toBe(410);
    expect(error.code).toBe('POST_DELETED');
    expect(error.message).toBe('帖子已删除');
  });

  it('exposes field errors for validation failures', async () => {
    const error = await trigger(422, {
      success: false,
      error: {
        code: 'VALIDATION_FAILED',
        message: '请求参数校验失败',
        fieldErrors: [{ field: 'title', message: '标题不能超过 100 个字符' }]
      }
    });
    expect(error.code).toBe('VALIDATION_FAILED');
    expect(error.fieldErrors).toHaveLength(1);
    expect(error.fieldErrors[0].field).toBe('title');
  });

  it('falls back to a readable message for 500 without payload', async () => {
    const error = await trigger(500, undefined);
    expect(error.status).toBe(500);
    expect(error.code).toBe('NETWORK_ERROR');
    expect(error.message).toBe('请求失败，请稍后重试');
  });

  it('maps network failure to a connection message', async () => {
    const error = await trigger(null, undefined);
    expect(error.status).toBeNull();
    expect(error.code).toBe('NETWORK_ERROR');
    expect(error.message).toContain('网络连接失败');
  });
});
