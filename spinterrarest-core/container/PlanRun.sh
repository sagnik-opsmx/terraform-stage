#!/bin/bash

echo "started terraspin service..."

nohup java -jar /home/terraspin/artifact/TerraSpin.jar > /home/terraspin/artifact/terraspin.log 2>&1 &

tail -f /home/terraspin/artifact/terraspin.log &

while :; do
  sleep 100
  # For Debugging, Docker should alive!
done
