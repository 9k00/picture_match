package com.example.objectmatch;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import android.annotation.SuppressLint;
import android.os.Environment;

public class PhotoCompare {
	
	protected static String maxKey;		
	protected static  List<String> piclist= getImagePathFromSD();
	public static List<String> getImagePathFromSD() {   // private
	
		 List<String> picList = new ArrayList<String>(); 
	
		  String imagePath = 
		  Environment.getExternalStorageDirectory().toString() 
		  + "/PhotoGallery/"; 
	
		  File mfile = new File(imagePath); 
		  File[] files = mfile.listFiles(); 
 
		 for (int i = 0; i <files.length; i++) { 
		  File file = files[i]; 
		  if (checkIsImageFile(file.getPath())) { 
		   picList.add(file.getPath()); 
		  } 
		 } 

		 return picList; 
		} 

		private static boolean checkIsImageFile(String fName) { 
		 boolean isImageFile = false; 

		 
		 String FileEnd = fName.substring(fName.lastIndexOf(".") + 1, 
		   fName.length()).toLowerCase(); 
		 if (FileEnd.equals("jpg") || FileEnd.equals("jpeg") 
		   || FileEnd.equals("bmp")||FileEnd.equals("png")) { 
		  isImageFile = true; 
		 } else { 
		  isImageFile = false; 
		 } 
		 
		 return isImageFile; 
		 
		}  
		
		public static Object getMaxKey(Map<Integer, String> map) {
			if (map == null) return null;
			Set<Integer> set = map.keySet();
			Object[] obj = set.toArray();
			Arrays.sort(obj); //×î³õÊÇobj //strArray, Collections.reverseOrder()
			return obj[obj.length-1];  //size()  ???
			}

 
 @SuppressLint("UseSparseArrays")
public static  String order(String aimpicture) {  //static ??
  String pic=new String();
  int j=getImagePathFromSD().size();
  int ret1=0;
  //int maxValue = 0;
  //String maxKey;
   Map<Integer, String> map = new HashMap<Integer, String>();
  for(int i=0;i<j;i++){
	  pic= getImagePathFromSD().get(i); //piclist.get(i);
  ret1 = compareFeature(aimpicture,pic);// getImagePathFromSD().get(i)

  map.put(ret1,pic);
  }
  Object a =getMaxKey(map);
  maxKey=map.get(a);
  
return maxKey;

 }
 
 /**
  * Compare that two images is similar using feature mapping  
  * @param filename1 - the first image
  * @param filename2 - the second image
  * @return integer - count that has the similarity within images 
  */
 public static int compareFeature(String filename1, String filename2) {
   int retVal=0;
  //long startTime = System.currentTimeMillis();
   int s=0;
  //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  
  // Load images to compare
  Mat img1 = Imgcodecs.imread(filename1, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);  //CV_LOAD_IMAGE_GRAYSCALE
  Mat img2 = Imgcodecs.imread(filename2, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);  //color

  // Declare key point of images
  MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
  MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
  Mat descriptors1 = new Mat();
  Mat descriptors2 = new Mat();

  // Definition of ORB key point detector and descriptor extractors
  FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB); 
  DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

  // Detect key points
  detector.detect(img1, keypoints1);
  detector.detect(img2, keypoints2);
  
  // Extract descriptors
  extractor.compute(img1, keypoints1, descriptors1);
  extractor.compute(img2, keypoints2, descriptors2);

  // Definition of descriptor matcher
  DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

  // Match points of two images
  //MatOfDMatch matches = new MatOfDMatch();
  List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
  // Avoid to assertion failed

  if (descriptors2.cols() == descriptors1.cols()) {
	  matcher.knnMatch(descriptors1, descriptors2, matches, 2);
	  LinkedList<DMatch> good_matches = new LinkedList<DMatch>();  //
	  for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
		    MatOfDMatch matOfDMatch = (MatOfDMatch) iterator.next();
		    float a=matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance ;
		    if ( a< 0.9) {
		    	 good_matches.add(matOfDMatch.toArray()[0]);  //
		    }
	  }
	
	  List<Point> pts1 = new ArrayList<Point>();
	  List<Point> pts2 = new ArrayList<Point>();
	  for(int i = 0; i<good_matches.size(); i++){
	      pts1.add(keypoints1.toList().get(good_matches.get(i).queryIdx).pt);
	      pts2.add(keypoints2.toList().get(good_matches.get(i).trainIdx).pt);
	  }

	  // convertion of data types - there is maybe a more beautiful way
	  Mat outputMask = new Mat();
	  MatOfPoint2f pts1Mat = new MatOfPoint2f();
	  pts1Mat.fromList(pts1);
	  MatOfPoint2f pts2Mat = new MatOfPoint2f();
	  pts2Mat.fromList(pts2);
	  Mat Homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 15, outputMask, 2000, 0.9);
	// outputMask contains zeros and ones indicating which matches are filtered
	//LinkedList<DMatch> better_matches = new LinkedList<DMatch>();
	for (int i = 0; i < good_matches.size(); i++) {
	    if (outputMask.get(i, 0)[0] != 0.0) {
	        retVal++;
	    }
	
	}

  }
  
  return retVal;
 }

 }
