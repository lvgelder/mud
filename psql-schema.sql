CREATE TABLE room (
  id INTEGER PRIMARY KEY,
  description TEXT NOT NULL
);

CREATE TABLE exit (
  id INTEGER PRIMARY KEY,
  from_room INTEGER REFERENCES room(id),
  to_room INTEGER REFERENCES room(id),
  keywords TEXT,
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
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  hit_points INTEGER NOT NULL
);

CREATE TABLE treasure (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  action_description TEXT,
  hit_points INTEGER DEFAULT 0,
  type TEXT);

CREATE TABLE room_monster (
  room_id INTEGER REFERENCES room(id),
  monster_id INTEGER REFERENCES monster(id)
);

CREATE TABLE monster_weapon (
 monster_id INTEGER REFERENCES monster(id),
 weapon_id INTEGER REFERENCES weapon(id)
);

CREATE TABLE monster_treasure (
  monster_id INTEGER REFERENCES monster(id),
  treasure_id INTEGER REFERENCES treasure(id)
);

CREATE TABLE room_treasure (
  room_id INTEGER REFERENCES room(id),
  treasure_id INTEGER REFERENCES treasure(id)
);

INSERT INTO weapon (id, name, damage) VALUES (1, 'fists', 3);
INSERT INTO weapon (id, name, damage) VALUES (2, 'teeth', 3);
INSERT INTO room (id, description) VALUES (1, 'An empty room. Quite boring.');
INSERT INTO room (id, description) VALUES (2, 'Lovely and ornate, with carvings everywhere. But you dont notice that because as soon as you walk in a vampire tries to eat you...');
INSERT INTO room (id, description) VALUES(3, 'A library with books all up to the ceilings and comfy sofas.');
INSERT INTO exit(id, from_room, to_room, description, keywords) VALUES (1, 1, 2, 'A very ordinary door. With a doorknob', 'door');
INSERT INTO exit(id, from_room, to_room, description, locked, keywords) VALUES (2, 2, 3, 'Wooden door with a keyhole.', 1, 'door');
INSERT INTO monster(id, name, description, hit_points) VALUES (1, 'vampire', 'pointy teeth', 2);
INSERT INTO monster_weapon (monster_id, weapon_id) VALUES (1, 2);
INSERT INTO room_monster(room_id, monster_id) VALUES (2, 1);
INSERT INTO treasure (id, name, description, type) VALUES (1, 'key', 'A key that looks like it might fit the lock...', 'key');
INSERT INTO treasure (id, name, description) VALUES (2, 'book', 'An illustrated book of traffic lights around the world.');
INSERT INTO treasure (id, name, description, type) VALUES (3, 'newspaper', 'Todays edition of your favourite newspaper', 'combinable');
INSERT INTO treasure (id, name, description, type, action_description, hit_points) VALUES (4, 'coffee', 'A cup of hot coffee. Mmm that smells good.', 'drinkable', 'Everything is always better after a cup of coffee.', 10);
INSERT INTO treasure (id, name, description, type, action_description, hit_points) VALUES (5, 'scone', 'A scone with clotted cream and jam.', 'edible', 'It tastes even better than you had hoped.', 50);
INSERT INTO treasure (id, name, description, type, action_description) VALUES (6, 'hat', 'A furry hat with ears. Looks warm.', 'wearable', 'Your ears are now warm.');
INSERT INTO treasure (id, name, description, type) VALUES (7, 'pen', 'A fountain pen.', 'combinable');
INSERT INTO treasure (id, name, description, type) VALUES (8, 'ink', 'A jar of ink.', 'combinable');
INSERT INTO treasure (id, name, description, type, action_description, hit_points) VALUES (9, 'yogurt', 'A tub of yogurt. Strawberry flavour.', 'edible', 'Tasty, although you suspect the strawberry is artificial', 5);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (2, 1);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 2);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 3);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 4);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 5);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 6);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 7);
INSERT INTO room_treasure(room_id, treasure_id) VALUES (3, 8);
INSERT INTO monster_treasure(monster_id, treasure_id) VALUES (1, 9);
INSERT INTO room (id, description) VALUES(4, 'A corridor');
INSERT INTO room (id, description) VALUES(5, 'A rickety staircase going down into the gloom. There is no handrail.');
INSERT INTO exit(id, from_room, to_room, description, keywords) VALUES (3, 3, 4, 'A door on the east wall.', 'east');
INSERT INTO exit(id, from_room, to_room, description, keywords) VALUES (4, 3, 5, 'A door on the west wall.', 'west');




CREATE TABLE player (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  hit_points INTEGER NOT NULL default 5,
  max_hit_points INTEGER NOT NULL default 5,
  items INTEGER NOT NULL default 0,
  level INTEGER default 1
);

CREATE TABLE player_weapon(
  player_id INTEGER REFERENCES player(id),
  weapon_id INTEGER REFERENCES weapon(id)
);

CREATE TABLE room_player (
  room_id INTEGER REFERENCES room(id),
  player_id INTEGER REFERENCES player(id)
);

CREATE TABLE player_treasure (
  treasure_id INTEGER REFERENCES treasure(id),
  player_id INTEGER REFERENCES player(id));

CREATE TABLE player_monster (
  monster_id INTEGER REFERENCES monster(id),
  player_id INTEGER REFERENCES player(id));

 CREATE TABLE fight_in_progress(
   monster_id INTEGER REFERENCES monster(id),
   player_id INTEGER REFERENCES player(id),
   monster_hit_points INTEGER
 );

CREATE TABLE eaten_treasure (
  treasure_id INTEGER references treasure(id),
  player_id INTEGER references player(id)
);

CREATE TABLE worn_treasure (
  treasure_id INTEGER references treasure(id),
  player_id INTEGER references player(id)
);

CREATE TABLE mud_user (
  id SERIAL PRIMARY KEY,
  username TEXT,
  password TEXT
);

CREATE TABLE user_player (
  mud_user_id INTEGER references mud_user(id),
  player_id INTEGER references player(id)
);

CREATE TABLE mud_role (
  id SERIAL PRIMARY KEY,
  name TEXT
);

CREATE TABLE user_role (
  mud_user_id INTEGER references mud_user(id),
  mud_role_id INTEGER references mud_role(id)
);

insert into mud_role(name) values ('::user');
insert into mud_role(name) values ('::admin');


CREATE TABLE combinable_treasure (
combined_treasure_id INTEGER references treasure(id),
treasure_id INTEGER references treasure(id)
);

INSERT INTO treasure (id, name, description, action_description, type) VALUES (10, 'pen', 'A pen with ink', 'You fill the pen with ink', 'combinable');
INSERT INTO treasure (id, name, description, action_description, type) VALUES (11, 'newspaper', 'Newspaper with doodles', 'You draw fangs on the faces in the newspaper.', 'combined');

insert into combinable_treasure (combined_treasure_id, treasure_id) VALUES (10, 7);
insert into combinable_treasure (combined_treasure_id, treasure_id) VALUES (10, 8);
insert into combinable_treasure (combined_treasure_id, treasure_id) VALUES (11, 10);
insert into combinable_treasure (combined_treasure_id, treasure_id) VALUES (11, 3);


CREATE TABLE friend_group (
  id SERIAL PRIMARY KEY,
  name TEXT
);

CREATE TABLE player_friend_group (
 friend_group_id INTEGER references friend_group(id),
 player_id INTEGER references player(id)
 );

 CREATE TABLE friend_group_treasure (
   treasure_id INTEGER REFERENCES treasure(id),
   friend_group_id INTEGER REFERENCES friend_group(id));






