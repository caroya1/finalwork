import { createRouter, createWebHistory } from "vue-router";
import UserHome from "../views/UserHome.vue";
import PostDetail from "../views/PostDetail.vue";
import PostCreate from "../views/PostCreate.vue";
import SearchResults from "../views/SearchResults.vue";
import CategoryShops from "../views/CategoryShops.vue";
import ShopDetail from "../views/ShopDetail.vue";
import UserProfile from "../views/UserProfile.vue";

const routes = [
  { path: "/", component: UserHome },
  { path: "/search", component: SearchResults },
  { path: "/posts/new", component: PostCreate },
  { path: "/posts/:id", component: PostDetail },
  { path: "/category", component: CategoryShops },
  { path: "/shops/:id", component: ShopDetail },
  { path: "/profile", component: UserProfile }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
