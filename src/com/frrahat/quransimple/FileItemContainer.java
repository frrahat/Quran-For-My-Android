package com.frrahat.quransimple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

/**
 * @author Rahat
 *	Started: 23-04-15
 *	This is a class for handling fileItems easily
 */
public class FileItemContainer {

	private static ArrayList<FileItem> fileItems;
	private static String textSotrageDirName = "additTexts";

	private static String dataStorageFileName = "fileItemData.ser";
	private static File dataStorageFile;

	private static File textStorageDir;

	// will be called only once
	public static void initializeFileItems(Context context) {

		fileItems = new ArrayList<>();
		
		File storageDir;
		// has SD card
		if (Environment.getExternalStorageState() != null) {
			storageDir = new File(Environment.getExternalStorageDirectory(),
					MainActivity.storageFolderName);
		} else {
			ContextWrapper contextWrapper = new ContextWrapper(
					context.getApplicationContext());
			storageDir = contextWrapper.getDir(MainActivity.storageFolderName,
					Context.MODE_PRIVATE);
		}

		textStorageDir = new File(storageDir, textSotrageDirName);

		if (!textStorageDir.exists()) {
			if (textStorageDir.mkdirs()) {
				Log.i("success", "new folder added");
			} else {
				Log.i("failure", "folder addition failure");
			}
		}

		// getting File and aliasNames
		boolean loadingSuccess=false;
		dataStorageFile = new File(storageDir, dataStorageFileName);

		if (dataStorageFile != null || dataStorageFile.exists()) {
			loadingSuccess=loadFromFile(dataStorageFile);
		} else {
			Log.i("ERROR!", "dataStorageFile is null or couldn't be found");
		}
		
		if(!loadingSuccess){
			//check any file exists in the folder			
			File[] allFiles;

			allFiles = textStorageDir.listFiles();
		
			if (allFiles == null)
				return;
		
			for (int i = 0; i < allFiles.length; i++) {
				fileItems.add(new FileItem(allFiles[i]));
			}
		}
	}

	public static ArrayList<FileItem> getFileItems() {
		return fileItems;
	}

	public static FileItem getFileItem(int index) {
		return fileItems.get(index);
	}
	
	public static int getFileItemsSize() {
		if(fileItems==null){
			return 0;
		}
		return fileItems.size();
	}

	public static File getTextStorageDir() {
		return textStorageDir;
	}

	public static File getDataStorageFile() {
		return dataStorageFile;
	}

	public static boolean saveDataToFile() {
		Log.i("saving", "saveData() called");

		if (dataStorageFile == null) {
			return false;
		}

		FileOutputStream outStream;
		ObjectOutputStream objectOutStream;
		try {
			outStream = new FileOutputStream(dataStorageFile);
			objectOutStream = new ObjectOutputStream(outStream);

			int noOfItems = fileItems.size();
			objectOutStream.writeInt(noOfItems);

			for (int i = 0; i < noOfItems; i++) {
				objectOutStream.writeObject(fileItems.get(i));
			}
			// objectOutStream.flush();
			objectOutStream.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean loadFromFile(File dataFile) {
		boolean success=false;
		
		Log.i("loading", "loading data");
		FileInputStream inStream;
		ObjectInputStream objectInStream;
		try {
			inStream = new FileInputStream(dataFile);
			objectInStream = new ObjectInputStream(inStream);

			int noOfItems = objectInStream.readInt();

			for (int i = 0; i < noOfItems; i++) {
				FileItem item = (FileItem) objectInStream.readObject();
				fileItems.add(item);
			}
			// objectOutStream.flush();
			
			success=true;
			objectInStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return success;
	}

/*	private static void retrieveInfos(FileItem[] items) {
		boolean checked[] = new boolean[fileItems.size()];
		// by default all are set to false

		for (int i = 0; i < items.length; i++) {
			for (int j = 0; j < fileItems.size(); j++) {
				if (checked[j])
					continue;

				if (fileItems.get(j).getFileName()
						.equals(items[i].getFileName())) {
					fileItems.get(j).setFileAliasName(
							items[i].getFileAliasName());

					checked[j] = true;
				}
			}
		}
	}*/
}
