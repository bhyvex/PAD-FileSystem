#Pad-fs a distributed  persistent data store 
Pad-fs is a distribute file system written in `java` that supports three main operation:
- `put(k,v)` : insert of a key value into the distributed file system.
- `get(k)` : retrieve the last update version of the data associated with the key.
- `list(ip)` : lists all the key values stored into the node with ip address.

### Structure of the project
The pad-fs project is divided in the sub projects:
- `core` provides the core classes and define a single storage node.
- `app` is a pre-configured Pad-fs storage system composed by 4 storage nodes. It is useful to run a simulation into a single machine.
- `cli` defines the node client, is used to performs the operations (put,get,list) into the distributed system.

## How to run the projects

### Core
The main class of the `core` project is the `PadFsNode.java` class. 

The parameters for the main are 

`./PadFsNode -ip <ipAddress> -id <String> -gp <int> -sp <int>  [<seedIP>:<id>:<gp>]`

where
- `-ip` is the ip address (x.y.z.w) of the node.
- `-id <String>` is a string representing the id of the node.
- `-gp <int>` setups the port for the gossip protocol (default port is defined in `Helper.GOSSIP_PORT``).
- `-sp <int>` setups the port for the storage service (default port is defined in `Helper.STORAGE_PORT`).
- `[<seedIP>:<String>:<gp>]` is a list of SEED node to be concacted initially by the gossip protocol. Each entry has: seed ip of the node, id of the node (must be equal of running remote node), listening gossip port of the seed node. If the gossip port is not defined by default is set to `Helper.GOSSIP_PORT``

In order to run a single node run the command
`mvn exec:java -pl core -Dexec.mainClass="com.dido.pad.PadFsNode" -Dexec.args="-ip 127.0.0.1 127.0.0.2:3000:4000" `


### App

On the root of the project.
`mvn package`

Into the folder /target is ceated a jar with dependencies

Go inside *app* folder
`java -cp target/app-1.0-SNAPSHOT-jar-with-dependencies.jar com.dido.pad.app.AppRunner`


## CLient command line

In order to run the Client command line.

`java -cp target/cli-1.0-SNAPSHOT-jar-with-dependencies.jar  com.dido.pad.cli.Cli `
