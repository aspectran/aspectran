DROP SCHEMA IF EXISTS petclinic_test;
CREATE SCHEMA IF NOT EXISTS petclinic_test;
RUNSCRIPT FROM 'classpath:config/h2/petclinic-schema.sql';
RUNSCRIPT FROM 'classpath:config/h2/petclinic-dataload.sql';
