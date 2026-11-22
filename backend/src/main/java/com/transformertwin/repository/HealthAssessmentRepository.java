package com.transformertwin.repository;
import com.transformertwin.domain.HealthAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.*;
public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment, Long> {
    Optional<HealthAssessment> findTopByTransformerIdOrderByAssessedAtDesc(Long transformerId);
    List<HealthAssessment> findByTransformerIdOrderByAssessedAtDesc(Long transformerId);
    List<HealthAssessment> findByTransformerIdAndAssessedAtBetweenOrderByAssessedAtDesc(Long transformerId, Instant from, Instant to);
}
