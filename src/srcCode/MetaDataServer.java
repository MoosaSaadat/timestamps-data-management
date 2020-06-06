package srcCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class MetaDataServer extends Thread {
	private class RequestHandler extends Thread {
		public final static int BufferSize = 1024;

		private Socket socket;

		public RequestHandler(Socket socket) {
			this.socket = socket;
		}
		
		private int getModificationBit(File file, String clientID) throws IOException {

			// Read previous timestamp and discard it
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			ois.readLong();
			
			// Read neighbors
			HashMap<String, Integer> neighbors = null;
			try {
				neighbors = (HashMap<String, Integer>) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			ois.close();

			// Return Modified Bit for the given clientID
			return neighbors.get(clientID);

		}
		
		private long getTimestamp(File file, String clientID) throws IOException {

			// Read previous timestamp
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			long timestamp = ois.readLong();
			ois.close();
			
			return timestamp;
			
		}

		private void sendMetaData(PrintWriter printer, String clientID) {
			// TODO: Send meta data of local files to client using "modified bit" approach.
			// That is, send meta data of all files that were modified since
			// the client downloaded the file the last time or that are new (= have never
			// been downloaded by this client so far)
			
			// Search all metadata files
			File[] files = rootDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.getPath().endsWith(GlobalConstants.MetaDataFileSuffix)) {
					int modificationBit = 0;
					long modificationTimestamp = 0;
					try {
						modificationBit = getModificationBit(file, clientID);
						if (modificationBit == 1)
							modificationTimestamp = getTimestamp(file, clientID);						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// Send filename and timestamp if it has been modified
					if (modificationBit == 1) {
						printer.print(file.getName().replace(GlobalConstants.MetaDataFileSuffix, ""));
						printer.print(modificationTimestamp);
					}
				}
			}


		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientID = reader.readLine();

				PrintWriter printer = new PrintWriter(socket.getOutputStream());
				sendMetaData(printer, clientID);

				reader.close();
				printer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private int port;
	private File rootDir;

	public MetaDataServer(int port, File rootDir) {
		this.port = port;
		this.rootDir = rootDir;
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				Socket socket = serverSocket.accept();
				RequestHandler requestHandler = new RequestHandler(socket);
				requestHandler.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
