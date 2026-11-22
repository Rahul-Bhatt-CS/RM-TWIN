package com.transformertwin.repository;
import com.transformertwin.domain.Transformer;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TransformerRepository extends JpaRepository<Transformer, Long> {}
