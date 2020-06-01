package srcCode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer extends Thread {
	private class RequestHandler extends Thread {
		public final static int BufferSize = 1024;
		
		private Socket socket;
		
		public RequestHandler(Socket socket) {
			this.socket = socket;
		}
		
		private void resetModificationBit(File file, String clientID) {
			// TODO: Clear the modification bit of this file.
			
		}
		
		@Override
		public void run() {
			byte[] buffer = new byte[BufferSize];
			
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientID = reader.readLine();
				String fileName = reader.readLine();
				
				File file = new File(rootDir.getCanonicalFile() + File.separator + fileName);
				
				OutputStream os = new BufferedOutputStream(socket.getOutputStream());
				
				InputStream fis = new BufferedInputStream(new FileInputStream(file));
				
				while (fis.read(buffer) != -1) {
					os.write(buffer);
				}
				
				resetModificationBit(file, clientID);
				
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
