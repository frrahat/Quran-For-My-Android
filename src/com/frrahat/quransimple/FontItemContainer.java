package com.frrahat.quransimple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

/**
 * @author Rahat
 * started: 01-05-15
 */
public class FontItemContainer {

	private static ArrayList<File> allFontFiles;
	
	private static String selectedFontFileNames[];
	
	private static String fontSotrageDirName = "additFonts";
	
	private static File fontStorageDir;
	
	private static String fontInfoStorageFileName=".fontItemData.ser";
	private static File fontInfoStorageFile;
	
	public final static String Default_Font_File_Name="Default";
	
	public static void initializeFontFiles(Context context){
		allFontFiles = new ArrayList<>();
		
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

		fontStorageDir = new File(storageDir, fontSotrageDirName);
		
		if (!fontStorageDir.exists()) {
			if (fontStorageDir.mkdirs()) {
				Log.i("success", "new folder added :"+ fontSotrageDirName);
			} else {
				Log.i("failure", "folder addition failure :"+fontSotrageDirName);
			}
		}
		
		resetFontFilesFromStorageFolder();
		
		fontInfoStorageFile = new File(storageDir, fontInfoStorageFileName);
		selectedFontFileNames=new String[MainActivity.Max_Quran_Texts];
				
		if(!loadFontInfo()){
			setSelectedFontNamesToDefault();
		}
	}


	public static void resetFontFilesFromStorageFolder() {
		//check any file exists in the folder			
		File[] allFiles;

		allFiles = fontStorageDir.listFiles();
	
		if (allFiles != null){
		
			for (int i = 0; i < allFiles.length; i++) {
				if(allFiles[i].getName().endsWith(".ttf"))
					allFontFiles.add(allFiles[i]);
			}
		}
	}


	@SuppressWarnings("resource")
	public static void addNewFile(File sourceFile) throws IOException{
		FileChannel source = null;
		FileChannel destination = null;
		
		File destFile=new File(fontStorageDir,sourceFile.getName());
		
		destFile.createNewFile();
	
		source=new FileInputStream(sourceFile).getChannel();
		destination=new FileOutputStream(destFile).getChannel();

		destination.transferFrom(source, 0, source.size());

			
		try{
			if(source!=null) source.close();
			if(destination!=null)destination.close();
		}catch(IOException ie){ie.printStackTrace();}
	}
		
	
	public static void removeFile(int index){
		if(allFontFiles.get(index).delete()){
			allFontFiles.remove(index);
		}
	}
	
	/*
	 * load selected font info
	 */
	public static boolean loadFontInfo(){
		
		boolean success=false;
		Log.i("loading", "loading data");
		
		FileInputStream inStream;
		ObjectInputStream objectInStream;
		try {
			inStream = new FileInputStream(fontInfoStorageFile);
			objectInStream = new ObjectInputStream(inStream);

			int noOfItems = objectInStream.readInt();

			for (int i = 0; i < noOfItems; i++) {
				String fileName = (String) objectInStream.readObject();
				selectedFontFileNames[i]=fileName;
			}
			// objectOutStream.flush();
			
			success=true;
			objectInStream.close();

		} catch (IOException | ClassCastException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
		return success;
	}
	
	public static boolean saveFontInfo(){
		Log.i("saving", "saveData() called");

		if (fontInfoStorageFile == null) {
			return false;
		}

		FileOutputStream outStream;
		ObjectOutputStream objectOutStream;
		try {
			outStream = new FileOutputStream(fontInfoStorageFile);
			objectOutStream = new ObjectOutputStream(outStream);

			int noOfItems = selectedFontFileNames.length;
			objectOutStream.writeInt(noOfItems);
			
			for (int i = 0; i < noOfItems; i++) {
				objectOutStream.writeObject(selectedFontFileNames[i]);
			}
			// objectOutStream.flush();
			objectOutStream.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static void setSelectedFontNamesToDefault() {
		//Word info text
		selectedFontFileNames[0]=MainActivity.getDefaultTypefaceName(0);
		
		//Arabic text
		selectedFontFileNames[1]=MainActivity.getDefaultTypefaceName(1);
		
		//English text
		selectedFontFileNames[2]=selectedFontFileNames[1];
		
		
		for(int i=3;i<selectedFontFileNames.length;i++){
			selectedFontFileNames[i]=Default_Font_File_Name;
		}
	}
	
	public static int getFontFileIndexInFiles(int textIndex){
		
		String fileName=selectedFontFileNames[textIndex];

		for(int i=0;i<allFontFiles.size();i++){
			if(allFontFiles.get(i).getName().equals(fileName)){
				return i;
			}
		}
		
		return -1;
	}
	
	public static int getFontFileIndexInAsset(int textIndex){
		
		String fileName=selectedFontFileNames[textIndex];

		for(int i=0;i<MainActivity.totalDefaultTypefaces;i++){
			if(fileName.equals(MainActivity.getDefaultTypefaceName(i))){
				return i;
			}
		}
		
		return -1;
	}
	
	public static File getFontFile(int index){
		return allFontFiles.get(index);
	}
	
	public static ArrayList<File> getAllFontFiles(){
		return allFontFiles;
	}
	
	public static int getFontItemSize(){
		if(allFontFiles==null){
			return 0;
		}
		
		return allFontFiles.size();  
	}
	
	public static String getSelectedFontName(int textIndex){
		return selectedFontFileNames[textIndex];
	}
	
	public static void setSelectedFontName(int textIndex, String fileName){
		selectedFontFileNames[textIndex]=fileName;
	}
}
