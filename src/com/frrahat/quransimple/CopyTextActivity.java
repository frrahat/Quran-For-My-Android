package com.frrahat.quransimple;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

/**
 * @author Rahat
 *
 */
public class CopyTextActivity extends Activity {
	
	private EditText editText;
	private InputMethodManager imm;
	private ActionBar actionBar;
	
	private boolean inputVerseMode;
	private EditText inputVerseEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_copy_text);
		
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		actionBar=getActionBar();
		actionBar.setCustomView(R.layout.actionbar_with_edittext);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_HOME);
		inputVerseMode=false;
		
		inputVerseEditText = (EditText) actionBar.getCustomView().findViewById(R.id.editText_commandText);
		inputVerseEditText.setHint("Enter Verse No.");
		inputVerseEditText.setInputType(InputType.TYPE_CLASS_DATETIME);
		inputVerseEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				hideSoftKeyBoard(inputVerseEditText);
				String inputText=inputVerseEditText.getText().toString();
				if(inputText!=null){
					Ayah appendingAyah=processInputString(inputText);
					if(appendingAyah==null){
						return false;
					}
					editText.append(MainActivity.getAyahText(appendingAyah)+"\n");
					inputVerseEditText.setText(appendingAyah.toString());
					Toast.makeText(CopyTextActivity.this, "Appended Ayah ["+appendingAyah.toString()+"]"
							, Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(CopyTextActivity.this, R.string.text_invalid_input, Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		
		editText=(EditText) findViewById(R.id.editText_copyActivity);
		editText.setTypeface(MainActivity.getMainTextTypeface());
		String text=getIntent().getStringExtra("text");
		
		if(text!=null){
			editText.setText(text);
			editText.setSelection(text.length());
		}else{
			editText.setText("No Text Found.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.copy_text, menu);
		if(!inputVerseMode){
			menu.findItem(R.id.action_appendAyah).setIcon(R.drawable.ic_plus);
		}else{
			menu.findItem(R.id.action_appendAyah).setIcon(R.drawable.ic_clear_search);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_appendAyah) {
			if(inputVerseMode){//clear search
				hideSoftKeyBoard(inputVerseEditText);
				setInputVerseModeOff();
			}
			else{
				setInputVerseModeModeOn();
			}
			return true;
		}
		if(id == R.id.action_copyFullText){
			hideSoftKeyBoard(editText);
			copyAllToClipBoard();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void copyAllToClipBoard() {
		ClipData clip = ClipData.newPlainText("text", editText.getText());
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(clip);

		Toast.makeText(getApplicationContext(), "Full Text Copied To Clipboard",
				Toast.LENGTH_SHORT).show();
	}
	
	private void hideSoftKeyBoard(EditText editText) {
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	
	
	private void setInputVerseModeOff(){
		inputVerseMode=false;
		actionBar.setDisplayShowCustomEnabled(false);
		invalidateOptionsMenu();
	}
	
	private void setInputVerseModeModeOn(){	
		inputVerseMode=true;
		actionBar.setDisplayShowCustomEnabled(true);
		inputVerseEditText.setText("");
		invalidateOptionsMenu();
	}
	
	private Ayah processInputString(String input){
		input=input.trim();
		// ':','/',' ' and '-' are acceptable .
		input = input.replace('/', ' ');
		input = input.replace(':', ' ');
		input = input.replace('-', ' ');
		String parts[] = input.split(" ");
		if (parts.length < 2) {
			Toast.makeText(this, R.string.text_invalid_input, Toast.LENGTH_SHORT).show();
			return null;
		}

		int suraNo, ayahNo;

		try {
			suraNo = Integer.parseInt(parts[0]);
			ayahNo = Integer.parseInt(parts[1]);

		} catch (NumberFormatException e) {
			Toast.makeText(this, R.string.text_invalid_input, Toast.LENGTH_SHORT).show();
			return null;
		}

		Ayah inputAyah = new Ayah(suraNo - 1, ayahNo - 1);

		return inputAyah;
	}
}
