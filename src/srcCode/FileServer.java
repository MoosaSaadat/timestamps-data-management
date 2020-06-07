package srcCode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FileServer extends Thread {
	private class RequestHandler extends Thread {
		private Socket socket;

		public RequestHandler(Socket socket) {
			this.socket = socket;
		}

		private void resetModificationBit(String fileName, String clientID) throws IOException {

			// TODO: Clear the modification bit of this file.

			File metaDataFile = new File(
					rootDir.getCanonicalFile() + File.separator + fileName + GlobalConstants.MetaDataFileSuffix);

			// Read previous timestamp
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metaDataFile));

			// Read timestamp and neighbors
			long timestamp = ois.readLong();
			HashMap<String, Integer> neighbors = null;
			try {
				neighbors = (HashMap<String, Integer>) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			// Reset Modified Bit for the given clientID
			neighbors.put(clientID, 0);
			neighbors.replaceAll((key, oldValue) -> 1);

			// Write new metadata back to file
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metaDataFile));
			oos.writeLong(timestamp);
			oos.writeObject(neighbors);

			ois.close();
			oos.close();

		}

		@Override
		public void run() {
			byte[] buffer = new byte[GlobalConstants.FileSize];

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientID = reader.readLine();
				String fileName = reader.readLine();

				File file = new File(rootDir.getCanonicalFile() + File.separator + fileName);

				OutputStream os = new BufferedOutputStream(socket.getOutputStream());

				InputStream fis = new BufferedInputStream(new FileInputStream(file));

				System.out.println("Sending file: " + fileName + " ...");
				while (fis.read(buffer) != -1) {
					os.write(buffer);
				}
				os.flush();

				resetModificationBit(fileName, clientID);
				System.out.println("Sent!");

				fis.close();
				os.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private int port;
	private File rootDir;

	public FileServer(int port, File rootDir) {
		this.port = port;
		this.rootDir = rootDir;
	}

	@Override
	public void run() {
		System.out.println("FileServer Started!");
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
