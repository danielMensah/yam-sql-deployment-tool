SELECT
    version,
    description,
    script,
    success,
    installed_by,
    installed_on
FROM yam_schema_history
ORDER BY installation_rank;