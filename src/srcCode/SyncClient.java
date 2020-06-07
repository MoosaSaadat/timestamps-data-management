package srcCode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: import GlobalConstants

public class SyncClient {

	private File rootDir;
	private String serverHostName;
	private int fileServerPort;
	private int metaDataServerPort;
	private String clientId;
	private String serverId;

	// Files to be received
	ArrayList<String> files;
	ArrayList<Long> timestamps;

	public SyncClient(String clientId, File rootDir, String serverHostName, int fileServerPort,
			int metaDataServerPort) {
		this.clientId = clientId;
		this.rootDir = rootDir;
		this.serverHostName = serverHostName;
		this.fileServerPort = fileServerPort;
		this.metaDataServerPort = metaDataServerPort;
		this.files = new ArrayList<String>();
		this.timestamps = new ArrayList<Long>();
		this.serverId = serverHostName + ":" + fileServerPort + File.pathSeparator + metaDataServerPort;
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

		String fileName = reader.readLine();
		while (!fileName.equals(GlobalConstants.endOfSharing)) {
			String timestampStr = reader.readLine();
			System.out.println("Modified file: " + fileName + " TimeStamp: " + timestampStr);
			files.add(fileName);
			timestamps.add(Long.parseLong(timestampStr));
			fileName = reader.readLine();
		}
		System.out.println("Done!");

		socket.close();

	}

	private void updateMetaData() throws IOException {

		// Return if there are no new/updated files
		if (files.size() == 0)
			return;

		for (int i = 0; i < files.size(); i++) {
			File metaDataFile = new File(
					rootDir.getCanonicalPath() + File.separator + files.get(i) + GlobalConstants.MetaDataFileSuffix);
			ObjectOutputStream oos = null;

			if (metaDataFile.exists()) {
				// File Update Received
				System.out.println("File exists: " + metaDataFile.getCanonicalPath());
				// Old Timestamp
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metaDataFile));
				long oldTimestamp = ois.readLong();
				long newTimestamp = timestamps.get(i);
				// Order Updates
				if (newTimestamp > oldTimestamp) {
					System.out.println("New update received");
					// Read neighbors
					HashMap<String, Integer> neighbors = null;
					try {
						neighbors = (HashMap<String, Integer>) ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					neighbors.put(serverId, 0);
					oos = new ObjectOutputStream(new FileOutputStream(metaDataFile));
					oos.writeLong(newTimestamp);
					oos.writeObject(neighbors);
					oos.close();
				} else if (newTimestamp == oldTimestamp) {
					System.out.println("Conflict");
					files.remove(i);
					timestamps.remove(i);
					i--;
				} else {
					System.out.println("Old file. DISCARDED!");
					files.remove(i);
					timestamps.remove(i);
					i--;
				}
				ois.close();
			} else {
				// New File Received -> Create a new metadata file and store info in it
				oos = new ObjectOutputStream(new FileOutputStream(metaDataFile));
				oos.writeLong(timestamps.get(i));
				HashMap<String, Integer> neighbors = new HashMap<String, Integer>();
				neighbors.put(serverId, 0);
				oos.writeObject(neighbors);
				oos.close();
			}
		}
	}

	private void syncFiles() throws UnknownHostException, IOException {
		// TODO: Retrieve files to be updated from server (i.e., replace local replica)
		// and change local meta data.

		// Return if there are no new/updated files
		if (files.size() == 0)
			return;

		for (int i = 0; i < files.size(); i++) {

			byte[] buffer = new byte[GlobalConstants.FileSize];

			Socket socket = new Socket(serverHostName, fileServerPort);
			PrintWriter printer = new PrintWriter(socket.getOutputStream());
			InputStream reader = new BufferedInputStream(socket.getInputStream());

			// Send clientId and fileName
			printer.println(clientId);
			printer.println(files.get(i));
			printer.flush();
			System.out.println("Requesting " + files.get(i));

			// Receive file
			File file = new File(rootDir.getCanonicalPath() + File.separator + files.get(i));
			OutputStream writer = new FileOutputStream(file);
			while (reader.read(buffer) != -1) {
				writer.write(buffer);
			}
			System.out.println("Created file at " + file.getAbsoluteFile());

			socket.close();
			writer.close();

		}

	}

	public void sync() throws UnknownHostException, IOException {
		getMetaData();
		updateMetaData();
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
