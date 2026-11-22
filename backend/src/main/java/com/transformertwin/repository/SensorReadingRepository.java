package com.transformertwin.repository;
import com.transformertwin.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.*;
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findTop3ByTransformerIdOrderByRecordedAtDesc(Long transformerId);
    List<SensorReading> findByTransformerIdAndRecordedAtBetweenOrderByRecordedAtDesc(Long transformerId, Instant from, Instant to);
    Optional<SensorReading> findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(Long transformerId, MeasurementType type);
}
