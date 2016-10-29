package com.example.objectmatch;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.hardware.Camera;
//import android.hardware.Camera.PictureCallback;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
import android.view.View;
//import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.View.OnClickListener;

public class FirstActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_layout);
		Button button1=(Button) findViewById(R.id.button1);
		Button button2=(Button) findViewById(R.id.button2);
		button1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent=new Intent(FirstActivity.this,MainActivity.class);
				startActivity(intent);
			}
		
		});
		button2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent=new Intent(FirstActivity.this,Building.class);
				startActivity(intent);
			}
		
		});
	}
	

}
