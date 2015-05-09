package com.frrahat.quransimple;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Rahat
 * started: 01-05-15
 */
public class FontSettingActivity extends Activity {

	private static boolean dataChanged;
	private ListView textVsFontListView;
	private BaseAdapter adapter;
	private Button saveButton;
	private Button cancelButton;
	
	private final int Request_font_selection=0;
	private final int Request_Help=1;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_font_setting);
		
		if(FileItemContainer.getFileItems()==null){
			((TextView) findViewById(R.id.textView_fontSettings)).setText("ERROR! Failed to "
					+ "load additional file(s).");
			return;
		}		
		
		final String defaultTexts[]=getResources()
				.getStringArray(R.array.array_textSelection);
		
		
		dataChanged=false;
		
        textVsFontListView=(ListView) findViewById(R.id.listView_fontSettings);
		
		adapter=new BaseAdapter() {
			
			LayoutInflater layoutInflater=(LayoutInflater) 
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			@SuppressLint("InflateParams")
			@Override
			public View getView(final int position, View view, ViewGroup parent) {
				if(view==null)
				{
					view=layoutInflater.inflate(R.layout.listitem_text_vs_font, null);
				}
				TextView textView=(TextView) view.findViewById(R.id.textView_textName);
				
				if(position<defaultTexts.length){
					textView.setText(Integer.toString(position+1)+"."+defaultTexts[position]);
				}
				else{
					textView.setText(Integer.toString(position+1)+"."+FileItemContainer.getFileItem(position-defaultTexts.length).getFileAliasName());
				}
				Button buttonChangeFont=(Button) view.findViewById(R.id.button_font);
				buttonChangeFont.setText(FontItemContainer.getSelectedFontName(position));
				
				buttonChangeFont.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						Intent intent=new Intent(FontSettingActivity.this, FontListActivity.class);
						//sending text name to track for which text the font is being edited
						if(position<defaultTexts.length){
							intent.putExtra("textName", defaultTexts[position]);
						}
						else{
							int p=position-defaultTexts.length;
							intent.putExtra("textName", FileItemContainer.getFileItem(p).getFileAliasName());
						}
						intent.putExtra("textIndex", position);
						startActivityForResult(intent, Request_font_selection);
					}
				});
				
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
				//return FileItemContainer.getFileItem(position); 
			}
			
			@Override
			public int getCount() {
				return FileItemContainer.getFileItemsSize()+defaultTexts.length;
			}
		};
		
		textVsFontListView.setAdapter(adapter);
		
		saveButton=(Button) findViewById(R.id.button_saveFontSettings);
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(dataChanged){
					saveFontSetting();
					finish();
				}
				else{
					Toast.makeText(FontSettingActivity.this, "No changes made. Nothing to save.", 
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		cancelButton=(Button) findViewById(R.id.button_cancelFontSettings);
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cancelFontSetting();
			}
		});
		
		
	}

	@Override
	public void onBackPressed() {
		if(dataChanged){
			saveFontSetting();
		}
		super.onBackPressed();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==Request_font_selection && resultCode==RESULT_OK){
			adapter.notifyDataSetChanged();
			dataChanged=true;
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.font_setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_showHelp) {
			Intent intent=new Intent(FontSettingActivity.this,HelpActivity.class);
			startActivityForResult(intent, Request_Help);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void saveFontSetting(){
		Thread saverThread=new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(!FontItemContainer.saveFontInfo()){
					Toast.makeText(getApplicationContext(), "Error! Font Setting Saving Failed.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		saverThread.start();
		setResult(RESULT_OK);
	}
	
	private void cancelFontSetting(){
		finish();
	}
	
	public static boolean isDataChanged(){
		return dataChanged;
	}
	
	public static void setDataChanged(boolean value){
		dataChanged=value;
	}
}
