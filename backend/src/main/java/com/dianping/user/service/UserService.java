package com.dianping.user.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.user.entity.User;
import com.dianping.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("username is required");
        }
        return userRepository.save(user);
    }

    public List<User> list() {
        return userRepository.findAll();
    }
}
