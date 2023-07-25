package com.example.api.service;

import com.example.api.model.User;
import com.example.api.payload.request.RegisterRequest;
import com.example.api.payload.response.UserInfoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    UserInfoResponse login(User userDetails);
    boolean existsByEmail(String email);
    User getByUuid(String uuid);
    User getByEmail(String email);
    void save(RegisterRequest registerRequest);
    UserInfoResponse update(String uuid, String name, /*String email, */MultipartFile avatar);
    void removeAvatar(String uuid);
    void delete(String uuid);

    void generateTokenResetPassword(String email);
    void changePassword(User user, String password);
    User validateResetPasswordToken(String token);
    void validateOldPassword(User user, String oldPassword);
    void usedToken(String token);
}
