package com.transformertwin.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="alert_notification", indexes={@Index(name="idx_notification_asset_time",columnList="transformer_id,created_at"),@Index(name="idx_notification_unread",columnList="transformer_id,read_at")})
public class AlertNotification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="transformer_id") private Transformer transformer;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="alert_id",unique=true) private Alert alert;
 @Column(nullable=false,length=180) private String title;
 @Column(nullable=false,length=1000) private String message;
 @Column(nullable=false,length=20) private String severity;
 @Column(nullable=false) private Instant createdAt;
 private Instant readAt;
 protected AlertNotification(){}
 public AlertNotification(Alert alert,Instant at){this.alert=alert;this.transformer=alert.getTransformer();this.title=alert.getTitle();this.message=alert.getMessage();this.severity=alert.getSeverity();this.createdAt=at;}
 public void markRead(Instant at){if(readAt==null)readAt=at;}
 public Long getId(){return id;} public Transformer getTransformer(){return transformer;} public Alert getAlert(){return alert;} public String getTitle(){return title;} public String getMessage(){return message;} public String getSeverity(){return severity;} public Instant getCreatedAt(){return createdAt;} public Instant getReadAt(){return readAt;}
}
