package com.transformertwin.repository;
import com.transformertwin.domain.DiagnosticTest; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.*; import java.util.*;
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest,Long> {
 @org.springframework.data.jpa.repository.Query("select d from DiagnosticTest d where d.transformer.id=:id and (:q is null or lower(d.parameterName) like lower(concat('%',:q,'%')) or lower(d.testType) like lower(concat('%',:q,'%')) or lower(coalesce(d.componentKey,'')) like lower(concat('%',:q,'%')) or lower(coalesce(d.phase,'')) like lower(concat('%',:q,'%')) or lower(coalesce(d.laboratory,'')) like lower(concat('%',:q,'%')) or lower(coalesce(d.notes,'')) like lower(concat('%',:q,'%'))) order by d.testDate desc")
 Page<DiagnosticTest> searchForTransformer(Long id,String q,Pageable pageable);
 boolean existsByTransformerIdAndSourceAndTestType(Long transformerId, com.transformertwin.domain.DataSourceType source, String testType);
 Page<DiagnosticTest> findByTransformerIdOrderByTestDateDesc(Long transformerId, Pageable pageable); List<DiagnosticTest> findTop100ByTransformerIdOrderByTestDateDesc(Long transformerId);
}
