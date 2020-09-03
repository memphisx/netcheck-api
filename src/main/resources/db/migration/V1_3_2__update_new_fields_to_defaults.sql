UPDATE public."domain" SET endpoint='' WHERE endpoint IS NULL;
UPDATE public."domain" SET timeout_ms=30000 WHERE timeout_ms IS NULL;