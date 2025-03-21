#!/bin/sh

echo " Waiting for Elasticsearch at http://elasticsearch:9200"

until curl -s http://elasticsearch:9200 >/dev/null; do
  echo " Elasticsearch not ready yet. Retrying in 5 seconds"
  sleep 5
done

echo " Elasticsearch is up! Starting the Product Service"

exec java $JAVA_OPTS -jar product-service.jar
