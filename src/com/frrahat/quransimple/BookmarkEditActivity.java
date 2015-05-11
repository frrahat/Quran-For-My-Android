package com.frrahat.quransimple;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BookmarkEditActivity extends Activity {

	private TextView textView;
	private EditText commentEditText;
	
	private Button buttonSave;
	private Button buttonClearComment;
	private Button buttonRemove;
	
	private int itemIndex;
	private boolean isNew;
	private Ayah ayah;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmark_edit);
		
		textView=(TextView) findViewById(R.id.textView_bookmarkEdit);
		commentEditText=(EditText) findViewById(R.id.editText_bookmarkEdit);
		
		textView.setText("Failed To Load");
		
		Intent intent=getIntent();
		itemIndex=intent.getIntExtra("index",-1);
		
		//checking if from add new bookmark
		int suraIndex=intent.getIntExtra("suraIndex", 0);
		int ayahIndex=intent.getIntExtra("ayahIndex", 0);
		ayah=new Ayah(suraIndex,ayahIndex); 
				
		
		if(BookmarkItemContainer.getBookmarkItems()==null){
			BookmarkItemContainer.initializeBookmarkItems(this);
		}
		
		//check repetation
		if(itemIndex<0){
			isNew=true;
			for(int i=0,j=BookmarkItemContainer.getBookmarkItemsSize();
					i<j;i++){
				Ayah itemAyah=BookmarkItemContainer.getBookmarkItem(i).getAyah();
	
				if((ayah.ayahIndex==itemAyah.ayahIndex) && (ayah.suraIndex == itemAyah.suraIndex)){
					isNew=false;
					itemIndex=i;
					break;
				}
			}
					
			if(isNew){
				textView.setText(ayah.toDetailedString());
				
				String text=intent.getStringExtra("text");
				if(text!=null){
					text="\""+text+"\"";
					commentEditText.setText(text);
					commentEditText.setSelection(text.length());
				}
			}
		}
		
		//item index changes from the given if not new
		if(itemIndex>=0){//from bookmark display
			BookmarkItem item=BookmarkItemContainer.getBookmarkItem(itemIndex);
			textView.setText(item.getAyah().toDetailedString());
			commentEditText.setText(item.getComment());
			commentEditText.setSelection(commentEditText.getText().length());
		}
		
		
		buttonClearComment=(Button) findViewById(R.id.button_bookmarkEditClearComment);
		buttonClearComment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				commentEditText.setText("");//clearing commentText
			}
		});
		
		
		buttonSave=(Button) findViewById(R.id.button_bookmarkEditSave);
		buttonSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveBookmark();
			}
		});
		buttonRemove=(Button) findViewById(R.id.button_bookmarkEditRemove);
		
		if(itemIndex<0){
			buttonRemove.setText("Cancel");
		}
		buttonRemove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(itemIndex<0){
					finish();
				}else{
					removeBookmark();
				}
			}
		});
		
		commentEditText.setTypeface(MainActivity.getMainTextTypeface());
	}

	private void saveBookmark(){
		if(itemIndex<0 && !isNew){//check if new
			Toast.makeText(this,"Error! Couldn't load the target bookmark.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		String text=commentEditText.getText().toString();
		
		if(isNew){//activity parent : main activity
			if(BookmarkItemContainer.getBookmarkItems()==null){
				BookmarkItemContainer.initializeBookmarkItems(getApplicationContext());
				
			}
			BookmarkItemContainer.getBookmarkItems().
				add(new BookmarkItem(ayah, text));
			
			Toast.makeText(getBaseContext(), "Bookmark added", Toast.LENGTH_SHORT).show();
			Intent intent=new Intent(BookmarkEditActivity.this,BookmarkDisplayActivity.class);
			BookmarkDisplayActivity.setDataChanged(true);
			startActivity(intent);
		}
		else{//activity parent main activity or bookmarkdisplayactivity
			BookmarkItemContainer.getBookmarkItem(itemIndex).setComment(text);
			Toast.makeText(getBaseContext(), "Note Edited", Toast.LENGTH_SHORT).show();
			
			if(!(ayah.suraIndex==0 && ayah.ayahIndex==0)){//activity parent not bookmarkdisplay, so: main activity 
				Intent intent=new Intent(BookmarkEditActivity.this,BookmarkDisplayActivity.class);
				BookmarkDisplayActivity.setDataChanged(true);
				startActivity(intent);
			}
			else{//activity parent bookmarkdisplayActivity
				setResult(RESULT_OK);//going to bookmark display
			}
		}
		
		finish();
	}
	
	private void removeBookmark(){
		new AlertDialog.Builder(BookmarkEditActivity.this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Confirm Remove")
		.setMessage("Do you want to remove this Bookmark from the list?")
		.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						
						BookmarkItemContainer.getBookmarkItems().remove(itemIndex);
						setResult(RESULT_OK);
						finish();
					}

				}).setNegativeButton("No", null).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bookmark_edit, menu);
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
}
