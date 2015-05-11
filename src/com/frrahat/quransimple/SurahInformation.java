package com.frrahat.quransimple;

public class SurahInformation {
	int id;
	String title;
	String meaning;
	public int ayahCount;
	//String descent;
	//int revealationOrder;
	String titleReference;
	//String[] mainTheme;
	
	public SurahInformation(int id,String title,String meaning,int ayahCount)
	{
		this.id=id;
		this.title=title;
		this.meaning=meaning;
		this.ayahCount=ayahCount;
	}
}
