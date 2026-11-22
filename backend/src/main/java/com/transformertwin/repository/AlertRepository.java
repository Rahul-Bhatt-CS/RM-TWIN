package com.transformertwin.repository;
import com.transformertwin.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByTransformerIdOrderByCreatedAtDesc(Long transformerId);
    Optional<Alert> findTopByTransformerIdAndConditionAndStatusOrderByCreatedAtDesc(Long transformerId, String condition, String status);
}
