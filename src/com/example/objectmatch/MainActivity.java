package com.example.objectmatch;

import android.R.bool;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.sql.Date;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
//import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
//import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Building {//Activity implements CvCameraViewListener2{

	private static Bitmap bestmatch;
	protected static Bitmap matchbitmap;  //pravite
	Building_view mOpenCvCameraView;
	private Mat mRgba;
	private Mat mGray;
	private Mat mByte;
	private Scalar CONTOUR_COLOR;
	private static boolean isProcess = false;
	String location =new String();
	String aimpicpath =Environment.getExternalStorageDirectory().getPath()+"/PhotoGallery/"+"/Temp/"+"preaimpic.jpg";
	private static final String TAG = "Dawn";
	static String aimpic=new String();
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {//load opencv
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
		
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		mOpenCvCameraView = (Building_view) findViewById(R.id.objectMatch); //CameraBridgeViewBase
		mOpenCvCameraView.setCvCameraViewListener(this);
		final TextView textView = (TextView) findViewById(R.id.locate); 
		final ImageView matchimg = (ImageView) findViewById(R.id.Imgmatch);
		Button showButton = (Button) findViewById(R.id.button_show);
		Button matchButton = (Button) findViewById(R.id.button_match);
		Button showmatchButton = (Button) findViewById(R.id.button_showmatch);
		matchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//take a pictrue
				String fileName = "preaimpic.jpg";
				mOpenCvCameraView.takePicture(fileName);
				//aimpic=PhotoCompare.maxKey;
				try {
					compressAndGenImage(aimpicpath, Environment.getExternalStorageDirectory().getPath()+"/PhotoGallery/"+"/Temp/"+fileName, 1200, true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isProcess = true;
				System.gc();
			}
		});
		showButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//location
				    aimpic=PhotoCompare.order(aimpicpath); 
                	location=getFileName(aimpic);
					textView.setText("You are now at "+location);//}	
//				}  
					System.gc();
			}
		});
		showmatchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// show match img in the Imageview
		
				if (isProcess){
				match(isProcess);
				matchimg.setImageBitmap(matchbitmap);
				isProcess=false;
				PhotoCompare.maxKey=null;//new 
				aimpic=null;}
				System.gc();
				
			}
		});    
	}
	public String getFileName(String pathandname){  
		if (pathandname==null){
			return null;
		};
        int start=pathandname.lastIndexOf("/");  
        int end=pathandname.lastIndexOf(".");  
        if(start!=-1 && end!=-1){  
            return pathandname.substring(start+1,end);    
        }else{  
            return null;  
        }  
    }
	public static Bitmap getLoacalBitmap(String url) {
        try {
             FileInputStream fis = new FileInputStream(url);
             return BitmapFactory.decodeStream(fis);  ///°ÑÁ÷×ª»¯ÎªBitmapÍ¼Æ¬        

          } catch (FileNotFoundException e) {
             e.printStackTrace();
             return null;
        }
   }

	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this,
				mLoaderCallback);
	}

	public void onCameraViewStarted(int realWidth, int realHeight) {  //new
		mRgba = new Mat(realHeight, realWidth, CvType.CV_8UC3);  //3
		mByte = new Mat(realHeight, realWidth, CvType.CV_8UC1);
	}
	
	public void onCameraViewStopped() { // Explicitly deallocate Mats
		mRgba.release();
	}
	private Options getBitmapOption(int inSampleSize){
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
}
	public void match(boolean isProcess){
		Mat mGray1=new Mat();
		Bitmap s_testimg;
		Bitmap aimpic1;
		Mat testimage = new Mat();
		Mat grayimage=new Mat();
		MatOfDMatch matches = new MatOfDMatch();
		MatOfKeyPoint keypoint_train = new MatOfKeyPoint();
		MatOfKeyPoint keypoint_test = new MatOfKeyPoint();
		Mat output = new Mat(); // Mat train=new Mat(); Mat
		Mat test = new Mat();
		Mat train = new Mat();
	    bestmatch=BitmapFactory.decodeFile(aimpic,getBitmapOption(2));
		String filename=new String();
			filename=Environment.getExternalStorageDirectory().getPath()+"/PhotoGallery/"+"/Temp/"+"preaimpic.jpg";
			aimpic1=BitmapFactory.decodeFile(filename,getBitmapOption(2));//Imgcodecs.CV_LOAD_IMAGE_COLOR
			
			Mat aimpicMat=new Mat();
			Utils.bitmapToMat(aimpic1, aimpicMat);
			aimpic1.recycle();
			Imgproc.cvtColor(aimpicMat,mGray1,Imgproc.COLOR_RGB2GRAY);//Imgproc.COLOR_RGB2GRAY
			//Mat Temp=imread("test.jpg");
			FeatureDetector detector_train = FeatureDetector
					.create(FeatureDetector.ORB);
			detector_train.detect(mGray1, keypoint_train);
			//×¢ÊÍ			Features2d.drawKeypoints(mGray1, keypoint_train, output, new Scalar(  //
			//×¢ÊÍ				2, 254, 255), Features2d.DRAW_RICH_KEYPOINTS);  //zhushi 
			DescriptorExtractor descriptor_train = DescriptorExtractor
					.create(DescriptorExtractor.ORB);
			descriptor_train.compute(mGray1, keypoint_train, train);
			s_testimg = Bitmap.createScaledBitmap(bestmatch, mGray1.width(), mGray1.height(), false);
			Utils.bitmapToMat(s_testimg, testimage);
			//s_testimg.recycle();
			Imgproc.cvtColor(testimage, grayimage, Imgproc.COLOR_RGB2GRAY);
			FeatureDetector detector_test = FeatureDetector
					.create(FeatureDetector.ORB);
			detector_test.detect(grayimage, keypoint_test);
//×¢ÊÍ		Features2d.drawKeypoints(testimage, keypoint_test, output,          
//×¢ÊÍ					new Scalar(2, 254, 255), Features2d.DRAW_RICH_KEYPOINTS);//×¢ÊÍ
			DescriptorExtractor descriptor_test = DescriptorExtractor
					.create(DescriptorExtractor.ORB);
			descriptor_test.compute(grayimage, keypoint_test, test);
			DescriptorMatcher descriptormatcher = DescriptorMatcher
					.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
			descriptormatcher.match(test, train, matches);
			Features2d.drawMatches(grayimage,keypoint_test,mGray1, keypoint_train, matches, output);
			matchbitmap=Bitmap.createScaledBitmap(bestmatch, output.width(), output.height(), false);   //output.width()    output.height()
			Utils.matToBitmap(output, matchbitmap);
			File file = new File(filename);   
			file.deleteOnExit();
	}
	
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		Mat mRgbaT = mRgba.t();//new start
	    Core.flip(mRgba.t(), mRgbaT, 1);
	    Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
		CONTOUR_COLOR = new Scalar(255);
		return mRgbaT;   //mRgba   mRgbaT
	}

}
