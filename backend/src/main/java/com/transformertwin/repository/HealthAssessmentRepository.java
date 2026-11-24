package com.transformertwin.repository;
import com.transformertwin.domain.HealthAssessment; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment,Long> {
 @EntityGraph(attributePaths="components") Optional<HealthAssessment> findTopByTransformerIdOrderByAssessedAtDesc(Long transformerId);
 long deleteByTransformerIdAndIdNot(Long transformerId,Long id);
}
