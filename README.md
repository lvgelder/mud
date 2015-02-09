# mud

Currently a sud rather than a mud :)

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

You also need sqlite3

## Running

First, create the db and import initial values:

    sqlite3 -init schema.sql mud.db .quit

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2015 FIXME
