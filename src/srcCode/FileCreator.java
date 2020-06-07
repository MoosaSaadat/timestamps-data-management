package srcCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

// TODO: import GlobalConstants

public class FileCreator {

	static void usage() {
		System.out.println("Usage:");
		System.out.println("FileCreator ROOT_DIRECTORY NUMBER_OF_FILES");
	}

	public static File createFile(File rootDir) throws IOException {
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
		File file = null;
		do {
			String fileNameStr = "";
			for (int i = 0; i < GlobalConstants.FileNameSize; i++) {
				fileNameStr += rand.nextInt(10);
			}
			file = new File(rootDir.getCanonicalPath() + File.separator + fileNameStr);
		} while (file.exists());

		OutputStream os = new FileOutputStream(file);
		for (int i = 0; i < GlobalConstants.FileSize; i++) {
			os.write(rand.nextInt(256) + Byte.MIN_VALUE);
		}
		os.close();

		return file;
	}

	public static void createMetaData(File file) throws IOException {
//		File metaDataFile = new File(file.getCanonicalPath() + File.pathSeparator + GlobalConstants.MetaDataFileSuffix);
		File metaDataFile = new File(file.getCanonicalPath() + GlobalConstants.MetaDataFileSuffix);

		// TODO: Write initial meta data to file "metaDataFile".

		// Add current timestamp
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metaDataFile));
		Date date= new Date();
		oos.writeLong(date.getTime());
		
		// Add empty HashMap (no neighbors yet)
		HashMap<String, Integer> neighbors = new HashMap<String, Integer>();
		oos.writeObject(neighbors);
		
		oos.close();

	}
	
	public static void displayFile(File file) throws IOException {
		System.out.println("====================");
		System.out.println(file.getAbsolutePath());
		System.out.println("====================");
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(byte b: is.readAllBytes()) {
			System.out.print(b + ", ");
		}
		System.out.println();
		System.out.println("====================");
		System.out.println();
	}
	
	public static void compareFiles(File fileOne, File fileTwo) {
		
		InputStream isOne = null;
		InputStream isTwo = null;
		try {
			isOne = new FileInputStream(fileOne);
			isTwo = new FileInputStream(fileTwo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		byte[] fileOneContent = null;
		byte[] fileTwoContent = null;
		try {
			fileOneContent = isOne.readAllBytes();
			fileTwoContent = isTwo.readAllBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (fileOneContent.equals(fileTwoContent)) {
			System.out.println("Files are equal");
		}
		else {
			System.out.println("Files are not equal");
		}
		System.out.println();
	}

	public static void main(String[] args) {
	
		if (args.length != 2) {
			usage();
			System.exit(-1);
		}

		String rootDirStr = args[0];
		File rootDir = new File(rootDirStr);
		if (!rootDir.isDirectory()) {
			System.err.println("Root directory '" + rootDir + "' is no directory.");
			System.exit(-1);
		}
		if (!rootDir.exists()) {
			System.err.println("Root directory '" + rootDir + "' does not exist.");
			System.exit(-1);
		}

		int numberOfFiles = -1;
		try {
			numberOfFiles = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid number of files");
			System.exit(-1);
		}
		if (numberOfFiles <= 0) {
			System.err.println("Invalid number of files");
			System.exit(-1);
		}

		for (int i = 0; i < numberOfFiles; i++) {
			File file;
			try {
				file = createFile(rootDir);
				createMetaData(file);
				displayFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
