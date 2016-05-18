#!/bin/bash

mvn clean -DskipTests=true  package

gnome-terminal -e "java -cp cli/target/cli-1.0-jar-with-dependencies.jar com.dido.pad.cli.MainClient -ip 127.0.0.254 -id client 127.0.0.1:node1"
echo "started Client 127.0.0.254"

gnome-terminal -e "java -cp core/target/core-1.0-jar-with-dependencies.jar com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1 127.0.0.1:node1"
echo "started node 127.0.0.1"
gnome-terminal -e   "java -cp core/target/core-1.0-jar-with-dependencies.jar com.dido.pad.PadFsNode -ip 127.0.0.2 -id node2 127.0.0.1:node1"
echo "started node 127.0.0.2"
gnome-terminal -e  "java -cp core/target/core-1.0-jar-with-dependencies.jar com.dido.pad.PadFsNode -ip 127.0.0.3 -id node3 127.0.0.1:node1"
echo "started node 127.0.0.3"
gnome-terminal -e  "java -cp core/target/core-1.0-jar-with-dependencies.jar com.dido.pad.PadFsNode -ip 127.0.0.4 -id node4 127.0.0.1:node1"
echo "started node 127.0.0.4"

