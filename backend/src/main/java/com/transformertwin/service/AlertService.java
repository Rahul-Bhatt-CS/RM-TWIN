package com.transformertwin.service;

import com.transformertwin.domain.*;
import com.transformertwin.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {
    private final AlertRepository repository;
    public AlertService(AlertRepository repository) { this.repository = repository; }

    @Transactional
    public void raise(Transformer transformer, String condition, String severity, BigDecimal observed,
                      BigDecimal threshold, String unit, Instant at) {
        Optional<Alert> existing = repository.findTopByTransformerIdAndConditionAndStatusOrderByCreatedAtDesc(
                transformer.getId(), condition, "ACTIVE");
        if (existing.isEmpty()) repository.save(new Alert(transformer, condition, severity, observed, threshold, "ACTIVE", at));
    }

    @Transactional
    public void resolveIfActive(Transformer transformer, String condition) {
        repository.findTopByTransformerIdAndConditionAndStatusOrderByCreatedAtDesc(
                transformer.getId(), condition, "ACTIVE").ifPresent(Alert::resolve);
    }

    public List<Alert> findAll(Long transformerId) {
        return repository.findByTransformerIdOrderByCreatedAtDesc(transformerId);
    }
}
