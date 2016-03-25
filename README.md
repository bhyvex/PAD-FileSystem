#PAD-FileSystem 


### Core

In order to run a single node run the command

`mvn exec:java -pl core -Dexec.mainClass="com.dido.pad.PadFsNode" -Dexec.args="-ip 127.0.0.1 127.0.0.2:3000:4000" `