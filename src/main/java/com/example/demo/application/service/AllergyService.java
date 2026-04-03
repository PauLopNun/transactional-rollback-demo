package com.example.demo.application.service;

import com.example.demo.entity.Allergy;
import com.example.demo.entity.User;
import com.example.demo.repository.AllergyRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AllergyService {

    private final AllergyRepository allergyRepository;
    private final UserRepository userRepository;

    public AllergyService(AllergyRepository allergyRepository, UserRepository userRepository) {
        this.allergyRepository = allergyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String addAllergyWithRule(Integer userId, String allergyName, long sleepMs) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (allergyRepository.countByUserId(userId) > 0) {
            throw new IllegalStateException("A user can only have one allergy");
        }

        sleep(sleepMs);

        Allergy saved = allergyRepository.save(new Allergy(null, allergyName, user));
        return "Allergy created with ID: " + saved.getId();
    }

    public long countByUserId(Integer userId) {
        return allergyRepository.countByUserId(userId);
    }

    public List<String> getAllergyNamesByUserId(Integer userId) {
        return allergyRepository.findByUserId(userId)
                .stream()
                .map(Allergy::getName)
                .toList();
    }

    private void sleep(long sleepMs) {
        if (sleepMs <= 0) {
            return;
        }

        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Sleep interrupted", exception);
        }
    }
}

