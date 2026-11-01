package org.example.banksimulator.service;

import org.example.banksimulator.dto.UserDto;
import org.example.banksimulator.dto.UserProfileDto;
import org.example.banksimulator.entity.User;
import org.example.banksimulator.enums.Role;
import org.example.banksimulator.enums.UserStatus;
import org.example.banksimulator.exceptions.AdminsException;
import org.example.banksimulator.exceptions.UserNotFoundException;
import org.example.banksimulator.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.encoder = passwordEncoder;
    }

    @Transactional
    public UserDto userRegistration(String phoneNumber, String password, String firstName, String lastName, LocalDate birthDate) {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new RuntimeException("A user with this login already exists");
        }
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .passwordHash(encoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .birthDate(birthDate)
                .role(Role.CLIENT)
                .status(UserStatus.ACTIVE)
                .build();
        User newUser = userRepository.save(user);
        return new UserDto(
                newUser.getId(),
                newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getStatus()
        );
    }
    @Transactional(readOnly = true)
    public UserProfileDto getMyProfile(Long userId){
        User user = userRepository.findById(userId).orElseThrow(
                ()-> new UsernameNotFoundException("User not found")
        );
        return new UserProfileDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public Page<UserProfileDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserProfileDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoneNumber(),
                        user.getRole()
                ));
    }

    @Transactional
    public UserProfileDto blockUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        if(user.getRole() != Role.CLIENT){
            throw new AdminsException("Insufficient rights");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User is not ACTIVE");
        }

        user.setStatus(UserStatus.BLOCKED);

        return new UserProfileDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    @Transactional
    public UserProfileDto unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        if(user.getRole() != Role.CLIENT){
            throw new AdminsException("Insufficient rights");
        }
        if (user.getStatus() != UserStatus.BLOCKED) {
            throw new IllegalStateException("User is not BLOCKED");
        }

        user.setStatus(UserStatus.ACTIVE);

        return new UserProfileDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

}


