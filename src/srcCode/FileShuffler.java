package srcCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

// TODO: import GlobalConstants

public class FileShuffler {
	// Change every 10th file
	public final static float ChangeProbability = 0.1f;

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("FileShuffler ROOT_DIRECTORY");
	}

	private static void changeFile(File file) throws IOException {
		Random rand = new Random();

		file.delete();
		OutputStream os = new FileOutputStream(file);
		for (int i = 0; i < GlobalConstants.FileSize; i++) {
			os.write(rand.nextInt(256) + Byte.MIN_VALUE);
		}
		os.close();
	}

	private static void changeMetaData(File file) throws IOException {
		File metaDataFile = new File(file.getCanonicalPath() + GlobalConstants.MetaDataFileSuffix);

		// TODO: File has been changed. --> Change meta-data of this file.
		
		// Read previous timestamp and discard it
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metaDataFile));
		ois.readLong();
		
		// Read neighbors
		HashMap<String, Integer> neighbors = null;
		try {
			neighbors = (HashMap<String, Integer>) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Update Modified Bit for all neighbors
		neighbors.replaceAll((key, oldValue) -> 1);
		
		// Update timestamp
		long timestamp = (new Date()).getTime();

		// Write new metadata back to file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metaDataFile));
		oos.writeLong(timestamp);
		oos.writeObject(neighbors);

		ois.close();
		oos.close();

	}

	public static void main(String[] args) {
		if (args.length != 1) {
			usage();
			System.exit(-1);
		}

		String rootDirStr = args[0];
		File rootDir = new File(rootDirStr);
		if (!rootDir.isDirectory()) {
			System.err.println("Root directory '" + rootDir + "' is no directorty.");
			System.exit(-1);
		}
		if (!rootDir.exists()) {
			System.err.println("Root directory '" + rootDir + "' does not exist.");
			System.exit(-1);
		}

		Random rand = new Random();
		File[] files = rootDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.getPath().endsWith(GlobalConstants.MetaDataFileSuffix)) {
				continue;
			}

			double r = rand.nextDouble();
			if (r <= ChangeProbability) {
				try {
					changeFile(file);
					changeMetaData(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
