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
-  `app` is a pre-configured  storage system composed by 4 storage nodes. It is useful to run a simulation into a single machine.

## How to compile the project
The project is develped using `Maven`.
The command: 

`mvn install` will:
- generate the `*.jar`in the  `/target` folder in each subprojects (app.core,app)
- cpy the `.jar` in to the `/src/main/docker` folder in each sub project (used for the docker build).


## How to run the projects
There are three ways to rrn the project:

- Run the single jar componenet dowloading the  latest [release](https://github.com/dido18/PAD-FileSystem/releases). 
- Run the simulation in a single machine (multithreaded)
- Create a Docker image with the PadFs node code inside, and than tun the image.


#### Running the release
### Core project
The main class of the `core` project is the `PadFsNode.java` class that run a single storage node.
The parameters are:

`./PadFsNode -ip <ipAddress> -id <String> -gp <int> -sp <int>  [<seedIP>:<id>:<gp>]`

where
- `-ip` is the ip address (x.y.z.w) of the node.
- `-id <String>` is a string representing the id of the node.
- `-gp <int>` setups the port for the gossip protocol (default port is defined in `Helper.GOSSIP_PORT=2000``).
- `-sp <int>` setups the port for the storage service (default port is defined in `Helper.STORAGE_PORT=3000`).
- `[<seedIP>:<String>[:<gp>]]` is a list of SEED node concatted by the gossip protocol. Each entry has: `ip` of the node, `id` of the node (id must be equal to remote node id),  `gp = gossip port` is the the gossip port, by default is set to `Helper.GOSSIP_PORT``.

#### How to run a storage Node
The command below run a storage node wwith ip `127.0.0.1` and node ID `node1` and set the seed node to `127.0.0.2:node2`

`java -cp core-0.1.jar  com.dido.pad.PadFsNode -ip 127.0.0.1 -id node1 127.0.0.2:node2`

## Cli project

Contains the client that is the external node that expose the `cli` (command line interface) to the user.

`Usage: Client [options] ipSeed:id[:gp] [ipSeed:id[:gp]`

Where `Options`:
  -  `--help`: show the help message
  -  `-id`: (String) Id of the client (Default  `-id client`)
  - `-ip`: Ip address of the client (Default: `-ip 127.0.0.254`)
The seed nodes contacted initially are:
  - `ipSeed:id[:gp]`: ipseed is the ip, id is the string of the node (equal to the remote), `gp` is the gossip port (default `Helper.GOSSIP_PORT`).
  
#### How to run the Client node
Run a client with `-ip 127.0.0.254` (default) and `- id client` (default) and set one seed node `127.0.0.1:node1`.
` java -cp cli-0.1.jar com.dido.pad.cli.MainClient -ip 127.0.0.254 - id client 127.0.0.1:node1`


### App project
Is the easy way to run a set of four storage nodes into a single machine.

Go inside *app* folder and type:

`java -cp app-0.1.jar com.dido.pad.app.AppRunner`

It runs four nodes: `127.0.0.1, 127.0.0.2 , 127.0.0.3 , 127.0.0.4` on local machine.

IN order to show the available command type `h` on the console, it shows the `usage`:
```
usage: 
  down ip : the node with ip  goes down 
  up  x : the node with ip (127.0.0.x) goes up 
```


### Docker images
It is possible to create the images starting from the DockerFile inside each project.
In order to create the images type the commmand:
`mvn docker:build`

In order to run the container :

`docker run padfs/core:<version> -ip 127.0.0.1 -id node1 127.0.0.2:node2`

The client container:

`docker run padfs/cli:<version>  -ip 127.0.0.254 -id client 127.0.0.1:node1`
