package com.transformertwin.service;

import com.transformertwin.repository.TransformerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Evaluates health every minute, updating one persistent snapshot per transformer and UTC hour. */
@Service
public class HourlyHealthSnapshotService {
 private final TransformerRepository transformers;
 private final HealthService health;
 public HourlyHealthSnapshotService(TransformerRepository transformers,HealthService health){this.transformers=transformers;this.health=health;}
 @Scheduled(cron="0 * * * * *",zone="UTC")
 @Transactional
 public void captureHourlySnapshots(){transformers.findAll().forEach(health::assess);}
}
