DELETE FROM health_assessment
WHERE id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY transformer_id ORDER BY assessed_at DESC, id DESC) AS row_num
        FROM health_assessment
    ) ranked
    WHERE row_num > 1
);
