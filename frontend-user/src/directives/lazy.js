/**
 * 图片懒加载指令 v-lazy
 * 
 * 使用方式:
 * <img v-lazy="imageUrl" :src="placeholder" />
 * 
 * 注意: 必须同时设置src作为占位图
 */

const lazyImages = new Set();
let observer = null;

function createObserver() {
  if (observer) return observer;
  
  const options = {
    root: null,
    rootMargin: '50px', // 提前50px开始加载
    threshold: 0.01
  };
  
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        loadImage(img);
        observer.unobserve(img);
        lazyImages.delete(img);
      }
    });
  }, options);
  
  return observer;
}

function loadImage(img) {
  const src = img.dataset.src;
  if (!src) return;
  
  // 创建新图片对象进行预加载
  const image = new Image();
  
  image.onload = () => {
    img.src = src;
    img.classList.add('lazy-loaded');
    img.classList.remove('lazy-loading');
  };
  
  image.onerror = () => {
    img.classList.add('lazy-error');
    img.classList.remove('lazy-loading');
    console.warn('图片加载失败:', src);
  };
  
  img.classList.add('lazy-loading');
  image.src = src;
}

const lazyDirective = {
  mounted(el, binding) {
    if (!(el instanceof HTMLImageElement)) {
      console.warn('v-lazy 指令只能用于 img 标签');
      return;
    }
    
    const src = binding.value;
    if (!src) return;
    
    // 保存真实图片地址
    el.dataset.src = src;
    
    // 添加懒加载样式
    el.classList.add('lazy-image');
    
    // 使用 IntersectionObserver 监听
    const obs = createObserver();
    obs.observe(el);
    lazyImages.add(el);
  },
  
  updated(el, binding) {
    if (binding.value !== binding.oldValue) {
      el.dataset.src = binding.value;
      
      // 如果图片还未加载，重新监听
      if (!el.classList.contains('lazy-loaded')) {
        const obs = createObserver();
        obs.observe(el);
        lazyImages.add(el);
      }
    }
  },
  
  unmounted(el) {
    if (observer && lazyImages.has(el)) {
      observer.unobserve(el);
      lazyImages.delete(el);
    }
  }
};

export default lazyDirective;
