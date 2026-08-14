package com.sharveshmart.dto;

import com.sharveshmart.entity.User;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse user
) {

    public static LoginResponse from(String token, long expiresIn, User user) {
        return new LoginResponse(token, "Bearer", expiresIn, UserResponse.from(user));
    }
}
