package com.frrahat.quransimple;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Rahat
 *	Started: 01-05-15
 */
public class FontListActivity extends Activity {

	private TextView infoTextView;
	private TextView textNameView;
	private ListView fontListView;
	private BaseAdapter adapter;
	private Button addFontButton;

	private String textName;
	private int textIndex;
	
	private String fileFormats[]={".ttf"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_font_list);
		
		//Already initialized from MainActivity.setTypefaces() from MainActivity.updateFromPrefs();
		/*if(FontItemContainer.getAllFontFiles()==null){
			//Initialize from predefined storage location
			FontItemContainer.initializeFontFiles(FontListActivity.this);
		}*/
		
		textName=getIntent().getStringExtra("textName");
		textIndex=getIntent().getIntExtra("textIndex", -1);
		
		infoTextView=(TextView) findViewById(R.id.textView_fontListInfo);
		textNameView=(TextView) findViewById(R.id.textView_textName);
		
		if(textIndex==-1){
			infoTextView.setText("Error! Failed to track selected text.");
			return;
		}
		
		infoTextView.setText(Integer.toString(FontItemContainer.getFontItemSize()+
				MainActivity.totalDefaultTypefaces)+" "
				+ "font file(s) found.\nSelect font for:");
		
		textNameView.setText(textName);
		
        fontListView=(ListView) findViewById(R.id.listView_fontsList);
		
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
				//TextView textView2=(TextView) view.findViewById(R.id.text2);
				if(position<MainActivity.totalDefaultTypefaces){
					String fontName=MainActivity.getDefaultTypefaceName(position);
					textView1.setText(Integer.toString(position+1)+"."+fontName);
				}
				else{
					int p=position-MainActivity.totalDefaultTypefaces;
					
					if(p<FontItemContainer.getFontItemSize()){
						File fontFile=FontItemContainer.getFontFile(p);
						textView1.setText(Integer.toString(position+1)+"."+fontFile.getName());
					}
					else{
						textView1.setText(Integer.toString(position+1)+"."+"Default");
					}
				}
				//textView2.setText("File : "+fileItem.getFileName());
				
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				if(position<MainActivity.totalDefaultTypefaces){
					String fontName=MainActivity.getDefaultTypefaceName(position);
					return fontName;
				}
				else{
					int p=position-MainActivity.totalDefaultTypefaces;
					
					if(p==FontItemContainer.getFontItemSize()){
						return "Default";
					}
					File fontFile=FontItemContainer.getFontFile(p);
					return fontFile.getName();
				}
			}
			
			@Override
			public int getCount() {
				return FontItemContainer.getFontItemSize() +
						MainActivity.totalDefaultTypefaces + 1;
			}
		};
		
		fontListView.setAdapter(adapter);
		
		fontListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent intent=new Intent();
				FontItemContainer.setSelectedFontName(textIndex, 
						(String)adapter.getItem(position));
				setResult(RESULT_OK);
				finish();
			}
		});
		
		fontListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				
				//if on default typefaces or on Default clicked
				if(position<MainActivity.totalDefaultTypefaces || 
						position==(MainActivity.totalDefaultTypefaces+
								FontItemContainer.getFontItemSize())){
					Toast.makeText(FontListActivity.this, 
							"Cannot be removed.", 
							Toast.LENGTH_SHORT).show();
					return true;
				}
				
				new AlertDialog.Builder(FontListActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Confirm Remove")
				.setMessage("Do you want to remove this file?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								FontItemContainer.removeFile(position-MainActivity.totalDefaultTypefaces);
								updateView();
							}

						}).setNegativeButton("No", null).show();
				
				return true;
			}			
		}); 
		
		addFontButton=(Button) findViewById(R.id.button_addMoreFonts);
		
		addFontButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				
				FileChooserDialog fileChooserDialog=new FileChooserDialog(fileFormats);
				fileChooserDialog.setOnFileChosenListener(
						new FileChooserDialog.OnFileChosenListener() {
					
					@Override
					public void onFileChosen(File file) {
						FontItemContainer.addNewFile(file);
						FontItemContainer.resetFontFilesFromStorageFolder();
						updateView();
						//Toast.makeText(getBaseContext(), file.getName(), Toast.LENGTH_SHORT).show();
					}
				});
				
				fileChooserDialog.show(getFragmentManager(), "fileChooser");
				
				//Toast.makeText(getBaseContext(), "add clicked", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.font_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private void updateView(){
		infoTextView.setText(Integer.toString(FontItemContainer.getFontItemSize()+
				MainActivity.totalDefaultTypefaces)+" "
				+ "font file(s) found.");
		
		adapter.notifyDataSetChanged();
	}
}
