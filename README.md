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
- `[<seedIP>:<String>[:<gp>]]` is a list of SEED node concatted by the gossip protocol. Each entry has: `ip` of the node, `id` of the node (id must be equal to remote node id),  `gp = gossip port` is the the gossip port, by default is set to `Helper.GOSSIP_PORT``.

#### example 
The command below run a storage node wwith ip `127.0.0.1` and node ID `node1` and set the seed node to `127.0.0.2:node2`

`java -cp target/core-1.0-SNAPSHOT-jar-with-dependencies.jar  com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1 127.0.0.2:node2`

## Client 

The client is the external node the expose a `cli` (command line interface) to the user.

The parameters are:

`Usage: Client [options] ipSeed:id[:gp] [ipSeed:id[:gp]`
Where `Options`:
  -  `--help`: show the help message
  -  `-id`
       Id name of the client
       Default: client
   - `-ip`
       Ip address of the client
       Default: 127.0.0.254


`java -cp target/cli-1.0-SNAPSHOT-jar-with-dependencies.jar  com.dido.pad.cli.Cli `
### App

On the root of the project.
`mvn package`

Into the folder /target is ceated a jar with dependencies

Go inside *app* folder
`java -cp target/app-1.0-SNAPSHOT-jar-with-dependencies.jar com.dido.pad.app.AppRunner`



