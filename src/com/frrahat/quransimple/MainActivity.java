package com.frrahat.quransimple;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
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
 * @author Rahat started: February 21, 2015 at 7:04:18 PM
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

	private QuranText SearchOperandText;
	private int MAX_AYAHS_IN_ONE_PAGE;

	private int PagesToSkipOnLongClick;

	private enum InputMode {
		MODE_VERSE, MODE_SEARCH, MODE_RANDOM
	};

	// for loadinfg texts
	public static final int Total_Default_Quran_Texts = 3;
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

	private int PRIMARY_TEXT_INDEX;
	private int SECONDARY_TEXT_INDEX = Word_Info_Index;
	private int MAX_SEARCH_COUNT;

	// private enum Text{Arabic, EngLish, Bengali};

	// for font faces
	//Typeface bengaliTypeface;
	Typeface translitTypeface;// for transliterartion font,not much clear but
	// supports all charecter
	Typeface translitTypeface2;// TODO can be removed if not used, clear but
								// doesn't support all

	private boolean isInSearchMode;

	private final int REQUEST_SETTINGS = 0;
	private final int REQUEST_SURAH_LIST = 1;
	private final int REQUEST_BookmarksDisplay=2;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Full Screen
		if (android.os.Build.VERSION.SDK_INT >= 14) {
		if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} 
		}
		setContentView(R.layout.activity_main);
		
		storageFolderName="."+getApplicationContext().getPackageName();

		initializeComponents();
	}

	/*
	 * @Override protected void onResume() { super.onResume(); //TODO change
	 * this Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show(); }
	 */

	private void initializeComponents() {

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		commandText = (EditText) findViewById(R.id.editText_commandText);

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
		WordInfoLoader.returnToInitialState();
		allQuranTexts = new QuranText[Max_Quran_Texts];
		// loading Quran Text files
		// loadAllFiles();
		loadFonts();

		if (sharedPrefs.getBoolean("pref_showTextSelection", true)) {
			//when no text or invalid text index has been selected
			updatePrimaryText();//updatefromprefs() calls it
			
			showTextSelectionDialog();
			// updateFromPrefs() is included there,
			// so that it will be called after the dialog diappears
			// else it will be updated while the dialog is visible
			
		} else {
			updateFromPrefs();
		}

		// load English text for searching if it's not loaded
		//loadTextFile(English_Text_Index);
		new TextUpdatingTask(this,"English Text").execute(English_Text_Index);

		// ToBePrintedAyahs=new ArrayList<>();

		setSearchModeOff();

	}

	@Override
	public void onBackPressed() {
		if (sharedPrefs.getBoolean("pref_confirmExit", true))
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
			}
		}
		
		else if(requestCode == REQUEST_BookmarksDisplay && resultCode == RESULT_OK){
			
			setSearchModeOff();
			CUR_INPUT_COMMAND=getBookmarkInputCommand();
			printAllAyahs();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_showList) {
			Intent intent = new Intent(this, SuraListActivity.class);
			this.startActivityForResult(intent, REQUEST_SURAH_LIST);
			return true;
		} if (id == R.id.action_settings) {
			this.startActivityForResult(SettingsActivity.start(this),
					REQUEST_SETTINGS);
			return true;
		} if (id == R.id.action_addToBookmark) {
			if(CUR_INPUT_COMMAND!=null && CUR_INPUT_COMMAND.inputMode==InputMode.MODE_VERSE){
				Intent intent = new Intent(this, BookmarkEditActivity.class);
				Ayah ayah=CUR_INPUT_COMMAND.ayah;
				intent.putExtra("suraIndex", ayah.suraIndex);
				intent.putExtra("ayahIndex", ayah.ayahIndex);
				intent.putExtra("text", primaryText.getQuranText(ayah));
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
			Toast.makeText(this, "invalid input", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(this, "invalid input", Toast.LENGTH_SHORT).show();
			return null;
		}

		// ayah construction validity is checked inside ayah class
		// no need to recheck here
		if (endAyahNo < startAyahNo) {
			Toast.makeText(this, "Error: start is smaller than end",
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
						"Can show only a single Ayah\n in Word by Word Text Mode",
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

			if (SearchOperandText == null) {

				if (allQuranTexts[English_Text_Index] == null) {
					Toast.makeText(
							getBaseContext(),
							"Search Operand file can't be "
									+ "loaded. Searching failed.",
							Toast.LENGTH_SHORT).show();

					return;
				} else {
					SearchOperandText = allQuranTexts[English_Text_Index];
				}
			}
			// ####
			ToBePrintedAyahs = SearchOperandText.search(Query,
					MAX_SEARCH_COUNT, startSuraIndex, endSuraIndex);

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
		//bengaliTypeface = Typeface.createFromAsset(getAssets(),
		//		"fonts/solaimanlipi.ttf");

		translitTypeface = Typeface.createFromAsset(getAssets(),
				"fonts/jaghb_uni_bold.ttf");

		translitTypeface2 = Typeface.createFromAsset(// TODO remove if not used
				getAssets(), "fonts/tahoma.ttf");
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
		isInSearchMode = true;
		commandText.setInputType(InputType.TYPE_CLASS_TEXT);
		commandText.setHint(R.string.hint_commandTextToSearch);
	}

	private void setSearchModeOff() {
		isInSearchMode = false;
		commandText.setInputType(InputType.TYPE_CLASS_DATETIME);
		commandText.setHint(R.string.hint_commandText);
	}

	private void showTextSelectionDialog() {
		AlertDialog.Builder textSelectorBuilder = new AlertDialog.Builder(this);

		PRIMARY_TEXT_INDEX = Integer.parseInt(sharedPrefs.getString(
				"pref_text_selection", Integer.toString(English_Text_Index)));

		final SharedPreferences.Editor editor = sharedPrefs.edit();
		final int newSelectedIndex[] = { PRIMARY_TEXT_INDEX };
		// one int element array to assign value from inner class

		textSelectorBuilder.setTitle(R.string.title_textSelection);
		
		String defaultItems[]=getResources().getStringArray(R.array.array_textSelection);
		int defaultItemSize= defaultItems.length;
		
		defaultItemSize= defaultItemSize <= Total_Default_Quran_Texts?
					defaultItemSize :Total_Default_Quran_Texts;
		
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
							editor.putString("pref_text_selection",
									Integer.toString(newSelectedIndex[0]));
							editor.commit();
						}

						// update now
						updateFromPrefs();
					}
				});
		textSelectorBuilder.setNegativeButton("Don't Show This Again",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						editor.putBoolean("pref_showTextSelection", false);
						if (newSelectedIndex[0] != PRIMARY_TEXT_INDEX) {
							editor.putString("pref_text_selection",
									Integer.toString(newSelectedIndex[0]));
						}

						editor.commit();

						// update now
						updateFromPrefs();
					}
				});

		textSelectorBuilder.create().show();
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
	 * items[PRIMARY_TEXT_INDEX]="Primary Text"; //TODO check if last index is
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
		}
	}
	
	// TODO, this is a marker for update pref function
	private void updateFromPrefs() {
		mainText.setTextSize(Float.parseFloat(sharedPrefs.getString(
				"pref_font_size", "15")));

		int previousPrimaryIndex = PRIMARY_TEXT_INDEX;

		updatePrimaryText();

		if (previousPrimaryIndex == Word_Info_Index
				&& previousPrimaryIndex != PRIMARY_TEXT_INDEX) {
			// clearing up a huge memory (word infos)
			WordInfoLoader.returnToInitialState();
			System.gc();
		}
		
		updateSecondaryText();

		MAX_SEARCH_COUNT = Integer.parseInt(sharedPrefs.getString(
				"pref_maxSearchCount", "5000"));

		MAX_AYAHS_IN_ONE_PAGE = Integer.parseInt(sharedPrefs.getString(
				"pref_maxAyahInPage", "10"));

		PagesToSkipOnLongClick = Integer.parseInt(sharedPrefs.getString(
				"pref_pgsToSkpOnLClick", "10"));

		//loadTextFile(PRIMARY_TEXT_INDEX);
		//updateTextInfo()
		new TextUpdatingTask(this,"Primary Text").execute(PRIMARY_TEXT_INDEX);

		/*// set Type face
		if (PRIMARY_TEXT_INDEX == Bengali_Text_Index) {// bengali
			mainText.setTypeface(bengaliTypeface);
		} else*/ if (PRIMARY_TEXT_INDEX == Word_Info_Index
				|| PRIMARY_TEXT_INDEX == Arabic_Text_Index) {
			mainText.setTypeface(translitTypeface2);// TODO change or remove "2"
		} else {
			mainText.setTypeface(Typeface.DEFAULT);
			// TODO set appropriate typeface to accept secondary text
		}

		// set gravity
		if (PRIMARY_TEXT_INDEX == Word_Info_Index)// word info
		{
			mainText.setGravity(Gravity.CENTER);
		} else {
			mainText.setGravity(Gravity.TOP);
		}

	}
	
	private void updatePrimaryText(){
		PRIMARY_TEXT_INDEX = Integer.parseInt(sharedPrefs.getString(
				"pref_text_selection", Integer.toString(English_Text_Index)));
		
		//if index is greater than available indices
		if(PRIMARY_TEXT_INDEX>
			(Total_Default_Quran_Texts+FileItemContainer.getFileItemsSize()-1)){
			
			storePrimaryTextToDefault();
		}
		
		
	}
	private void updateSecondaryText(){
		// default value is "No Secondary Text" that is 0 or -1
		SECONDARY_TEXT_INDEX = Integer.parseInt(sharedPrefs.getString(
				"pref_scndryTxtIndex", Integer.toString(Word_Info_Index)));
		
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
		} else{//No secondary Text
			// loadTextFile(SECONDARY_TEXT_INDEX);
			//updateTextInfo()
			new TextUpdatingTask(this, "Secondary Text")
					.execute(SECONDARY_TEXT_INDEX);
		}
		
		
	}
	/*
	 * strore secondary text index as Word Info index
	 */
	private void storeSecondaryTextToDefault(){
		SECONDARY_TEXT_INDEX=Word_Info_Index;
		// save to pref
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("pref_scndryTxtIndex",
				Integer.toString(Word_Info_Index));// No secondary Text
		editor.commit();
	}
	
	private void storePrimaryTextToDefault(){
		PRIMARY_TEXT_INDEX=English_Text_Index;
		
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("pref_text_selection",
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
}
