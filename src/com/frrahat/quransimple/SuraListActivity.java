package com.frrahat.quransimple;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SuraListActivity extends Activity {

	private ListView suraListView;
	private ActionBar actionBar;
	private EditText searchEditText;
	private InputMethodManager imm;
	private boolean searchMode;
	
	private ArrayList<SuraInformation>displayedSurahInfos;
	private BaseAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_sura_list);
		
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		actionBar=getActionBar();
		actionBar.setCustomView(R.layout.actionbar_with_edittext);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_HOME);
		searchMode=false;
		
		searchEditText = (EditText) actionBar.getCustomView().findViewById(R.id.editText_commandText);
		searchEditText.setHint("Enter Surah Name");
		searchEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		searchEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				hideSoftKeyBoard();
				operateSearch();
				return false;
			}
		});
		
		if(SuraInformation.getSuraInformations()==null){
			SuraInformation.loadAllSuraInfos(SuraListActivity.this);
		}
		
		displayedSurahInfos=SuraInformation.getSuraInformations();
		
		suraListView=(ListView) findViewById(R.id.listView);
		
		adapter=new BaseAdapter() {
			
			LayoutInflater layoutInflater=(LayoutInflater) 
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			@SuppressLint("InflateParams")
			@Override
			public View getView(int position, View view, ViewGroup parent) {
				if(view==null)
				{
					view=layoutInflater.inflate(R.layout.surah_list_item, null);
				}
				TextView textView1=(TextView) view.findViewById(R.id.text1);
				TextView textView2=(TextView) view.findViewById(R.id.text2);
				
				SuraInformation s=displayedSurahInfos.get(position);
				
				textView1.setText(Integer.toString(s.id)+"."+s.title);
				textView2.setText(s.meaning+" , total verses:"+
						Integer.toString(s.ayahCount));
				
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				return displayedSurahInfos.get(position).id;
			}
			
			@Override
			public Object getItem(int position) {
				return displayedSurahInfos.get(position); 
			}
			
			@Override
			public int getCount() {
				return displayedSurahInfos.size();
			}
		};
		
		suraListView.setAdapter(adapter);
		
		suraListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("sura_num", displayedSurahInfos.get(position).id);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
			
		}); 
		
		searchMode=false;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sura_list, menu);
		if(!searchMode){
			menu.findItem(R.id.action_searchSurah).setIcon(R.drawable.ic_ab_search);
		}else{
			menu.findItem(R.id.action_searchSurah).setIcon(R.drawable.ic_clear_search);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_searchSurah) {
			if(searchMode){//clear search
				hideSoftKeyBoard();
				setSearchModeOff();
			}
			else{
				setSearchModeOn();
			}
		}
			
		return super.onOptionsItemSelected(item);
	}
	
	private void hideSoftKeyBoard() {
		imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
	}
	
	private void setSearchModeOff(){
		searchMode=false;
		actionBar.setDisplayShowCustomEnabled(false);
		invalidateOptionsMenu();
		
		setListToDefault();
	}
	
	private void setSearchModeOn(){	
		searchMode=true;
		actionBar.setDisplayShowCustomEnabled(true);
		searchEditText.setText("");
		invalidateOptionsMenu();
	}
	
	public void operateSearch(){
		String query=searchEditText.getText().toString().trim();
		
		if(query.length()<1){
			setListToDefault();
			return;
		}
		
		displayedSurahInfos=new ArrayList<>();
		
		for(int i=0;i<114;i++){
			SuraInformation s=SuraInformation.getSuraInfo(i);
			String operandString=s.title+" "+s.meaning;
			if(contains(operandString, query)){
				displayedSurahInfos.add(s);
			}
		}
		
		adapter.notifyDataSetChanged();
	}
	
	private boolean contains(String large,String small){
		large=large.toUpperCase(Locale.getDefault());
		small=small.toUpperCase(Locale.getDefault());
		
		int lenLarge=large.length();
		int lenSmall=small.length();
		
		for(int i=0;i<lenLarge-lenSmall+1;i++){
			
			int j=0;
			for(;j<lenSmall;j++){
				if(small.charAt(j)!=large.charAt(i+j)){
					break;
				}
			}
			if(j==lenSmall){
				return true;
			}
		}
		return false;
	}
	
	private void setListToDefault(){
		if(displayedSurahInfos.size()!=114){
			displayedSurahInfos=SuraInformation.getSuraInformations();
			adapter.notifyDataSetChanged();
		}
	}
}
