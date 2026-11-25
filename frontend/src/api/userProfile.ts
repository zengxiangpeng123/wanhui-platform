import { http } from './http';

export interface UserProfile {
  id: string;
  username: string;
  nickname: string;
  avatarKey: string;
  emailMasked: string;
  phoneMasked: string | null;
  interest: string | null;
  bio: string | null;
  createdAt: string;
}

export async function fetchProfile(): Promise<UserProfile> {
  const response = await http.get<{ data: UserProfile }>('/users/me/profile');
  return response.data.data;
}

export async function updateProfile(payload: {
  nickname?: string;
  bio?: string;
  interest?: string;
  avatarKey?: string;
}): Promise<UserProfile> {
  const response = await http.patch<{ data: UserProfile }>('/users/me/profile', payload);
  return response.data.data;
}

export async function changePassword(oldPassword: string, newPassword: string): Promise<void> {
  await http.post('/users/me/password', { oldPassword, newPassword });
}
