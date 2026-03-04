import { createRouter, createWebHistory } from "vue-router";
import MerchantHome from "../views/MerchantHome.vue";

const routes = [
  { path: "/", component: MerchantHome }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
