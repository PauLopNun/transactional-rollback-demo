package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByName(String name);

    List<User> findByAgeGreaterThan(Integer age);

    boolean existsByAge(Integer age);

    long countByAge(Integer age);

    long countByNameStartingWith(String prefix);

    Page<User> findByNameStartingWith(String prefix, Pageable pageable);
}