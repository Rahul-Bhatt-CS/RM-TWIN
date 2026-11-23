package com.transformertwin.repository;
import com.transformertwin.domain.ProtectionEvent; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.*; import java.util.*;
public interface ProtectionEventRepository extends JpaRepository<ProtectionEvent,Long> {
 boolean existsByTransformerIdAndSourceAndEventType(Long transformerId, com.transformertwin.domain.DataSourceType source, String eventType);
 Page<ProtectionEvent> findByTransformerIdOrderByOccurredAtDesc(Long transformerId, Pageable pageable);
}
