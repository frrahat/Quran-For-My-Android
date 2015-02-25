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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SuraListActivity extends Activity {

	private ListView suraListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Full Screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//------------*/
		setContentView(R.layout.activity_sura_list);
		
		final String suraNames[] = getResources().getStringArray(R.array.sura_name);
		final String titleMeanings[]=getResources().getStringArray(R.array.sura_title_meanings);
		
		suraListView=(ListView) findViewById(R.id.listView);
		
		BaseAdapter adapter=new BaseAdapter() {
			
			LayoutInflater layoutInflater=(LayoutInflater) 
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			@SuppressLint("InflateParams")
			@Override
			public View getView(int position, View view, ViewGroup parent) {
				if(view==null)
				{
					view=layoutInflater.inflate(R.layout.list_item, null);
				}
				TextView textView1=(TextView) view.findViewById(R.id.text1);
				TextView textView2=(TextView) view.findViewById(R.id.text2);
				
				textView1.setText(Integer.toString(position+1)+"."+suraNames[position]);
				textView2.setText(titleMeanings[position]+" , total verses:"+
						Integer.toString(SuraInformation.totalAyas[position]));
				
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				return position+1;
			}
			
			@Override
			public Object getItem(int position) {
				return position+1; 
			}
			
			@Override
			public int getCount() {
				return suraNames.length;
			}
		};
		
		suraListView.setAdapter(adapter);
		
		suraListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("sura_num", position+1);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
			
		}); 
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sura_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
}
