package com.frrahat.quransimple;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * @author Rahat
 * started: February 21, 2015 at 7:04:18 PM
 */
public class MainActivity extends Activity {

	private InputMethodManager imm;
	private SharedPreferences sharedPrefs;
	
	private EditText commandText;
	private EditText mainText;
	private ScrollView scrollView;
	
	private Button prevButton;
	private Button nextButton;
	
	private InputCommand currentInputCommand;

	
	//for loadinfg texts
	private QuranText allQuranTexts[];
	private int[] resourceIDs={
			R.raw.quran_uthmani,
			R.raw.en_yusufali,
			R.raw.bn_bengali};
	
	private int selectedTextIndex;
	
	//private enum Text{Arabic, EngLish, Bengali};
	
	//for font faces
	Typeface bengaliTypeface;
	Typeface translitTypeface;//for transliterartion font
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Full Screen
		//requestWindowFeature(Window.FEATURE_NO_TITLE); This line was breaking ActionBar in full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//------------*/
		setContentView(R.layout.activity_main);
		
		initializeComponents();
	}

	private void initializeComponents() {
		
		imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		sharedPrefs=PreferenceManager.getDefaultSharedPreferences(this);
		
		commandText=(EditText)findViewById(R.id.editText_commandText);
		mainText=(EditText)findViewById(R.id.editText_mainText);
		scrollView=(ScrollView)findViewById(R.id.scrollView);
		
		mainText.setText(" ");//preparing the scrollView(may be)
		
		commandText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				hideSoftKeyBoard();
				currentInputCommand=processInput(commandText.getText().toString());
				mainText.setText("");
				if(currentInputCommand!=null)
					printAllAyah();				
				return true;
			}
		});
		
		mainText.setFocusable(false);
		mainText.setLongClickable(false);
		
		prevButton=(Button) findViewById(R.id.button_prev);
		nextButton=(Button) findViewById(R.id.button_next);
		
		prevButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(currentInputCommand!=null)
				{
					currentInputCommand.proceedToPrevAyah();
					mainText.setText("");
					printAyah(currentInputCommand.ayah);
				}
				else
				{
					Toast.makeText(getBaseContext(), "No previous input", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		nextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentInputCommand!=null)
				{
					currentInputCommand.proceedToNextAyah();
					mainText.setText("");
					printAyah(currentInputCommand.ayah);
				}
				else
				{
					Toast.makeText(getBaseContext(), "No previous input", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		allQuranTexts=new QuranText[3];
		//loading Quran Text files
		//loadAllFiles();
		loadFonts();
		
		updateFromPrefs();
		
	}
	
	@Override
	public void onBackPressed() {
		tryExitApp();
	}

	private void tryExitApp() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.title_editor_exit)
		.setMessage(R.string.text_exit_confirmation)
		.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						finish();
					}

				}).setNegativeButton("No", null).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode==0)
	    {
	    	updateFromPrefs();
	    }
	    else if(requestCode==1 && resultCode==RESULT_OK && data!=null)
	    {
	    	int suraNum=data.getIntExtra("sura_num", 0);
	    	if(suraNum>0)
	    	{
	    		Ayah ayah=new Ayah(suraNum-1,0);
	    		currentInputCommand=new InputCommand(ayah, 1);
	    		mainText.setText("");
	    		printAyah(ayah);
	    		commandText.setText(ayah.toString());
	    		//send cursor to the end
	    		commandText.setSelection(commandText.getText().length());
	    	}
	    }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			this.startActivityForResult(SettingsActivity.start(this),0);
			return true;
		}
		else if(id==R.id.action_copyAll){
			copyAllToClipBoard();
			return true;
		}
		else if(id==R.id.action_showList){
			Intent intent=new Intent(this,SuraListActivity.class);
			this.startActivityForResult(intent, 1);
			return true;
		}
		else if(id==R.id.action_showInfo){
			Intent intent=new Intent(this,InfoActivity.class);
			this.startActivity(intent);
			return true;
		}
		else if(id==R.id.action_tryExit){
			tryExitApp();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private InputCommand processInput(String input)
	{
		//all input strings are as time patter i.e. 2:3:4:5
		String parts[]=input.split(":");
		if(parts.length<2)
		{
			Toast.makeText(this, "invalid input", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		int suraNo,startAyahNo,endAyahNo;
		
		try{
			suraNo=Integer.parseInt(parts[0]);
			startAyahNo=Integer.parseInt(parts[1]);
			//default endAyah no. is set to start ayah no. by default
			//else printAllAyah() will give error
			endAyahNo=startAyahNo;
			
			if(parts.length>2)
				endAyahNo=Integer.parseInt(parts[2]);
		}catch(NumberFormatException e){
			Toast.makeText(this, "invalid input", Toast.LENGTH_LONG).show();
			return null;
		}
		
		//ayah construction validity is checked inside ayah class
		//no need to recheck here
		if(endAyahNo<startAyahNo)
		{
			Toast.makeText(this, "Error: start is smaller than end", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		int totalToPrint=endAyahNo-startAyahNo+1;
		Ayah inputAyah=new Ayah(suraNo-1,startAyahNo-1);
		
		//when limiting case happens
		commandText.setText(inputAyah.toString());
		//send cursor to the end
		commandText.setSelection(commandText.getText().length());

		return new InputCommand(inputAyah,totalToPrint);
	}
	
	private void printAyah(Ayah ayah)
	{
		if(selectedTextIndex==3)//word info
		{
			if(!WordInfoLoader.isLoadingCompleted){
				mainText.append("All data haven't yet been loaded. Try again later.");
			}
			else{
				printWordInfo(currentInputCommand.ayah);
			}
				
		}
		else{//other text
			if(allQuranTexts[selectedTextIndex]!=null)
				mainText.append("["+ayah.toString()+"] "+
						allQuranTexts[selectedTextIndex].getQuranText(ayah)+"\n");
			else
			{
				mainText.append("Couldn't Load Selected Text File\n");
			}
		}
		
		scrollToTop();
	}
	
	private void printAllAyah()
	{
		if(selectedTextIndex==3)//word info
		{
			if(currentInputCommand.totalToPrint!=1){
				Toast.makeText(this, "Can show only a single Ayah\n in Word by Word Text Mode", Toast.LENGTH_SHORT).show();
			}
			printAyah(currentInputCommand.ayah);
		}
		else{//other text
			runOnUiThread(new AyahPrinterRunnable());
		}
	}
	
	class AyahPrinterRunnable implements Runnable{
		
		@Override
		public void run() {
			StringBuilder sb = new StringBuilder();
			int totalNextToPrint=currentInputCommand.totalToPrint;
			Ayah ayah=currentInputCommand.ayah;
			
			while((totalNextToPrint--)>0 && ayah!=null){
				
				if(allQuranTexts[selectedTextIndex]!=null)
				{
					sb.append("["+ayah.toString()+"] "+
							allQuranTexts[selectedTextIndex].getQuranText(ayah)+"\n");
				
					sb.append("\n\n");
				}
				ayah=ayah.getNexTAyah();
				
			}
			
			currentInputCommand.ayah=ayah.getPrevAyah();
			mainText.append(sb);
			
			scrollToTop();
		}
		
	}
	
	class InputCommand {
		private Ayah ayah;
		private int totalToPrint;
		
		public InputCommand(Ayah ayah, int totalToPrint)
		{
			this.ayah=ayah;
			this.totalToPrint=totalToPrint;
		}
		
		public void proceedToPrevAyah()
		{
			Ayah prevAyah=ayah.getPrevAyah();
			if(prevAyah!=null){
				ayah=prevAyah;
				commandText.setText(ayah.toString());
				//send cursor to the end
				commandText.setSelection(commandText.getText().length());
			}
			else
				Toast.makeText(getBaseContext(), "Reached to first ayah.", Toast.LENGTH_SHORT).show();
		}
		
		public void proceedToNextAyah()
		{
			Ayah nextAyah=ayah.getNexTAyah();
			if(nextAyah!=null){
				ayah=nextAyah;
				commandText.setText(ayah.toString());
				//send cursor to the end
				commandText.setSelection(commandText.getText().length());
			}
			else
				Toast.makeText(getBaseContext(), "Reached to last ayah.", Toast.LENGTH_LONG).show();
		}
	}
	
	private void hideSoftKeyBoard()
	{
		imm.hideSoftInputFromWindow(commandText.getWindowToken(),0);
	}
	
	/*private void loadAllFiles() {
		
		//load text files--------------------------------------
		Log.i("init","loading files");
		
		final Context context=getBaseContext();
		//arabic text

		Runnable quranTextLoader= new Runnable() {
			
			@Override
			public void run() {
				allQuranTexts[0]=new QuranText(context,R.raw.quran_uthmani,true);
			}
		};
		Thread qTextThread=new Thread(quranTextLoader);
		
		//english translation text
		Runnable englishTextLoader = new Runnable() {
			
			@Override
			public void run() {
				allQuranTexts[1]=new QuranText(context,R.raw.en_yusufali,false);
			}
		};
		Thread eTextThread=new Thread(englishTextLoader);
		
		//bengali translation text
		Runnable bengaliTextLoader = new Runnable() {
			
			@Override
			public void run() {
				allQuranTexts[2]=new QuranText(context,R.raw.bn_bengali,false);
			}
		};
		Thread bTextThread=new Thread(bengaliTextLoader);
		
		//publishToLog("Loading Files...\n");
		qTextThread.start();
		eTextThread.start();
		bTextThread.start();
		//loading Sura Information
		//SuraInformation.loadSuraInformations(getBaseContext(),R.raw.sura_information);
	}*/

	private void loadTextFile(final int index)
	{	
		if(index==3)//wordInfo
		{	//TODO find more memory efficient code
			//arabic text also should have to be loaded;
			Log.i("call", "called load quranText");
			loadTextFile(0);//load arabic text
			
			if(WordInfoLoader.isInfoLoaded){}
			else
			{
				Thread wordInfoLoaderThread = new Thread(new Runnable() {

					@Override
					public void run() {
						new WordInfoLoader().load(getBaseContext());
					}
				});
				wordInfoLoaderThread.start();
			}
			
			return;
		}
		
		if (allQuranTexts[index] == null) {

			Thread textLoaderThread = new Thread(new Runnable() {

				@Override
				public void run() {
					boolean isArabic = false;
					if (index == 0)
						isArabic = true;
					Log.i("init", "loading file, index: " + index);
					allQuranTexts[index] = new QuranText(getBaseContext(),
							resourceIDs[index], isArabic);
					Log.i("success", "Text loading complete");
				}

			});

			textLoaderThread.start();
		}
	}
	
	private void updateFromPrefs() {
		mainText.setTextSize(Float.parseFloat(sharedPrefs.getString
				("pref_font_size", "15")));
		
		selectedTextIndex=Integer.parseInt(sharedPrefs.getString
				("pref_text_selection", "1"));
		
		loadTextFile(selectedTextIndex);
		
		//set Type face
		if(selectedTextIndex==2){//bengali
			mainText.setTypeface(bengaliTypeface);
		}
		else if(selectedTextIndex==3 || selectedTextIndex==0){
			mainText.setTypeface(translitTypeface);
		}
		else{
			mainText.setTypeface(Typeface.DEFAULT);
		}
		
		//set gravity
		if(selectedTextIndex==3)//word info
		{
			mainText.setGravity(Gravity.CENTER);
		}
		else
		{
			mainText.setGravity(Gravity.TOP);			
		}
		
	}
	
	private void copyAllToClipBoard() {
		ClipData clip = ClipData.newPlainText("text",mainText.getText());
		ClipboardManager clipboard= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(clip);
		
		Toast.makeText(getApplicationContext(), "Full Text Copied", 
			      Toast.LENGTH_SHORT).show();
	}
	
	/*private void lowerVersionAlert(){
		Toast.makeText(getApplicationContext(), "Not available in this version.", 
			      Toast.LENGTH_SHORT).show();
	}*/
	
	private void loadFonts()
	{
		bengaliTypeface = Typeface.createFromAsset(
				getAssets(), "fonts/solaimanlipi.ttf");
		
		translitTypeface=Typeface.createFromAsset(
				getAssets(), "fonts/jaghb_uni_bold.ttf");
	}
	
	private List<WordInformation> getInfoOfWords(Ayah ayah)
	{
		//index of first ayah of the sura in all ayah sets
		int indexOfFirstAyah=SuraInformation.totalAyahsUpto(ayah.suraIndex);
		int indexOfSelectedAyah=indexOfFirstAyah+ayah.ayahIndex;
		//address in the info list of that selected ayah
		//index of the first word of this ayah
		int indxOfFirstWord=WordInfoLoader.startIndexOfAyah.get(indexOfSelectedAyah);
		//index of the first word of the next ayah
		int indxOfFWNextA=WordInfoLoader.startIndexOfAyah.get(indexOfSelectedAyah+1);
		
		//now listing all the word informations
		List<WordInformation>wordsOfAyah=new ArrayList<>();
		//List<Image>images=new ArrayList<>();
		
		for(int i=indxOfFirstWord;i<indxOfFWNextA;i++)
		{
			wordsOfAyah.add(WordInfoLoader.infoWords.get(i));
			//images.add(ImageLoader.getImageFromFile(i));
		}
		
		return wordsOfAyah;
		
	}
	
	private void printWordInfo(Ayah ayah)
	{
		String arabicAyahTextWords[]=allQuranTexts[0].getQuranText(ayah).split(" ");
		List<WordInformation>wordsOfAyah=getInfoOfWords(ayah);
		int wordIndexToDisplay=0;
		int wordInfoIndexToDisplay=0;
		
		String text="";
		while(wordIndexToDisplay<arabicAyahTextWords.length){
			
			//waqf checking
			int k=arabicAyahTextWords[wordIndexToDisplay].charAt(0);
			
			if((k>='\u0610' && k<='\u0615') || (k>='\u06D6' && k<='\u06ED'))
			{
				wordIndexToDisplay++;
				continue;
			}
			//waqf checked
			
			text+=arabicAyahTextWords[wordIndexToDisplay];
			
			if(wordInfoIndexToDisplay<wordsOfAyah.size()){
				text+="\n["+wordsOfAyah.get(wordInfoIndexToDisplay).transliteration+"]"
				+"\n"+wordsOfAyah.get(wordInfoIndexToDisplay).meaning;
			}
			else
			{
				text+="\n-------";//not found
			}
			
			text+="\n\n";
			
			wordInfoIndexToDisplay++;
			wordIndexToDisplay++;
		}
		
		mainText.append(text);
	}
	
	
	private void scrollToTop(){
		scrollView.post(new Runnable() {
			public void run() {
				scrollView.fullScroll(View.FOCUS_UP);
			}
		});
	}
}
