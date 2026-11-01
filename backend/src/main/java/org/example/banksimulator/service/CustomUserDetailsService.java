package org.example.banksimulator.service;

import org.example.banksimulator.entity.User;
import org.example.banksimulator.enums.UserStatus;
import org.example.banksimulator.repository.UserRepository;
import org.example.banksimulator.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userNumber)
            throws UsernameNotFoundException{
    User user = userRepository.findByPhoneNumber(userNumber).orElseThrow(() ->
            new UsernameNotFoundException("User not found"));
    return new CustomUserDetails(user.getId(), user.getPhoneNumber(), user.getPasswordHash(), user.getRole(), user.getStatus());
    }
}
