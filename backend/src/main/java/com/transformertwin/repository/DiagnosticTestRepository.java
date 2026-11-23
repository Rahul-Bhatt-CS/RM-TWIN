package com.transformertwin.repository;
import com.transformertwin.domain.DiagnosticTest; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.*; import java.util.*;
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest,Long> {
 boolean existsByTransformerIdAndSourceAndTestType(Long transformerId, com.transformertwin.domain.DataSourceType source, String testType);
 Page<DiagnosticTest> findByTransformerIdOrderByTestDateDesc(Long transformerId, Pageable pageable); List<DiagnosticTest> findTop100ByTransformerIdOrderByTestDateDesc(Long transformerId);
}
