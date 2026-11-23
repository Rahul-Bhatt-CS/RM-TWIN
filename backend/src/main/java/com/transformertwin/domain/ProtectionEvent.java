package com.transformertwin.domain;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="protection_event")
public class ProtectionEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="transformer_id") private Transformer transformer;
 @Column(nullable=false,length=80) private String deviceName; @Column(nullable=false,length=40) private String eventType;
 private boolean active; @Column(nullable=false) private Instant occurredAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private DataSourceType source;
 @Column(length=500) private String notes;
 protected ProtectionEvent(){}
 public ProtectionEvent(Transformer t,String d,String e,boolean a,Instant at,DataSourceType s,String n){transformer=t;deviceName=d;eventType=e;active=a;occurredAt=at;source=s;notes=n;}
 public Long getId(){return id;} public Transformer getTransformer(){return transformer;} public String getDeviceName(){return deviceName;} public String getEventType(){return eventType;} public boolean isActive(){return active;} public Instant getOccurredAt(){return occurredAt;} public DataSourceType getSource(){return source;} public String getNotes(){return notes;}
}
