package com.transformertwin.repository;
import com.transformertwin.domain.AlertNotification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AlertNotificationRepository extends JpaRepository<AlertNotification,Long> {
 Page<AlertNotification> findByTransformerIdOrderByCreatedAtDesc(Long transformerId, Pageable pageable);
 long countByTransformerIdAndReadAtIsNull(Long transformerId);
}
