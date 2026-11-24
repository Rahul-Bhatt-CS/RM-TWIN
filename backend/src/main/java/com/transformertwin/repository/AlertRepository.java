package com.transformertwin.repository;
import com.transformertwin.domain.Alert; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.*; import java.util.*;
public interface AlertRepository extends JpaRepository<Alert,Long> {
 Page<Alert> findByTransformerIdOrderByCreatedAtDesc(Long transformerId, Pageable pageable); Optional<Alert> findFirstByTransformerIdAndConditionKeyAndStatus(Long transformerId, String conditionKey, String status); long countByTransformerIdAndStatus(Long transformerId, String status); Optional<Alert> findTopByTransformerIdAndConditionKeyOrderByCreatedAtDesc(Long transformerId,String conditionKey);
}
