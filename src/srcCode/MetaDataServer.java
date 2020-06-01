package srcCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MetaDataServer extends Thread {
	private class RequestHandler extends Thread {
		public final static int BufferSize = 1024;

		private Socket socket;

		public RequestHandler(Socket socket) {
			this.socket = socket;
		}

		private void sendMetaData(PrintWriter printer, String clientID) {
			// TODO: Send meta data of local files to client using "modified bit" approach.
			// That is, send meta data of all files that were modified since
			// the client downloaded the file the last time or that are new (= have never
			// been downloaded by this client so far)

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
