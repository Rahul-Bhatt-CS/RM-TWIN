package com.transformertwin.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transformer")
public class Transformer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "asset_code", length = 80, unique = true)
    private String assetCode;
    @Column(length = 80)
    private String rating;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transformer() {}
    public Transformer(String name, String assetCode, String rating) {
        this.name = name; this.assetCode = assetCode; this.rating = rating; this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAssetCode() { return assetCode; }
    public String getRating() { return rating; }
    public Instant getCreatedAt() { return createdAt; }
}
