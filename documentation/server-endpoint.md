#Server API

The server (which the Distributed Algorithm gets loaded into) is just an HTTP server at the end of the day.
The server contains three types of endpoints for three various purposes:
- **Client Endpoints:** These endpoints are for clients to send GET/POST/DELETE requests to update messages in the 
distributed system. 
- **Coordination Endpoints:** These endpoints allow the servers to communicate with one another and propagate the client 
update throughout the servers.
- **Control Endpoints:** These endpoints are responsible for controlling the simulation state of a server.

To get an in-depth view of the API, run the following command, but replace `{path to the folder containing ServerEndpoints.yaml}` 
with the corresponding path, which should be in the `server` package of this repository: 
```
docker run -p 80:8080 -e SWAGGER_JSON=/foo/ServerEndpoints.yaml -v /{path to the folder containing ServerEndpoints.yaml}:/foo swaggerapi/swagger-ui
```

**Note:** This command will require Docker to be installed.

## Client Endpoints

Client endpoints should only be interacted with by a client attempting to update the state of messages stored in the server
and not be interacted with by other servers. A client can send an update to one of the servers, and then
it gets propagated through the rest of the system.

Client endpoints contain the following endpoints:
- 'GET /client/?messageId={id}': The user can retrieve a message with the specific message ID.
- 'POST /client': Send a message to store on the server.
- 'DELETE /client/?messageID={id}:' The user can delete a message with 'id' if present in the system.

## Coordination Endpoints

Coordination endpoints should only be interacted with through the Distributed Algorithm API. It would be best if you did not interact with
these endpoints directly unless you are testing a specific interaction in your system.

Coordination endpoints contain the following endpoints:
- 'POST /coordinate/label': The user can set a server's label to the specified label.
- 'POST /coordinate/message': The user can send a message that the Distributed Algorithm will interact with containing: 
a message, a custom user metadata field, and message metadata. 

## Control Endpoints

Control endpoints set a server's settings to see the probability it will receive a message and emulates the situation 
where a server 'fails' to receive a message from another server/client. The state of the server will affect the 
likelihood a server fails to receive an update. The states a server can be in are as follows:
- **Available:** The server will not fail to receive a message
- **Probabilistic Failure:** The server will have a probability 'x' of failing to receive a message
- **Unreachable:** The server will be unable to receive a message.
 
Control endpoints contain the following endpoints:
- 'POST /server': Update the server settings to change both the likelihood of failing a message and the server's status.
- 'GET /coordinate/message': Retrieves the server settings.

**Note:** These endpoints are *always* reachable by a client. Thus, the client can send an update to change 
the server-status to "Available" even if the server is "Unreachable" status.
