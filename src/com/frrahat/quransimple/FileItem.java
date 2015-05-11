package com.frrahat.quransimple;

import java.io.File;
import java.io.Serializable;

/**
 * @author Rahat
 *	Date: 18-04-15
 */
public class FileItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private File file;
	private String fileAliasName;

	
	public FileItem(File file){
		this.file=file;
		String fileName=file.getName();
		
		int k=fileName.lastIndexOf(".");
		if(k==-1)
			k=fileName.length();
		
		//for initial value. 
		//if file reading fails then it will be displayed as alias name
		fileAliasName=fileName.substring(0,k);
		
	}

	public File getFile() {
		return file;
	}

	public String getFileAliasName(){
		return fileAliasName;
	}
	
	public String getFileName(){
		return file.getName();
	}
	/*public void setFileName(String text){//rename
		File newPath=new File(file.getParent()+File.separator+text);
		file.renameTo(newPath);
	}*/
	public void setFileAliasName(String text) {
		fileAliasName=text;
	}
}
	