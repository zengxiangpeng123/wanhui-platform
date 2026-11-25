import { createRouter, createWebHistory } from 'vue-router';
import ResourceHomePage from '../pages/ResourceHomePage.vue';
import LoginPage from '../pages/LoginPage.vue';
import NotFoundPage from '../pages/NotFoundPage.vue';
import RegisterPage from '../pages/RegisterPage.vue';
import UserHomePage from '../pages/UserHomePage.vue';
import ResourceFavoritesPage from '../pages/ResourceFavoritesPage.vue';
import RecentVisitsPage from '../pages/RecentVisitsPage.vue';
import MySitesPage from '../pages/MySitesPage.vue';
import ForumPage from '../pages/ForumPage.vue';
import PostDetailPage from '../pages/PostDetailPage.vue';
import UserProfilePage from '../pages/UserProfilePage.vue';
import UserSettingsPage from '../pages/UserSettingsPage.vue';
import AdminContentPage from '../pages/AdminContentPage.vue';

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: ResourceHomePage },
    { path: '/login', name: 'login', component: LoginPage, meta: { guestOnly: true } },
    { path: '/register', name: 'register', component: RegisterPage, meta: { guestOnly: true } },
    { path: '/forum', name: 'forum', component: ForumPage },
    { path: '/forum/post/:id', name: 'forum-post', component: PostDetailPage },
    { path: '/admin/content', name: 'admin-content', component: AdminContentPage, meta: { requiresAuth: true } },
    { path: '/user', name: 'user', component: UserHomePage, meta: { requiresAuth: true } },
    { path: '/user/profile', name: 'user-profile', component: UserProfilePage, meta: { requiresAuth: true } },
    { path: '/user/settings', name: 'user-settings', component: UserSettingsPage, meta: { requiresAuth: true } },
    { path: '/user/favorites', name: 'user-favorites', component: ResourceFavoritesPage, meta: { requiresAuth: true } },
    { path: '/user/recent-visits', name: 'user-recent-visits', component: RecentVisitsPage, meta: { requiresAuth: true } },
    { path: '/user/sites', name: 'user-sites', component: MySitesPage, meta: { requiresAuth: true } },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundPage }
  ]
});



