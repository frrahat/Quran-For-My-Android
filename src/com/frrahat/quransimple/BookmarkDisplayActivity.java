package com.frrahat.quransimple;

import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BookmarkDisplayActivity extends Activity {

	private BaseAdapter adapter;
	private ListView bookmarksListView;
	private Button showAllBookmarksButton;
	
	private final int Request_bookmarkEdit=0;
	private static boolean dataChanged;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmark_display);

		bookmarksListView = (ListView) findViewById(R.id.listView_bookmarkDisplay);

		adapter = new BaseAdapter() {

			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			@SuppressLint("InflateParams")
			@Override
			public View getView(int position, View view, ViewGroup parent) {
				if (view == null) {
					view = layoutInflater.inflate(R.layout.surah_list_item,
							null);
				}
				TextView textView1 = (TextView) view.findViewById(R.id.text1);
				TextView textView2 = (TextView) view.findViewById(R.id.text2);

				BookmarkItem item = BookmarkItemContainer
						.getBookmarkItem(position);

				textView1.setText(Integer.toString(position + 1) + ")  "
						+ item.getAyah().toString());
				textView2.setText(item.getComment());

				return view;
			}

			@Override
			public long getItemId(int position) {
				return position + 1;
			}

			@Override
			public Object getItem(int position) {
				return BookmarkItemContainer.getBookmarkItem(position);
			}

			@Override
			public int getCount() {
				return BookmarkItemContainer.getBookmarkItemsSize();
			}
		};

		bookmarksListView.setAdapter(adapter);
		bookmarksListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Intent intent=new Intent(BookmarkDisplayActivity.this, BookmarkEditActivity.class);
				intent.putExtra("index",position);
				startActivityForResult(intent, Request_bookmarkEdit);
			}
			
		});
		
		
		
		if(BookmarkItemContainer.getBookmarkItems()==null){
			new BookmarksloadingTask(this).execute();
		}
		
		showAllBookmarksButton= (Button) findViewById(R.id.button_bookmarkDisplay);
		showAllBookmarksButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				saveData();
				finish();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_OK && requestCode==Request_bookmarkEdit){
			adapter.notifyDataSetChanged();
			dataChanged=true;
		}
	}
	
	@Override
	public void onBackPressed() {
		saveData();
		super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bookmark_display, menu);
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
		if (id == R.id.action_sortBookmarks) {
			
			if(BookmarkItemContainer.getBookmarkItems()==null){
				Toast.makeText(this, "No Bookmarks Found", Toast.LENGTH_SHORT).show();
				return true;
			}
			Collections.sort(BookmarkItemContainer.getBookmarkItems(),
					new Comparator<BookmarkItem>() {

						@Override
						public int compare(BookmarkItem lhs, BookmarkItem rhs) {
							int lhsAyahIndex=lhs.getAyah().ayahIndex;
							int lhsSuraIndex=lhs.getAyah().suraIndex;
							
							int rhsAyahIndex=rhs.getAyah().ayahIndex;
							int rhsSuraIndex=rhs.getAyah().suraIndex;
							
							return (lhsAyahIndex+lhsSuraIndex*1000) - 
									(rhsAyahIndex+rhsSuraIndex*1000);
						}
					});
			
			adapter.notifyDataSetChanged();
			dataChanged=true;
			
			return true;
		}
		if (id == R.id.action_clearAllBookmarks) {
			
			new AlertDialog.Builder(BookmarkDisplayActivity.this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Confirm Remove")
			.setMessage("Do you want to remove ALL Bookmarks from the list?")
			.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							
							if(BookmarkItemContainer.getBookmarkItems()!=null){
								BookmarkItemContainer.getBookmarkItems().clear();
								Toast.makeText(getBaseContext(), "All bookmarks cleared",
										Toast.LENGTH_SHORT).show();
								
								dataChanged=true;
								adapter.notifyDataSetChanged();
							}else{
								Toast.makeText(getBaseContext(), 
										"No bookmark found",
										Toast.LENGTH_SHORT).show();
							}
						}

					}).setNegativeButton("No", null).show();
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static void setDataChanged(boolean isDataChanged){
		dataChanged=isDataChanged;
	}
	
	private void saveData(){
		if(dataChanged){
			Thread bookmarkDataSaver=new Thread(new Runnable() {
				
				@Override
				public void run() {
					if(!BookmarkItemContainer.saveDataToFile()){
						Log.e("saveData() failure","Couldn't save bookmark data");
					};
				}
			});
			bookmarkDataSaver.start();
			dataChanged=false;
		}
	}
	
	class BookmarksloadingTask extends AsyncTask<Void, Void, Void>{
		
		ProgressDialog progressDialog;
		Context context;
		
		public BookmarksloadingTask(Context context) {
			this.context=context;
			progressDialog=new ProgressDialog(context);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage("Loading Bookmarks...");
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}
		@Override
		protected Void doInBackground(Void... params) {
			BookmarkItemContainer.initializeBookmarkItems(context);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			adapter.notifyDataSetChanged();
		}
	}
}
