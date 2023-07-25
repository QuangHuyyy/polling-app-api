package com.example.api.service.impl;

import com.example.api.exception.AppException;
import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.*;
import com.example.api.payload.request.RegisterRequest;
import com.example.api.payload.response.UserInfoResponse;
import com.example.api.repository.IResetPasswordTokenRepository;
import com.example.api.repository.IRoleRepository;
import com.example.api.repository.IUserRepository;
import com.example.api.service.IEmailService;
import com.example.api.service.IFileDataService;
import com.example.api.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IFileDataService fileDataService;
    private final IEmailService emailService;
    private final IResetPasswordTokenRepository resetPasswordTokenRepository;

    @Override
    public UserInfoResponse login(User userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return UserInfoResponse.builder()
                        .uuid(userDetails.getUuid())
                        .name(userDetails.getName())
                        .email(userDetails.getEmail())
                        .avatar(userDetails.getAvatar() == null
                                ? null : fileDataService.getUrlFile(userDetails.getAvatar().getFilename()))
                        .roles(roles)
                        .build();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User getByUuid(String uuid) {
        return userRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("User", "uuid", uuid));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    public void save(RegisterRequest registerRequest) {
        User user = convertUserRequest(registerRequest);

        userRepository.save(user);
    }

    @Override
    public UserInfoResponse update(String uuid, String name, /*String email, */MultipartFile avatar) {
        String avatarFilenameOld = "";
        User user = userRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("User", "uuid", uuid));

        user.setName(name != null ? name : user.getName());
//        user.setEmail(email != null ? email : user.getEmail());

        if (avatar != null){
            avatarFilenameOld = user.getAvatar() == null ? "" : user.getAvatar().getFilename();
            FileData avatarFile = fileDataService.save(avatar);
            user.setAvatar(avatarFile);
        }

        try {
            User userDb = userRepository.save(user);
            List<String> roles = userDb.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                    .uuid(uuid)
                    .name(userDb.getName())
                    .email(userDb.getEmail())
                    .roles(roles)
                    .build();

            if (userDb.getAvatar() != null){
                String avatarFilename = userDb.getAvatar().getFilename();
                String avatarUrl = fileDataService.getUrlFile(avatarFilename);
                userInfoResponse.setAvatar(avatarUrl);
            } else {
                userInfoResponse.setAvatar(null);
            }

            if (!avatarFilenameOld.equals("")){
                fileDataService.delete(avatarFilenameOld);
            }

            return userInfoResponse;
        } catch (DataIntegrityViolationException e){
            throw new BadRequestException("Email address already being used!");
        }
    }

    @Override
    public void removeAvatar(String uuid) {
        User user = userRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("User", "uuid", uuid));

        String avatarFilenameOld = user.getAvatar() == null ? "" : user.getAvatar().getFilename();

        user.setAvatar(null);
        userRepository.save(user);

        if (!avatarFilenameOld.equals("")){
            fileDataService.delete(avatarFilenameOld);
        }
    }

    @Override
    public void delete(String uuid) {
        userRepository.deleteById(uuid);
    }

    @Override
    public void generateTokenResetPassword(String email) {
        User user = getByEmail(email);

        Optional<ResetPasswordToken> passwordTokenOpt = resetPasswordTokenRepository.findFirstByUserOrderById(user);

        if (passwordTokenOpt.isPresent() && passwordTokenOpt.get().getExpiryTime() != null &&  passwordTokenOpt.get().getExpiryTime().isAfter(LocalDateTime.now())){
            throw new BadRequestException("A verification email has been sent. Please check your inbox!");
        }

        ResetPasswordToken token = new ResetPasswordToken(UUID.randomUUID().toString(), user);

        resetPasswordTokenRepository.save(token);

        emailService.sendResetPasswordMail(email, user.getName(), token.getToken());
        user.setPassword("");
        userRepository.save(user);
    }

    @Override
    public void changePassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public User validateResetPasswordToken(String token) {
        ResetPasswordToken passwordToken = resetPasswordTokenRepository.findByToken(token)
                .orElseThrow(() -> new PermissionException("Invalid token!"));
        if (passwordToken.getExpiryTime() == null){
            throw new BadRequestException("This token has already been used!");
        }

        if (passwordToken.getExpiryTime().isBefore(LocalDateTime.now())){
            throw new BadRequestException("Expiration time for reset password access token!");
        }

        return passwordToken.getUser();
    }

    @Override
    public void validateOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new BadRequestException("Current password not match!");
        }
    }

    @Override
    public void usedToken(String token){
        ResetPasswordToken passwordToken = resetPasswordTokenRepository.findByToken(token)
                .orElseThrow(() -> new PermissionException("Invalid token!"));
        passwordToken.setExpiryTime(null);
        resetPasswordTokenRepository.save(passwordToken);
    }

    private User convertUserRequest(RegisterRequest registerRequest) {
        List<String> rolesStr = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (rolesStr == null || rolesStr.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new AppException("Role user not set!"));
            roles.add(userRole);
        } else {
            rolesStr.forEach(role -> {
                switch (role) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new AppException("Role admin not set!"));
                        roles.add(adminRole);
                    }
                    case "mod" -> {
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR).orElseThrow(() -> new AppException("Role moderator not set!"));
                        roles.add(modRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new AppException("Role user not set!"));
                        roles.add(userRole);
                    }
                }
            });
        }
        return User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(roles)
                .build();
    }
}
