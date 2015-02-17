CREATE TABLE room (
  id INTEGER PRIMARY KEY,
  description TEXT NOT NULL
);

CREATE TABLE exit (
  id INTEGER PRIMARY KEY,
  from_room INTEGER REFERENCES room(id),
  to_room INTEGER REFERENCES room(id),
  description TEXT NOT NULL,
  locked INTEGER default 0
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
  description TEXT NOT NULL);

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
INSERT INTO weapon (id, name, damage) VALUES (2, 'teeth', 1);
INSERT INTO room (id, description) VALUES (1, "An empty room. Quite boring.");
INSERT INTO room (id, description) VALUES (2, "Lovely and ornate, with carvings everywhere. But you don't notice that because as soon as you walk in a vampire tries to eat you...");
INSERT INTO room (id, description) VALUES(3, "A library with books all up to the ceilings and comfy sofas.");
INSERT INTO exit(id, from_room, to_room, description) VALUES (1, 1, 2, "A very ordinary door. With a doorknob");
INSERT INTO exit(id, from_room, to_room, description, locked) VALUES (2, 2, 3, "Wooden door with a keyhole.", 1);
INSERT INTO monster(id, weapon_id, name, description, hit_points) VALUES (1, 2, "vampire", "pointy teeth", 1);
INSERT INTO room_monster(room_id, monster_id) VALUES (2, 1);
INSERT INTO treasure (id, name, description) VALUES (1, 'key', 'A key that looks like it might fit the lock...');
INSERT INTO treasure (id, name, description) VALUES (2, 'book', 'An illustrated book of traffic lights around the world.');
INSERT INTO treasure (id, name, description) VALUES (3, 'newspaper', "Today's edition of your favourite newspaper");
INSERT INTO treasure (id, name, description) VALUES (4, 'coffee', 'A cup of hot coffee. Mmm that smells good.');
INSERT INTO treasure (id, name, description) VALUES (5, 'scone', 'A scone with clotted cream and jam.');
INSERT INTO treasure (id, name, description) VALUES (6, 'hat', 'A furry hat with ears. Looks warm.');
INSERT INTO treasure (id, name, description) VALUES (7, 'pen', 'A fountain pen.');
INSERT INTO treasure (id, name, description) VALUES (8, 'ink', 'A jar of ink.');
INSERT INTO room_treasure(room_id, treasure_id) VALUES (2, 1);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 2);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 3);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 4);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 5);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 6);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 7);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 8);

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