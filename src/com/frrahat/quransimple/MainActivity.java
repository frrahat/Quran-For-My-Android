package com.frrahat.quransimple;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
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

	public static String storageFolderName;
	private InputMethodManager imm;
	private SharedPreferences sharedPrefs;

	private EditText commandText;
	private EditText mainText;
	private ScrollView scrollView;

	private Button prevButton;
	private Button nextButton;

	private InputCommand CUR_INPUT_COMMAND;

	private int MAX_AYAHS_IN_ONE_PAGE;

	private int PagesToSkipOnLongClick;

	private enum InputMode {
		MODE_VERSE, MODE_SEARCH, MODE_RANDOM
	};

	// for loadinfg texts
	public static final int Max_Quran_Texts = 10; 
	//quranText[0] will be always empty
	private QuranText[] allQuranTexts;
	
	private QuranText primaryText;
	private QuranText secondaryText;
	/*
	 * allQuranText is initialized by this code: if (index == 0) isArabic =
	 * true; Log.i("init", "loading file, index: " + index);
	 * allQuranTexts[index] = new QuranText(getBaseContext(),
	 * resourceIDs[index], isArabic); index++, upto 3
	 */

	//resourceIDs[0] never used, like QuranText[0]
	private int[] resourceIDs = { -1,R.raw.quran_uthmani, R.raw.en_yusufali};

	private final int Word_Info_Index = 0;
	private final int Arabic_Text_Index = 1;
	private final int English_Text_Index = 2;
	public static final int Total_Default_Quran_Texts = 3;
	
	private final int Search_Operand_Only_English=0;
	private final int Search_Operand_Pri_And_Secondary=1;
	private final int Search_Operand_All=2;

	private int PRIMARY_TEXT_INDEX;
	private int SECONDARY_TEXT_INDEX = Word_Info_Index;
	private int Search_Operand_Text_Id=Search_Operand_Pri_And_Secondary;
	private int MAX_SEARCH_COUNT;

	// private enum Text{Arabic, EngLish, Bengali};

	// for font faces
	public static final int totalDefaultTypefaces=4;
	private Typeface defaultTypefaces[];
	private static String defaultTypefaceNames[];

	private boolean isInSearchMode;

	private final int REQUEST_SETTINGS = 0;
	private final int REQUEST_SURAH_LIST = 1;
	private final int REQUEST_BookmarksDisplay=2;
	
	
	private final int ActionOnStartNone=0;
	private final int ActionOnStartSurahList=1;
	private final int ActionOnStartRandomAyah=2;
	
	private boolean showARandomAyah;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		storageFolderName="."+getApplicationContext().getPackageName();

		initializeComponents();
	}


	private void initializeComponents() {

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		ActionBar actionBar=getActionBar();
		actionBar.setCustomView(R.layout.actionbar_with_edittext);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);
		
		commandText = (EditText) actionBar.getCustomView().findViewById(R.id.editText_commandText);

		mainText = (EditText) findViewById(R.id.editText_mainText);
		scrollView = (ScrollView) findViewById(R.id.scrollView);

		mainText.setText(" ");// preparing the scrollView(may be)

		commandText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				hideSoftKeyBoard();

				CUR_INPUT_COMMAND = processInput(commandText.getText()
						.toString());

				if (CUR_INPUT_COMMAND != null) {
					printAllAyahs();
				}

				return true;
			}
		});

		commandText.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				commandText.setText("");
				if (isInSearchMode) {
					setSearchModeOff();
				} else {
					setSearchModeOn();
				}

				return true;
			}
		});

		mainText.setFocusable(false);
		mainText.setLongClickable(false);

		prevButton = (Button) findViewById(R.id.button_prev);
		nextButton = (Button) findViewById(R.id.button_next);

		prevButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (CUR_INPUT_COMMAND != null) {
					CUR_INPUT_COMMAND.proceedToPrev();
				} else {
					Toast.makeText(getBaseContext(), "No previous input",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		prevButton.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (CUR_INPUT_COMMAND != null) {
					skipPages(PagesToSkipOnLongClick, -1);
					CUR_INPUT_COMMAND.proceedToPrev();
				} else {
					Toast.makeText(getBaseContext(), "No previous input",
							Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});

		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CUR_INPUT_COMMAND != null) {
					CUR_INPUT_COMMAND.proceedToNext();
				} else {
					Toast.makeText(getBaseContext(), "No previous input",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		nextButton.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (CUR_INPUT_COMMAND != null) {
					skipPages(PagesToSkipOnLongClick, 1);
					CUR_INPUT_COMMAND.proceedToNext();
				} else {
					Toast.makeText(getBaseContext(), "No previous input",
							Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});

		FileItemContainer.initializeFileItems(getApplicationContext());
		//to pass the setTextTypeFace when prefs are updated in updatePrefs()
		FontSettingActivity.setDataChanged(true);
		
		WordInfoLoader.returnToInitialState();
		allQuranTexts = new QuranText[Max_Quran_Texts];
		// loading Quran Text files
		// loadAllFiles();
		loadFonts();
		showARandomAyah=false;
		
		if (sharedPrefs.getBoolean(getString(R.string.key_showTextSelectOnStart), true)) {
			//when no text or invalid text index has been selected
			updatePrimaryTextIndex();//updatefromprefs() calls it
			
			showTextSelectionDialog();
			// updateFromPrefs() is included there,
			// so that it will be called after the dialog diappears
			// else it will be updated while the dialog is visible
			
		} else {
			updateFromPrefs();
			executeOnStartActions();
		}

		// load English text for searching if it's not loaded
		//loadTextFile(English_Text_Index);
		//new TextUpdatingTask(this,"English Text").execute(English_Text_Index);

		// ToBePrintedAyahs=new ArrayList<>();

		setSearchModeOff();

	}

	@Override
	public void onBackPressed() {
		if (sharedPrefs.getBoolean(getString(R.string.key_confirmExit), true))
			tryExitApp();
		else
			finish();
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
		if(isInSearchMode){
			menu.findItem(R.id.action_addBookmark).setVisible(false);
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// from settings
		if (requestCode == REQUEST_SETTINGS) {
			updateFromPrefs();
		}
		// from sura list
		else if (requestCode == REQUEST_SURAH_LIST && resultCode == RESULT_OK
				&& data != null) {
			int suraNum = data.getIntExtra("sura_num", 0);
			if (suraNum > 0) {
				Ayah ayah = new Ayah(suraNum - 1, 0);
				CUR_INPUT_COMMAND = new InputCommand(ayah, 1);
				mainText.setText("");
				printSingleAyah(ayah);
				// send cursor to the end
				commandText.setSelection(commandText.getText().length());
				if(isInSearchMode){
					setSearchModeOff();
				}
			}
		}
		
		else if(requestCode == REQUEST_BookmarksDisplay && resultCode == RESULT_OK
				&& data!=null){
			if(isInSearchMode){
				setSearchModeOff();
			}
			
			int index=data.getIntExtra("index", -1);
			if(index<0){
				CUR_INPUT_COMMAND=getBookmarkInputCommand();
				printAllAyahs();
			}
			else{
				CUR_INPUT_COMMAND = new InputCommand(BookmarkItemContainer.getBookmarkItem(index).getAyah(), 1);
				mainText.setText("");
				printSingleAyah(CUR_INPUT_COMMAND.ayah);
				// send cursor to the end
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

		if (id == R.id.action_showSurahList) {
			Intent intent = new Intent(this, SuraListActivity.class);
			this.startActivityForResult(intent, REQUEST_SURAH_LIST);
			return true;
		} if (id == R.id.action_settings) {
			this.startActivityForResult(SettingsActivity.start(this),
					REQUEST_SETTINGS);
			return true;
		} if (id == R.id.action_addBookmark) {
			if(CUR_INPUT_COMMAND!=null && CUR_INPUT_COMMAND.inputMode==InputMode.MODE_VERSE){
				Intent intent = new Intent(this, BookmarkEditActivity.class);
				Ayah ayah=CUR_INPUT_COMMAND.ayah;
				intent.putExtra("suraIndex", ayah.suraIndex);
				intent.putExtra("ayahIndex", ayah.ayahIndex);
				if(PRIMARY_TEXT_INDEX!=Word_Info_Index){
					intent.putExtra("text", primaryText.getQuranText(ayah));
				}else if(secondaryText!=null){
					intent.putExtra("text", secondaryText.getQuranText(ayah));
				}
				this.startActivity(intent);
			}
			else{
				Toast.makeText(this, "Couldn't get the target ayah. Try in verse mode.",
						Toast.LENGTH_SHORT).show();
			}
			
			return true;
		} if (id == R.id.action_showAllBookmark) {
			Intent intent = new Intent(this, BookmarkDisplayActivity.class);
			this.startActivityForResult(intent, REQUEST_BookmarksDisplay);
			return true;
		} if (id == R.id.action_copyAll) {
			copyAllToClipBoard();
			return true;
		} if (id == R.id.action_additText) {
			Intent intent = new Intent(this, AdditTextActivity.class);
			this.startActivity(intent);
			return true;
		} if (id == R.id.action_showInfo) {
			Intent intent = new Intent(this, InfoActivity.class);
			this.startActivity(intent);
			return true;
		} if (id == R.id.action_showHelp) {
			Intent intent = new Intent(this, HelpActivity.class);
			this.startActivity(intent);
			return true;
		}
		if (id == R.id.action_tryExit) {
			tryExitApp();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private InputCommand processInput(String input) {
		input = input.trim();
		if (isInSearchMode) {

			// input string should be of length greater than 1, else Table[1]
			// AIOB exception
			if (input.length() < 2) {
				Toast.makeText(
						this,
						"Searching text should have to be a word "
								+ "containing more than one letter",
						Toast.LENGTH_LONG).show();

				return null;
			}

			int startSuraIndex = 0;
			int endSuraIndex = 113;

			if (input.charAt(0) == '[' && input.indexOf(']') != -1) {
				int indexOfBracketClosed = input.indexOf(']');
				String rangeText = input.substring(1, indexOfBracketClosed);

				// ',','-',':' and space are acceptable
				rangeText = rangeText.replace(",", " ");
				rangeText = rangeText.replace("-", " ");
				rangeText = rangeText.replace(":", " ");
				String parts[] = rangeText.split(" ");

				try {
					startSuraIndex = Integer.parseInt(parts[0]) - 1;// sura no-1
																	// =suraIndex
					endSuraIndex = Integer.parseInt(parts[1]) - 1;

					startSuraIndex %= 114;
					endSuraIndex %= 114;

					if (input.length() > indexOfBracketClosed + 1) {
						input = input.substring(indexOfBracketClosed + 1);
						// remove a formal space from the query string
						if (input.charAt(0) == ' ')
							input = input.substring(1);
					} else {
						Toast.makeText(getBaseContext(),
								"Target String is null", Toast.LENGTH_SHORT)
								.show();

						return null;
					}

				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
					Toast.makeText(
							getBaseContext(),
							"Couldn't apply ranged search\n"
									+ "See Info for information.",
							Toast.LENGTH_SHORT).show();
				}

			}

			return new InputCommand(startSuraIndex, endSuraIndex, input);
		}

		// else not in search mode
		// all input strings are as time patter i.e. 2:3:4:5
		// ':','/',' ' and '-' are acceptable .
		input = input.replace('/', ' ');
		input = input.replace(':', ' ');
		input = input.replace('-', ' ');
		String parts[] = input.split(" ");
		if (parts.length < 2) {
			Toast.makeText(this, R.string.text_invalid_input, Toast.LENGTH_SHORT).show();
			return null;
		}

		int suraNo, startAyahNo, endAyahNo;

		try {
			suraNo = Integer.parseInt(parts[0]);
			startAyahNo = Integer.parseInt(parts[1]);
			// default endAyah no. is set to start ayah no. by default
			// else printAllAyah() will give error
			endAyahNo = startAyahNo;

			if (parts.length > 2)
				endAyahNo = Integer.parseInt(parts[2]);
		} catch (NumberFormatException e) {
			Toast.makeText(this, R.string.text_invalid_input, Toast.LENGTH_SHORT).show();
			return null;
		}

		// ayah construction validity is checked inside ayah class
		// no need to recheck here
		if (endAyahNo < startAyahNo) {
			Toast.makeText(this, "Error: start should be smaller than end",
					Toast.LENGTH_SHORT).show();
			return null;
		}

		int totalToPrint = endAyahNo - startAyahNo + 1;
		Ayah inputAyah = new Ayah(suraNo - 1, startAyahNo - 1);

		return new InputCommand(inputAyah, totalToPrint);
	}

	private void printSingleAyah(Ayah ayah) {

		mainText.setText("");
		commandText.setText(ayah.toString());
		// send cursor to the end
		commandText.setSelection(commandText.getText().length());

		if (PRIMARY_TEXT_INDEX == Word_Info_Index)// word info
		{
			if (!WordInfoLoader.isLoadingCompleted) {
				mainText.append("All data haven't yet been loaded. Please Wait "
						+ "a little and enter your command again.");
			} else {
				printWordInfo(CUR_INPUT_COMMAND.ayah);
			}

		} else {// other text
			if (primaryText != null)
				mainText.append("[" + ayah.toString() + "] "
						+ primaryText.getQuranText(ayah)
						+ "\n");
			else {
				mainText.append("Couldn't Load Selected Text File\n");
			}
		}

		if (secondaryText != null) {
			mainText.append("\n[" + ayah.toString() + "] "
					+ secondaryText.getQuranText(ayah)
					+ "\n");
		}

		scrollToTop();
	}

	private void printAllAyahs() {
		mainText.setText("");
		if (PRIMARY_TEXT_INDEX == Word_Info_Index)// word info
		{
			if (CUR_INPUT_COMMAND.totalToPrint != 1) {
				Toast.makeText(
						this,
						"Cannot show multiple Ayahs in Word by Word Text Mode. Change the primary text.",
						Toast.LENGTH_SHORT).show();
			} else if (CUR_INPUT_COMMAND.inputMode == InputMode.MODE_VERSE)
				printSingleAyah(CUR_INPUT_COMMAND.ayah);
		} else {// other text
				// single ayah printing mode
				// only one single ayah have to be printed
			if (CUR_INPUT_COMMAND.inputMode == InputMode.MODE_VERSE
					&& CUR_INPUT_COMMAND.totalToPrint == 1) {
				printSingleAyah(CUR_INPUT_COMMAND.ayah);
			} else {

				if (CUR_INPUT_COMMAND.inputMode == InputMode.MODE_VERSE) {
					commandText.setText(CUR_INPUT_COMMAND.ayah.toString()
							+ "-"
							+ Integer.toString(CUR_INPUT_COMMAND.totalToPrint
									+ CUR_INPUT_COMMAND.ayah.ayahIndex));
					// send cursor to the end
					commandText.setSelection(commandText.getText().length());
				}

				runOnUiThread(new AyahPrinterRunnable());
			}
		}
	}

	class AyahPrinterRunnable implements Runnable {

		int printStartIndex;
		int printEndIndex;
		int sizePrintable;
		ArrayList<Ayah> listOfAyahs;

		public AyahPrinterRunnable() {
			sizePrintable = CUR_INPUT_COMMAND.totalToPrint;

			printStartIndex = CUR_INPUT_COMMAND.ayahPrntStartIndex;
			printEndIndex = printStartIndex + MAX_AYAHS_IN_ONE_PAGE - 1;
			if (printEndIndex >= sizePrintable) {
				printEndIndex = sizePrintable - 1;
			}
			listOfAyahs = CUR_INPUT_COMMAND.ToBePrintedAyahs;
		}

		@Override
		public void run() {

			int size = listOfAyahs.size();

			StringBuilder sb = new StringBuilder();

			if (CUR_INPUT_COMMAND.inputMode == InputMode.MODE_SEARCH) {
				sb.append("Searched for: \'" + CUR_INPUT_COMMAND.Query + "\'\n");
				if (size > MAX_SEARCH_COUNT) {
					sb.append("More than " + Integer.toString(MAX_SEARCH_COUNT)
							+ " results found.\n\n");
					sb.append("Showing only " + Integer.toString(sizePrintable)
							+ " result(s) :\n\n");
				} else {
					sb.append("Total of " + Integer.toString(size)
							+ " result(s) found.\n\n");
				}
			}
			
			//for all modes
			
			sb.append("This is page "
					+ Integer.toString(printStartIndex / MAX_AYAHS_IN_ONE_PAGE
							+ 1)
					+ " of "
					+ Integer.toString((sizePrintable - 1)
							/ MAX_AYAHS_IN_ONE_PAGE + 1) + " ["
					+ Integer.toString(printEndIndex - printStartIndex + 1)
					+ " ayah(s)]\n\n");

			for (int i = printStartIndex; i <= printEndIndex; i++) {

				Ayah ayah = listOfAyahs.get(i);

				// printing text is primaryText;
				sb.append("[" + ayah.toString() + "] "
						+ primaryText.getQuranText(ayah)
						+ "\n");

				sb.append("\n\n");

				// Secondary text selected , should not be word by word text
				if (SECONDARY_TEXT_INDEX != Word_Info_Index) {
					sb.append("["
							+ ayah.toString()
							+ "] "
							+ secondaryText
									.getQuranText(ayah) + "\n");

					sb.append("\n\n");
				}
			}

			mainText.append(sb);

			scrollToTop();
		}

	}

	class InputCommand {
		Ayah ayah;
		int totalToPrint;
		InputMode inputMode;
		String Query;

		int ayahPrntStartIndex;

		ArrayList<Ayah> ToBePrintedAyahs;

		//for printing bookmarks
		public InputCommand(ArrayList<Ayah> ayahs){
			this.inputMode = InputMode.MODE_RANDOM;
			ToBePrintedAyahs=ayahs;
			
			totalToPrint = ToBePrintedAyahs.size();
			ayahPrntStartIndex = 0;
		}
		
		public InputCommand(Ayah ayah, int ttlToPrint) {
			this.inputMode = InputMode.MODE_VERSE;

			this.ayah = ayah;

			// ####
			ToBePrintedAyahs = new ArrayList<>();

			while ((ttlToPrint--) > 0 && ayah != null) {
				ToBePrintedAyahs.add(ayah);
				ayah = ayah.getNexTAyah();
			}

			totalToPrint = ToBePrintedAyahs.size();
			ayahPrntStartIndex = 0;
		}

		// for search command
		public InputCommand(int startSuraIndex, int endSuraIndex, String query) {

			this.inputMode = InputMode.MODE_SEARCH;
			this.Query = query;

			// ####
			ToBePrintedAyahs=new ArrayList<>();
			
			if(Search_Operand_Text_Id==Search_Operand_Only_English){
				allQuranTexts[English_Text_Index].search(Query,
						MAX_SEARCH_COUNT, startSuraIndex, endSuraIndex,
						ToBePrintedAyahs);
			}
			else if(Search_Operand_Text_Id==Search_Operand_Pri_And_Secondary){
				primaryText.search(Query,
						MAX_SEARCH_COUNT, startSuraIndex, endSuraIndex,
						ToBePrintedAyahs);
				
				if(ToBePrintedAyahs.size()==0 && secondaryText!=null){
					secondaryText.search(Query,
							MAX_SEARCH_COUNT, startSuraIndex, endSuraIndex,
							ToBePrintedAyahs);
				}
			}
			else if(Search_Operand_Text_Id==Search_Operand_All){
				allQuranTexts[English_Text_Index].search(Query,
						MAX_SEARCH_COUNT, startSuraIndex, endSuraIndex,
						ToBePrintedAyahs);
				
				if(ToBePrintedAyahs.size()==0 && PRIMARY_TEXT_INDEX!=English_Text_Index){
					primaryText.search(Query,
							MAX_SEARCH_COUNT, startSuraIndex, endSuraIndex,
							ToBePrintedAyahs);
				}
				
				if(ToBePrintedAyahs.size()==0 && secondaryText!=null 
						&& SECONDARY_TEXT_INDEX!=English_Text_Index){
					secondaryText.search(Query,
							MAX_SEARCH_COUNT, startSuraIndex, endSuraIndex,
							ToBePrintedAyahs);
				}
			}

			totalToPrint = ToBePrintedAyahs.size();

			if (totalToPrint > MAX_SEARCH_COUNT) {
				totalToPrint = MAX_SEARCH_COUNT;
			}

			ayahPrntStartIndex = 0;
		}

		public void proceedToPrev() {
			//search mode or random mode
			if (ayahPrntStartIndex - MAX_AYAHS_IN_ONE_PAGE >= 0) {
				ayahPrntStartIndex -= MAX_AYAHS_IN_ONE_PAGE;

				printAllAyahs();
				return;
			}

			if (inputMode == InputMode.MODE_VERSE) {
				// Enabling single ayah printing mode
				if (totalToPrint != 1) {
					totalToPrint = 1;
					// clearing unnecessary items
					ToBePrintedAyahs.clear();
				}

				Ayah prevAyah = ayah.getPrevAyah();
				if (prevAyah != null) {
					ayah = prevAyah;

					printSingleAyah(CUR_INPUT_COMMAND.ayah);
				} else
					Toast.makeText(getBaseContext(), "Reached to first ayah.",
							Toast.LENGTH_SHORT).show();
			}
			// else if(inputMode==InputMode.MODE_SEARCH){
			else {
				Toast.makeText(getBaseContext(), "Reached to first page.",
						Toast.LENGTH_SHORT).show();
			}

		}

		public void proceedToNext() {
			//search mode or random mode
			if (ayahPrntStartIndex < totalToPrint - MAX_AYAHS_IN_ONE_PAGE) {
				ayahPrntStartIndex += MAX_AYAHS_IN_ONE_PAGE;

				printAllAyahs();
				return;
			}
			if (inputMode == InputMode.MODE_VERSE) {

				// Enabling single ayah printing mode
				if (totalToPrint != 1) {
					// current ayah is last ayah at ToBePrintedAyahs
					ayah = ToBePrintedAyahs.get(totalToPrint - 1);
					totalToPrint = 1;
					// clearing unnecessary items
					ToBePrintedAyahs.clear();
				}

				Ayah nextAyah = ayah.getNexTAyah();
				if (nextAyah != null) {
					ayah = nextAyah;

					printSingleAyah(CUR_INPUT_COMMAND.ayah);
				} else
					Toast.makeText(getBaseContext(), "Reached to last ayah.",
							Toast.LENGTH_SHORT).show();
			}// else if(inputMode==InputMode.MODE_SEARCH){
			else {
				Toast.makeText(getBaseContext(), "Reached to last page.",
						Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	
	private InputCommand getBookmarkInputCommand(){
		
		if(BookmarkItemContainer.getBookmarkItemsSize()==0){
			return new InputCommand(new Ayah(0,0), 1);
		}
		
		ArrayList<Ayah> ayahs=new ArrayList<>();
		for(int i=0,j=BookmarkItemContainer.getBookmarkItemsSize();i<j;i++){
			ayahs.add(BookmarkItemContainer.getBookmarkItem(i).getAyah());
		}
		
		return new InputCommand(ayahs);
	}

	private void hideSoftKeyBoard() {
		imm.hideSoftInputFromWindow(commandText.getWindowToken(), 0);
	}

	/*
	 * private void loadAllFiles() {
	 * 
	 * //load text files--------------------------------------
	 * Log.i("init","loading files");
	 * 
	 * final Context context=getBaseContext(); //arabic text
	 * 
	 * Runnable quranTextLoader= new Runnable() {
	 * 
	 * @Override public void run() { allQuranTexts[0]=new
	 * QuranText(context,R.raw.quran_uthmani,true); } }; Thread qTextThread=new
	 * Thread(quranTextLoader);
	 * 
	 * //english translation text Runnable englishTextLoader = new Runnable() {
	 * 
	 * @Override public void run() { allQuranTexts[1]=new
	 * QuranText(context,R.raw.en_yusufali,false); } }; Thread eTextThread=new
	 * Thread(englishTextLoader);
	 * 
	 * //bengali translation text Runnable bengaliTextLoader = new Runnable() {
	 * 
	 * @Override public void run() { allQuranTexts[2]=new
	 * QuranText(context,R.raw.bn_bengali,false); } }; Thread bTextThread=new
	 * Thread(bengaliTextLoader);
	 * 
	 * //publishToLog("Loading Files...\n"); qTextThread.start();
	 * eTextThread.start(); bTextThread.start(); //loading Sura Information
	 * //SuraInformation
	 * .loadSuraInformations(getBaseContext(),R.raw.sura_information); }
	 */

	private void loadTextFile(int index) {
		if (index == Word_Info_Index)// wordInfo
		{ // TODO find more memory efficient code
			// arabic text also should have to be loaded;
			Log.i("call", "called load quranText");
			loadTextFile(Arabic_Text_Index);// load arabic text

			if (WordInfoLoader.isInfoLoaded) {
				// already loaded or being loaded
				// do nothing
			} else {
				
				new WordInfoLoader().load(getBaseContext());
					
			}

			return;
		}

		//not loaded yet
		if (allQuranTexts[index] == null) {

			boolean isArabic = false;
			if (index == Arabic_Text_Index)
				isArabic = true;
			Log.d("init", "loading file, index: " + index);

			if (index < Total_Default_Quran_Texts) {
				InputStream in = getResources()
						.openRawResource(resourceIDs[index]);
				allQuranTexts[index] = new QuranText(in, isArabic);

			} else {
				InputStream in = null;
				try {
					in = new FileInputStream(FileItemContainer
							.getFileItem(index-Total_Default_Quran_Texts).getFile());
				} catch (IOException ie) {
					ie.printStackTrace();
				}
				allQuranTexts[index] = new QuranText(in, false);
			}

			Log.i("success", "Text loading complete");
		}	
	}

	

	private void copyAllToClipBoard() {
		ClipData clip = ClipData.newPlainText("text", mainText.getText());
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(clip);

		Toast.makeText(getApplicationContext(), "Full Text Copied",
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * private void lowerVersionAlert(){ Toast.makeText(getApplicationContext(),
	 * "Not available in this version.", Toast.LENGTH_SHORT).show(); }
	 */

	private void loadFonts() {
		
		defaultTypefaces=new Typeface[totalDefaultTypefaces];
		defaultTypefaceNames=new String[totalDefaultTypefaces];
		
		defaultTypefaces[0] = Typeface.createFromAsset(getAssets(),
				"fonts/jaghb_uni_bold.ttf");
		defaultTypefaceNames[0]="jaghbub_uni_bold.ttf";

		defaultTypefaces[1] = Typeface.createFromAsset(
				getAssets(), "fonts/tahoma.ttf");
		defaultTypefaceNames[1]="tahoma.ttf";
		
		defaultTypefaces[2] = Typeface.createFromAsset(
				getAssets(), "fonts/droid_naskh_regular.ttf");
		defaultTypefaceNames[2]="droid_naskh_regular.ttf";
		
		defaultTypefaces[3] = Typeface.createFromAsset(
				getAssets(), "fonts/siyamrupali.ttf");
		defaultTypefaceNames[3]="siyamrupali.ttf";
	}

	private List<WordInformation> getInfoOfWords(Ayah ayah) {
		// index of first ayah of the sura in all ayah sets
		int indexOfFirstAyah = SuraInformation.totalAyahsUpto(ayah.suraIndex);
		int indexOfSelectedAyah = indexOfFirstAyah + ayah.ayahIndex;
		// address in the info list of that selected ayah
		// index of the first word of this ayah
		int indxOfFirstWord = WordInfoLoader.startIndexOfAyah
				.get(indexOfSelectedAyah);
		// index of the first word of the next ayah
		int indxOfFWNextA = WordInfoLoader.startIndexOfAyah
				.get(indexOfSelectedAyah + 1);

		// now listing all the word informations
		List<WordInformation> wordsOfAyah = new ArrayList<>();
		// List<Image>images=new ArrayList<>();

		for (int i = indxOfFirstWord; i < indxOfFWNextA; i++) {
			wordsOfAyah.add(WordInfoLoader.infoWords.get(i));
			// images.add(ImageLoader.getImageFromFile(i));
		}

		return wordsOfAyah;

	}

	private void printWordInfo(Ayah ayah) {
		String arabicAyahTextWords[] = allQuranTexts[Arabic_Text_Index].getQuranText(ayah)
				.split(" ");
		List<WordInformation> wordsOfAyah = getInfoOfWords(ayah);
		int wordIndexToDisplay = 0;
		int wordInfoIndexToDisplay = 0;

		String text = "[" + ayah.toString() + "]\n\n";
		while (wordIndexToDisplay < arabicAyahTextWords.length) {

			// arabic text
			text += arabicAyahTextWords[wordIndexToDisplay];
			// waqf checking
			int k = arabicAyahTextWords[wordIndexToDisplay].charAt(0);

			if ((k >= '\u0610' && k <= '\u0615')
					|| (k >= '\u06D6' && k <= '\u06ED')) {
				wordIndexToDisplay++;
				text += "\n--[waqf]--\n\n";
				continue;
			}
			// waqf checked

			// transliteration and meaning
			if (wordInfoIndexToDisplay < wordsOfAyah.size()) {
				text += "\n["
						+ wordsOfAyah.get(wordInfoIndexToDisplay).transliteration
						+ "]" + "\n"
						+ wordsOfAyah.get(wordInfoIndexToDisplay).meaning;
			} else {
				text += "\n(missing word meaning)";// not found
			}

			text += "\n\n";

			wordInfoIndexToDisplay++;
			wordIndexToDisplay++;
		}

		text += "\n==========\n";
		mainText.append(text);
	}

	private void scrollToTop() {
		scrollView.post(new Runnable() {
			public void run() {
				scrollView.fullScroll(View.FOCUS_UP);
			}
		});
	}

	private void setSearchModeOn() {
		if(PRIMARY_TEXT_INDEX==Word_Info_Index){
			Toast.makeText(this, "Searching Cannot Be Done In Word By Word Text Mode."
					+ "\nChange Primary Text.", Toast.LENGTH_LONG).show();
			return;
			
		}
		if((Search_Operand_Text_Id==Search_Operand_Only_English
				|| Search_Operand_Text_Id==Search_Operand_All)
				&& allQuranTexts[English_Text_Index]==null){
			new TextUpdatingTask(this,"English Text").execute(English_Text_Index);
			//update text info, that is, search operand text is allQText[english text index]
		}
		isInSearchMode = true;
		commandText.setInputType(InputType.TYPE_CLASS_TEXT);
		commandText.setHint(R.string.hint_commandTextToSearch);
		
		invalidateOptionsMenu();
	}

	private void setSearchModeOff() {
		isInSearchMode = false;
		commandText.setInputType(InputType.TYPE_CLASS_DATETIME);
		commandText.setHint(R.string.hint_commandText);
		
		invalidateOptionsMenu();
	}


	private void showTextSelectionDialog() {
		AlertDialog.Builder textSelectorBuilder = new AlertDialog.Builder(this);

		PRIMARY_TEXT_INDEX = Integer.parseInt(sharedPrefs.getString(
				getString(R.string.key_primary_text_selection), Integer.toString(English_Text_Index)));

		final SharedPreferences.Editor editor = sharedPrefs.edit();
		final int newSelectedIndex[] = { PRIMARY_TEXT_INDEX };
		// one int element array to assign value from inner class

		textSelectorBuilder.setTitle(R.string.title_textSelection);
		
		String defaultItems[]=getResources().getStringArray(R.array.array_textSelection);
		int defaultItemSize= defaultItems.length;
		
		/*defaultItemSize= defaultItemSize <= Total_Default_Quran_Texts?
					defaultItemSize :Total_Default_Quran_Texts;*/
		
		int additinalItemSize=FileItemContainer.getFileItemsSize();
		
		String allItems[]=new String[defaultItemSize+
		                             additinalItemSize];
		
		for(int i=0;i<defaultItemSize;i++){
			allItems[i]=defaultItems[i];
		}
		for(int i=defaultItemSize,j=0;j<additinalItemSize;i++,j++){
			allItems[i]=FileItemContainer.getFileItem(j).getFileAliasName();
		}
		
		textSelectorBuilder.setSingleChoiceItems(allItems,
				PRIMARY_TEXT_INDEX, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int whichIndex) {
						newSelectedIndex[0] = whichIndex;
					}
				});

		textSelectorBuilder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (newSelectedIndex[0] != PRIMARY_TEXT_INDEX) {
							editor.putString(getString(R.string.key_primary_text_selection),
									Integer.toString(newSelectedIndex[0]));
							editor.commit();
						}
					}
				});
		textSelectorBuilder.setNegativeButton("Don't Show This Again",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						editor.putBoolean(getString(R.string.key_showTextSelectOnStart), false);
						if (newSelectedIndex[0] != PRIMARY_TEXT_INDEX) {
							editor.putString(getString(R.string.key_primary_text_selection),
									Integer.toString(newSelectedIndex[0]));
						}

						editor.commit();
					}
				});
		
		
		AlertDialog dialog=textSelectorBuilder.create();
		
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				updateFromPrefs();
				executeOnStartActions();
			}
		});
		
		dialog.show();
	}

	private void skipPages(int pages, int direction) {

		int skippedAyah = (pages - 1) * MAX_AYAHS_IN_ONE_PAGE;
		// as proceed to next or proceed to prev is called
		// one page is automatically skipped, so (page-1)

		int startIndex;

		if (direction > 0)
			startIndex = CUR_INPUT_COMMAND.ayahPrntStartIndex + skippedAyah;

		else
			startIndex = CUR_INPUT_COMMAND.ayahPrntStartIndex - skippedAyah;

		if (startIndex < 0 || startIndex >= CUR_INPUT_COMMAND.totalToPrint) {
			Toast.makeText(this, "Failed to Skip pages", Toast.LENGTH_SHORT)
					.show();
		} else {
			CUR_INPUT_COMMAND.ayahPrntStartIndex = startIndex;
		}
	}

	/*
	 * private void showScndryTxtSltctnDialog() { AlertDialog.Builder builder =
	 * new AlertDialog.Builder(this);
	 * 
	 * final int selectedIndex[] = { -1 }; // one int element array to assign
	 * value from inner class
	 * 
	 * builder.setTitle(R.string.title_scdndryTxtSelection);
	 * 
	 * final String
	 * items[]=getResources().getStringArray(R.array.array_textSelection);
	 * items[PRIMARY_TEXT_INDEX]="Primary Text"; 
	 * of word by word items[items.length-1]="No Secondary Text";
	 * 
	 * if(SECONDARY_TEXT_INDEX==-1){ SECONDARY_TEXT_INDEX=items.length-1; }
	 * 
	 * builder.setSingleChoiceItems(items, SECONDARY_TEXT_INDEX, new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int whichIndex) {
	 * selectedIndex[0] = whichIndex; } });
	 * 
	 * builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
	 * public void onClick(DialogInterface dialog, int id) { //not equal to
	 * primary text index if (selectedIndex[0] != PRIMARY_TEXT_INDEX) {
	 * SECONDARY_TEXT_INDEX=selectedIndex[0]; //if "No Secondary Text"
	 * if(SECONDARY_TEXT_INDEX==items.length-1){ SECONDARY_TEXT_INDEX=-1; }else{
	 * loadTextFile(SECONDARY_TEXT_INDEX); } }else{
	 * Toast.makeText(getBaseContext(),
	 * "Primary Text cannot be Selected as Secondary",
	 * Toast.LENGTH_LONG).show();
	 * 
	 * SECONDARY_TEXT_INDEX=-1; } } }); builder.setNegativeButton("Cancel", new
	 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
	 * dialog, int id) { //do nothing } });
	 * 
	 * builder.create().show(); }
	 */
	
	class TextUpdatingTask extends AsyncTask<Integer, Void, Void>{

		ProgressDialog progressDialog;
		public TextUpdatingTask(Context context, String textName) {
			Log.d("new task textName",textName);
			progressDialog=new ProgressDialog(context);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage("Loading "+textName +"...");
			progressDialog.setCancelable(false);

			//progressDialog.setCancelable(false);
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}
		@Override
		protected Void doInBackground(Integer... indices) {
			Log.d("indices[0]", Integer.toString(indices[0]));
			loadTextFile(indices[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			updateTextInfo();
			
			if(primaryText!=null && showARandomAyah){
				CUR_INPUT_COMMAND = new InputCommand(getARandomAyah(), 1);
				mainText.setText("");
				printSingleAyah(CUR_INPUT_COMMAND.ayah);
				// send cursor to the end
				commandText.setSelection(commandText.getText().length());
				showARandomAyah=false;
			}
		}
	}
	
	// TODO, this is a marker for update pref function
	private void updateFromPrefs() {
		mainText.setTextSize(Float.parseFloat(sharedPrefs.getString(
				getString(R.string.key_fontSize), "15")));

		int previousPrimaryIndex = PRIMARY_TEXT_INDEX;

		updatePrimaryTextIndex();

		if (previousPrimaryIndex == Word_Info_Index
				&& previousPrimaryIndex != PRIMARY_TEXT_INDEX) {
			// clearing up a huge memory (word infos)
			WordInfoLoader.returnToInitialState();
			System.gc();
		}
		
		updateSecondaryTextIndex();

		MAX_SEARCH_COUNT = Integer.parseInt(sharedPrefs.getString(
				getString(R.string.key_maxSearchCount), "5000"));

		MAX_AYAHS_IN_ONE_PAGE = Integer.parseInt(sharedPrefs.getString(
				getString(R.string.key_maxAyahInPage), "10"));

		PagesToSkipOnLongClick = Integer.parseInt(sharedPrefs.getString(
				getString(R.string.key_pagesToSkipOnClick), "10"));

		//loadTextFile(PRIMARY_TEXT_INDEX);
		//updateTextInfo()
		new TextUpdatingTask(this,"Primary Text").execute(PRIMARY_TEXT_INDEX);
		
		updateSearchOperandTextIndex();

		setTextTypeface();

		// set gravity
		if (PRIMARY_TEXT_INDEX == Word_Info_Index)// word info
		{
			mainText.setGravity(Gravity.CENTER);
		} else {
			mainText.setGravity(Gravity.TOP);
		}

	}
	
	private void updatePrimaryTextIndex(){
		PRIMARY_TEXT_INDEX = Integer.parseInt(sharedPrefs.getString(
				getString(R.string.key_primary_text_selection), Integer.toString(English_Text_Index)));
		
		//if index is greater than available indices
		if(PRIMARY_TEXT_INDEX>
			(Total_Default_Quran_Texts+FileItemContainer.getFileItemsSize()-1)){
			
			storePrimaryTextToDefault();
		}
		
		
	}
	private void updateSecondaryTextIndex(){
		// default value is "No Secondary Text" that is 0 or -1
		SECONDARY_TEXT_INDEX = Integer.parseInt(sharedPrefs.getString(
				getString(R.string.key_secondary_text_selection), Integer.toString(Word_Info_Index)));
		
		if(SECONDARY_TEXT_INDEX==Word_Info_Index)//no secondary text
		{
			return;
		}
		if (SECONDARY_TEXT_INDEX == PRIMARY_TEXT_INDEX) {
			Toast.makeText(
					this,
					"Primary Text and Secondary Text are same."
							+ "\nSecondary Text Disabled.", Toast.LENGTH_LONG)
					.show();

			storeSecondaryTextToDefault();

		} else if (SECONDARY_TEXT_INDEX > Total_Default_Quran_Texts
				+ FileItemContainer.getFileItemsSize() - 1) {
			
			storeSecondaryTextToDefault();
		} else if(allQuranTexts[SECONDARY_TEXT_INDEX]==null){//No secondary Text
			// loadTextFile(SECONDARY_TEXT_INDEX);
			//updateTextInfo()
			new TextUpdatingTask(this, "Secondary Text")
					.execute(SECONDARY_TEXT_INDEX);
		}
		
		
	}
	
	private void updateSearchOperandTextIndex(){
		Search_Operand_Text_Id = Integer.parseInt(sharedPrefs.getString(
				getString(R.string.key_searchOperandTextIndex), Integer.toString(Search_Operand_All)));
	}
	/*
	 * strore secondary text index as Word Info index
	 */
	private void storeSecondaryTextToDefault(){
		SECONDARY_TEXT_INDEX=Word_Info_Index;
		// save to pref
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(getString(R.string.key_secondary_text_selection),
				Integer.toString(Word_Info_Index));// No secondary Text
		editor.commit();
	}
	
	private void storePrimaryTextToDefault(){
		PRIMARY_TEXT_INDEX=English_Text_Index;
		
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(getString(R.string.key_primary_text_selection),
				Integer.toString(English_Text_Index));
		editor.commit();
	}
	
	private void updateTextInfo(){
		primaryText = allQuranTexts[PRIMARY_TEXT_INDEX];
		if (SECONDARY_TEXT_INDEX != Word_Info_Index)
			secondaryText = allQuranTexts[SECONDARY_TEXT_INDEX];
		else
			secondaryText = null;
	}
	
	//set font
	private void setTextTypeface(){
		
		//no change has been made
		if(!FontSettingActivity.isDataChanged()){
			return;
		}
		
		if(FontItemContainer.getAllFontFiles()==null){
			//Initialize from predefined storage location
			FontItemContainer.initializeFontFiles(MainActivity.this);
		}
		/*if (PRIMARY_TEXT_INDEX == Word_Info_Index
				|| PRIMARY_TEXT_INDEX == Arabic_Text_Index) {
			mainText.setTypeface(defaultTypefaces[1]);
		} else {
			mainText.setTypeface(Typeface.DEFAULT);
		}*/
		
		if(FontItemContainer.getSelectedFontName(PRIMARY_TEXT_INDEX).equals(
				FontItemContainer.Default_Font_File_Name))
		{
			mainText.setTypeface(Typeface.DEFAULT);
			return;
		}
		
		int fontFileIndex=FontItemContainer.getFontFileIndexInAsset(PRIMARY_TEXT_INDEX);
		
		if(fontFileIndex!=-1){//file found in asset
			mainText.setTypeface(defaultTypefaces[fontFileIndex]);
		}
		else{//file not found in asset
			fontFileIndex=FontItemContainer.getFontFileIndexInFiles(PRIMARY_TEXT_INDEX);
			
			if(fontFileIndex!=-1){//file found in storage 
				Typeface typeface=Typeface.createFromFile(FontItemContainer.getFontFile(fontFileIndex));
				mainText.setTypeface(typeface);
			}
			else{//file not found in storage
				mainText.setTypeface(Typeface.DEFAULT);
				FontItemContainer.setSelectedFontName(PRIMARY_TEXT_INDEX,
						FontItemContainer.Default_Font_File_Name);
			}
		}		
	}

	public static String getDefaultTypefaceName(int index){
		return defaultTypefaceNames[index];
	}
	
	private void executeOnStartActions(){
		int actionIndexOnStart=Integer.parseInt(sharedPrefs.getString(getString(R.string.key_prefActionOnStart),
				Integer.toString(ActionOnStartNone)));
		
		if(actionIndexOnStart==ActionOnStartNone){
			return;
		}
		else if(actionIndexOnStart==ActionOnStartSurahList){
			Intent intent = new Intent(MainActivity.this, SuraListActivity.class);
			startActivityForResult(intent, REQUEST_SURAH_LIST);
		}
		else if(actionIndexOnStart==ActionOnStartRandomAyah){
			showARandomAyah=true;
		}
	}
	
	private Ayah getARandomAyah(){
		Random generator=new Random();
		int suraIndex=generator.nextInt(114);
		int ayahIndex=generator.nextInt(SuraInformation.totalAyas[suraIndex]);
		
		return new Ayah(suraIndex, ayahIndex);
	}
}
