package com.transformertwin.service;

import com.transformertwin.domain.DiagnosticTest;
import com.transformertwin.repository.DiagnosticTestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** Sample-coherent DGA evidence. It deliberately does not claim a standards diagnosis. */
@Service
public class DgaAnalysisService {
 private final DiagnosticTestRepository repo;
 public DgaAnalysisService(DiagnosticTestRepository repo){this.repo=repo;}
 public Map<String,Object> analyze(Long transformerId){
  List<DiagnosticTest> rows=repo.findByTransformerIdOrderByTestDateDesc(transformerId, PageRequest.of(0,1000)).getContent().stream().filter(d->d.getTestType().equalsIgnoreCase("DGA")).toList();
  Map<String,List<DiagnosticTest>> grouped=rows.stream().collect(Collectors.groupingBy(this::sampleKey));
  List<Map<String,Object>> samples=new ArrayList<>();
  grouped.values().stream().sorted(Comparator.comparing(this::sampleDate)).forEach(group->{
   Map<String,Object> sample=new LinkedHashMap<>(); DiagnosticTest first=group.get(0);
   sample.put("sampleId",first.getSampleId()==null?sampleKey(first):first.getSampleId()); sample.put("sampleDate",sampleDate(group)); sample.put("samplePoint",first.getSamplePoint()); sample.put("component",first.getComponentKey()); sample.put("laboratory",first.getLaboratory()); sample.put("source",first.getSource().name());
   Map<String,Object> gases=new LinkedHashMap<>(); for(DiagnosticTest d:group){Map<String,Object> v=new LinkedHashMap<>();v.put("value",d.getValue());v.put("unit",d.getUnit());v.put("detectionLimit",null);gases.put(d.getParameterName().toUpperCase(Locale.ROOT),v);} sample.put("gases",gases); sample.put("ratios",ratios(group)); samples.add(sample);
  });
  Map<String,Object> trends=new LinkedHashMap<>();
  Set<String> gasNames=rows.stream().map(d->d.getParameterName().toUpperCase(Locale.ROOT)).collect(Collectors.toCollection(TreeSet::new));
  for(String gas:gasNames){List<Map<String,Object>> points=new ArrayList<>();List<DiagnosticTest> ordered=rows.stream().filter(d->d.getParameterName().equalsIgnoreCase(gas)).sorted(Comparator.comparing(DiagnosticTest::getTestDate)).toList();for(int i=0;i<ordered.size();i++){DiagnosticTest current=ordered.get(i);Map<String,Object> point=new LinkedHashMap<>();point.put("sampleId",current.getSampleId()==null?sampleKey(current):current.getSampleId());point.put("sampleDate",current.getTestDate());point.put("value",current.getValue());point.put("unit",current.getUnit());Object rate="INSUFFICIENT_COMPARABLE_HISTORY";if(i>0){DiagnosticTest previous=ordered.get(i-1);boolean comparable=Objects.equals(previous.getComponentKey(),current.getComponentKey())&&Objects.equals(previous.getSamplePoint(),current.getSamplePoint());long hours=Duration.between(previous.getTestDate(),current.getTestDate()).toHours();if(comparable&&hours>0)rate=current.getValue().subtract(previous.getValue()).multiply(BigDecimal.valueOf(24)).divide(BigDecimal.valueOf(hours),4,RoundingMode.HALF_UP);}point.put("ratePerDay",rate);points.add(point);}trends.put(gas,points);}
  Map<String,Object> out=new LinkedHashMap<>();out.put("transformerId",transformerId);out.put("sampleCount",samples.size());out.put("samples",samples);out.put("gasTrends",trends);out.put("latestSample",samples.isEmpty()?null:samples.get(samples.size()-1));out.put("interpretationStatus",samples.isEmpty()?"NO_DGA_DATA":"SAMPLE_LEVEL_EVIDENCE");out.put("interpretationNote","Ratios are calculated only within a single sample identity. Trends require comparable samples, sampling points, methods and detection-limit handling. This is evidence for engineering review, not a standalone IEC 60599 / IEEE C57.104 diagnosis.");return out;
 }
 private String sampleKey(DiagnosticTest d){if(d.getSampleId()!=null&&!d.getSampleId().isBlank())return d.getSampleId()+"|"+Objects.toString(d.getComponentKey(),"UNKNOWN")+"|"+Objects.toString(d.getSamplePoint(),"UNKNOWN");return Objects.toString(d.getComponentKey(),"UNKNOWN")+"|"+Objects.toString(d.getSamplePoint(),"UNKNOWN")+"|"+d.getTestDate().toString();}
 private Instant sampleDate(List<DiagnosticTest> group){return group.stream().map(DiagnosticTest::getTestDate).min(Instant::compareTo).orElse(Instant.EPOCH);}
 private Map<String,Object> ratios(List<DiagnosticTest> group){Map<String,BigDecimal> v=new HashMap<>();group.forEach(d->v.put(d.getParameterName().toUpperCase(Locale.ROOT),d.getValue()));Map<String,Object> r=new LinkedHashMap<>();r.put("CH4_H2",ratio(v.get("METHANE"),v.get("HYDROGEN")));r.put("C2H2_C2H4",ratio(v.get("ACETYLENE"),v.get("ETHYLENE")));r.put("C2H4_C2H6",ratio(v.get("ETHYLENE"),v.get("ETHANE")));r.put("CO2_CO",ratio(v.get("CARBON_DIOXIDE"),v.get("CARBON_MONOXIDE")));return r;}
 private Object ratio(BigDecimal a,BigDecimal b){return a==null||b==null||b.signum()==0?"UNAVAILABLE":a.divide(b,4,RoundingMode.HALF_UP);}
}
