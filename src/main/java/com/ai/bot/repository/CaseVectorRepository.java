package com.ai.bot.repository;

import com.ai.bot.model.CaseVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseVectorRepository extends JpaRepository<CaseVector, Long> {
}
