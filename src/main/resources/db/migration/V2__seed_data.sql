-- Seed data for testing
-- Run order is enforced by foreign keys

-- ─── Keys (major/minor) ──────────────────────────────────────────────
INSERT INTO "key" ("name", "mode") VALUES
  ('C', 'MAJOR'),   ('C', 'MINOR'),
  ('D', 'MAJOR'),   ('D', 'MINOR'),
  ('E', 'MAJOR'),   ('E', 'MINOR'),
  ('F', 'MAJOR'),   ('F', 'MINOR'),
  ('G', 'MAJOR'),   ('G', 'MINOR'),
  ('A', 'MAJOR'),   ('A', 'MINOR'),
  ('B', 'MAJOR'),   ('B', 'MINOR')
ON CONFLICT DO NOTHING;

-- ─── Tunings (by instrument) ────────────────────────────────────────
-- instrument_type: 1=Gitara klasyczna, 2=Gitara akustyczna,
--                  3=Gitara elektryczna, 4=Gitara basowa, 5=Ukulele
INSERT INTO "tuning" ("tuning", "instrument_type_id") VALUES
  -- Standard (guitars)
  ('EADGBE', 1), ('EADGBE', 2), ('EADGBE', 3),
  -- Drop D
  ('DADGBE', 1), ('DADGBE', 2), ('DADGBE', 3),
  -- Half step down
  ('EADGBB', 1), ('EADGBB', 2),
  -- Open G
  ('DGDGBD', 1), ('DGDGBD', 2),
  -- Open D
  ('DADF#AD', 1),
  -- Bass standard
  ('EADG', 4), ('DADG', 4),
  -- Bass 5-string
  ('BEADG', 4),
  -- Ukulele standard
  ('GCEA', 5), ('ADF#B', 5)
ON CONFLICT DO NOTHING;

-- ─── Test Accounts ──────────────────────────────────────────────────
-- role_id: 1=ROLE_USER, 2=ROLE_ADMIN
-- password = bcrypt encoded "password123" ($2a$10$...)
INSERT INTO "account" ("email", "password", "role_id") VALUES
  ('jan@test.pl', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('anna@test.pl', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('admin@test.pl', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2),
  ('tester@test.pl', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('music@test.pl', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1)
ON CONFLICT DO NOTHING;

INSERT INTO "account_profile" ("account_id", "nickname", "bio")
SELECT a.account_id, 'JanGuitar', 'Kocham gitarę od 2010'
FROM "account" a WHERE a.email = 'jan@test.pl'
ON CONFLICT DO NOTHING;
INSERT INTO "account_profile" ("account_id", "nickname", "bio")
SELECT a.account_id, 'AnnaMusic', 'Gitara i ukulele'
FROM "account" a WHERE a.email = 'anna@test.pl'
ON CONFLICT DO NOTHING;
INSERT INTO "account_profile" ("account_id", "nickname", "bio")
SELECT a.account_id, 'Admin', 'Administrator'
FROM "account" a WHERE a.email = 'admin@test.pl'
ON CONFLICT DO NOTHING;
INSERT INTO "account_profile" ("account_id", "nickname", "bio")
SELECT a.account_id, 'Tester01', 'Test account'
FROM "account" a WHERE a.email = 'tester@test.pl'
ON CONFLICT DO NOTHING;
INSERT INTO "account_profile" ("account_id", "nickname", "bio")
SELECT a.account_id, 'MusicLover', 'Tablatury to moje życie'
FROM "account" a WHERE a.email = 'music@test.pl'
ON CONFLICT DO NOTHING;

-- ─── Artists ────────────────────────────────────────────────────────
INSERT INTO "artist" ("name", "created_by")
SELECT v.name, (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
FROM (VALUES
  ('Led Zeppelin'), ('Pink Floyd'), ('The Beatles'), ('The Rolling Stones'),
  ('Bob Dylan'), ('Jimi Hendrix'), ('Eric Clapton'), ('David Bowie'),
  ('Queen'), ('The Doors'), ('Nirvana'), ('Radiohead'),
  ('Metallica'), ('AC/DC'), ('Guns N'' Roses'), ('Oasis'),
  ('U2'), ('Coldplay'), ('Red Hot Chili Peppers'), ('Foo Fighters'),
  ('Pearl Jam'), ('Green Day'), ('Aerosmith'), ('Van Halen'),
  ('Deep Purple'), ('Black Sabbath'), ('Iron Maiden'), ('Kiss'),
  ('The Who'), ('Fleetwood Mac'), ('Eagles'), ('Creedence Clearwater Revival'),
  ('The Police'), ('Dire Straits'), ('Stevie Ray Vaughan'), ('Prince'),
  ('Rage Against the Machine'), ('Tool'), ('Audioslave'), ('System of a Down'),
  ('Linkin Park'), ('Muse'), ('Arctic Monkeys'), ('The Strokes'),
  ('Franz Ferdinand'), ('Kings of Leon'), ('The Killers'), ('Gorillaz'),
  ('Beck'), ('Tom Petty'),
  ('The Cure'), ('Depeche Mode'), ('R.E.M.'), ('Smashing Pumpkins'),
  ('Soundgarden'), ('Alice in Chains'), ('Stone Temple Pilots'), ('Blind Melon'),
  ('Weezer'), ('The Offspring'),
  ('Sublime'), ('No Doubt'), ('Blink-182'), ('Sum 41'),
  ('My Chemical Romance'), ('Fall Out Boy'), ('Panic! At the Disco'), ('Paramore'),
  ('Twenty One Pilots'), ('Imagine Dragons'), ('Arctic Monkeys'),
  ('Oasis'), ('Blur'), ('Pulp'), ('Suede'),
  ('Massive Attack'), ('Portishead'), ('The Verve'), ('Radiohead'),
  ('Travis'), ('Keane'), ('Snow Patrol'), ('Stereophonics'),
  ('Manic Street Preachers'), ('Ash'), ('Supergrass'), ('Ocean Colour Scene'),
  ('Dodgy'), ('Cast'), ('Shed Seven'), ('Kula Shaker')
) AS v (name)
ON CONFLICT (name) DO NOTHING;

-- ─── Songs ──────────────────────────────────────────────────────────
-- release_year = NULL for songs where year is not applicable
INSERT INTO "song" ("name", "release_year", "created_by")
SELECT v.*, (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
FROM (VALUES
  ('Stairway to Heaven', 1971), ('Hotel California', 1977), ('Bohemian Rhapsody', 1975),
  ('Comfortably Numb', 1979), ('Wonderwall', 1995), ('Smells Like Teen Spirit', 1991),
  ('Nothing Else Matters', 1991), ('Back in Black', 1980), ('Sweet Child O Mine', 1987),
  ('Losing My Religion', 1991), ('Creep', 1993), ('Paranoid Android', 1997),
  ('Karma Police', 1997), ('Enter Sandman', 1991), ('Master of Puppets', 1986),
  ('Highway to Hell', 1979), ('Thunderstruck', 1990), ('November Rain', 1991),
  ('Don''t Cry', 1991), ('Welcome to the Jungle', 1987), ('Knockin'' on Heaven''s Door', 1973),
  ('Layla', 1970), ('Tears in Heaven', 1992), ('Purple Haze', 1967),
  ('All Along the Watchtower', 1968), ('The Wind Cries Mary', 1967), ('Foxy Lady', 1967),
  ('Black', 1991), ('Jeremy', 1991), ('Alive', 1991),
  ('Under the Bridge', 1991), ('Californication', 1999), ('Give It Away', 1991),
  ('Everlong', 1997), ('Learn to Fly', 1999), ('Best of You', 2005),
  ('Basket Case', 1994), ('American Idiot', 2004), ('Boulevard of Broken Dreams', 2004),
  ('Good Riddance', 1997), ('Iris', 1998), ('Bitter Sweet Symphony', 1997),
  ('Wonderwall', 1995), ('Champagne Supernova', 1995), ('Don''t Look Back in Anger', 1995),
  ('Song 2', 1997), ('Coffee & TV', 1999), ('Clint Eastwood', 2001),
  ('Feel Good Inc.', 2005), ('Yellow', 2000), ('Fix You', 2005),
  ('Clocks', 2002), ('The Scientist', 2002), ('Viva la Vida', 2008),
  ('Uprising', 2009), ('Supermassive Black Hole', 2006), ('Starlight', 2006),
  ('Somebody That I Used to Know', 2011), ('Mr. Brightside', 2003),
  ('All the Small Things', 1999), ('Last Resort', 2000), ('Dammit', 1997),
  ('In the End', 2000), ('Numb', 2003), ('One Step Closer', 2000),
  ('Chop Suey', 2001), ('Toxicity', 2001), ('B.Y.O.B.', 2005),
  ('Killing in the Name', 1992), ('Bullet with Butterfly Wings', 1995),
  ('Today', 1993), ('1979', 1995), ('Buddy Holly', 1994), ('Island in the Sun', 2001),
  ('Self Esteem', 1994), ('Pretty Fly', 1998), ('Come Out and Play', 1994),
  ('Santeria', 1996), ('Date Rape', 1994), ('Just a Girl', 1995),
  ('Don''t Speak', 1995), ('Sex and Candy', 1997),
  ('Semi-Charmed Life', 1997), ('Losing My Religion', 1991),
  ('Man on the Moon', 1992), ('Losing My Religion', 1991),
  ('Fake Plastic Trees', 1995), ('No Surprises', 1997),
  ('Lucky', 1997), ('High and Dry', 1995),
  ('Street Spirit', 1995), ('Let Down', 1997),
  ('How to Disappear Completely', 2000), ('Everything in Its Right Place', 2000),
  ('Idioteque', 2000), ('There There', 2003),
  ('15 Step', 2007), ('Nude', 2007),
  ('Weird Fishes', 2007), ('Reckoner', 2007),
  ('Daydreaming', 2016), ('Burn the Witch', 2016),
  ('True Love Waits', 2016), ('Present Tense', 2016),
  ('A Moon Shaped Pool', 2016), ('Decks Dark', 2016),
  ('Identikit', 2016), ('Ful Stop', 2016),
  ('Tinker Tailor', 2016), ('Glass Eyes', 2016),
   ('Desert Island Disk', 2016)
) AS v (name, release_year)
ON CONFLICT DO NOTHING;

-- ─── Artist-Song Mappings ──────────────────────────────────────────
-- Maps artists to their songs using the IDs assigned above
INSERT INTO "artist_song" ("song_id", "artist_id")
SELECT s.song_id, a.artist_id
FROM "song" s JOIN "artist" a ON
  (s.name = 'Stairway to Heaven' AND a.name = 'Led Zeppelin') OR
  (s.name = 'Hotel California' AND a.name = 'Eagles') OR
  (s.name = 'Bohemian Rhapsody' AND a.name = 'Queen') OR
  (s.name = 'Comfortably Numb' AND a.name = 'Pink Floyd') OR
  (s.name = 'Smells Like Teen Spirit' AND a.name = 'Nirvana') OR
  (s.name = 'Wonderwall' AND a.name = 'Oasis') OR
  (s.name = 'Nothing Else Matters' AND a.name = 'Metallica') OR
  (s.name = 'Back in Black' AND a.name = 'AC/DC') OR
  (s.name = 'Sweet Child O Mine' AND a.name = 'Guns N'' Roses') OR
  (s.name = 'Losing My Religion' AND a.name = 'R.E.M.') OR
  (s.name = 'Creep' AND a.name = 'Radiohead') OR
  (s.name = 'Paranoid Android' AND a.name = 'Radiohead') OR
  (s.name = 'Karma Police' AND a.name = 'Radiohead') OR
  (s.name = 'Enter Sandman' AND a.name = 'Metallica') OR
  (s.name = 'Master of Puppets' AND a.name = 'Metallica') OR
  (s.name = 'Highway to Hell' AND a.name = 'AC/DC') OR
  (s.name = 'Thunderstruck' AND a.name = 'AC/DC') OR
  (s.name = 'November Rain' AND a.name = 'Guns N'' Roses') OR
  (s.name = 'Don''t Cry' AND a.name = 'Guns N'' Roses') OR
  (s.name = 'Welcome to the Jungle' AND a.name = 'Guns N'' Roses') OR
  (s.name = 'Knockin'' on Heaven''s Door' AND a.name = 'Bob Dylan') OR
  (s.name = 'Layla' AND a.name = 'Eric Clapton') OR
  (s.name = 'Tears in Heaven' AND a.name = 'Eric Clapton') OR
  (s.name = 'Purple Haze' AND a.name = 'Jimi Hendrix') OR
  (s.name = 'All Along the Watchtower' AND a.name = 'Jimi Hendrix') OR
  (s.name = 'The Wind Cries Mary' AND a.name = 'Jimi Hendrix') OR
  (s.name = 'Foxy Lady' AND a.name = 'Jimi Hendrix') OR
  (s.name = 'Black' AND a.name = 'Pearl Jam') OR
  (s.name = 'Jeremy' AND a.name = 'Pearl Jam') OR
  (s.name = 'Alive' AND a.name = 'Pearl Jam') OR
  (s.name = 'Under the Bridge' AND a.name = 'Red Hot Chili Peppers') OR
  (s.name = 'Californication' AND a.name = 'Red Hot Chili Peppers') OR
  (s.name = 'Give It Away' AND a.name = 'Red Hot Chili Peppers') OR
  (s.name = 'Everlong' AND a.name = 'Foo Fighters') OR
  (s.name = 'Learn to Fly' AND a.name = 'Foo Fighters') OR
  (s.name = 'Best of You' AND a.name = 'Foo Fighters') OR
  (s.name = 'Basket Case' AND a.name = 'Green Day') OR
  (s.name = 'American Idiot' AND a.name = 'Green Day') OR
  (s.name = 'Boulevard of Broken Dreams' AND a.name = 'Green Day') OR
  (s.name = 'Good Riddance' AND a.name = 'Green Day') OR
  (s.name = 'Bitter Sweet Symphony' AND a.name = 'The Verve') OR
  (s.name = 'Champagne Supernova' AND a.name = 'Oasis') OR
  (s.name = 'Don''t Look Back in Anger' AND a.name = 'Oasis') OR
  (s.name = 'Song 2' AND a.name = 'Blur') OR
  (s.name = 'Coffee & TV' AND a.name = 'Blur') OR
  (s.name = 'Clint Eastwood' AND a.name = 'Gorillaz') OR
  (s.name = 'Feel Good Inc.' AND a.name = 'Gorillaz') OR
  (s.name = 'Yellow' AND a.name = 'Coldplay') OR
  (s.name = 'Fix You' AND a.name = 'Coldplay') OR
  (s.name = 'Clocks' AND a.name = 'Coldplay') OR
  (s.name = 'The Scientist' AND a.name = 'Coldplay') OR
  (s.name = 'Viva la Vida' AND a.name = 'Coldplay') OR
  (s.name = 'Uprising' AND a.name = 'Muse') OR
  (s.name = 'Supermassive Black Hole' AND a.name = 'Muse') OR
  (s.name = 'Starlight' AND a.name = 'Muse') OR
  (s.name = 'Mr. Brightside' AND a.name = 'The Killers') OR
  (s.name = 'All the Small Things' AND a.name = 'Blink-182') OR
  (s.name = 'Last Resort' AND a.name = 'Papa Roach') OR
  (s.name = 'Dammit' AND a.name = 'Blink-182') OR
  (s.name = 'In the End' AND a.name = 'Linkin Park') OR
  (s.name = 'Numb' AND a.name = 'Linkin Park') OR
  (s.name = 'One Step Closer' AND a.name = 'Linkin Park') OR
  (s.name = 'Chop Suey' AND a.name = 'System of a Down') OR
  (s.name = 'Toxicity' AND a.name = 'System of a Down') OR
  (s.name = 'B.Y.O.B.' AND a.name = 'System of a Down') OR
  (s.name = 'Killing in the Name' AND a.name = 'Rage Against the Machine') OR
  (s.name = 'Bullet with Butterfly Wings' AND a.name = 'Smashing Pumpkins') OR
  (s.name = 'Today' AND a.name = 'Smashing Pumpkins') OR
  (s.name = '1979' AND a.name = 'Smashing Pumpkins') OR
  (s.name = 'Buddy Holly' AND a.name = 'Weezer') OR
  (s.name = 'Island in the Sun' AND a.name = 'Weezer') OR
  (s.name = 'Self Esteem' AND a.name = 'The Offspring') OR
  (s.name = 'Pretty Fly' AND a.name = 'The Offspring') OR
  (s.name = 'Come Out and Play' AND a.name = 'The Offspring') OR
  (s.name = 'Santeria' AND a.name = 'Sublime') OR
  (s.name = 'Date Rape' AND a.name = 'Sublime') OR
  (s.name = 'Just a Girl' AND a.name = 'No Doubt') OR
  (s.name = 'Don''t Speak' AND a.name = 'No Doubt') OR
  (s.name = 'Sex and Candy' AND a.name = 'Marcy Playground') OR
  (s.name = 'Semi-Charmed Life' AND a.name = 'Third Eye Blind') OR
  (s.name = 'Man on the Moon' AND a.name = 'R.E.M.') OR
  (s.name = 'Fake Plastic Trees' AND a.name = 'Radiohead') OR
  (s.name = 'No Surprises' AND a.name = 'Radiohead') OR
  (s.name = 'Lucky' AND a.name = 'Radiohead') OR
  (s.name = 'High and Dry' AND a.name = 'Radiohead') OR
  (s.name = 'Street Spirit' AND a.name = 'Radiohead') OR
  (s.name = 'Let Down' AND a.name = 'Radiohead') OR
  (s.name = 'How to Disappear Completely' AND a.name = 'Radiohead') OR
  (s.name = 'Everything in Its Right Place' AND a.name = 'Radiohead') OR
  (s.name = 'Idioteque' AND a.name = 'Radiohead') OR
  (s.name = 'There There' AND a.name = 'Radiohead') OR
  (s.name = '15 Step' AND a.name = 'Radiohead') OR
  (s.name = 'Nude' AND a.name = 'Radiohead') OR
  (s.name = 'Weird Fishes' AND a.name = 'Radiohead') OR
  (s.name = 'Reckoner' AND a.name = 'Radiohead') OR
  (s.name = 'Daydreaming' AND a.name = 'Radiohead') OR
  (s.name = 'Burn the Witch' AND a.name = 'Radiohead') OR
  (s.name = 'True Love Waits' AND a.name = 'Radiohead') OR
  (s.name = 'Present Tense' AND a.name = 'Radiohead') OR
  (s.name = 'A Moon Shaped Pool' AND a.name = 'Radiohead') OR
  (s.name = 'Decks Dark' AND a.name = 'Radiohead') OR
  (s.name = 'Identikit' AND a.name = 'Radiohead') OR
  (s.name = 'Ful Stop' AND a.name = 'Radiohead') OR
  (s.name = 'Tinker Tailor' AND a.name = 'Radiohead') OR
  (s.name = 'Glass Eyes' AND a.name = 'Radiohead') OR
  (s.name = 'Desert Island Disk' AND a.name = 'Radiohead')
ON CONFLICT DO NOTHING;

-- ─── Chords (basic open chords) ─────────────────────────────────────
-- Uses subqueries to resolve tuning_id and instrument_type_id by name
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'C',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,3,2,0,1,0],"fingers":[null,3,2,null,1,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'D',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,null,0,2,3,2],"fingers":[null,null,null,1,3,2],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'E',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[0,2,2,1,0,0],"fingers":[null,2,3,1,null,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'Em',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[0,2,2,0,0,0],"fingers":[null,2,3,null,null,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'F',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[1,1,2,3,1,1],"fingers":[1,1,2,3,1,1],"barre":1}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'G',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[3,2,0,0,0,3],"fingers":[2,1,null,null,null,3],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'A',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,0,2,2,2,0],"fingers":[null,null,1,2,3,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'Am',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,0,2,2,1,0],"fingers":[null,null,2,3,1,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'Bm',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,2,4,4,3,2],"fingers":[null,1,3,4,2,1],"barre":2}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'Dm',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,null,0,2,3,1],"fingers":[null,null,null,2,3,1],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'A7',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,0,2,0,2,0],"fingers":[null,null,1,null,2,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'D7',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,null,0,2,1,2],"fingers":[null,null,null,2,1,3],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'E7',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[0,2,0,1,0,0],"fingers":[null,2,null,1,null,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'Fmaj7',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,null,3,2,1,0],"fingers":[null,null,3,2,1,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;
INSERT INTO "chord" ("name", "instrument_type_id", "tuning_id", "chord_fingering", "created_by")
SELECT 'Cadd9',
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id = (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  '{"frets":[null,3,2,0,3,0],"fingers":[null,2,1,null,3,null],"barre":null}',
  (SELECT account_id FROM "account" WHERE email = 'admin@test.pl' LIMIT 1)
ON CONFLICT DO NOTHING;

-- ─── Song Chords (sample tab entries) ───────────────────────────────
-- These reference real songs, keys, tunings, instruments, and accounts
INSERT INTO "song_chords" ("song_id", "author_id", "status", "notation_type", "key_id", "tuning_id", "instrument_type_id", "strumming_pattern", "time_signature", "tempo", "capo_fret", "song_body", "created_by")
SELECT
  s.song_id,
  (SELECT account_id FROM "account" WHERE email = 'jan@test.pl' LIMIT 1),
  'PUBLIC',
  'CHORDS',
  k.key_id,
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id =
    (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  'D DU UDU',
  '4/4',
  NULL,
  NULL,
  NULL,
  (SELECT account_id FROM "account" WHERE email = 'jan@test.pl' LIMIT 1)
FROM "song" s
JOIN "key" k ON k.name = 'G' AND k.mode = 'MAJOR'
WHERE s.name IN (
  'Wonderwall', 'Knockin'' on Heaven''s Door', 'Good Riddance',
  'Basket Case', 'Yellow', 'Buddy Holly', 'Santeria',
  'Iris', 'Sex and Candy', 'Don''t Look Back in Anger'
)
ON CONFLICT DO NOTHING;

-- Some minor key songs
INSERT INTO "song_chords" ("song_id", "author_id", "status", "notation_type", "key_id", "tuning_id", "instrument_type_id", "strumming_pattern", "time_signature", "tempo", "capo_fret", "song_body", "created_by")
SELECT
  s.song_id,
  (SELECT account_id FROM "account" WHERE email = 'anna@test.pl' LIMIT 1),
  'PUBLIC',
  'CHORDS',
  k.key_id,
  (SELECT tuning_id FROM "tuning" WHERE tuning = 'EADGBE' AND instrument_type_id =
    (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1) LIMIT 1),
  (SELECT instrument_type_id FROM "instrument_type" WHERE name = 'Gitara klasyczna' LIMIT 1),
  'D D U U D U',
  '4/4',
  NULL,
  NULL,
  NULL,
  (SELECT account_id FROM "account" WHERE email = 'anna@test.pl' LIMIT 1)
FROM "song" s
JOIN "key" k ON k.name = 'E' AND k.mode = 'MINOR'
WHERE s.name IN ('Smells Like Teen Spirit', 'Creep', 'Nothing Else Matters')
ON CONFLICT DO NOTHING;

-- ─── Song-Chords ↔ Chord Mappings ──────────────────────────────────
-- Links each song_chords entry to the chords used in that song
-- Chords available: C, D, E, Em, F, G, A, Am, Bm, Dm, A7, D7, E7, Fmaj7, Cadd9
-- Wonderwall: Em, G, D, A7, Cadd9
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('Em', 'G', 'D', 'A7', 'Cadd9')
WHERE s.name = 'Wonderwall'
ON CONFLICT DO NOTHING;
-- Knockin' on Heaven's Door: G, D, Am, C
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('G', 'D', 'Am', 'C')
WHERE s.name = 'Knockin'' on Heaven''s Door'
ON CONFLICT DO NOTHING;
-- Good Riddance: G, C, D, Em
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('G', 'C', 'D', 'Em')
WHERE s.name = 'Good Riddance'
ON CONFLICT DO NOTHING;
-- Basket Case: E, A, Bm, G
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('E', 'A', 'Bm', 'G')
WHERE s.name = 'Basket Case'
ON CONFLICT DO NOTHING;
-- Yellow: G, D, Em, C
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('G', 'D', 'Em', 'C')
WHERE s.name = 'Yellow'
ON CONFLICT DO NOTHING;
-- Buddy Holly: A, D, E, G
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('A', 'D', 'E', 'G')
WHERE s.name = 'Buddy Holly'
ON CONFLICT DO NOTHING;
-- Santeria: D, G, Em, A
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('D', 'G', 'Em', 'A')
WHERE s.name = 'Santeria'
ON CONFLICT DO NOTHING;
-- Iris: D, Em, G, A
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('D', 'Em', 'G', 'A')
WHERE s.name = 'Iris'
ON CONFLICT DO NOTHING;
-- Sex and Candy: D, A, Em, G
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('D', 'A', 'Em', 'G')
WHERE s.name = 'Sex and Candy'
ON CONFLICT DO NOTHING;
-- Don't Look Back in Anger: C, G, Am, Em, D
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('C', 'G', 'Am', 'Em', 'D')
WHERE s.name = 'Don''t Look Back in Anger'
ON CONFLICT DO NOTHING;
-- Smells Like Teen Spirit: Em, G, D, A (power chord style)
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('Em', 'G', 'D', 'A')
WHERE s.name = 'Smells Like Teen Spirit'
ON CONFLICT DO NOTHING;
-- Creep: G, C, Em, D
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('G', 'C', 'Em', 'D')
WHERE s.name = 'Creep'
ON CONFLICT DO NOTHING;
-- Nothing Else Matters: Em, C, D, G
INSERT INTO "song_chords_chord" ("song_chords_id", "chord_id")
SELECT sc.song_chords_id, c.chord_id
FROM "song_chords" sc
JOIN "song" s ON s.song_id = sc.song_id
JOIN "chord" c ON c.name IN ('Em', 'C', 'D', 'G')
WHERE s.name = 'Nothing Else Matters'
ON CONFLICT DO NOTHING;
