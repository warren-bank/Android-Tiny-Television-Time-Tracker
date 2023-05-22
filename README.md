### [DroidShowsDatabaseMigrationTool](https://github.com/warren-bank/Android-Tiny-Television-Time-Tracker/tree/jre/db-migration-tool)

#### Background:

* the [TV-Tracker](https://github.com/warren-bank/Android-Tiny-Television-Time-Tracker/tree/fork/tmdb) Android application supports the direct import of a database file that has been exported from any version of [DroidShows](https://github.com/ltguillaume/droidshows) up to and including: [`7.11.2`](https://f-droid.org/repo/nl.asymmetrics.droidshows_71102.apk)
  - when this occurs, migration of the database proceeds
  - depending upon the number of series and episodes in the database, this process can take a while

#### Goal:

* the purpose for this branch is to:
  - use the code in tag [`tmdb/008.00.22-09API`](https://github.com/warren-bank/Android-Tiny-Television-Time-Tracker/releases/tag/tmdb%2F008.00.22-09API) as a starting point
  - remove everything that isn't needed for database migration
  - polyfill the native Android SQLite wrapper
  - provide a command-line utility that only requires Java JRE to run a database migration from _DroidShows_ to _TV-Tracker_

#### Usage (generic, bash, posix):

```bash
  DIR=$(realpath '.')
  CLASSPATH="${DIR}/libs/classes/commons-lang3-3.9.jar:${DIR}/libs/classes/jackson-annotations-2.13.3.jar:${DIR}/libs/classes/jackson-core-2.13.3.jar:${DIR}/libs/classes/jackson-databind-2.13.3.jar:${DIR}/libs/classes/jna-5.13.0.jar"
  CLASSPATH="${CLASSPATH}:${DIR}/dist/DroidShowsDatabaseMigrationTool.jar"
  MAINCLASS='com.github.warren_bank.tiny_television_time_tracker.DroidShowsDatabaseMigrationTool'
  java '-Djna.library.path=/path/to/native/sqlite3/library' -cp "$CLASSPATH" "$MAINCLASS" '/path/to/input/DroidShows.db' '/path/to/output/TV-Tracker.db'
```

#### Usage (generic, bash, win64):

```bash
  DIR=$(cygpath -a '.')
  CLASSPATH="${DIR}/libs/classes/commons-lang3-3.9.jar:${DIR}/libs/classes/jackson-annotations-2.13.3.jar:${DIR}/libs/classes/jackson-core-2.13.3.jar:${DIR}/libs/classes/jackson-databind-2.13.3.jar:${DIR}/libs/classes/jna-5.13.0.jar"
  CLASSPATH="${CLASSPATH}:${DIR}/dist/DroidShowsDatabaseMigrationTool.jar"
  MAINCLASS='com.github.warren_bank.tiny_television_time_tracker.DroidShowsDatabaseMigrationTool'
  java "-Djna.library.path=${DIR}/dist/libs/win64" -cp "$CLASSPATH" "$MAINCLASS" "${DIR}/tests/01/data/DroidShows.db" "${DIR}/tests/01/data/TV-Tracker.db"
```

#### Usage (jar, bash, posix):

```bash
  # pre-condition: libs/classes/*.jar
  java '-Djna.library.path=/path/to/native/sqlite3/library' -jar 'dist/DroidShowsDatabaseMigrationTool.jar' '/path/to/input/DroidShows.db' '/path/to/output/TV-Tracker.db'
```

#### Usage (jar, bash, win64):

```bash
  # pre-condition: libs/classes/*.jar
  DIR=$(cygpath -a '.')
  java "-Djna.library.path=${DIR}/dist/libs/win64" -jar 'dist/DroidShowsDatabaseMigrationTool.jar' "${DIR}/tests/01/data/DroidShows.db" "${DIR}/tests/01/data/TV-Tracker.db"
```

#### Usage (jar, cmd, win64):

```bash
  rem pre-condition: libs/classes/*.jar
  call bin/windows-bat/run.bat "%cd%/tests/01/data/DroidShows.db" "%cd%/tests/01/data/TV-Tracker.db"
```

#### Usage (jar, cmd, win64, w/ provided test script):

* download the release [zip bundle](https://github.com/warren-bank/Android-Tiny-Television-Time-Tracker/releases/download/db-migration-tool%2F001.00.01-11JRE/001.00.01-11JRE.zip)
* unzip
* copy `DroidShows.db` from phone to the directory: `tests/01/data`
* run: `tests/01/run-test-migration.bat`
* wait for the terminal to close when complete
  - can monitor progress by reading the output that is piped to: `tests/01/run-test-migration.log`
* copy `TV-Tracker.db` to phone from the directory: `tests/01/data`
* import `TV-Tracker.db` to `TV-Tracker` app
* wait for thumbnails to download and resize

#### Legal:

* copyright: [Warren Bank](https://github.com/warren-bank)
* license: [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.txt)
