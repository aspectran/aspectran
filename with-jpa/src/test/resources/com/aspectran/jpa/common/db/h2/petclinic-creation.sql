DROP SCHEMA IF EXISTS petclinic_test;
CREATE SCHEMA IF NOT EXISTS petclinic_test;
RUNSCRIPT FROM 'classpath:com/aspectran/jpa/common/db/h2/petclinic-schema.sql';
RUNSCRIPT FROM 'classpath:com/aspectran/jpa/common/db/h2/petclinic-dataload.sql';
