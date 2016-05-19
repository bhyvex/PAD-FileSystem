#Pad-fs a distributed persistent data store 

  - [Pad-fs architecture](#arch)
  - [Structure of the project](#structure)
  - [How to run](#run)

Pad-fs is a distribute persistent data store written in `java`. It has not a GUI, it is toally managed by command lines.

## <a name="arch"></a> Pad-Fs architecture 

![architecture](https://cloud.githubusercontent.com/assets/9201530/15389916/745e2008-1db9-11e6-9d90-fba983478c69.png)

Pad-fs system is composed by:
- `Storage system `: a set of communicating storage nodes that are responsible
to manage the data to be stored. Each storage node has a local database and it is
composed by two main services

- `Client` is an external independent node that interact with the storage system
in order to perform the operation of the file system. 

- `User` : human that perform the operation through a command line.

#### API operations
Pad-fs exposes four main API:
- `put(k,v)` : insert of a key k and value v into the system.
- `get(k)` : retrieve the last update version of the data associated with the key k.
- `list(ip)` : lists all the key values stored into the node with ip address.
- `rm(k)` : removes the value associated with the key k.

### <a name="structure"></a>Structure of the project
The pad-fs project is divided in three sub projects:
- `core` contains the code of a single storage node.
- `cli` contains the node client code. The client is an external node that is used to performs the operations (put, get, list) into the distributed system.
-  `app` is a pre-configured  storage system composed by 4 storage nodes. It is useful to run a simulation into a single machine.


## <a name="run"></a> How to run the project

There are three ways to run the project
- [Distributed](#runD) Run with remote nodes.
- [Multithreaded](#app) the simulation in a single machine (multithreaded)
- [Docker](#docker) Creates Docker images (core and client) and run the conatiners in Docker network.


### (#name="runD") Run distributed version 



##### Run Storage Node

Dowload the  latest `core-<version>.jar` [release](https://github.com/dido18/PAD-FileSystem/releases)
in the remote nodes where you want to execute the storage node.

Run a single node with the command below: where `<ip>` is the ip of the remote machine, and  `<id>` is the name of the machine, and set the seed node to `[<ip1>:<id1> [<ipn>:<idn>]]` ( IMPORTANT: `idi` are the id of the seed nodes that this node must contact initially, they must correspond to the name given to the remote machine)

`java -cp core-<version>.jar  com.dido.pad.PadFsNode -ip <ip> -id <id>  [<ip1>:<id1> [<ipn>:<idn>]]`


example: (run a node with ip `127.0.0.1` with id `node1` and seed nodes `127.0.0.2` and`127.0.0.3`
`java -cp core-<version>.jar  com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1  127.0.0.2:node2 127.0.0.3:node3`

The parameters are:

`./PadFsNode -ip <ipAddress> -id <String> -gp <int> -sp <int>  [<seedIP>:<id>:<gp>]`

where
- `-ip` is the ip address (x.y.z.w) of the node.
-
- `-id <String>` is a string representing the id of the node.

- `-gp <int>` setups the port for the gossip protocol (default port is defined in `Helper.GOSSIP_PORT=2000``).

- `-sp <int>` setups the port for the storage service (default port is defined in `Helper.STORAGE_PORT=3000`).

- `[<seedIP>:<String>[:<gp>]]` is a list of SEED node concatted by the gossip protocol. Each entry has: `ip` of the node, `id` of the node (id must be equal to remote node id),  `gp = gossip port` is the the gossip port, by default is set to `Helper.GOSSIP_PORT``.


## Cli project
Donload the latest version of the client node `cli-<version>.jar`[release](https://github.com/dido18/PAD-FileSystem/releases)

Run a client with `-ip 127.0.0.254` (default) and `- id client` (default) and set one seed node `127.0.0.1:node1`.

` java -cp cli-<version>.jar com.dido.pad.cli.MainClient -ip 127.0.0.254 -id client 127.0.0.1:node1`

The  parameters are :

`./Client -ip <ipClient> -id <idClient> ipSeed:id[:gp] [ipSeed:id[:gp]`

Where `Options`:
  -  `--help`: show the help message
  -  `-id`: (String) Id of the client (Default  `-id client`)
  - `-ip`: Ip address of the client (Default: `-ip 127.0.0.254`)

  - `ipSeed:id[:gp]`: identify the seed node that must be conactted initially. (IMPORTANT: the `id`  string must be equal to the remote), `gp` is the gossip port (default `Helper.GOSSIP_PORT`).
  

## <a name="structure"></a> Run in Docker 

The project con be runned in a Docker environment.
The steps are :
    - create the `padfs/core:<version>` image (see below)
    - crate the `padfs/cli:<version>` image (see below)
    - run the perl scripts `runDocker.pl` that create a Docekr  network (`pad-net`) and run the containers.

#### Docker core image

In order to compile compile and package the `.jar` into the `/target` folder and into the `src/main/docker` folder, type:

`mvn clean install  -DskipTests=true  -pl core -am`

Go inside the `core` folder:

` cd core`

Build the image `padfs/core:<version>` starting from the DockerFile and adding the jar inside the image.

` mvn docker:build`

Run the node container (if you don't execute`reunDocker.pl`)
`docker run --net <network docker> padfs/core:<version> com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1 127.0.0.2:node2`

#### Docker client image

Compile and package the `.jar` into the `/target` folder and into the `src/main/docker` folder.

`mvn clean install -DskipTests=true   -pl cli -am`

Go inside the folder:

`cd cli`

Build the image `padfs/cli:<version>` starting from the DockerFile and adding the jar inside the image.

`mvn docker:build`

Run the client container ((if you don't execute`reunDocker.pl`):

`docker run padfs/cli:<version> com.dido.pad.cli.MainClient -ip 127.0.0.254 -id client 127.0.0.1:node1`


### <a id="app"> </a> Run multithreaded version (for testing)
For testing environment is possible to run a set of four nodes into a local machine.

Is the easy way to run a set of four storage nodes into a single machine (multi threaded).

Download the latest release of `app-<version>.jar` [release](https://github.com/dido18/PAD-FileSystem/releases)

In order to run four nodes: `127.0.0.1, 127.0.0.2 , 127.0.0.3 , 127.0.0.4` on local machine:

`java -cp app-0.1.jar com.dido.pad.app.AppRunner`

- `h` (into the console) shows the available commands:
```
usage: 
  down ip : the node with ip  goes down 
  up  x   : the node with ip (127.0.0.x ) goes up 
```
