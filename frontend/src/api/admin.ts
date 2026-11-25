import { http } from './http';
import type { PostSummary } from './forum';

export interface AdminPostPage {
  items: PostSummary[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export async function fetchAdminPosts(page = 1, size = 20): Promise<AdminPostPage> {
  const response = await http.get<{ data: AdminPostPage }>('/admin/posts', { params: { page, size } });
  return response.data.data;
}

export async function deleteAdminPost(postId: string): Promise<void> {
  await http.delete(`/admin/posts/${postId}`);
}
