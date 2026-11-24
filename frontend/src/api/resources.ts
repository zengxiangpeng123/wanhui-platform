import { http } from './http';
import type { ApiResponse } from './auth';

export interface CatalogResource {
  key: string;
  name: string;
  description: string;
  category: string;
  subcategory: string;
  url: string;
  icon: string;
  tags: string[];
}

export interface CatalogPage {
  items: CatalogResource[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
  sort: string;
}

export interface ResourceQuery {
  category?: string;
  q?: string;
  page?: number;
  size?: number;
}

export async function fetchResources(query: ResourceQuery): Promise<CatalogPage> {
  const response = await http.get<ApiResponse<CatalogPage>>('/resources', { params: query });
  return response.data.data;
}
