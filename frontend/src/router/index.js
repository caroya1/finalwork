import { createRouter, createWebHistory } from "vue-router";
import UserHome from "../views/UserHome.vue";
import MerchantHome from "../views/MerchantHome.vue";
import AdminHome from "../views/AdminHome.vue";
import PostDetail from "../views/PostDetail.vue";
import CategoryShops from "../views/CategoryShops.vue";
import ShopDetail from "../views/ShopDetail.vue";

const routes = [
  { path: "/", component: UserHome },
  { path: "/posts/:id", component: PostDetail },
  { path: "/category", component: CategoryShops },
  { path: "/shops/:id", component: ShopDetail },
  { path: "/merchant", component: MerchantHome, meta: { role: "merchant" } },
  { path: "/admin", component: AdminHome, meta: { role: "admin" } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;

router.beforeEach((to) => {
  if (!to.meta || !to.meta.role) {
    return true;
  }
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  const role = localStorage.getItem("dp_role");
  if (!token || !refreshToken) {
    return "/";
  }
  if (to.meta.role && role !== to.meta.role) {
    return "/";
  }
  return true;
});
