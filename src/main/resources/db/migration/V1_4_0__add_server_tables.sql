CREATE TABLE public.server (
    id varchar(255) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    server_name varchar(255) NOT NULL,
    description varchar(255) NULL,
    password varchar(255) NOT NULL,
    CONSTRAINT server_pkey PRIMARY KEY (id)
);

CREATE TABLE public.server_metric (
   id varchar(255) NOT NULL,
   created_at timestamp NOT NULL,
   updated_at timestamp NOT NULL,
   collected_at timestamp NOT NULL,
   server_id varchar(255) NOT NULL,
   metrics jsonb NOT NULL,
   CONSTRAINT server_metric_pkey PRIMARY KEY (id),
   CONSTRAINT server_metric_server_fk FOREIGN KEY (server_id) REFERENCES server(id)
);

CREATE TABLE public.server_metric_definition (
  field_name varchar(255) NOT NULL,
  server_id varchar(255) NOT NULL,
  created_at timestamp NOT NULL,
  updated_at timestamp NOT NULL,
  label varchar(255) NOT NULL,
  suffix varchar(255) NULL,
  max_threshold varchar(255) NULL,
  min_threshold varchar(255) NULL,
  notify bool NOT NULL DEFAULT FALSE,
  value_type varchar(255) NOT NULL,
  extended_type varchar(255) NULL,
  metric_kind varchar(255) NOT NULL,
  CONSTRAINT server_metric_definition_pkey PRIMARY KEY (field_name),
  CONSTRAINT server_metric_definition_server_fk FOREIGN KEY (server_id) REFERENCES server(id)
);

CREATE TABLE public.server_domain (
   server_id varchar(255) NOT NULL,
   domain varchar(255) NOT NULL,
   CONSTRAINT server_domain_pkey PRIMARY KEY (server_id, domain),
   CONSTRAINT server_domain_server_fk FOREIGN KEY (server_id) REFERENCES server(id),
   CONSTRAINT server_domain_domain_fk FOREIGN KEY (domain) REFERENCES domain(domain)
);

CREATE INDEX server__id_password_idx ON public.server USING btree (id, password);
CREATE INDEX server_metric__server_created_idx ON public.server_metric USING btree (server_id, created_at);
CREATE INDEX server_metric__server_collected_idx ON public.server_metric USING btree (server_id, collected_at);
CREATE INDEX server_metric_definition__server_created_idx ON public.server_metric_definition USING btree (server_id, created_at);