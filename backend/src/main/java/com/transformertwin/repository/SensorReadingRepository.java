package com.transformertwin.repository;
import com.transformertwin.domain.SensorReading; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.*; import java.util.*; import com.transformertwin.domain.MeasurementType; import java.util.List;
public interface SensorReadingRepository extends JpaRepository<SensorReading,Long> {
 Page<SensorReading> findByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(Long transformerId, MeasurementType measurementType, Pageable pageable); List<SensorReading> findTop5ByTransformerIdOrderByRecordedAtDesc(Long transformerId); Optional<SensorReading> findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(Long transformerId, MeasurementType measurementType); Page<SensorReading> findByTransformerIdAndMeasurementTypeAndRecordedAtBetweenOrderByRecordedAtDesc(Long transformerId,MeasurementType measurementType,java.time.Instant from,java.time.Instant to,Pageable pageable);
}
