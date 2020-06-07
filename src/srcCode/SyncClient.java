package srcCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

// TODO: import GlobalConstants

public class SyncClient {

	private File rootDir;
	private String serverHostName;
	private int fileServerPort;
	private int metaDataServerPort;
	private String clientId;
	
	public SyncClient(String clientId, File rootDir, String serverHostName, int fileServerPort, int metaDataServerPort) {
		this.clientId = clientId;
		this.rootDir = rootDir;
		this.serverHostName = serverHostName;
		this.fileServerPort = fileServerPort;
		this.metaDataServerPort = metaDataServerPort;
	}
	
	private void getMetaData() throws UnknownHostException, IOException {
		// Retrieve meta data from server.
		Socket socket = new Socket(serverHostName, metaDataServerPort);
		PrintWriter printer = new PrintWriter(socket.getOutputStream());
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		printer.println(clientId);
		printer.flush();
		System.out.println("Sent " + clientId + " to " + serverHostName + ", " + metaDataServerPort);
		// TODO: Retrieve meta data via reader object.
		
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<Long> timestamps = new ArrayList<Long>();
		
		String fileName = reader.readLine();
		while (!fileName.equals(GlobalConstants.endOfSharing)) {
			String timestampStr = reader.readLine();
			System.out.println("Receiving file: " + fileName + " " + timestampStr);
			files.add(fileName);
			timestamps.add(Long.parseLong(timestampStr));
			fileName = reader.readLine();
		}
		
		System.out.println("Done! Closing Socket.");
		
		socket.close();
		
	}
	
	private void syncFiles() {
		// TODO: Retrieve files to be updated from server (i.e., replace local replica) and change local meta data.
		
	}
	
	public void sync() throws UnknownHostException, IOException {
		getMetaData();
		syncFiles();
	}
	
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("FileSync CLIENT_ID ROOT_DIRECTORY SERVER_HOST_NAME FILE_SERVER_PORT META_DATA_SERVER_PORT");
	}
	
	public static void main(String[] args) {
		if (args.length != 5) {
			usage();
			System.exit(-1);
		}
		
		String clientId = args[0];
		
		String rootDirStr = args[1];
		File rootDir = new File(rootDirStr);
		if (!rootDir.isDirectory()) {
			System.err.println("Root directory '" + rootDir + "' is no directorty.");
			System.exit(-1);
		}
		if (!rootDir.exists()) {
			System.err.println("Root directory '" + rootDir + "' does not exist.");
			System.exit(-1);
		}
	
		String serverHostName = args[2];
		
		int fileServerPort = -1;
		try {
			fileServerPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid file server port.");
			System.exit(-1);
		}
		
		int metaDataServerPort = -1;
		try {
		 metaDataServerPort = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid meta data server port.");
			System.exit(-1);
		}
		
		SyncClient client = new SyncClient(clientId, rootDir, serverHostName, fileServerPort, metaDataServerPort);
		try {
			client.sync();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
