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

public class FileItemEditActivity extends Activity {

	EditText transNameText;
	Button saveButton;
	Button removeButton;
	
	int itemIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_item_edit);
		
		transNameText=(EditText) findViewById(R.id.editTextTransName);
		saveButton=(Button) findViewById(R.id.buttonSaveTransName);
		removeButton=(Button) findViewById(R.id.buttonRemoveTransFile);
		
		itemIndex=getIntent().getIntExtra("itemIndex", -1);
		FileItem fileItem=FileItemContainer.getFileItem(itemIndex);

		transNameText.setText(fileItem.getFileAliasName());
		
		//TODO add listener
		
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(itemIndex<0){
					Toast.makeText(getBaseContext(),
							"Error! Operation Failed",
							Toast.LENGTH_SHORT).show();
					
					return;
				}
				changeFileAliasName();

				setResult(Activity.RESULT_OK);
				finish();
			}
		});
		
		removeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(itemIndex<0){
					Toast.makeText(getBaseContext(),
							"Error! Operation Failed",
							Toast.LENGTH_SHORT).show();
					
					return;
				}
				
				new AlertDialog.Builder(FileItemEditActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Confirm Remove")
				.setMessage("Do you want to remove this file from the list?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								
								removeFile(itemIndex);
								setResult(Activity.RESULT_OK);
								finish();
							}

						}).setNegativeButton("No", null).show();
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_item_edit, menu);
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
	
	
	private void changeFileAliasName(){
		String text=transNameText.getText().toString();
		FileItemContainer.getFileItems().get(itemIndex).setFileAliasName(text);
		//TODO saveData()
	}
	
	private void removeFile(int index){
		FileItem fileItem=FileItemContainer.getFileItems().get(index);
		boolean isDeleted=fileItem.getFile().delete();
		
		if(isDeleted){
			Toast.makeText(getBaseContext(), "File Successfully Removed",
					Toast.LENGTH_SHORT).show();
			
			FileItemContainer.getFileItems().remove(index);
		}else{
			Toast.makeText(getBaseContext(), "File Removing Failed",
					Toast.LENGTH_SHORT).show();
		}
	}
}
