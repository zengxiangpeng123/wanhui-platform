import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import { createPinia } from 'pinia';
import 'element-plus/dist/index.css';
import App from './App.vue';
import { router } from './router';
import { installAuthGuard } from './router/guards';
import './style.css';

const app = createApp(App);
const pinia = createPinia();

installAuthGuard(router, pinia);
app.use(pinia).use(router).use(ElementPlus).mount('#app');
