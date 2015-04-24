package com.frrahat.quransimple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

/**
 * @author Rahat
 * date : 17-04-15
 * Activity for displaying Additional Text
 */
public class AdditTextActivity extends Activity {

	private ListView additTextListView;
	
	Button addTextFileButton;
	
	private BaseAdapter adapter;
	
	private FileChooserDialog fileChooserDialog;
	
	private final int REQUEST_FILE_EDIT=0;
	private TextView noOfFilesTextView;
	private boolean dataChanged;
	
	private int totalAyahs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addit_text);
		
		noOfFilesTextView=(TextView)
				findViewById(R.id.textView_noOfAddedFile);
		
		if(FileItemContainer.getFileItems()==null){
			noOfFilesTextView.setText("ERROR! Failed to "
					+ "load additional file(s).");
			return;
		}		
		
		totalAyahs=SuraInformation.totalAyahsUpto(114);
		dataChanged=false;
        updateNoOfFilesTextView();
		
        additTextListView=(ListView) findViewById(R.id.listView_additText);
		
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
				
				FileItem fileItem=FileItemContainer.getFileItems().get(position);
				
				textView1.setText(Integer.toString(position+1)+"."+fileItem.getFileAliasName());
				textView2.setText("File : "+fileItem.getFileName());
				
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return FileItemContainer.getFileItem(position); 
			}
			
			@Override
			public int getCount() {
				return FileItemContainer.getFileItemsSize();
			}
		};
		
		additTextListView.setAdapter(adapter);
		
		additTextListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent intent=new Intent(AdditTextActivity.this,FileItemEditActivity.class);
				intent.putExtra("itemIndex", position);
				startActivityForResult(intent, REQUEST_FILE_EDIT);
			}
			
		}); 
		
		addTextFileButton=(Button) findViewById(R.id.button_addAdditText);
		
		addTextFileButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				
				if(MainActivity.Total_Default_Quran_Texts+ FileItemContainer.getFileItemsSize()==
						MainActivity.Max_Quran_Texts){
					
					Toast.makeText(getBaseContext(),
							"No More Files Can Be Added. "
							+ "You Have To Remove Some Files From The List First.",
							Toast.LENGTH_LONG).show();
					
					return;
				}
				
				fileChooserDialog=new FileChooserDialog();
				fileChooserDialog.setOnFileChosenListener(
						new FileChooserDialog.OnFileChosenListener() {
					
					@Override
					public void onFileChosen(File file) {
						new FileAdderTask().execute(file);
						//Toast.makeText(getBaseContext(), file.getName(), Toast.LENGTH_LONG).show();
						//addNewFile(file);
					}
				});
				
				fileChooserDialog.show(getFragmentManager(), "fileChooser");
				
				//Toast.makeText(getBaseContext(), "add clicked", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void updateNoOfFilesTextView() {
		
		if(FileItemContainer.getFileItems().size()!=0){
        	noOfFilesTextView.setText("Previously Added "+
        Integer.toString(FileItemContainer.getFileItems().size())+" file(s)");
        }else{
        	noOfFilesTextView.setText("No file added. Click \'"+getString(R.string.txt_but_addAdditText)
        			+"\' and select your downloaded/previously stored file.\nIf the file is not in your local storage"
        			+ " you may download it from here: http://zekr.org/resources.html#translation"
        			+ "\nDisclaimer: Their authenticity and/or accuracy is not guaranteed. Please use them at your own risk.");
        	Linkify.addLinks(noOfFilesTextView, Linkify.WEB_URLS);
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode==RESULT_OK){
			
			/*if(requestCode==REQUEST_FILE_EDIT){		
				
			}*/
			//name edit or remove happened
			dataChanged=true;

			adapter.notifyDataSetChanged();
			
			updateNoOfFilesTextView();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(dataChanged){
			Thread dataSaver = new Thread(new Runnable() {
				
				@Override
				public void run() {
					if(! FileItemContainer.saveDataToFile()){
						
						Toast.makeText(getApplicationContext(),
								"Unfortunately failed to save data", 
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			dataSaver.start();
			dataChanged=false;//not important
		}
		super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.addit_text, menu);
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
	
	private InputStream getInputStream(File file){
		InputStream inStream=null;
		try{
			inStream=new FileInputStream(file);
		}catch(IOException ie){
			//very low chance of this exception
		}
		
		return inStream;
	}
	private boolean addNewFile(final File file){

		if(file.getName().endsWith(".txt")){
			
			return writeToFileFrom(file.getName(), getInputStream(file));
		}
		else if(file.getName().endsWith(".zip")){
			return addFilesFromZipFile(file);
		}
		else return false;
		
	}
	
	
	private boolean writeToFileFrom(String writingFileName, InputStream fromInStream)
	{
		boolean success=false;
		File writingFile=new File(FileItemContainer.getTextStorageDir(),writingFileName);
		
        try {
        	BufferedReader br=new BufferedReader(new InputStreamReader(fromInStream));
        	
            FileOutputStream f = new FileOutputStream(writingFile);
            PrintWriter pw = new PrintWriter(f);
            
            String text=null;
            int lineCount=0;
            while((text=br.readLine()) != null)
            {
                pw.append(text).append("\n");
                lineCount++;
            }
            
            if(lineCount>=totalAyahs){//validity check
            
	            pw.flush();           
	            pw.close();
	            
	            success=true;
	            //Toast.makeText(this,"Saved Successfully :\n"+toFile.getName(), Toast.LENGTH_LONG).show();
	            addToFileItems(writingFile);
	            f.close();
            }else{
            	if(writingFile.exists())
            		writingFile.delete();
            }

            fromInStream.close();
            br.close();
            
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        return success;
	}
	
	private boolean addFilesFromZipFile(File file){
		boolean atLeastOnefileWritten=false;
		try {
	        @SuppressWarnings("resource")
			ZipFile zipFile = new ZipFile(file);
	        for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
	            ZipEntry entry = (ZipEntry) e.nextElement();

	            if (!entry.isDirectory() && entry.getName().endsWith(".txt")){
	            	//TODO do something
	            	//read it and check if it contains total ayahs of 7...
	            	//no checking for replacements of same files
	            	InputStream inStream=zipFile.getInputStream(entry);
	            	if(writeToFileFrom(entry.getName(),inStream)){
	            		if(!atLeastOnefileWritten){
	            			atLeastOnefileWritten=true;
	            		}
	            	}

	            }
	        }
	    } catch (ZipException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
		return atLeastOnefileWritten;
	}
	
	
	class FileAdderTask extends AsyncTask<File, Void, Boolean>{
		
		private ProgressDialog progressDialog;
		
		public FileAdderTask(){
			//initialize progress dialog
			progressDialog=new ProgressDialog(AdditTextActivity.this);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage("Checking validity...");
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		};
		@Override
		protected Boolean doInBackground(File... files) {
			return addNewFile(files[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			if(result){
				Toast.makeText(getBaseContext(),
						"File Added Successfully",
						Toast.LENGTH_SHORT).show();
				//need to save data
				dataChanged=true;
				
				adapter.notifyDataSetChanged();
				updateNoOfFilesTextView();
				
			}else{
				Toast.makeText(getBaseContext(),
						"Invalid or Corrupted File",
						Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	private void addToFileItems(File writingFile){
		//replacement check
		for(int i=0,size=FileItemContainer.getFileItemsSize();i<size;i++){
			if(FileItemContainer.getFileItem(i).getFile().equals(writingFile))
				return;
		}
		
		FileItemContainer.getFileItems().add(new FileItem(writingFile));
	}
}
