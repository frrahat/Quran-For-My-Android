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

public class BookmarkItemContainer {

	private static ArrayList<BookmarkItem> bookmarkItems;

	private static String dataStorageFileName = "bookmarkItemData.ser";
	private static File dataStorageFile;

	// will be called only once
	public static void initializeBookmarkItems(Context context) {

		bookmarkItems = new ArrayList<>();

		File storageDir;
		String state=Environment.getExternalStorageState();
		// has writable external  storage
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			storageDir = new File(Environment.getExternalStorageDirectory(),
					MainActivity.storageFolderName);
		} else {
			ContextWrapper contextWrapper = new ContextWrapper(
					context.getApplicationContext());
			storageDir = contextWrapper.getDir(MainActivity.storageFolderName,
					Context.MODE_PRIVATE);
		}

		// getting File and aliasNames
		//boolean loadingSuccess = false;
		dataStorageFile = new File(storageDir, dataStorageFileName);

		if (dataStorageFile != null || dataStorageFile.exists()) {
			loadFromFile(dataStorageFile);
		} else {
			Log.i("ERROR!", "dataStorageFile is null or couldn't be found");
		}
	}

	public static ArrayList<BookmarkItem> getBookmarkItems() {
		return bookmarkItems;
	}

	public static BookmarkItem getBookmarkItem(int index) {
		return bookmarkItems.get(index);
	}

	public static int getBookmarkItemsSize() {
		if(bookmarkItems==null){
			return 0;
		}
		
		return bookmarkItems.size();
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

			int noOfItems = bookmarkItems.size();
			objectOutStream.writeInt(noOfItems);

			for (int i = 0; i < noOfItems; i++) {
				objectOutStream.writeObject(bookmarkItems.get(i));
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
		boolean success = false;

		Log.i("loading", "loading data");
		FileInputStream inStream;
		ObjectInputStream objectInStream;
		try {
			inStream = new FileInputStream(dataFile);
			objectInStream = new ObjectInputStream(inStream);

			int noOfItems = objectInStream.readInt();

			for (int i = 0; i < noOfItems; i++) {
				BookmarkItem item = (BookmarkItem) objectInStream.readObject();
				bookmarkItems.add(item);
			}
			// objectOutStream.flush();

			success = true;
			objectInStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return success;
	}
}
