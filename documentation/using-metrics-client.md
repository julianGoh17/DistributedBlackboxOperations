# Metrics Client

Metrics client is a pre-configured configured client that allows the user to easily send updates to a server and collect general metrics on algorithm performance. 
A user can load multiple sequences of updates or operation chains into the client, which the client can then use to perform a sequence of updates.
A user can also load pre-configured messages into the client, which the client can send throughout its runtime.

At the end of the metric client's run time, it can create a general report about how many successful and unsuccessful updates they received.
However, the client cannot gather specific per message metrics. 

## Loading Configured Messages
You can create a folder of pre-configured messages that the metrics client can use. 
The metrics client will load every file in the specified folder as a pre-configured message. 
Each file should be a `json` file that replicates a message you expect would be sent by a real-world client.

## Creating Operation Chain
An operation chain is a sequence of updates the client will perform in parallel or sequentially. 
The metrics client will load all operation chains in a specified folder, allow the user to load multiple chains at once.
A single operation chain is a json file in the folder with a specific format. 
For an example of a valid operation chain, see the [sequential operation chain](https://github.com/julianGoh17/DistributedBlackboxOperations/blob/Main/client/src/Main/resources/generated/operations/sequential-operations.json).
An operation chain file contains two important high-level fields: Operations and Configuration.

### Operations
Operations will specify the updates that the client will run in this operation chain and the expected result.
Operations is simply an array of operations.

An operation is a JSON that comprises two important fields: 
- `action`: The update and the corresponding message to perform
- `expected`: What the expected returned status code which determines if an update has succeeded/failed

The `action` field is mapped to a JSON that should contain the following fields:
- `method`: The HTTP request that the client will make
- `messageNumber`: corresponds to the preconfigured message loaded into the server

The `expected` field is mapped to a JSON that should contain the following two fields:
- `statusCode`: The expected returned status code 
- `messageNumber`: The expected preconfigured message the sever should return

### Configuration
Configuration controls how the operation chain will run. 
The only thing that a user can specify is the `willRunInParallel` field, which determines whether the operation chain runs parallel or sequentially.
An example of a valid configuration field that specifies a sequential operation is as follows:
```
{
    "configuration": {
        "willRunInParallel": false
    }
}
```

## Using Metrics Client
Follow the following set of steps to run metrics client:
1. Ensure to have the Docker image for the metrics client to have been built. If not, run `$ ./build.sh` in the repository's root folder.
2. Run `$ ./run-client.sh`

