package com.dianping.common.context;

public final class UserContext {
    private static final ThreadLocal<UserSession> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(UserSession session) {
        HOLDER.set(session);
    }

    public static UserSession get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long getUserId() {
        UserSession session = get();
        return session != null ? session.getId() : null;
    }

    public static String getRole() {
        UserSession session = get();
        return session != null ? session.getRole() : null;
    }

    public static Long getMerchantId() {
        UserSession session = get();
        return session != null ? session.getMerchantId() : null;
    }
}
