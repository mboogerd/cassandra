#!/usr/bin/env bash

docker volume create --name cassandra-data

docker-compose build --no-cache
docker-compose up -d --force-recreate
docker-compose logs -f