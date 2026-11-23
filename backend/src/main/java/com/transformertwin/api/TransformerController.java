package com.transformertwin.api;
import com.transformertwin.api.dto.ApiDtos; import com.transformertwin.api.dto.ApiDtos.*; import com.transformertwin.domain.*; import com.transformertwin.repository.*;
import com.transformertwin.service.*; import jakarta.validation.Valid;
import org.springframework.data.domain.*; import org.springframework.http.MediaType; import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.Instant; import java.util.*;
@RestController @RequestMapping("/api")
public class TransformerController {
 private final TransformerService service; private final TransformerRepository transformers; private final SensorReadingRepository readings; private final DiagnosticTestRepository diagnostics; private final ProtectionEventRepository protections; private final AlertRepository alerts; private final HealthAssessmentRepository health; private final RealtimeService realtime;
 public TransformerController(TransformerService s,TransformerRepository t,SensorReadingRepository r,DiagnosticTestRepository d,ProtectionEventRepository p,AlertRepository a,HealthAssessmentRepository h,RealtimeService rt){service=s;transformers=t;readings=r;diagnostics=d;protections=p;alerts=a;health=h;realtime=rt;}
 @GetMapping("/transformers") public PageResponse<TransformerView> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size){return service.list(page,clamp(size));}
 @PostMapping("/transformers") public TransformerView create(@Valid @RequestBody TransformerCreate body){return service.create(body);}
 @GetMapping("/transformers/{id}") public TransformerView get(@PathVariable Long id){return TransformerView.from(service.get(id));}
 @GetMapping("/transformers/{id}/dashboard") public Dashboard dashboard(@PathVariable Long id){return service.dashboard(id);}
 @GetMapping("/transformers/{id}/readings") public PageResponse<ReadingView> readings(@PathVariable Long id,@RequestParam MeasurementType metric,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size){
  service.get(id);
  Page<SensorReading> p=readings.findByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(id,metric,PageRequest.of(safePage(page),clamp(size)));
  return ApiDtos.page(p,ReadingView::from);
 }
 @GetMapping("/transformers/{id}/diagnostics") public PageResponse<DiagnosticView> diagnostics(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size){service.get(id);return ApiDtos.page(diagnostics.findByTransformerIdOrderByTestDateDesc(id,PageRequest.of(safePage(page),clamp(size))),DiagnosticView::from);}
 @PostMapping("/transformers/{id}/diagnostics") public DiagnosticView addDiagnostic(@PathVariable Long id,@Valid @RequestBody DiagnosticCreate x){Transformer t=service.get(id);DiagnosticTest d=diagnostics.save(new DiagnosticTest(t,x.testType().trim(),x.parameterName().trim().toUpperCase(Locale.ROOT),x.value(),x.unit(),x.testDate(),DataSourceType.IMPORTED,x.laboratory(),x.referenceNote(),x.notes(),x.componentKey(),x.phase()));return DiagnosticView.from(d);}
 @GetMapping("/transformers/{id}/protection-events") public PageResponse<ProtectionView> protection(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size){service.get(id);return ApiDtos.page(protections.findByTransformerIdOrderByOccurredAtDesc(id,PageRequest.of(safePage(page),clamp(size))),ProtectionView::from);}
 @PostMapping("/transformers/{id}/protection-events") public ProtectionView addProtection(@PathVariable Long id,@Valid @RequestBody ProtectionCreate x){Transformer t=service.get(id);var p=protections.save(new ProtectionEvent(t,x.deviceName().trim(),x.eventType().trim(),x.active(),x.occurredAt(),DataSourceType.IMPORTED,x.notes()));return ProtectionView.from(p);}
 @GetMapping("/transformers/{id}/alerts") public PageResponse<AlertView> alertPage(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size){service.get(id);return ApiDtos.page(alerts.findByTransformerIdOrderByCreatedAtDesc(id,PageRequest.of(safePage(page),clamp(size))),AlertView::from);}
 @GetMapping("/transformers/{id}/health-history") public PageResponse<HealthView> healthHistory(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size){service.get(id);return ApiDtos.page(health.findByTransformerIdOrderByAssessedAtDesc(id,PageRequest.of(safePage(page),clamp(size))),HealthView::summary);}
 @GetMapping(value="/transformers/{id}/events",produces=MediaType.TEXT_EVENT_STREAM_VALUE) public SseEmitter events(@PathVariable Long id){service.get(id);return realtime.subscribe(id);}
 private int safePage(int page){if(page<0)throw new IllegalArgumentException("Page must be zero or greater.");return page;}
 private int clamp(int size){if(size<1)throw new IllegalArgumentException("Page size must be positive.");return Math.min(size,100);}
}
