import { createRouter, createWebHistory } from "vue-router";
import AdminHome from "../views/AdminHome.vue";
import AuditRecords from "../views/AuditRecords.vue";

const routes = [
  { path: "/", component: AdminHome },
  { path: "/audit-records", component: AuditRecords }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
