package com.dianping.common.web;

public final class AuthUserResolver {
    private AuthUserResolver() {
    }

    public static Long resolveUserId(Object principal, String headerUserId) {
        if (principal != null) {
            try {
                return Long.parseLong(principal.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (headerUserId == null || headerUserId.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(headerUserId.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
