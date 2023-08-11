ALTER TABLE public."domain" ADD COLUMN http_port int4 DEFAULT 80;
ALTER TABLE public."domain" ADD COLUMN https_port int4 DEFAULT 443;