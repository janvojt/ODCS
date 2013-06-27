#!/bin/bash

script=`readlink -f $0`
basedir=`dirname $script`
dumpdir="${basedir}/../virtuoso/rdbms"

# TODO parse out login info from configuration
dbuser="dba"
dbpass="dba"
dbhost="localhost"
dbport="1111"

# recreate schema and import data
cat "${dumpdir}/schema.sql" "${dumpdir}/data.sql" | \
	isql-v "${dbhost}:${dbport}" "$dbuser" "$dbpass"
