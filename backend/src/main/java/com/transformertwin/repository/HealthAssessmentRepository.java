package com.transformertwin.repository;
import com.transformertwin.domain.HealthAssessment; import org.springframework.data.jpa.repository.*; import java.time.Instant; import java.util.*;
public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment,Long> {
 @EntityGraph(attributePaths="components") Optional<HealthAssessment> findTopByTransformerIdOrderByAssessedAtDesc(Long transformerId);
 @EntityGraph(attributePaths="components") Optional<HealthAssessment> findByTransformerIdAndAssessmentHour(Long transformerId, Instant assessmentHour);
 @EntityGraph(attributePaths="components") org.springframework.data.domain.Page<HealthAssessment> findByTransformerIdOrderByAssessmentHourDesc(Long transformerId, org.springframework.data.domain.Pageable pageable);
}
