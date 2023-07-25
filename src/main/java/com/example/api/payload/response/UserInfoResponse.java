package com.example.api.payload.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private String uuid;
    private String name;
    private String email;
    private String avatar;
    private List<String> roles;
}
