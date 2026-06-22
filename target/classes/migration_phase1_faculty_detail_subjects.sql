-- Phase 1 migration: faculty_detail_subjects becomes a real entity table
-- (adds an id primary key + is_optional flag for the Optional/Compulsory subject feature)
--
-- IMPORTANT: back up your database before running this.
-- With spring.jpa.hibernate.ddl-auto=update, Hibernate will create the new
-- columns automatically on a FRESH database, but on an EXISTING database the
-- old table has a composite primary key (faculty_detail_id, subject_id) and
-- no `id`/`is_optional` columns, so run this once manually.

USE rms;

-- 1. Drop the old composite primary key
ALTER TABLE faculty_detail_subjects DROP PRIMARY KEY;

-- 2. Add the new surrogate primary key
ALTER TABLE faculty_detail_subjects
  ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

-- 3. Add the optional/compulsory flag (defaults everything to Compulsory)
ALTER TABLE faculty_detail_subjects
  ADD COLUMN is_optional BOOLEAN NOT NULL DEFAULT FALSE;

-- After this, start the application once with ddl-auto=update so Hibernate
-- can add foreign-key constraints/indexes if needed.
