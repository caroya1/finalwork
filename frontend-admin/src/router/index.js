import { createRouter, createWebHistory } from "vue-router";
import Login from "../views/Login.vue";
import Layout from "../components/Layout.vue";
import Dashboard from "../views/Dashboard.vue";
import MerchantAudit from "../views/MerchantAudit.vue";
import ShopAudit from "../views/ShopAudit.vue";
import PostAudit from "../views/PostAudit.vue";
import OrderManage from "../views/OrderManage.vue";
import Statistics from "../views/Statistics.vue";

const routes = [
  { 
    path: "/login", 
    component: Login 
  },
  { 
    path: "/", 
    component: Layout,
    children: [
      { path: "", component: Dashboard },
      { path: "merchants", component: MerchantAudit },
      { path: "shops", component: ShopAudit },
      { path: "posts", component: PostAudit },
      { path: "orders", component: OrderManage },
      { path: "stats", component: Statistics }
    ]
  }
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
