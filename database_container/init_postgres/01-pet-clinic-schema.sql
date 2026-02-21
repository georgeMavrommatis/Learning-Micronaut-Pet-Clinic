-- 1) SCHEMA DEFINITION
CREATE SCHEMA IF NOT EXISTS petclinic;

-- 1) Specialties lookup: no two specialties can share the same name
CREATE TABLE IF NOT EXISTS petclinic.specialties (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE   -- ← this UNIQUE prevents duplicate names
);

-- 2) Vets: no two vets can share both the same first and last name
CREATE TABLE IF NOT EXISTS petclinic.vets (
  id         SERIAL PRIMARY KEY,
  first_name VARCHAR(50) NOT NULL,
  last_name  VARCHAR(50) NOT NULL,
  CONSTRAINT uk_vets_fullname UNIQUE (first_name, last_name)  -- ← composite unique
);

-- 3) Join table: which vet has which specialties
CREATE TABLE IF NOT EXISTS petclinic.vet_specialties (
  vet_id       INT NOT NULL
                  REFERENCES petclinic.vets(id)
                  ON DELETE CASCADE, --  if a vet record is deleted then all relation rows are deleted automatically
  specialty_id INT NOT NULL
                  REFERENCES petclinic.specialties(id)
                  ON DELETE CASCADE, --  if a speciallty record is deleted then all relation rows are deleted automatically
  PRIMARY KEY (vet_id, specialty_id)
);
