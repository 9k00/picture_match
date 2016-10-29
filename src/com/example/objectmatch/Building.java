package com.example.objectmatch;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class Building extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private Building_view mOpenCvCameraView;  //
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private Mat mRgba;
	private Mat mGray;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Building.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Building() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.buiding_layout);

        mOpenCvCameraView = (Building_view) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);}
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		Mat mRgbaT = mRgba.t();//new start
	    Core.flip(mRgba.t(), mRgbaT, 1);
	    Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
        return mRgbaT;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
           String element = effectItr.next();
           mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
           idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }

        return true;
    }
    
    public Bitmap getBitmap(String imgPath) {  
        // Get bitmap through image path  
        BitmapFactory.Options newOpts = new BitmapFactory.Options();  
        newOpts.inJustDecodeBounds = false;  
        // Do not compress  
        newOpts.inSampleSize = 1;  
        newOpts.inPreferredConfig = Config.RGB_565;  
        return BitmapFactory.decodeFile(imgPath, newOpts);  
    }  
    
    /** 
     * Compress by quality,  and generate image to the path specified 
     * Use to upload 
     * 
     * @param image 
     * @param outPath 
     * @param maxSize target will be compressed to be smaller than this size.(kb) 
     * @throws IOException 
     */  
    public static void compressAndGenImage(Bitmap image, String outPath, int maxSize) throws  
            IOException {  
        ByteArrayOutputStream os = new ByteArrayOutputStream();  
        // scale  
        int options = 100;  
        // Store the bitmap into output stream(no compress)  
        image.compress(Bitmap.CompressFormat.JPEG, options, os);  
        // Compress by loop  
        while (os.toByteArray().length / 1024 > maxSize) {  
            // Clean up os  
            os.reset();  
            // interval 10  
            options -= 10;  
            if (options < 0) {  
                options = 0;  
                image.compress(Bitmap.CompressFormat.JPEG, options, os);  
                break;  
            }  
            image.compress(Bitmap.CompressFormat.JPEG, options, os);  
        }  
  
        // Generate compressed image file  
        FileOutputStream fos = new FileOutputStream(outPath);  
        fos.write(os.toByteArray());  
        fos.flush();  
        fos.close();  
    }  
  
    /** 
     * Compress by quality,  and generate image to the path specified 
     * 
     * @param imgPath 
     * @param outPath 
     * @param maxSize     target will be compressed to be smaller than this size.(kb) 
     * @param needsDelete Whether delete original file after compress 
     * @throws IOException 
     */  
    public void compressAndGenImage(String imgPath, String outPath, int maxSize, boolean  
            needsDelete) throws IOException {  
        compressAndGenImage(getBitmap(imgPath), outPath, maxSize);  
  
        // Delete original file  
        if (needsDelete) {  
            File file = new File(imgPath);  
            if (file.exists()) {  
                file.delete();  
            }  
        }  
    }  
  
    
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
    	
        Log.i(TAG,"onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        final String currentDateandTime = sdf.format(new Date());
        final String fileName =currentDateandTime+ ".jpg";//Environment.getExternalStorageDirectory().getPath() +
        mOpenCvCameraView.takePicture(fileName);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        final EditText editText = new EditText(this);
        builder.setIcon(android.R.drawable.ic_dialog_info); 
        builder.setTitle("Please enter the location you want to add");  
        builder.setView(editText);  
        builder.setPositiveButton("add",  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                    	String currentLocation =editText.getText().toString();
                        String fileName = currentLocation + ".jpg";//??? Environment.getExternalStorageDirectory().getPath() + ???
                        //mOpenCvCameraView.takePicture(fileName);
                        String filePath=Environment.getExternalStorageDirectory().getPath()+"/PhotoGallery/"+"/Temp/"+currentDateandTime+ ".jpg" ;
                        File oleFile = new File(filePath); //要重命名的文件或文件夹
                        File newFile = new File(Environment.getExternalStorageDirectory().getPath()+"/PhotoGallery/"+fileName);  //重命名为zhidian1
                        try {
							compressAndGenImage(filePath, Environment.getExternalStorageDirectory().getPath()+"/PhotoGallery/"+fileName, 1200, true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                       
                       // oleFile.renameTo(newFile);  //执行重命名
                        oleFile.deleteOnExit();
                         } 
                   
        }); 
        
        builder.setNegativeButton("cancel",  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	File temp=new File(Environment.getExternalStorageDirectory().getPath()+"/PhotoGallery/"+fileName);
                    	temp.delete();
                         // setTitle("已取消");  
                    }  
                });

        builder.create().show();
    
        return false;
    }
}

