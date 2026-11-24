package com.transformertwin.service;
import com.transformertwin.domain.*; import com.transformertwin.repository.AlertRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.Instant;
@Service public class AlertService {
 private final AlertRepository repo; public AlertService(AlertRepository repo){this.repo=repo;}
 @Transactional public void raise(Transformer t,String key,String title,String message,String severity,BigDecimal value,String unit){
  Instant now=Instant.now();var existing=repo.findFirstByTransformerIdAndConditionKeyAndStatus(t.getId(),key,"ACTIVE");
  if(existing.isPresent())return;
  int recurrence=repo.findTopByTransformerIdAndConditionKeyOrderByCreatedAtDesc(t.getId(),key).map(a->a.getOccurrenceCount()+1).orElse(1);
  Alert next=new Alert(t,key,title,message,severity,value,unit,now);next.setOccurrenceCount(recurrence);repo.save(next);
 }
 @Transactional public void resolve(Transformer t,String key){resolve(t,key,"Condition no longer meets the active rule during the latest evaluation.");}
 @Transactional public void resolve(Transformer t,String key,String evidence){repo.findFirstByTransformerIdAndConditionKeyAndStatus(t.getId(),key,"ACTIVE").ifPresent(a->a.resolve(Instant.now(),evidence));}
}
