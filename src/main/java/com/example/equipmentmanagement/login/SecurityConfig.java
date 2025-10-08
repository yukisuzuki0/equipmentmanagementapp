package com.example.equipmentmanagement.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private LoginUserDetailsService loginUserDetailsSrevice;

    @Autowired
    private CustomLoginSuccessHandler successHandler;

    public void configureAuthenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(loginUserDetailsSrevice).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(login -> login
                .loginProcessingUrl("/login")
                .loginPage("/loginForm")
                //.defaultSuccessUrl("/hello", true)
                .successHandler(successHandler)
                .failureUrl("/loginForm?error")
                .usernameParameter("username").passwordParameter("password")
                .permitAll()).logout(logout -> logout
                        .logoutSuccessUrl("/loginForm"))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/webjars/**", "/css/**").permitAll()
                        .requestMatchers("/loginForm").permitAll()
                        .requestMatchers("/users").permitAll()
                        .requestMatchers("/users/create").permitAll()
                        .requestMatchers("/").permitAll() 
                        .requestMatchers("/index").permitAll()
                        .requestMatchers("/index.html").permitAll()
                        .requestMatchers("/home").permitAll()
                        // 設備編集はROLE_ADMINのみ
                        .requestMatchers("/equipment/edit/**", "/equipment/create-form", "/equipment/delete-mode").hasRole("ADMIN")
                        .anyRequest().authenticated());
        return http.build();
    }
}