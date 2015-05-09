package com.frrahat.quransimple;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;


public class SettingsActivity extends PreferenceActivity {

	private final int REQUEST_FontsSetting=0;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		setListPreferenceData();
		
		Preference setFontButton = (Preference)findPreference(getString(R.string.key_setFonts));
		setFontButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
            public boolean onPreferenceClick(Preference preference) {   
            	Intent intent = new Intent(SettingsActivity.this, FontSettingActivity.class);
    			startActivityForResult(intent, REQUEST_FontsSetting); 
                return true;
            }
		});
	}

	private void setListPreferenceData() {
		ArrayList<FileItem>fileItems=FileItemContainer.getFileItems();
		if(fileItems==null || fileItems.size()==0)
			return;
		
		@SuppressWarnings("deprecation")
		ListPreference listPrefPrimary=(ListPreference) findPreference(getString(R.string.key_primary_text_selection));
		CharSequence[] oldEntries = listPrefPrimary.getEntries();
		
		int oldItemSize=oldEntries.length;
		
		/*oldItemSize=oldItemSize<=MainActivity.Total_Default_Quran_Texts ? 
				oldItemSize : MainActivity.Total_Default_Quran_Texts;*/
		
		int totalItems=oldItemSize+fileItems.size();
		
		CharSequence[] newEntries=new CharSequence[totalItems];
		CharSequence[] entryValues=new CharSequence[totalItems];
		
		for(int i=0;i<oldItemSize;i++){
			newEntries[i]=oldEntries[i];
			entryValues[i]=Integer.toString(i);
			
		}
		for(int i=0,j=oldItemSize;i<fileItems.size();i++){
			newEntries[i+j]=fileItems.get(i).getFileAliasName();
			entryValues[i+j]=Integer.toString(i+j);
		}

		listPrefPrimary.setEntries(newEntries);
		listPrefPrimary.setEntryValues(entryValues);

		@SuppressWarnings("deprecation")
		ListPreference listPrefSecondary=(ListPreference) findPreference(getString(R.string.key_secondary_text_selection));
		CharSequence[] newEntriesSecondary=newEntries.clone();
		newEntriesSecondary[0]="No Secondary Text";
		listPrefSecondary.setEntries(newEntriesSecondary);
		listPrefSecondary.setEntryValues(entryValues);
		
		Log.i("update", "list pref updated");
	}
	
	public static Intent start(Activity activity) {
		return new Intent(activity, SettingsActivity.class);
	}
}