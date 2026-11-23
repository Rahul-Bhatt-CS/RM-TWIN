package com.transformertwin.service;
import com.transformertwin.domain.*; import com.transformertwin.repository.AlertRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.Instant;
@Service
public class AlertService {
 private final AlertRepository repo;
 public AlertService(AlertRepository repo){this.repo=repo;}
 @Transactional public void raise(Transformer t,String key,String title,String message,String severity,BigDecimal value,String unit){
  if(repo.findFirstByTransformerIdAndConditionKeyAndStatus(t.getId(),key,"ACTIVE").isEmpty()) repo.save(new Alert(t,key,title,message,severity,value,unit,Instant.now()));
 }
 @Transactional public void resolve(Transformer t,String key){
  repo.findFirstByTransformerIdAndConditionKeyAndStatus(t.getId(),key,"ACTIVE").ifPresent(a->a.resolve(Instant.now()));
 }
}
