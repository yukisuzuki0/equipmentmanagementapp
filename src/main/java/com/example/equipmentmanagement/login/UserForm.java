package com.example.equipmentmanagement.login;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserForm {
    @NotNull
    @Size(min = 6, max = 12)
    private String username;
    @Size(min = 6, max = 12)
    private String password;
    
    private String role;
}
