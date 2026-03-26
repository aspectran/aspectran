-- DROP SEQUENCE base_entity_id_seq;
-- DROP TABLE vet_specialties IF EXISTS;
-- DROP TABLE vets IF EXISTS;
-- DROP TABLE specialties IF EXISTS;
-- DROP TABLE visits IF EXISTS;
-- DROP TABLE pets IF EXISTS;
-- DROP TABLE types IF EXISTS;
-- DROP TABLE owners IF EXISTS;

CREATE SEQUENCE base_entity_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NOMAXVALUE;

CREATE TABLE vets (
    id         NUMBER CONSTRAINT pk_vets PRIMARY KEY,
    first_name VARCHAR2(30),
    last_name  VARCHAR2(30)
);
CREATE INDEX vets_last_name ON vets (last_name);

CREATE TABLE specialties (
    id   NUMBER CONSTRAINT pk_specialties PRIMARY KEY,
    name VARCHAR2(80)
);
CREATE INDEX specialties_name ON specialties (name);

CREATE TABLE vet_specialties (
    vet_id       NUMBER NOT NULL,
    specialty_id NUMBER NOT NULL
);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_vets FOREIGN KEY (vet_id) REFERENCES vets (id);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_specialties FOREIGN KEY (specialty_id) REFERENCES specialties (id);

CREATE TABLE types (
    id   NUMBER CONSTRAINT pk_types PRIMARY KEY,
    name VARCHAR2(80)
);
CREATE INDEX types_name ON types (name);

CREATE TABLE owners (
    id         NUMBER CONSTRAINT pk_owners PRIMARY KEY,
    first_name VARCHAR2(30),
    last_name  VARCHAR2(30),
    address    VARCHAR2(255),
    city       VARCHAR2(80),
    telephone  VARCHAR2(20)
);
CREATE INDEX owners_last_name ON owners (last_name);

CREATE TABLE pets (
    id         NUMBER CONSTRAINT pk_pets PRIMARY KEY,
    name       VARCHAR2(30),
    birth_date DATE,
    type_id    NUMBER NOT NULL,
    owner_id   NUMBER
);
ALTER TABLE pets ADD CONSTRAINT fk_pets_owners FOREIGN KEY (owner_id) REFERENCES owners (id);
ALTER TABLE pets ADD CONSTRAINT fk_pets_types FOREIGN KEY (type_id) REFERENCES types (id);
CREATE INDEX pets_name ON pets (name);

CREATE TABLE visits (
    id          NUMBER CONSTRAINT pk_visits PRIMARY KEY,
    pet_id      NUMBER,
    visit_date  DATE,
    description VARCHAR2(255)
);
ALTER TABLE visits ADD CONSTRAINT fk_visits_pets FOREIGN KEY (pet_id) REFERENCES pets (id);
CREATE INDEX visits_pet_id ON visits (pet_id);
