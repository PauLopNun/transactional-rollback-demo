package com.example.demo.repository;

import com.example.demo.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Integer> {

    long countByUserId(Integer userId);

    List<Allergy> findByUserId(Integer userId);
}

