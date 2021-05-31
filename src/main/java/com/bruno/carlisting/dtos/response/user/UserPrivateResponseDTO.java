package com.bruno.carlisting.dtos.response.user;

import com.bruno.carlisting.domain.Role;
import com.bruno.carlisting.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UserPrivateResponseDTO {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String password;
    private String displayName;
    private String contact;

    private List<Role> roles;

    public static UserPrivateResponseDTO toDTO(User user) {
        return new UserPrivateResponseDTO(user.getUserId(), user.getUsername(), user.getPassword(),
                user.getDisplayName(), user.getContact(), user.getRoles());
    }
}