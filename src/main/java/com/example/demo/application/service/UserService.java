package com.example.demo.application.service;

import com.example.demo.entity.User;
import com.example.demo.model.UserDTO;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getPaginatedUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<UserDTO> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::toDTO);
    }

    public String saveUser(UserDTO dto) {
        User user = toEntity(dto);
        User saved = userRepository.save(user);
        return "User created with ID: " + saved.getId();
    }

    public boolean deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    public Optional<UserDTO> updateUserById(Integer id, UserDTO userToUpdate) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(userToUpdate.getName());
                    existingUser.setAge(userToUpdate.getAge());
                    return userRepository.save(existingUser);
                })
                .map(this::toDTO);
    }

    public List<UserDTO> findUsersByName(String name) {
        return userRepository.findByName(name)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public long countUsersByPrefix(String prefix) {
        return userRepository.countByNameStartingWith(prefix);
    }

    public List<UserDTO> getUsersByPrefix(String prefix, int page, int size) {
        return userRepository.findByNameStartingWith(prefix, PageRequest.of(page, size))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String saveUserWithUniqueAgeRule(UserDTO dto) {
        validateForAgeRule(dto);

        if (userRepository.existsByAge(dto.getAge())) {
            throw new IllegalStateException("A user with this age already exists");
        }

        sleepAgeRuleWindow();

        User saved = userRepository.save(toEntity(dto));
        return "User created with ID: " + saved.getId();
    }

    public long countUsersByAge(Integer age) {
        return userRepository.countByAge(age);
    }

    public void saveUsersAndFailWithoutTransaction(String prefix) throws DemoCheckedException {
        saveTwoUsersThenFail(prefix, "without transactional");
    }

    @Transactional(rollbackFor = DemoCheckedException.class)
    public void saveUsersAndFailWithTransaction(String prefix) throws DemoCheckedException {
        saveTwoUsersThenFail(prefix, "with transactional");
    }

    private void saveTwoUsersThenFail(String prefix, String mode) throws DemoCheckedException {
        userRepository.save(User.builder().name(prefix + "_FIRST").age(30).build());
        forceFailure(mode);
        userRepository.save(User.builder().name(prefix + "_SECOND").age(35).build());
    }

    private void forceFailure(String mode) throws DemoCheckedException {
        throw new DemoCheckedException("Forced exception " + mode);
    }

    private void validateForAgeRule(UserDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank() || dto.getAge() == null) {
            throw new IllegalArgumentException("Name and age are required");
        }
    }

    private void sleepAgeRuleWindow() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Sleep interrupted", exception);
        }
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .age(user.getAge())
                .build();
    }

    private User toEntity(UserDTO dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .age(dto.getAge())
                .build();
    }
}