CREATE TABLE public.certificate (
    id varchar(255) NOT NULL,
    created_at timestamp NULL,
    updated_at timestamp NULL,
    basic_constraints int4 NULL,
    expired bool NULL,
    is_valid bool NULL,
    issued_by varchar(255) NULL,
    issued_for varchar(255) NULL,
    not_after timestamp NULL,
    not_before timestamp NULL,
    not_yet_valid bool NULL,
    CONSTRAINT certificate_pkey PRIMARY KEY (id)
);

CREATE TABLE public."domain" (
    "domain" varchar(255) NOT NULL,
    created_at timestamp NULL,
    updated_at timestamp NULL,
    check_frequency_minutes int4 NULL,
    CONSTRAINT domain_pkey PRIMARY KEY (domain)
);

CREATE TABLE public.protocol_check (
    id varchar(255) NOT NULL,
    created_at timestamp NULL,
    updated_at timestamp NULL,
    connection_accepted bool NULL,
    dns_resolves bool NULL,
    hostname varchar(255) NULL,
    protocol varchar(255) NULL,
    redirect_uri varchar(255) NULL,
    status_code int4 NULL,
    CONSTRAINT protocol_check_pkey PRIMARY KEY (id)
);

CREATE TABLE public.domain_check (
    id varchar(255) NOT NULL,
    created_at timestamp NULL,
    updated_at timestamp NULL,
    change_type varchar(255) NULL,
    "domain" varchar(255) NULL,
    http_ip_address varchar(255) NULL,
    http_response_time_ns int8 NULL,
    https_ip_address varchar(255) NULL,
    https_response_time_ns int8 NULL,
    check_date timestamp NULL,
    CONSTRAINT domain_check_pkey PRIMARY KEY (id),
    CONSTRAINT domain_check_domain_fk FOREIGN KEY (domain) REFERENCES domain(domain)
);
CREATE INDEX domain_check__domain_change_type_idx ON public.domain_check USING btree (domain, change_type);
CREATE INDEX domain_check__domain_check_date_idx ON public.domain_check USING btree (domain, check_date);
CREATE INDEX domain_check__domain_created_idx ON public.domain_check USING btree (domain, created_at);

CREATE TABLE public.domain_metric (
    id varchar(255) NOT NULL,
    created_at timestamp NULL,
    updated_at timestamp NULL,
    avg_response_time_ns int8 NULL,
    "domain" varchar(255) NULL,
    end_period timestamp NULL,
    max_response_time_ns int8 NULL,
    min_response_time_ns int8 NULL,
    period_type varchar(255) NULL,
    protocol varchar(255) NULL,
    start_period timestamp NULL,
    successful_checks int4 NULL,
    total_checks int4 NULL,
    CONSTRAINT domain_metric_pkey PRIMARY KEY (id),
    CONSTRAINT domain_metric_domain_fk FOREIGN KEY (domain) REFERENCES domain(domain)
);
CREATE INDEX domain_metric__domain_period_created_idx ON public.domain_metric USING btree (domain, period_type, created_at);
CREATE INDEX domain_metric__domain_period_protocol_idx ON public.domain_metric USING btree (domain, period_type, protocol);

CREATE TABLE public.previous_check_certificate (
    domain_check_id varchar(255) NOT NULL,
    certificate_id varchar(255) NOT NULL,
    CONSTRAINT previous_check_certificate_pkey PRIMARY KEY (domain_check_id, certificate_id),
    CONSTRAINT previous_check_certificate_fk FOREIGN KEY (certificate_id) REFERENCES certificate(id),
    CONSTRAINT previous_check_domain_check_fk FOREIGN KEY (domain_check_id) REFERENCES domain_check(id)
);

CREATE TABLE public.previous_check_protocol (
    domain_check_id varchar(255) NOT NULL,
    protocol_check_id varchar(255) NOT NULL,
    CONSTRAINT previous_check_protocol_pkey PRIMARY KEY (domain_check_id, protocol_check_id),
    CONSTRAINT previous_check_protocol_fk FOREIGN KEY (protocol_check_id) REFERENCES protocol_check(id),
    CONSTRAINT previous_check_domain_check_fk FOREIGN KEY (domain_check_id) REFERENCES domain_check(id)
);

CREATE TABLE public.check_certificate (
    domain_check_id varchar(255) NOT NULL,
    certificate_id varchar(255) NOT NULL,
    CONSTRAINT check_certificate_pkey PRIMARY KEY (domain_check_id, certificate_id),
    CONSTRAINT check_certificate_domain_check_fk FOREIGN KEY (domain_check_id) REFERENCES domain_check(id),
    CONSTRAINT check_certificate_certificate_fk FOREIGN KEY (certificate_id) REFERENCES certificate(id)
);

CREATE TABLE public.check_protocol (
    domain_check_id varchar(255) NOT NULL,
    protocol_check_id varchar(255) NOT NULL,
    CONSTRAINT check_protocol_pkey PRIMARY KEY (domain_check_id, protocol_check_id),
    CONSTRAINT check_protocol_domain_check_fk FOREIGN KEY (domain_check_id) REFERENCES domain_check(id),
    CONSTRAINT check_protocol_protocol_check_fk FOREIGN KEY (protocol_check_id) REFERENCES protocol_check(id)
);