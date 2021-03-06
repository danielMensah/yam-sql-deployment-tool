# YAM

### What is YAM?
YAM is an SQL script deployment tool created to make the process of deploying SQL scripts and version control easier.

### How it works?
YAM initially baselines a new database.

| installation\_rank | version | description | script | success | installed\_by | installed\_on |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | 1.0.0 | yam baseline | baseline script | true | yam | 2020-09-05 09:57:10.947748 |
| 2 | 1.0.1 | Hotfix | animal.sql | true | db_user | 2020-09-05 09:59:14.521461 |
| 3 | 1.0.1 | Hotfix | get\_test\_users.sql | true | db_user | 2020-09-05 09:59:14.553077 |
| 4 | 1.0.1 | Hotfix | person.sql | true | db_user | 2020-09-05 09:59:14.586926 |

Baselining is useful for the tool to:
1. Know the current version.
2. For the developers to know what has been deployed.
3. Know which script failed to be deployed.
4. Other features in the future.

Please use the following script to baseline your DB.

```
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
```

### Getting Started
To get started with YAM, firstly update the yam.properties.

Then run the Main.kt file.

### Supported JDBCs:
1. PostgreSQL
