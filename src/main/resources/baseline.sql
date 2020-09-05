CREATE TABLE IF NOT EXISTS yam_schema_history (
    installation_rank serial primary key,
    version varchar(50) not null,
    description varchar(200),
    script varchar(200) not null,
    success boolean not null,
    installed_by varchar(200) not null,
    installed_on timestamptz default current_timestamp
);

INSERT INTO yam_schema_history (version, description, script, success, installed_by)
VALUES ('1.0.0', 'yam baseline', 'baseline script', true, 'yam');