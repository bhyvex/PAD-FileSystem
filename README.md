#Pad-fs a distributed  persistent data store 
Pad-fs is a distribute persistent data store written in `java`. 

Pad-fs exposes four main API:
- `put(k,v)` : insert of a key k and value v into the system.
- `get(k)` : retrieve the last update version of the data associated with the key k.
- `list(ip)` : lists all the key values stored into the node with ip address.
- `rm(k)` : removes the value associated with the key k.

### Structure of the project
The pad-fs project is divided in the sub projects:
- `core` contains the code of a single storage node.
- `cli` contains the node client code. The client is an external node that is used to performs the operations (put, get, list) into the distributed system.
- - `app` is a pre-configured Pad-fs storage system composed by 4 storage nodes. It is useful to run a simulation into a single machine.

## How to run the projects

### Core
The main class of the `core` project is the `PadFsNode.java` class that run a single storage node.
The parameters are:

`./PadFsNode -ip <ipAddress> -id <String> -gp <int> -sp <int>  [<seedIP>:<id>:<gp>]`

where
- `-ip` is the ip address (x.y.z.w) of the node.
- `-id <String>` is a string representing the id of the node.
- `-gp <int>` setups the port for the gossip protocol (default port is defined in `Helper.GOSSIP_PORT``).
- `-sp <int>` setups the port for the storage service (default port is defined in `Helper.STORAGE_PORT`).
- `[<seedIP>:<String>[:<gp>]]` is a list of SEED node concatted by the gossip protocol. Each entry has: `ip` of the node, `id` of the node (id must be equal to remote node id),  `gp = gossip port` is the the gossip port, by default is set to `Helper.GOSSIP_PORT``.

#### How to run a storage Node
The command below run a storage node wwith ip `127.0.0.1` and node ID `node1` and set the seed node to `127.0.0.2:node2`

`java -cp target/core-1.0-SNAPSHOT-jar-with-dependencies.jar  com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1 127.0.0.2:node2`

## Client 

The client is the external node that expose a `cli` (command line interface) to the user.

`Usage: Client [options] ipSeed:id[:gp] [ipSeed:id[:gp]`

Where `Options`:
  -  `--help`: show the help message
  -  `-id`: (String) Id of the client (Default  `client`)
  - `-ip`: Ip address of the client (Default: `127.0.0.254`)
The seed nodes contacted initially are:
  - `ipSeed:id[:gp]`: ipseed is the ip, id is the string of the node (equal to the remote), `gp` is the gossip port (default `Helper.GOSSIP_PORT`).
  
#### How to run the Client node
Run a client with `ip=127.0.0.254` (default) and `id=client` (default) and set one seed node `127.0.0.1:node1`.
` java -cp target/cli-1.0-SNAPSHOT-jar-with-dependencies.jar com.dido.pad.cli.MainClient 127.0.0.1:node1`


### App
Is the easy way to run a set of nodes into a single machine.


Go inside *app* folder and type:

`java -cp target/app-1.0-SNAPSHOT-jar-with-dependencies.jar com.dido.pad.app.AppRunner`

It runs four nodes: `127.0.0.1, 127.0.0.2 , 127.0.0.3 , 127.0.0.4` on local machine.


