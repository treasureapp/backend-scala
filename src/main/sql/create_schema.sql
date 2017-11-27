-- assume database findw already created
\connect findw

-- lowercase schema, table, and column names (ISO standard)
-- everything is requires quotes "" ignored/not accepted by postgres / aws

CREATE SCHEMA IF NOT EXISTS dim;

CREATE SCHEMA IF NOT EXISTS fact;

CREATE SCHEMA IF NOT EXISTS staging;

-- create tables using Slick