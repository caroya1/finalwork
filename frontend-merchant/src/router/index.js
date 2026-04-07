import { createRouter, createWebHistory } from "vue-router";
import Layout from "../components/Layout.vue";
import Dashboard from "../views/Dashboard.vue";
import ShopManage from "../views/ShopManage.vue";
import DishManage from "../views/DishManage.vue";
import CouponManage from "../views/CouponManage.vue";
import OrderManage from "../views/OrderManage.vue";
import Login from "../views/Login.vue";

const routes = [
  { path: "/login", component: Login },
  {
    path: "/",
    component: Layout,
    children: [
      { path: "", component: Dashboard },
      { path: "shops", component: ShopManage },
      { path: "dishes", component: DishManage },
      { path: "coupons", component: CouponManage },
      { path: "orders", component: OrderManage }
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
