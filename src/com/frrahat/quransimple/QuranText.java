/*
 *               In the name of Allah
 * This file is part of The "Quran Teacher or Learn Arabic" Project. Use is subject to
 * license terms.
 *
 * @author:         Fazle Rabbi Rahat
 * 
 * Edit: this has been edited for "Quran For My Android Project"
 */
package com.frrahat.quransimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.util.Log;

import com.frrahat.quransimple.Ayah;
import com.frrahat.quransimple.SurahInformationContainer;


public class QuranText {
	/*
	 * loads Quran Arabic Text
	 */
	private String[][] quranText; 
	
	//public static String QuranTextFileName="/searchQuran/files/texts/quran-uthmani.txt";
	
	public QuranText(InputStream inStream, boolean isArabic)
	{
		
		if(inStream==null){
			Log.i("failure", "file not found");
			return;
		}
		BufferedReader reader=null;
		
		try
		{
			reader=new BufferedReader(new InputStreamReader(inStream,"utf-8"));
			
			String text;
			String firstAyah;
			
			quranText=new String[114][];
			int suraIndex=0;
			int ayahRead=0;
			int ayahCount=SurahInformationContainer.totalAyas[0];
			quranText[0]=new String[ayahCount];
			
			while((text=reader.readLine())!=null)
			{
				if(isArabic && ayahRead==0)//first ayah
				{
					if(suraIndex!=8)//sura at Tawba
						firstAyah=filterBismillah(text);
					else
						firstAyah=text;
					
					quranText[suraIndex][ayahRead]=firstAyah;
				}
				else
				{
					quranText[suraIndex][ayahRead]=text;
				}
				
				ayahRead++;
				
				if(ayahRead==ayahCount)
				{
					if(suraIndex==113)
						break;
					suraIndex++;
					ayahRead=0;
					ayahCount=SurahInformationContainer.totalAyas[suraIndex];
					quranText[suraIndex]=new String[ayahCount];
				}
			}
			Log.i("success", "quran Text loading success");
			reader.close();
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
	}
	/**
	 * @return the quranText
	 */
	public String getQuranText(Ayah ayah) {
		return quranText[ayah.suraIndex][ayah.ayahIndex];
	}
	
	public String filterBismillah(String firstAyah) {
		int sp = -1;
		for (int i = 0; i < 4; i++) {
			// pass 4 whitespaces. in sura fatiha has 3 spaces, so ultimate sp=-1
			sp = firstAyah.indexOf(' ', sp + 1);
		}
		return firstAyah.substring(sp + 1);
	}
	
	private boolean searchInAyah(Ayah ayah, String query, int[] Table) {

		int queryLength = query.length();
		String S = getQuranText(ayah);
		// search

		// define variables:
		int m = 0;// (the beginning of the current match in S)
		int i = 0;// (the position of the current character in W)
		// an array of integers, T (the table, computed elsewhere)

		while (m + i < S.length()) {
			if (areCharsEqualIgnoreCase(query.charAt(i), S.charAt((m + i)))) {
				if (i == queryLength - 1) {
					return true;
				}
				i++;
			} else {
				if (Table[i] > -1) {
					m = m + i - Table[i];
					i = Table[i];
				} else {
					i = 0;
					m++;
				}
			}
		}
		// (if we reach here, we have searched all of S unsuccessfully)
		return false;
	}

	public void search(String query, int maxSearchCount,
			int startSuraIndex, int endSuraIndex, ArrayList<Ayah> matchedAyahs){
		//ArrayList<Integer> ranks=new ArrayList<>();
		
		int queryLength=query.length();
		//build table
		
		int Table[]=new int[queryLength];

		//define variables:
	    int pos = 2 ;//(the current position we are computing in T)
	    int cnd = 0 ;//(the zero-based index in W of the next 
	    			//character of the current candidate substring)

	    //(the first few values are fixed but different from what the algorithm 
	    //might suggest)
	    Table[0] = -1;
	    Table[1] = 0;
	    
	    while (pos < queryLength){
	        //(first case: the substring continues)
	        if (areCharsEqualIgnoreCase(query.charAt(pos-1) , query.charAt(cnd))){
	            cnd++;
	        	Table[pos] = cnd;
	        	pos++;
	        }

	        //(second case: it doesn't, but we can fall back)
	        else if (cnd > 0){
	            cnd = Table[cnd];
	        }

	        //(third case: we have run out of candidates.  Note cnd = 0)
	        else{
	            Table[pos] = 0;
	            pos++;
	        }
	    }
	    //return the length of S
	    int count=0;
	    for(int i=startSuraIndex;i<=endSuraIndex;i++){
	    	for(int j=0,k=SurahInformationContainer.totalAyas[i];j<k;j++){
	    		Ayah ayah=new Ayah(i,j);
	    		if(searchInAyah(ayah, query, Table)){
	    			matchedAyahs.add(ayah);
	    			count++;
	    			
	    			if(count>maxSearchCount)// store more than max if exists
	    				return;
	    		}
	    	}
	    }
		return;
	}
	

	private boolean areCharsEqualIgnoreCase(char a, char b) {//uppering cases
		if (a > 'Z')//lowercase
			a = (char) ('A' + a - 'a');//uppercase

		if (b > 'Z')//lowercase
			b = (char) ('A' + b - 'a');//uppercase

		if (a == b)
			return true;
		else
			return false;
	}
}
