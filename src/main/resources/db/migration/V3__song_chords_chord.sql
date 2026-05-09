CREATE TABLE IF NOT EXISTS "song_chords_chord" (
    "song_chords_id" INT NOT NULL REFERENCES "song_chords" ("song_chords_id"),
    "chord_id" INT NOT NULL REFERENCES "chord" ("chord_id"),
    "position" INT NOT NULL,
    PRIMARY KEY ("song_chords_id", "chord_id")
);

-- Assign chords to the first 10 song_chords (G major songs)
-- Each song gets a realistic set of open chords
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id", "position")
SELECT
    sc.song_chords_id,
    c.chord_id,
    row_number() OVER (PARTITION BY sc.song_chords_id ORDER BY c.name)
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('G', 'C', 'D', 'Em', 'Am')
WHERE s.name IN (
    'Wonderwall', 'Knockin'' on Heaven''s Door', 'Good Riddance',
    'Basket Case', 'Yellow', 'Buddy Holly', 'Santeria',
    'Iris', 'Sex and Candy', 'Don''t Look Back in Anger'
)
ON CONFLICT DO NOTHING;

-- Assign chords to E minor songs
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id", "position")
SELECT
    sc.song_chords_id,
    c.chord_id,
    row_number() OVER (PARTITION BY sc.song_chords_id ORDER BY c.name)
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('Em', 'Am', 'C', 'G', 'Dm')
WHERE s.name IN ('Smells Like Teen Spirit', 'Creep', 'Nothing Else Matters')
ON CONFLICT DO NOTHING;
