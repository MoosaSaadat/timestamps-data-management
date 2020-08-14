# Data Management in IoT

### Description
This is a console based application for simulating Data Management in mobile nodes. Nodes can be added or removed from the network. Connections among different nodes can be specified following the instructions given below. Following protocols are used in this project.

### Protocols
- **Recording Updates:** State-based model
- **Sending Updates:** Modified-Bit protocol
- **Ordering Updates:** Timestamps
- **Conflict Management:** Latest update wins

### How to run?
The following instructions are an example of creating a network with *2 nodes*. The file directory for the first node is *node_data* and the second node is *node_data1*. As we are running it on a single machine, we will have to specify and keep track of port numbers for processes of each node.<br>
Two processes need to be run manually for each node: *SyncServer* and *SyncClient*. The first process keeps running until terminated manually by the user. The second process will be terminated automatically as soon as the sync is completed.


1. Create 2 directories in project folder (same folder as of src):
    - `node_data`
    - `node_data1`
2. Run `FileCreator.java` with the following command line arguments:
    - `node_data 5`
    - It will create 10 files in `node_data` (5 data files, 5 metadata files)
3. Run `SyncServer.java` with the following command line arguments:
    - `node_data 5000 5001`
    - It will start server with two threads on above ports
4. Run `SyncClient.java` with the following command line arguments:
    - `localhost:6000;6001 node_data1 localhost 5000 5001`
    - It will connect with server and send clientID `localhost:6000;6001` **\***
5. All the files in `node_data` will be replicated in `node_data1` (sync success)

**\*** `clientID` follows the following pattern:

```
nodeHostName:fileServerPort;metaDataServerPort
```

In our case, `nodeHostName` will always be equal to `localhost` because we are running all nodes on the same machine but port numbers will vary.
