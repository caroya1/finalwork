/**
 * 无限滚动组合式函数
 * 
 * 使用示例:
 * const { containerRef, isLoading, hasMore, loadMore } = useInfiniteScroll(
 *   async (page) => {
 *     const res = await fetchData(page);
 *     return { list: res.list, hasMore: res.hasMore };
 *   }
 * );
 */

import { ref, onMounted, onUnmounted } from 'vue';

export function useInfiniteScroll(fetchFn, options = {}) {
  const {
    threshold = 100,       // 距离底部多少像素时触发加载
    immediate = true,      // 是否立即加载第一页
    pageSize = 10          // 每页数量
  } = options;
  
  const containerRef = ref(null);
  const isLoading = ref(false);
  const hasMore = ref(true);
  const currentPage = ref(0);
  const list = ref([]);
  
  let observer = null;
  let loadingTrigger = null;
  
  const loadMore = async () => {
    if (isLoading.value || !hasMore.value) return;
    
    isLoading.value = true;
    
    try {
      const nextPage = currentPage.value + 1;
      const result = await fetchFn(nextPage, pageSize);
      
      if (result && result.list) {
        if (nextPage === 1) {
          list.value = result.list;
        } else {
          list.value.push(...result.list);
        }
        
        hasMore.value = result.hasMore !== false && result.list.length === pageSize;
        currentPage.value = nextPage;
      } else {
        hasMore.value = false;
      }
    } catch (error) {
      console.error('加载数据失败:', error);
      hasMore.value = false;
    } finally {
      isLoading.value = false;
    }
  };
  
  const reset = () => {
    currentPage.value = 0;
    hasMore.value = true;
    list.value = [];
    loadMore();
  };
  
  const createObserver = () => {
    if (!containerRef.value) return;
    
    // 创建加载触发器元素
    if (!loadingTrigger) {
      loadingTrigger = document.createElement('div');
      loadingTrigger.className = 'infinite-scroll-trigger';
      loadingTrigger.style.height = '1px';
      containerRef.value.appendChild(loadingTrigger);
    }
    
    observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry.isIntersecting && !isLoading.value && hasMore.value) {
          loadMore();
        }
      },
      {
        root: containerRef.value,
        rootMargin: `${threshold}px`,
        threshold: 0
      }
    );
    
    observer.observe(loadingTrigger);
  };
  
  onMounted(() => {
    createObserver();
    if (immediate) {
      loadMore();
    }
  });
  
  onUnmounted(() => {
    if (observer) {
      observer.disconnect();
    }
    if (loadingTrigger && loadingTrigger.parentNode) {
      loadingTrigger.parentNode.removeChild(loadingTrigger);
    }
  });
  
  return {
    containerRef,
    list,
    isLoading,
    hasMore,
    loadMore,
    reset
  };
}

export default useInfiniteScroll;
