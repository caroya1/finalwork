import { createRouter, createWebHistory } from "vue-router";
import UserHome from "../views/UserHome.vue";
import MerchantHome from "../views/MerchantHome.vue";
import AdminHome from "../views/AdminHome.vue";

const routes = [
  { path: "/", component: UserHome },
  { path: "/merchant", component: MerchantHome },
  { path: "/admin", component: AdminHome }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
