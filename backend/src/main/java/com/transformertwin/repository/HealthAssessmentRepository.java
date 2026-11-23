package com.transformertwin.repository;
import com.transformertwin.domain.HealthAssessment; import org.springframework.data.jpa.repository.*; import org.springframework.data.domain.*; import java.util.*;
public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment,Long> {
 Page<HealthAssessment> findByTransformerIdOrderByAssessedAtDesc(Long transformerId,Pageable pageable);
 @EntityGraph(attributePaths="components") Optional<HealthAssessment> findTopByTransformerIdOrderByAssessedAtDesc(Long transformerId);
}
