package com.transformertwin.repository;
import com.transformertwin.domain.MaintenanceRecord;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord,Long>{Page<MaintenanceRecord> findByTransformerIdOrderByPerformedAtDesc(Long id,Pageable pageable);}
