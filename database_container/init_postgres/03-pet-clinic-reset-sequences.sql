-- Specialties sequence reset
SELECT nextval('petclinic.specialties_id_seq');
SELECT setval('petclinic.specialties_id_seq', (SELECT MAX(id) FROM petclinic.specialties));
SELECT nextval('petclinic.specialties_id_seq');

-- Vets sequence reset
SELECT nextval('petclinic.vets_id_seq');
SELECT setval('petclinic.vets_id_seq', (SELECT MAX(id) FROM petclinic.vets));
SELECT nextval('petclinic.vets_id_seq');