package com.dianping.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptChecker {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("123456 matches N hash: " + encoder.matches("123456", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6CQz9E7bBQVXXi1Izzl78v5xK"));
        System.out.println("123456 matches mhd hash: " + encoder.matches("123456", "$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii"));
    }
}
