package com.dianping.user.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.user.dto.UserCreateRequest;
import com.dianping.user.dto.UpdateCityRequest;
import com.dianping.user.entity.User;
import com.dianping.user.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<User> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @GetMapping
    public ApiResponse<List<User>> list() {
        return ApiResponse.ok(userService.list());
    }

    @PutMapping("/{id}/city")
    public ApiResponse<User> updateCity(@PathVariable("id") Long id,
                                        @Valid @RequestBody UpdateCityRequest request) {
        return ApiResponse.ok(userService.updateCity(id, request.getCity()));
    }
}
