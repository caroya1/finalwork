let toastInstance = null;

export const setToastInstance = (instance) => {
  toastInstance = instance;
};

export const useToast = () => {
  return {
    success: (msg) => toastInstance?.success(msg),
    error: (msg) => toastInstance?.error(msg),
    info: (msg) => toastInstance?.info(msg),
    warning: (msg) => toastInstance?.warning(msg)
  };
};
