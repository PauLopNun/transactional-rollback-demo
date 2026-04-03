package com.example.demo.controller;

import com.example.demo.application.service.DemoCheckedException;
import com.example.demo.application.service.AllergyService;
import com.example.demo.application.service.UserService;
import com.example.demo.model.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TestController {

    private final UserService userService;
    private final AllergyService allergyService;

    public TestController(UserService userService, AllergyService allergyService) {
        this.userService = userService;
        this.allergyService = allergyService;
    }

    @GetMapping("/users")
    public List<UserDTO> getUsers(@RequestParam int page, @RequestParam int size) {
        return userService.getPaginatedUsers(page, size);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/user")
    public String saveUser(@RequestBody UserDTO user) {
        return userService.saveUser(user);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody UserDTO userToUpdate) {
        return userService.updateUserById(id, userToUpdate)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/user/search")
    public ResponseEntity<List<UserDTO>> findUsersByName(@RequestParam String name) {
        return ResponseEntity.ok(userService.findUsersByName(name));
    }

    @PostMapping("/demo/no-tx")
    public ResponseEntity<String> runNoTransactionalDemo(@RequestParam(defaultValue = "NO_TX") String prefix) {
        try {
            userService.saveUsersAndFailWithoutTransaction(prefix);
            return ResponseEntity.ok("Demo without transaction finished");
        } catch (DemoCheckedException exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PostMapping("/demo/with-tx")
    public ResponseEntity<String> runTransactionalDemo(@RequestParam(defaultValue = "WITH_TX") String prefix) {
        try {
            userService.saveUsersAndFailWithTransaction(prefix);
            return ResponseEntity.ok("Demo with transaction finished");
        } catch (DemoCheckedException exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @GetMapping("/demo/count")
    public ResponseEntity<Long> countUsersByPrefix(@RequestParam String prefix) {
        return ResponseEntity.ok(userService.countUsersByPrefix(prefix));
    }

    @GetMapping("/demo/users")
    public ResponseEntity<List<UserDTO>> getUsersByPrefix(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsersByPrefix(prefix, page, size));
    }

    @PostMapping("/demo/isolation/add-allergy")
    public ResponseEntity<String> addAllergyWithIsolationRule(
            @RequestParam Integer userId,
            @RequestParam String allergyName,
            @RequestParam(defaultValue = "3000") long sleepMs) {
        try {
            return ResponseEntity.ok(allergyService.addAllergyWithRule(userId, allergyName, sleepMs));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(exception.getMessage());
        }
    }

    @GetMapping("/demo/isolation/count")
    public ResponseEntity<Long> countAllergiesByUser(@RequestParam Integer userId) {
        return ResponseEntity.ok(allergyService.countByUserId(userId));
    }

    @GetMapping("/demo/isolation/list")
    public ResponseEntity<List<String>> listAllergiesByUser(@RequestParam Integer userId) {
        return ResponseEntity.ok(allergyService.getAllergyNamesByUserId(userId));
    }

    @PostMapping("/demo/isolation/add-user-age-rule")
    public ResponseEntity<String> addUserWithUniqueAgeRule(@RequestBody UserDTO user) {
        try {
            return ResponseEntity.ok(userService.saveUserWithUniqueAgeRule(user));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(exception.getMessage());
        }
    }

    @GetMapping("/demo/isolation/count-users-by-age")
    public ResponseEntity<Long> countUsersByAge(@RequestParam Integer age) {
        return ResponseEntity.ok(userService.countUsersByAge(age));
    }
}
