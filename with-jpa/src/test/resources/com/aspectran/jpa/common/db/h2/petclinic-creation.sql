DROP ALL OBJECTS;
RUNSCRIPT FROM 'classpath:com/aspectran/jpa/common/db/h2/petclinic-schema.sql';
RUNSCRIPT FROM 'classpath:com/aspectran/jpa/common/db/h2/petclinic-dataload.sql';
