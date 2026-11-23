package com.transformertwin.repository;
import com.transformertwin.domain.Transformer; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.*; import java.util.*;
public interface TransformerRepository extends JpaRepository<Transformer,Long> {
 Optional<Transformer> findByAssetCode(String assetCode);
}
