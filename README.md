#Pad-fs a distributed  persistent data store 
Pad-fs is a distribute persistent data store written in `java`. 

Pad-fs exposes four main API:
- `put(k,v)` : insert of a key k and value v into the system.
- `get(k)` : retrieve the last update version of the data associated with the key k.
- `list(ip)` : lists all the key values stored into the node with ip address.
- `rm(k)` : removes the value associated with the key k.

### Structure of the project
The pad-fs project is divided in three sub projects:
- `core` contains the code of a single storage node.
- `cli` contains the node client code. The client is an external node that is used to performs the operations (put, get, list) into the distributed system.
-  `app` is a pre-configured  storage system composed by 4 storage nodes. It is useful to run a simulation into a single machine.


## How to run the projects
THe system is composed by a fixed number of nodes that must be present:
A clinet that interact with the storage system.

There are three ways to run the nodes.

- Run the nodes dowloading the  latest `core` [release](https://github.com/dido18/PAD-FileSystem/releases). 
- Run the simulation in a single machine (multithreaded)
- Create a Docker image for each node and run it.


#### Running the release

### Core project
The command below run a storage node wwith ip `127.0.0.1` and node ID `node1` and set the seed node to `127.0.0.2:node2`

`java -cp core-<version>.jar  com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1 127.0.0.2:node2`

The main class of the `core` project is the `PadFsNode.java` class that run a single storage node.
The parameters are:

`./PadFsNode -ip <ipAddress> -id <String> -gp <int> -sp <int>  [<seedIP>:<id>:<gp>]`

where
- `-ip` is the ip address (x.y.z.w) of the node.
- `-id <String>` is a string representing the id of the node.
- `-gp <int>` setups the port for the gossip protocol (default port is defined in `Helper.GOSSIP_PORT=2000``).
- `-sp <int>` setups the port for the storage service (default port is defined in `Helper.STORAGE_PORT=3000`).
- `[<seedIP>:<String>[:<gp>]]` is a list of SEED node concatted by the gossip protocol. Each entry has: `ip` of the node, `id` of the node (id must be equal to remote node id),  `gp = gossip port` is the the gossip port, by default is set to `Helper.GOSSIP_PORT``.

#### Docker core image

Compile and package the `.jar` into the `/target` folder and into the `src/main/docker` folder.

`mvn clean install  -DskipTests=true  -pl core -am`

Go inside the `core` folder:

` cd core`

Build the image `padfs/core:<version>` starting from the DockerFile and adding the jar inside the image.

` mvn docker:build`

Run the container:

`docker run padfs/core:<version> com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1 127.0.0.2:node2`

## Cli project
Donload the latest versio of `cli-<version>.jar`.

Run a client with `-ip 127.0.0.254` (default) and `- id client` (default) and set one seed node `127.0.0.1:node1`.

` java -cp cli-<version>.jar com.dido.pad.cli.MainClient -ip 127.0.0.254 -id client 127.0.0.1:node1`

`Usage: Client [options] ipSeed:id[:gp] [ipSeed:id[:gp]`

Where `Options`:
  -  `--help`: show the help message
  -  `-id`: (String) Id of the client (Default  `-id client`)
  - `-ip`: Ip address of the client (Default: `-ip 127.0.0.254`)
The seed nodes contacted initially are:
  - `ipSeed:id[:gp]`: ipseed is the ip, id is the string of the node (equal to the remote), `gp` is the gossip port (default `Helper.GOSSIP_PORT`).
  
#### Docker client image

Compile and package the `.jar` into the `/target` folder and into the `src/main/docker` folder.

`mvn clean install -DskipTests=true   -pl cli -am`

Go inside the folder:

`cd cli`

Build the image `padfs/cli:<version>` starting from the DockerFile and adding the jar inside the image.
`
` mvn docker:build`

Run the container:

`docker run padfs/cli:<version> com.dido.pad.cli.MainClient -ip 127.0.0.254 - id client 127.0.0.1:node1`

### App project
Is the easy way to run a set of four storage nodes into a single machine (multi threaded).
Download the latest release of `app-<version>.jar`.

In order to run four nodes: `127.0.0.1, 127.0.0.2 , 127.0.0.3 , 127.0.0.4` on local machine:

`java -cp app-0.1.jar com.dido.pad.app.AppRunner`

- `h` (into the console) shows the available commands:
```
usage: 
  down ip : the node with ip  goes down 
  up  x   : the node with ip (127.0.0.x ) goes up 
```

