/*
 *               In the name of Allah
 * This file is part of The "Quran Teacher or Learn Arabic" Project. Use is subject to
 * license terms.
 *
 * @author:         Fazle Rabbi Rahat
 * 
 * edited: 24-feb-2015 for Quran for my Android Project
 */
package com.frrahat.quransimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;



public class WordInfoLoader {
	/*
	 * loads word informations such as meaning, grammar etc.
	 */
	public static List<WordInformation>infoWords=new ArrayList<>(77430);
	public static List<Integer>startIndexOfSura=new ArrayList<>(115);
	public static List<Integer>startIndexOfAyah=new ArrayList<>(6240);
	
	public static boolean isInfoLoaded;
	public static boolean isLoadingCompleted;
	
	public WordInfoLoader()
	{
		isInfoLoaded=true;
		//or, info is loading=true
		//it is not included in load() func
		//because, if it will return false till the load() executes
	}
	
	public void load(Context context)
	{
		InputStream inStream=context.getResources().openRawResource(R.raw.wbw_short_info);
		BufferedReader reader=null;
		try
		{
			Log.i("loading", "loading word Info");
			reader=new BufferedReader(new InputStreamReader(inStream,"utf-8"));
			String text;
			WordInformation tempInfo=null;
			while((text=reader.readLine())!=null)
			{
				if(text.startsWith("id"))
				{
					//first element of a wordinfo found
					tempInfo=new WordInformation();
					tempInfo.wordId=text.substring(text.indexOf('=')+1);
				}
				else if(text.startsWith("tr"))
				{
					tempInfo.transliteration=text.substring(text.indexOf('=')+1);
				}
				else if(text.startsWith("me"))
				{
					tempInfo.meaning=text.substring(text.indexOf('=')+1);
					//last element of a word info found
					infoWords.add(tempInfo);
				}		
			}
			
			reader.close();
			Log.i("success"," loading success");
		}catch(IOException ie){
			ie.printStackTrace();
		}
		
		
		
		organizeWordInfo();
		Log.i("complete","organized");
		
		isLoadingCompleted=true;
	}
	
	private void organizeWordInfo()
	{	
		int i;
		for(i=0;i<infoWords.size();i++)
		{
			WordId tempId=formatWordId(infoWords.get(i).wordId);
			if(tempId.ayahNo==1 && tempId.wordNo==1)
			{
				startIndexOfSura.add(i);
			}
			if(tempId.wordNo==1)
			{
				startIndexOfAyah.add(i);
			}
		}
		startIndexOfAyah.add(i);//for advantage, otherwise invalid ayahIndex
	}
	
	private WordId formatWordId(String wordId)
	{
		String withoutBracket=
				wordId.substring(wordId.indexOf('(')+1, wordId.indexOf(')',1));
		
		String[] numbers=withoutBracket.split(":");
		
		int suraNo=Integer.parseInt(numbers[0]);
		int ayahNo=Integer.parseInt(numbers[1]);
		int wordNo=Integer.parseInt(numbers[2]);
		
		return new WordId(suraNo, ayahNo, wordNo);
	}
	

	public static void returnToInitialState(){
		isInfoLoaded=false;
		isLoadingCompleted=false;
		
		//if(!infoWords.isEmpty())
			infoWords.clear();
		//if(startIndexOfAyah.isEmpty())
			startIndexOfAyah.clear();
		//if(startIndexOfSura.isEmpty())
			startIndexOfSura.clear();
	}
	
}

class WordId
{
	int suraNo;
	int ayahNo;
	int wordNo;
	
	public WordId(int i, int j, int k) 
	{
		suraNo=i;
		ayahNo=j;
		wordNo=k;
	}
	
	public String toString()
	{
		return "\nSuraNo "+suraNo
				+"\nAyahNo "+ayahNo
				+"\nWordNo "+wordNo+"\n";
		
	}

}

