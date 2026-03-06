package com.dianping.common.port;

public interface PasswordPort {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
