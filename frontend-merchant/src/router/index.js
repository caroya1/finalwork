import { createRouter, createWebHistory } from "vue-router";
import MerchantHome from "../views/MerchantHome.vue";
import Login from "../views/Login.vue";

const routes = [
  { path: "/", component: MerchantHome },
  { path: "/login", component: Login }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("dp_token");
  if (!token && to.path !== "/login") {
    next("/login");
  } else if (token && to.path === "/login") {
    next("/");
  } else {
    next();
  }
});

export default router;
