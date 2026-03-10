/**
 * 埋点SDK - 前端用户行为采集
 * 
 * 使用示例:
 * import Tracking from '@/utils/tracking';
 * Tracking.pageView('首页');
 * Tracking.click('首页', 'btn-search');
 * Tracking.search('火锅', 50);
 */

const Tracking = (function() {
  const config = {
    apiUrl: '/api/analytics/track',
    sessionId: '',
    maxQueueSize: 50
  };

  const eventQueue = [];

  function init(options) {
    Object.assign(config, options);
    config.sessionId = 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  function getUserId() {
    const token = localStorage.getItem('access_token');
    if (token) {
      try {
        return JSON.parse(atob(token.split('.')[1])).sub;
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  function createEvent(eventType, data = {}) {
    return {
      eventId: 'evt_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9),
      eventType,
      userId: getUserId(),
      sessionId: config.sessionId,
      pageName: data.pageName || location.pathname,
      elementId: data.elementId || '',
      properties: data.properties || {},
      timestamp: new Date().toISOString()
    };
  }

  function addEvent(event) {
    eventQueue.push(event);
    if (eventQueue.length >= config.maxQueueSize) {
      flush();
    }
  }

  async function flush() {
    if (eventQueue.length === 0) return;
    const events = eventQueue.splice(0, eventQueue.length);
    try {
      await fetch(config.apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ events }),
        keepalive: true
      });
    } catch (e) {
      eventQueue.unshift(...events);
    }
  }

  function pageView(pageName) {
    addEvent(createEvent('page_view', { pageName }));
  }

  function click(pageName, elementId) {
    addEvent(createEvent('click', { pageName, elementId }));
  }

  function search(keyword, resultCount) {
    addEvent(createEvent('search', { properties: { keyword, resultCount } }));
  }

  function favorite(targetId, targetType) {
    addEvent(createEvent('favorite', { properties: { targetId, targetType } }));
  }

  function order(orderId, shopId, amount) {
    addEvent(createEvent('order', { properties: { orderId, shopId, amount } }));
  }

  function payment(orderId, amount) {
    addEvent(createEvent('payment', { properties: { orderId, amount } }));
  }

  function recommendClick(position, shopId) {
    addEvent(createEvent('recommend_click', { properties: { position, shopId } }));
  }

  return {
    init,
    pageView,
    click,
    search,
    favorite,
    order,
    payment,
    recommendClick,
    flush
  };
})();

export default Tracking;
