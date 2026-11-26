import { http } from './http';

export interface PostAuthor {
  id: string;
  username: string;
  nickname: string;
}

export interface PostCategory {
  code: string;
  name: string;
}

export interface PostSummary {
  id: string;
  title: string;
  excerpt: string;
  author: PostAuthor;
  category: PostCategory;
  tags: string[];
  viewCount: number;
  commentCount: number;
  likeCount: number;
  favoriteCount: number;
  createdAt: string;
}

export interface PostDetail extends Omit<PostSummary, 'excerpt'> {
  content: string;
  liked: boolean;
  favorited: boolean;
}

export interface PostPage {
  items: PostSummary[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface Comment {
  id: string;
  content: string;
  author: PostAuthor;
  likeCount: number;
  liked: boolean;
  createdAt: string;
}

export interface CommentPage {
  items: Comment[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface InteractionState {
  liked?: boolean;
  favorited?: boolean;
  likeCount?: number;
  favoriteCount?: number;
}

export async function fetchCategories(): Promise<PostCategory[]> {
  const response = await http.get<{ data: PostCategory[] }>('/forum/categories');
  return response.data.data;
}

export async function fetchPosts(category: string | undefined, page: number, size = 20): Promise<PostPage> {
  const response = await http.get<{ data: PostPage }>('/forum/posts', {
    params: { category: category || undefined, page, size }
  });
  return response.data.data;
}

export async function fetchPostDetail(postId: string): Promise<PostDetail> {
  const response = await http.get<{ data: PostDetail }>(`/forum/posts/${postId}`);
  return response.data.data;
}

export async function createPost(payload: {
  title: string;
  categoryCode: string;
  tags: string[];
  content: string;
}): Promise<PostDetail> {
  const response = await http.post<{ data: PostDetail }>('/forum/posts', payload);
  return response.data.data;
}

export async function setPostLike(postId: string, liked: boolean): Promise<InteractionState> {
  const response = liked
    ? await http.put<{ data: InteractionState }>(`/forum/posts/${postId}/like`)
    : await http.delete<{ data: InteractionState }>(`/forum/posts/${postId}/like`);
  return response.data.data;
}

export async function setPostFavorite(postId: string, favorited: boolean): Promise<InteractionState> {
  const response = favorited
    ? await http.put<{ data: InteractionState }>(`/forum/posts/${postId}/favorite`)
    : await http.delete<{ data: InteractionState }>(`/forum/posts/${postId}/favorite`);
  return response.data.data;
}

export async function fetchComments(postId: string, page = 1, size = 20): Promise<CommentPage> {
  const response = await http.get<{ data: CommentPage }>(`/forum/posts/${postId}/comments`, {
    params: { page, size }
  });
  return response.data.data;
}

export async function setCommentLike(commentId: string, liked: boolean): Promise<InteractionState> {
  const response = liked
    ? await http.put<{ data: InteractionState }>(`/forum/comments/${commentId}/like`)
    : await http.delete<{ data: InteractionState }>(`/forum/comments/${commentId}/like`);
  return response.data.data;
}
export async function createComment(postId: string, content: string): Promise<Comment> {
  const response = await http.post<{ data: Comment }>(`/forum/posts/${postId}/comments`, { content });
  return response.data.data;
}

