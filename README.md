#PAD-FileSystem 


### Core

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

`java -cp target/cli-1.0-SNAPSHOT-jar-with-dependencies.jar  com.dido.com.pad.cli.Cli `