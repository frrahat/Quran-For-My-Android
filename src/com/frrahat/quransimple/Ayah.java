
package com.frrahat.quransimple;

import java.io.Serializable;

/**
 * @author Rahat
 *
 */
public class Ayah implements Serializable{
	public int suraIndex;
	public int ayahIndex;
	
	public Ayah()
	{
		suraIndex=0;
		ayahIndex=0;
	}
	public Ayah(int suraIndex,int ayahIndex)
	{
		if(suraIndex>=0 && suraIndex<114)
			this.suraIndex=suraIndex;
		else
			this.suraIndex=113;
		
		if(ayahIndex>=0 && ayahIndex<SuraInformation.totalAyas[this.suraIndex])
			this.ayahIndex=ayahIndex;
		else
			this.ayahIndex=SuraInformation.totalAyas[this.suraIndex]-1;
	}
	
	public Ayah getNexTAyah()
	{
		if(ayahIndex+1<SuraInformation.totalAyas[suraIndex])
			return new Ayah(suraIndex,ayahIndex+1);
		
		else if(ayahIndex+1==SuraInformation.totalAyas[suraIndex] && suraIndex<113)
			return new Ayah(suraIndex+1,0);
		
		return null;
	}
	
	public Ayah getPrevAyah()
	{
		if(ayahIndex>0)
			return new Ayah(suraIndex,ayahIndex-1);
		
		else if(suraIndex>0)
			return new Ayah(suraIndex-1,SuraInformation.totalAyas[suraIndex-1]-1);
		
		return null;
	}
	
	@Override
	public String toString()
	{
		return (suraIndex+1)+":"+(ayahIndex+1);
	}
}
