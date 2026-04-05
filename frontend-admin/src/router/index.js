import { createRouter, createWebHistory } from "vue-router";
import AdminHome from "../views/AdminHome.vue";
import AuditRecords from "../views/AuditRecords.vue";
import Login from "../views/Login.vue";

const routes = [
  { path: "/login", component: Login },
  { path: "/", component: AdminHome },
  { path: "/audit-records", component: AuditRecords }
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
