-- 1) SCHEMA DEFINITION
CREATE SCHEMA IF NOT EXISTS petclinic;

-- Specialties lookup
CREATE TABLE petclinic.specialties (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

-- Vets
CREATE TABLE petclinic.vets (
  id         SERIAL PRIMARY KEY,
  first_name VARCHAR(50) NOT NULL,
  last_name  VARCHAR(50) NOT NULL
);

-- Join table: which vet has which specialties
CREATE TABLE petclinic.vet_specialties (
  vet_id       INT NOT NULL
                  REFERENCES petclinic.vets(id)
                  ON DELETE CASCADE,
  specialty_id INT NOT NULL
                  REFERENCES petclinic.specialties(id)
                  ON DELETE CASCADE,
  PRIMARY KEY (vet_id, specialty_id)
);
