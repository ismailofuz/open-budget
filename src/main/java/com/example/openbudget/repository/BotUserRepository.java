package com.example.openbudget.repository;

import com.example.openbudget.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserRepository extends JpaRepository<BotUser, Integer> {
    Optional<BotUser> findByChatId(String chatId);
}
