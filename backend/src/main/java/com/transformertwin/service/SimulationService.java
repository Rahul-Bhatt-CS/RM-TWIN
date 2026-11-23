package com.transformertwin.service;
import com.transformertwin.domain.*; import com.transformertwin.repository.*;
import com.transformertwin.api.dto.ApiDtos; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.context.event.ApplicationReadyEvent; import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.Instant; import java.util.concurrent.ConcurrentHashMap; import java.util.concurrent.atomic.AtomicLong;
@Service
public class SimulationService {
 private final TransformerRepository transformers; private final SensorReadingRepository readings; private final HealthService health; private final RealtimeService realtime; private final TransformerService transformerService;
 private final ConcurrentHashMap<Long,AtomicLong> ticks=new ConcurrentHashMap<>();
 @Value("${app.simulation.enabled:true}") private boolean enabled;
 public SimulationService(TransformerRepository t,SensorReadingRepository r,HealthService h,RealtimeService rt,TransformerService ts){transformers=t;readings=r;health=h;realtime=rt;transformerService=ts;}
 @EventListener(ApplicationReadyEvent.class) @Transactional public void initialize(){if(!enabled)return;}
 @Scheduled(fixedDelayString="${app.simulation.interval-ms:5000}") @Transactional
 public void tick(){if(!enabled)return;for(Transformer t:transformers.findAll()){if(!t.isSimulationEnabled())continue;if(t.getRatedPowerMva()==null||t.getVoltageClassKv()==null||t.getLvVoltageKv()==null||t.getRatedFrequencyHz()==null)continue;long n=ticks.computeIfAbsent(t.getId(),x->new AtomicLong()).getAndIncrement();int phase=(int)(n%48);double load,oil,hot,ambient;
  if(phase<12){load=58+phase*.9;oil=52+phase*.35;hot=68+phase*.7;}
  else if(phase<24){int p=phase-12;load=72+p*2.8;oil=57+p*1.7;hot=77+p*2.1;}
  else if(phase<36){int p=phase-24;load=106+p*.5;oil=78+p*1.0;hot=101+p*1.2;}
  else {int p=phase-36;load=96-p*3.0;oil=78-p*2.0;hot=94-p*2.0;}
  ambient=27+(n%8)*.5;Instant now=Instant.now();
  // The selected reference profile is 100 MVA, 220/33 kV, three-phase, 50 Hz.
  // Current and apparent power are derived from the asset nameplate and simulated load fraction.
  double ratedMva=t.getRatedPowerMva().doubleValue();
  double hvKv=t.getVoltageClassKv().doubleValue();
  double lvKv=t.getLvVoltageKv().doubleValue();
  double apparentMva=ratedMva*load/100.0;
  double hvCurrentA=apparentMva*1000.0/(Math.sqrt(3.0)*hvKv);
  double lvCurrentA=apparentMva*1000.0/(Math.sqrt(3.0)*lvKv);
  // Fan state is a scripted demo scenario, not a manufacturer WTI control setting.
  double fanStatus=(phase>=12&&phase<36)?1.0:0.0;
  readings.save(new SensorReading(t,MeasurementType.TOP_OIL_TEMPERATURE,bd(oil),"°C",DataSourceType.SIMULATED,now));
  readings.save(new SensorReading(t,MeasurementType.WINDING_HOTSPOT_TEMPERATURE,bd(hot),"°C",DataSourceType.SIMULATED,now));
  readings.save(new SensorReading(t,MeasurementType.LOAD_PERCENT,bd(load),"%",DataSourceType.SIMULATED,now));
  readings.save(new SensorReading(t,MeasurementType.AMBIENT_TEMPERATURE,bd(ambient),"°C",DataSourceType.SIMULATED,now));
  readings.save(new SensorReading(t,MeasurementType.APPARENT_POWER_MVA,bd(apparentMva),"MVA",DataSourceType.SIMULATED,now));
  readings.save(new SensorReading(t,MeasurementType.HV_LINE_CURRENT_A,bd(hvCurrentA),"A",DataSourceType.SIMULATED,now));
  readings.save(new SensorReading(t,MeasurementType.LV_LINE_CURRENT_A,bd(lvCurrentA),"A",DataSourceType.SIMULATED,now));
  readings.save(new SensorReading(t,MeasurementType.COOLING_FAN_STATUS,bd(fanStatus),"ON=1 / OFF=0",DataSourceType.SIMULATED,now));
  var assessment=health.assess(t);realtime.publish(t.getId(),transformerService.dashboard(t.getId()));
 }}
 private BigDecimal bd(double d){return BigDecimal.valueOf(d).setScale(2,java.math.RoundingMode.HALF_UP);}
}
