# Distributed Algorithm

Think of the distributed algorithm as the control center of the server. 
The servers are merely agents to test whether a distributed algorithm appropriately propagates a client request to the rest of the system. 
The prebuilt client is the vehicle by which a user can send updates into the server to see if the algorithm functions properly. 
Both the client and the servers are merely ways of verifying a distributed algorithm.
## Setting Up Packages

This project uses the Maven binary to manage dependencies for each package in the repository. 
To begin development, create a Java package with a pom.xml that includes the `server` package as a dependency. 
Including this dependency allows you to easily access the `DistributedAlgorithm` class, which will allow you to control the server effectively. 
One can see an example of this in [test package pom.xml](https://github.com/julianGoh17/DistributedBlackboxOperations/blob/Main/test/pom.xml#L27).

## Creating a Distributed Algorithm

A distributed algorithm that the server can use must extend the [DistributedAlgorithm](https://github.com/julianGoh17/DistributedBlackboxOperations/blob/17-documentation/server/src/Main/java/io/julian/server/api/DistributedAlgorithm.java) class to properly control the servers. 
Please refer to the [example DistributedAlgorithm](https://github.com/julianGoh17/DistributedBlackboxOperations/blob/Main/test/src/Main/java/io/julian/ExampleDistributedAlgorithm.java) as a reference.
 
One must override two methods to create a program that can run on the server. 

The `actOnInitialMessage` method is called every time the server receives an update request from the client. 
To pull a client request from the queue, use the `getClientMessage` method. 
The current server will have already processed the current request; hence the user does not need to do anything to the client message except propagate it as a coordinate message to the other servers. 
Use the `Server-Client` to easily propagate the update.

The `actOnCoordinateMessage` method is called every time it receives an update from another server. 
To pull the update from another server, use the `getCoordinateMessage` method. 
It is then up to the user to design what the algorithm should do. 

You might be wondering how we differentiate between messages accepted/rejected by other servers? 
The coordinate message contains a `userDefinition` field that will allow the user to add any metadata that they may need to differentiate messages in different states. 
This differentiation of state would allow the code to have different code paths in the `actOnCoordinateMessage` method.

## Testing a Distributing Algorithm

It is highly recommended to unit test your distributed algorithm before attempting to run it in practice.
Unit tests will allow you to test the `actOnCoordinateMessage` by setting up specific edge conditions. 
You can do this by simply adding a specific coordinate message into the `coordinatMessageBuffer.`

## Loading a Distributed Algorithm

Once you have your distributed algorithm fully tested and confident in the algorithm's execution,  you can now attempt to load it into the server and begin an actual simulation run. Please follow
the following steps to run the simulation:

1. Build the Java package containing your distributed algorithm and ensure the JAR is available on your machine.
2. Build the `server` Docker image by running: `$ ./build.sh` in the repository's root directory.
3. Run `$ ./run-server.sh` and the necessary arguments to override where the JAR lies.

The necessary arguments for `$ ./run-server.sh` are as follows:
1. The path to the folder that contains the JAR of the distributed algorithm you want to load into the server 
2. The name of the JAR of the distributed algorithm
3. The name of the package of the distributed algorithm (The linked example is `io.julian.ExampleDistributedAlgorithm`)
