#!/bin/sh

set -e

host="$1"
shift
cmd="$@"

until [ "$(curl --silent --fail http://$host/v1/health/service/postgres | jq -r '.[] | .Checks[] | .Status' | grep -v 'passing' | wc -l)" -eq "0" ]; do
  echo "Postgres is unavailable - sleeping"
  sleep 1
done

>&2 echo "Postgres is up - executing command"
exec $cmd
