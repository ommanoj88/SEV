package com.evfleet.driver.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDriverCommand {
    @NotBlank(message = "Company ID is required")
    private String companyId;

    @NotBlank(message = "Driver name is required")
    private String name;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String phone;

    @Email(message = "Valid email is required")
    private String email;
}
