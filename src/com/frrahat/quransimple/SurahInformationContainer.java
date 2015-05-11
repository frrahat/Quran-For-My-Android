/**
 *               In the name of Allah
 * This file is part of The "Quran Teacher or Learn Arabic" Project. Use is subject to
 * license terms.
 *
 * @author:         Fazle Rabbi Rahat
 * 
 */

package com.frrahat.quransimple;

import java.util.ArrayList;

import android.content.Context;

/**
 * @author Rahat
 *	Edited : 06-02-2015
 *Edited for android:24-FEB-2015
 */
public class SurahInformationContainer {
	
	/*
	 * Loads information of all the surahs for informationPanel
	 */
	
	private static ArrayList<SurahInformation>surahInformations;
	
	public static int[] totalAyas={7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123,
			111, 43, 52, 99, 128, 111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93,
			88, 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59,
			37, 35, 38, 29, 18, 45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11,
			11, 18, 12, 12, 30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
			29, 19, 36, 25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8,
			11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6};
	
	
	public static void loadAllSuraInfos(Context context){
		String suraNames[] = context.getResources().getStringArray(R.array.sura_name);
		String titleMeanings[]=context.getResources().getStringArray(R.array.sura_title_meanings);
		
		surahInformations=new ArrayList<>();
		
		for(int i=0;i<114;i++){
			SurahInformation s=new SurahInformation(i+1,suraNames[i],titleMeanings[i],totalAyas[i]);
			surahInformations.add(s);
		}
	}
	
	/*
	 * returns total ayahs before the first ayah of the sura Index
	 */
	
	public static int totalAyahsUpto(int suraIndex)
	{
		int sum=0;
		for(int i=0;i<suraIndex;i++)
			sum+=totalAyas[i];
		
		return sum;
	}
	
	public static ArrayList<SurahInformation> getSuraInformations(){
		return surahInformations;
	}
	
	public static SurahInformation getSuraInfo(int index){
		return surahInformations.get(index);
	}
}
