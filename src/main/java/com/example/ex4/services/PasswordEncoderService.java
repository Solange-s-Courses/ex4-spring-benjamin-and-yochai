//package com.example.ex4.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class PasswordEncoderService {
//
//    private final PasswordEncoder passwordEncoder;
//
//    @Autowired
//    public PasswordEncoderService(PasswordEncoder passwordEncoder) {
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    public String encodePassword(String rawPassword) {
//        return passwordEncoder.encode(rawPassword);
//    }
//
//    public boolean matches(String rawPassword, String encodedPassword) {
//        return passwordEncoder.matches(rawPassword, encodedPassword);
//    }
//}