import { http } from './http';
import type { ApiResponse } from './auth';

export interface FavoriteResource {
  resourceKey: string;
  name: string;
  description: string;
  category: string;
  subcategory: string;
  url: string;
  icon: string;
  available: boolean;
  favoritedAt: string;
}

export interface FavoritePage {
  items: FavoriteResource[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface RecentVisit {
  resourceKey: string;
  resourceName: string;
  visitedAt: string;
}

export interface UserSite {
  id: string;
  name: string;
  url: string;
  iconUrl: string | null;
  createdAt: string;
}

export interface UserSitePayload {
  name: string;
  url: string;
  iconUrl?: string;
}

export async function fetchFavorites(page = 1, size = 20): Promise<FavoritePage> {
  const response = await http.get<ApiResponse<FavoritePage>>('/users/me/favorites/resources', { params: { page, size } });
  return response.data.data;
}

export async function fetchRecentVisits(): Promise<RecentVisit[]> {
  const response = await http.get<ApiResponse<RecentVisit[]>>('/users/me/recent-visits');
  return response.data.data;
}

export async function clearRecentVisits(): Promise<void> {
  await http.delete('/users/me/recent-visits');
}

export async function fetchSites(): Promise<UserSite[]> {
  const response = await http.get<ApiResponse<UserSite[]>>('/users/me/sites');
  return response.data.data;
}

export async function addSite(payload: UserSitePayload): Promise<UserSite> {
  const response = await http.post<ApiResponse<UserSite>>('/users/me/sites', payload);
  return response.data.data;
}

export async function deleteSite(siteId: string): Promise<void> {
  await http.delete(`/users/me/sites/${siteId}`);
}
