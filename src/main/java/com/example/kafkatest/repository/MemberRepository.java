package com.example.kafkatest.repository;

import com.example.kafkatest.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    @Query("select uniqueNumber from Member where userId = :userId")
    String findUniqueNumberByUserId(String userId);

    Member findByUserId(String userId);
}
