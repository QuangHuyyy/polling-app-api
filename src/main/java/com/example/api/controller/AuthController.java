package com.example.api.controller;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.User;
import com.example.api.payload.request.LoginRequest;
import com.example.api.payload.request.RegisterRequest;
import com.example.api.payload.response.JwtResponse;
import com.example.api.payload.response.ResponseMessage;
import com.example.api.payload.response.UserInfoResponse;
import com.example.api.security.JwtUtils;
import com.example.api.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = {"https://quanghuy-polling-app.web.app", "http://localhost:8080"}, maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IUserService userService;

    @PostMapping("/login")
//    public ResponseEntity<UserInfoResponse> login(@Valid @RequestBody LoginRequest loginRequest){
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User userDetails = (User) authentication.getPrincipal();
//        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        String jwt = jwtUtils.generateJwtToken(userDetails);

        UserInfoResponse userInfoResponse = userService.login(userDetails);
        return ResponseEntity.ok()
                .body(new JwtResponse(jwt, userInfoResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseMessage> register(@RequestBody RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())){
            return ResponseEntity.badRequest().body(new ResponseMessage("Email is already in use!"));
        }
        userService.save(registerRequest);
        return ResponseEntity.ok(new ResponseMessage("Register user successfully."));
    }

    @PutMapping(value = "/{uuid}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserInfoResponse> update(@PathVariable("uuid") String uuid,
                                                  @RequestParam(name = "name", required = false) String name,
                                                  /*@RequestParam(name = "email", required = false) String email,*/
                                                  @RequestParam(name = "avatar", required = false) MultipartFile avatar) {
        try{
            UserInfoResponse userInfoResponse = userService.update(uuid, name, /*email, */avatar);

            return ResponseEntity.ok(userInfoResponse);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping(value = "/{uuid}/avatar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseMessage> removeAvatar(@PathVariable("uuid") String uuid) {
        try{
            userService.removeAvatar(uuid);

            return ResponseEntity.ok(new ResponseMessage("Remove avatar successfully!"));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> delete(@PathVariable("uuid") String uuid){
        userService.delete(uuid);
        return ResponseEntity.ok(new ResponseMessage("Delete user successfully: " + uuid));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> logout(){
        ResponseCookie cleanCookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(new ResponseMessage("You've been signed out!"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseMessage> resetPassword(@RequestParam(name = "email") String email){
        try {
            userService.generateTokenResetPassword(email);

            return ResponseEntity.ok().body(new ResponseMessage("Successfully created email invitations."));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PutMapping("/save-password")
    public ResponseEntity<ResponseMessage> savePassword(@RequestParam(name = "newPassword") String newPassword,
                                                          @RequestParam(name = "token") String token){
        try {
            User user = userService.validateResetPasswordToken(token);
            userService.changePassword(user, newPassword);
            userService.usedToken(token);
            return ResponseEntity.ok().body(new ResponseMessage("Update password successfully."));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (PermissionException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }

    @PutMapping("change-password")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> updatePassword(@RequestParam(name = "oldPassword") String oldPassword,
                                                          @RequestParam(name = "newPassword") String newPassword){
        try {
            final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            User user = userService.getByUuid(currentUser.getUuid());
            userService.validateOldPassword(user, oldPassword);
            userService.changePassword(user, newPassword);
            return ResponseEntity.ok().body(new ResponseMessage("Update password successfully."));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (PermissionException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }
}
