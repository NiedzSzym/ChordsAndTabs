CREATE TABLE "instrument_type" (
  "instrument_type_id" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "name" varchar(30) UNIQUE NOT NULL,
  "string_count" integer NOT NULL
);

CREATE TYPE key_mode_enum AS ENUM ('MAJOR', 'MINOR');

CREATE TABLE "key" (
  "key_id" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "name" varchar(10) NOT NULL,
  "mode" key_mode_enum NOT NULL
);


CREATE TABLE "role" (
  "role_id" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "name" VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE "account" (
  "account_id" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "email" VARCHAR(255) UNIQUE NOT NULL,
  "password" VARCHAR(255) NOT NULL,
  "role_id" INT NOT NULL REFERENCES "role" ("role_id"),
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  "updated_at" TIMESTAMPTZ,
  "deleted_at" TIMESTAMPTZ,
  "email_verified_at" TIMESTAMPTZ
);

CREATE TABLE "account_profile" (
  "account_id" BIGINT NOT NULL PRIMARY KEY,
  "nickname" VARCHAR(100) not null,
  "bio" TEXT,
  "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  "updated_at" TIMESTAMPTZ,
  FOREIGN KEY ("account_id") REFERENCES "account" ("account_id")
);
CREATE TABLE "tuning" (
  "tuning_id" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "tuning" VARCHAR(12) NOT NULL ,
  "instrument_type_id" INT REFERENCES "instrument_type" ("instrument_type_id"),
  "created_by" BIGINT REFERENCES "account" ("account_id"),
  "deleted_at" TIMESTAMPTZ
);

CREATE TABLE "song" (
  "song_id" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "name" VARCHAR(100) NOT NULL,
  "release_year" INTEGER,
  "created_by" BIGINT REFERENCES "account" ("account_id"),
  "deleted_at" TIMESTAMPTZ
);
CREATE TABLE "artist" (
  "artist_id" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "name" VARCHAR(100) NOT NULL,
  UNIQUE ("name", "created_by"),
  "created_by" BIGINT REFERENCES "account" ("account_id"),
  "deleted_at" TIMESTAMPTZ
);

CREATE TABLE "artist_song" (
  "song_id" INT NOT NULL REFERENCES "song" ("song_id"),
  "artist_id" INT NOT NULL REFERENCES "artist" ("artist_id"),
  PRIMARY KEY ("song_id", "artist_id")
);

CREATE TABLE "song_chords" (
  "song_chords_id" SERIAL PRIMARY KEY,
  "song_id" INT NOT NULL,
  "author_id" BIGINT REFERENCES "account" ("account_id"),
  "created_at" TIMESTAMPTZ,
  "updated_at" TIMESTAMPTZ,
  "deleted_at" TIMESTAMPTZ,
  "status" VARCHAR(10) NOT NULL DEFAULT 'PUBLIC',
  "notation_type" VARCHAR(10),
  "key_id" INT REFERENCES "key" ("key_id"),
  "tuning_id" INT REFERENCES "tuning" ("tuning_id"),
  "instrument_type_id" INT REFERENCES "instrument_type" ("instrument_type_id"),
  "strumming_pattern" VARCHAR(100),
  "time_signature" VARCHAR(10),
  "tempo" INT,
  "capo_fret" INT,
  "song_body" TEXT,
  "created_by" BIGINT REFERENCES "account" ("account_id")
);


CREATE TABLE "chord" (
  "chord_id" SERIAL PRIMARY KEY,
  "name" VARCHAR(20) NOT NULL,
  "instrument_type_id" INT NOT NULL REFERENCES "instrument_type" ("instrument_type_id"),
  "tuning_id" INT NOT NULL REFERENCES "tuning" ("tuning_id"),
  "chord_fingering" TEXT NOT NULL,
  "created_by" BIGINT REFERENCES "account" ("account_id"),
  "deleted_at" TIMESTAMPTZ
);

CREATE TABLE "song_chords_chord" (
  "song_chords_id" INT NOT NULL REFERENCES "song_chords" ("song_chords_id"),
  "chord_id" INT NOT NULL REFERENCES "chord" ("chord_id"),
  PRIMARY KEY ("song_chords_id", "chord_id")
);

ALTER TABLE "song_chords" ADD FOREIGN KEY ("song_id") REFERENCES "song" ("song_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "artist_song" ADD FOREIGN KEY ("artist_id") REFERENCES "artist" ("artist_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "artist_song" ADD FOREIGN KEY ("song_id") REFERENCES "song" ("song_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "account" ADD FOREIGN KEY ("role_id") REFERENCES "role" ("role_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "account_profile" ADD FOREIGN KEY ("account_id") REFERENCES "account" ("account_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "song_chords" ADD FOREIGN KEY ("author_id") REFERENCES "account" ("account_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "song_chords" ADD FOREIGN KEY ("key_id") REFERENCES "key" ("key_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "tuning" ADD FOREIGN KEY ("instrument_type_id") REFERENCES "instrument_type" ("instrument_type_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "song_chords" ADD FOREIGN KEY ("tuning_id") REFERENCES "tuning" ("tuning_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "chord" ADD FOREIGN KEY ("instrument_type_id") REFERENCES "instrument_type" ("instrument_type_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "chord" ADD FOREIGN KEY ("tuning_id") REFERENCES "tuning" ("tuning_id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "song_chords" ADD FOREIGN KEY ("instrument_type_id") REFERENCES "instrument_type" ("instrument_type_id") DEFERRABLE INITIALLY IMMEDIATE;

INSERT INTO role (name)
VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO "instrument_type" ("name", "string_count") VALUES
    ('Gitara klasyczna', 6),
    ('Gitara akustyczna', 6),
    ('Gitara elektryczna', 6),
    ('Gitara basowa', 4),
    ('Ukulele', 4)
ON CONFLICT ("name") DO NOTHING;