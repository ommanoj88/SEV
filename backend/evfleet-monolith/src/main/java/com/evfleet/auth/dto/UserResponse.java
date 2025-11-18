package com.evfleet.auth.dto;

import com.evfleet.auth.model.Role;
import com.evfleet.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Response DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String firebaseUid;
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String phone;
    private Long companyId;
    private String companyName;
    private Boolean active;
    private Boolean emailVerified;
    private String profileImageUrl;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    /**
     * Convert User entity to UserResponse DTO
     */
    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .firebaseUid(user.getFirebaseUid())
            .email(user.getEmail())
            .name(user.getName())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .companyId(user.getCompanyId())
            .companyName(user.getCompanyName())
            .active(user.getActive())
            .emailVerified(user.getEmailVerified())
            .profileImageUrl(user.getProfileImageUrl())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLogin(user.getLastLogin())
            .build();
    }
}
