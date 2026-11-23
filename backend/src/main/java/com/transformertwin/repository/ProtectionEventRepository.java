package com.transformertwin.repository;
import com.transformertwin.domain.ProtectionEvent; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.*; import java.util.*;
public interface ProtectionEventRepository extends JpaRepository<ProtectionEvent,Long> {
 Page<ProtectionEvent> findByTransformerIdOrderByOccurredAtDesc(Long transformerId, Pageable pageable);
}
