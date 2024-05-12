package com.example.basichandlingexceptionvalidation.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 8, message = "password must be at least 8 character.")
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate dob;
}
