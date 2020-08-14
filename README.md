# Data Management in IoT

### Description
This is a console based application for simulating Data Management in mobile nodes. Nodes can be added or removed from the network. Connections among different nodes can be specified following the instructions given below. Following protocols are used in this project.

## Protocols
- **Recording Updates:** State-based model
- **Sending Updates:** Modified-Bit protocol
- **Ordering Updates:** Timestamps
- **Conflict Management:** Latest update wins

### How to run?
1. Create 2 directories in DistributedDataManagement folder (same folder as of src):
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
    - It will connect with server with clientID = `localhost:6000;6001` (**Note:** Don't change it for now)
5. All the files in `node_data` will be replicated in `node_data1` (sync success)

`clientID` follows the following pattern:

```
nodeHostName:fileServerPort;metaDataServerPort
```

In our case, `nodeHostName` will always be equal to `localhost` but port numbers will vary.


