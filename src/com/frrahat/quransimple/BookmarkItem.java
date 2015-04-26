package com.frrahat.quransimple;

import java.io.Serializable;

public class BookmarkItem implements Serializable{
	private Ayah ayah;
	private String comment;
	
	public BookmarkItem(Ayah ayah, String comment){
		this.ayah=ayah;
		this.comment=comment;
	}
	
	public Ayah getAyah(){
		return ayah;
	}
	
	public String getComment(){
		return comment;
	}
	
	public void setComment(String text){
		comment=text;
	}
}
