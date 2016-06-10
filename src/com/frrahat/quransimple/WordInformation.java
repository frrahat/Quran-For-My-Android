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
	
	
	public WordInformation(String wordId, String transliteration, String meaning) {
		this.wordId = wordId;
		this.transliteration = transliteration;
		this.meaning = meaning;
	}


	@Override
	public String toString() {
		return    "\nwordId=" + wordId
				+ "\ntransLiteration=" + transliteration 
				+ "\nmeaning="+ meaning;
	}
}
