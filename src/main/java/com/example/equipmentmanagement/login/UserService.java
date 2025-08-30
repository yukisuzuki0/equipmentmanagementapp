package com.example.equipmentmanagement.login;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public UserForm create(UserForm userForm) {
        userForm.setPassword(new BCryptPasswordEncoder().encode(userForm.getPassword()));
        // ロールが空なら自動的に ROLE_USER にする
        if (userForm.getRole() == null || userForm.getRole().isBlank()) {
            userForm.setRole("ROLE_USER");
        }
        UserBean userBean = new UserBean();
        BeanUtils.copyProperties(userForm, userBean);
        userRepository.save(userBean);
        return userForm;
    }
}