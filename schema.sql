CREATE TABLE room (
  id INTEGER PRIMARY KEY,
  description TEXT NOT NULL
);

CREATE TABLE exit (
  id INTEGER PRIMARY KEY,
  from_room INTEGER REFERENCES room(id),
  to_room INTEGER REFERENCES room(id),
  description TEXT NOT NULL
);

CREATE TABLE weapon (
   id INTEGER PRIMARY KEY,
   name TEXT NOT NULL,
   damage INTEGER
);

CREATE TABLE monster (
  id INTEGER PRIMARY KEY,
  weapon_id INTEGER REFERENCES weapon(id) NOT NULL,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  hit_points INTEGER NOT NULL
);

CREATE TABLE treasure (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  worth INTEGER NOT NULL
);

CREATE TABLE room_monster (
  room_id INTEGER REFERENCES room(id),
  monster_id INTEGER REFERENCES monster(id)
);

CREATE TABLE monster_treasure (
  monster_id INTEGER REFERENCES monster(id),
  treasure_id INTEGER REFERENCES treasure(id)
);

CREATE TABLE room_treasure (
  room_id INTEGER REFERENCES room(id),
  treasure_id INTEGER REFERENCES treasure(id)
);

INSERT INTO weapon (id, name, damage) VALUES (1, 'fists', 1);
INSERT INTO room (id, description) VALUES (1, "An empty room. Quite boring.");
INSERT INTO room (id, description) VALUES (2, "Lovely and ornate, with carvings everywhere. But you don't notice that because as soon as you walk in a vampire tries to eat you...");
INSERT INTO exit(id, from_room, to_room, description) VALUES (1, 1, 2, "A very ordinary door. With a doorknob");

CREATE TABLE player (
  id INTEGER PRIMARY KEY,
  weapon_id INTEGER REFERENCES weapon(id) NOT NULL default 1,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  hit_points INTEGER NOT NULL default 5,
  items INTEGER NOT NULL default 0,
  level INTEGER default 1
);

CREATE TABLE room_player (
  room_id INTEGER REFERENCES room(id),
  player_id INTEGER REFERENCES player(id)
);

CREATE TABLE player_treasure (
  treasure_id INTEGER REFERENCES treasure(id),
  player_id INTEGER REFERENCES player(id)
);

CREATE TABLE player_monster (
  monster_id INTEGER REFERENCES monster(id),
  player_id INTEGER REFERENCES player(id)
);