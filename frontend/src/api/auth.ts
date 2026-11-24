import { http } from './http';

export interface CurrentUser {
  id: string;
  username: string;
  nickname: string;
  avatarKey: string;
  emailMasked: string | null;
  phoneMasked: string | null;
  roles: string[];
  permissions: string[];
}

export interface RegisterResult {
  userId: string;
  username: string;
  next: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  traceId?: string;
}

export async function loadMe(): Promise<CurrentUser | null> {
  const response = await http.get<ApiResponse<CurrentUser | null>>('/auth/me');
  return response.data.data;
}

export async function login(account: string, password: string): Promise<CurrentUser> {
  const response = await http.post<ApiResponse<CurrentUser>>('/auth/login', { account, password });
  return response.data.data;
}

export async function register(username: string, email: string, password: string): Promise<RegisterResult> {
  const response = await http.post<ApiResponse<RegisterResult>>('/auth/register', {
    username,
    email,
    password
  });
  return response.data.data;
}

export async function logout(): Promise<void> {
  await http.get('/auth/csrf');
  await http.post('/auth/logout');
}
