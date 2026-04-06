CREATE SEQUENCE IF NOT EXISTS base_entity_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NOMAXVALUE;

CREATE TABLE IF NOT EXISTS vets (
    id         NUMBER CONSTRAINT pk_vets PRIMARY KEY,
    first_name VARCHAR2(30),
    last_name  VARCHAR2(30)
);
CREATE INDEX IF NOT EXISTS vets_last_name ON vets (last_name);

CREATE TABLE IF NOT EXISTS specialties (
    id   NUMBER CONSTRAINT pk_specialties PRIMARY KEY,
    name VARCHAR2(80)
);
CREATE INDEX IF NOT EXISTS specialties_name ON specialties (name);

CREATE TABLE IF NOT EXISTS vet_specialties (
    vet_id       NUMBER NOT NULL,
    specialty_id NUMBER NOT NULL
);
ALTER TABLE vet_specialties DROP CONSTRAINT IF EXISTS fk_vet_specialties_vets;
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_vets FOREIGN KEY (vet_id) REFERENCES vets (id);
ALTER TABLE vet_specialties DROP CONSTRAINT IF EXISTS fk_vet_specialties_specialties;
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_specialties FOREIGN KEY (specialty_id) REFERENCES specialties (id);

CREATE TABLE IF NOT EXISTS types (
    id   NUMBER CONSTRAINT pk_types PRIMARY KEY,
    name VARCHAR2(80)
);
CREATE INDEX IF NOT EXISTS types_name ON types (name);

CREATE TABLE IF NOT EXISTS owners (
    id         NUMBER CONSTRAINT pk_owners PRIMARY KEY,
    first_name VARCHAR2(30),
    last_name  VARCHAR2(30),
    address    VARCHAR2(255),
    city       VARCHAR2(80),
    telephone  VARCHAR2(20)
);
CREATE INDEX IF NOT EXISTS owners_last_name ON owners (last_name);

CREATE TABLE IF NOT EXISTS pets (
    id         NUMBER CONSTRAINT pk_pets PRIMARY KEY,
    name       VARCHAR2(30),
    birth_date DATE,
    type_id    NUMBER NOT NULL,
    owner_id   NUMBER
);
ALTER TABLE pets DROP CONSTRAINT IF EXISTS fk_pets_owners;
ALTER TABLE pets ADD CONSTRAINT fk_pets_owners FOREIGN KEY (owner_id) REFERENCES owners (id);
ALTER TABLE pets DROP CONSTRAINT IF EXISTS fk_pets_types;
ALTER TABLE pets ADD CONSTRAINT fk_pets_types FOREIGN KEY (type_id) REFERENCES types (id);
CREATE INDEX IF NOT EXISTS pets_name ON pets (name);

CREATE TABLE IF NOT EXISTS visits (
    id          NUMBER CONSTRAINT pk_visits PRIMARY KEY,
    pet_id      NUMBER,
    visit_date  DATE,
    description VARCHAR2(255)
);
ALTER TABLE visits DROP CONSTRAINT IF EXISTS fk_visits_pets;
ALTER TABLE visits ADD CONSTRAINT fk_visits_pets FOREIGN KEY (pet_id) REFERENCES pets (id);
CREATE INDEX IF NOT EXISTS visits_pet_id ON visits (pet_id);
