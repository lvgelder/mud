# mud

Currently a sud rather than a mud :)

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

You also need postgres

## Setting up postgres

```
brew install postgres
```

To have launchd start postgresql at login:

```
ln -sfv /usr/local/opt/postgresql/*.plist ~/Library/LaunchAgents
```

Then to load postgresql now:

```
launchctl load ~/Library/LaunchAgents/homebrew.mxcl.postgresql.plist
```

Create your database and user:

```
psql postgres
```

```
create user YOURUSERNAMEGOESHERE;
```

```
create database mud;
```

```
grant all privileges on database mud to YOURUSENAMEGOESHERE;
```


Run this command to create tables and import starting data into your new db:

```
psql -U YOURUSERNAMEGOESHERE -d mud -a -f psql-schema.sql

```

psql mud YOURUSERNAMEHERE to see whats in your local db

## Configure profiles.clj with your database information

Create a file called profiles.clj

Add to it:

```
{:dev  {:env {:database-name "mud"
              :database-username "YOURUSERNAMEGOESHERE"
              :database-password ""
              :database-host "localhost"
              :database-port "5432"
              :websocket-url "ws://localhost:8080/messages"
              :cross-domain-url "http://localhost:8080"}}}
```


## Running

First, create the db and import initial values, as above

To start a web server for the application, run:

    lein run

## Tests

To run all tests:

```
lein test
```

Or to run only one test file:

```
lein test mud.treasure-test
```



## License

This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
