-- 2) SAMPLE DATA

-- 2.1 Specialties
INSERT INTO petclinic.specialties (name) VALUES
('Radiology'),
('Surgery'),
('Dentistry'),
('Cardiology'),
('Dermatology');

-- 2.2 Vets
INSERT INTO petclinic.vets (first_name, last_name) VALUES
('Alice',   'Anderson'),
('Bob',     'Brown'),
('Carol',   'Clark'),
('David',   'Davis');

-- 2.3 Assign specialties to vets
INSERT INTO petclinic.vet_specialties (vet_id, specialty_id) VALUES
-- Alice: Radiology, Surgery
(1, 1),
(1, 2),
-- Bob: Surgery, Dentistry
(2, 2),
(2, 3),
-- Carol: Cardiology, Dermatology
(3, 4),
(3, 5),
-- David: Radiology, Cardiology, Surgery
(4, 1),
(4, 4),
(4, 2);