import { createRouter, createWebHistory } from "vue-router";
import AdminHome from "../views/AdminHome.vue";

const routes = [
  { path: "/", component: AdminHome }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
