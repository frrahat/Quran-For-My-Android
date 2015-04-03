/*
 *               In the name of Allah
 * This file is part of The "Quran Teacher or Learn Arabic" Project. Use is subject to
 * license terms.
 *
 * @author:         Fazle Rabbi Rahat
 * 
 * edited: 24-feb-2015 for Quran for my Android app
 */
package com.frrahat.quransimple;

public class WordInformation{
	
	public String wordId;
	public String transliteration;
	public String meaning;
	
	@Override
	public String toString() {
		return    "\nwordId=" + wordId
				+ "\ntransLiteration=" + transliteration 
				+ "\nmeaning="+ meaning;
	}
}
