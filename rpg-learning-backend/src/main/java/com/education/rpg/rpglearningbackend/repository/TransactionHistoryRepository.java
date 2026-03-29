package com.education.rpg.rpglearningbackend.repository;

import com.education.rpg.rpglearningbackend.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
}