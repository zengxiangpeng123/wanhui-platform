import { createRouter, createWebHistory } from 'vue-router';
import ResourceHomePage from '../pages/ResourceHomePage.vue';
import LoginPage from '../pages/LoginPage.vue';
import NotFoundPage from '../pages/NotFoundPage.vue';
import RegisterPage from '../pages/RegisterPage.vue';
import UserHomePage from '../pages/UserHomePage.vue';

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: ResourceHomePage },
    { path: '/login', name: 'login', component: LoginPage, meta: { guestOnly: true } },
    { path: '/register', name: 'register', component: RegisterPage, meta: { guestOnly: true } },
    { path: '/user', name: 'user', component: UserHomePage, meta: { requiresAuth: true } },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundPage }
  ]
});
