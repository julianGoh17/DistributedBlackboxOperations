# README

This file provides a quick overview of the project and the requirements to get the project working.

## Distributed Algorithm Simulation

This project allows a user to easily create, simulate, and test a distributed system by providing them with a platform 
to simulate their system in various situations and obtain metrics about the simulation's algorithm performance. The 
simulation emulates the situation where a client sends and receives messages from a system of servers. When a client 
message to one of the servers, that server must coordinate with other servers to fulfill the client's request. 

To create the distributed system, the user must create a class that extends AbstractDistributedAlgorithm, which will 
then get loaded in each server. Thus, the user will write a single program that controls how a server acts, 
which will include: 
* what does the server do when a client sends a message in
* what does the server receives a message from another client,
* what does the server do to coordinate with other servers

## Requirements 
* [OpenJDK's Java 11 JDK](https://jdk.java.net/java-se-ri/11)
* [Maven](https://maven.apache.org/download.cgi) 
* Recommend installing [Docker](https://www.docker.com/get-started) to easily set up and build servers/clients.

## Development Environment
[IntelliJ](https://www.jetbrains.com/idea/) is the recommended development environment. The developer can give limited 
support to any other IDEs.

Suppose you have chosen to use IntelliJ as your development environment. In that case, 
[the IntelliJ Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok) will make the development experience 
easier and fix the errors that will occur without it installed. Lombok can create Getters and Setters for a Java class 
without the need for the user to create the actual methods. Without this Lombok plugin, IntelliJ will error as 
it thinks the user has not created the Getters and Setters because it doesn't know Lombok created it.
