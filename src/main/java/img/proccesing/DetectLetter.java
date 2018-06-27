package img.proccesing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DetectLetter {

	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	
	private List<ArrayList<MatOfPoint>> contoursGlob = new ArrayList<ArrayList<MatOfPoint>>();
	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private List<Scalar> maxMin = new ArrayList<Scalar>();
	 
	
	public DetectLetter(String src, String dst) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.src = src;
		this.dst = dst;
	}
		
	private void getAndCreateDir() {
		dirSrc = new File(src);
		if(dirSrc.isDirectory()) {
			listOfFileDirSrc = dirSrc.listFiles();
		} else {
			listOfFileDirSrc = new File[1];
			listOfFileDirSrc[0] = dirSrc;
		}
		dirDst= new File(dst);

		if (!dirDst.exists()) {
			try {
				dirDst.mkdirs();
				System.out.println("DIR created" + dst);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}	
	
	
	public  void detectLetterUseContours() {
		getAndCreateDir();
		List<String> loadImageFailed = new ArrayList<String>();
		for (int j = 0; j < listOfFileDirSrc.length; j += 1) {
			Mat srcImg = new Mat();
			if(listOfFileDirSrc.length != 1) {
				srcImg = Imgcodecs.imread(dirSrc + "\\" + listOfFileDirSrc[j].getName(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			}else {
				srcImg = Imgcodecs.imread(listOfFileDirSrc[j].getPath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			}
			if (srcImg.empty()) {
				System.out.println("Nie mozna zaladowac obrazu" + "\\" + listOfFileDirSrc[j].getName());
				loadImageFailed.add(listOfFileDirSrc[j].getName().toString());
				continue;
			}
			Mat srcImgUltimate = srcImg;
			
			Mat srcImgBlur = new Mat();
			Imgproc.medianBlur(srcImg, srcImgBlur, 5); 
			Mat dstImg = new Mat();
			Imgproc.Canny(srcImgBlur, dstImg, 2.0, 2.0);
			
			gameWithContours(dstImg);
			sortExtremeCoordinates();
			
			String name[] = listOfFileDirSrc[j].getName().split("\\.");
			saveCharacter(srcImgUltimate, j, name[0]);
			contoursGlob.clear();
			contours.clear();
			maxMin.clear();
		}
	}
	
	public void gameWithContours(Mat dstImg) {
		Mat hierarchy = new Mat();
		Imgproc.findContours(dstImg, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		MatOfInt hull = new MatOfInt(contours.size());
		for( int i = 0; i < contours.size(); i++ ){ 
			Imgproc.convexHull( contours.get(i), hull, false ); 
		}
		Mat drawing = Mat.zeros(dstImg.size(), CvType.CV_8UC3);
		for( int i = 0; i< contours.size(); i++ ) {
			Scalar color = new Scalar(0,255,0  );
			Imgproc.drawContours( drawing, contours, i, color );
		}
		for( int i = 0; i< contours.size(); i++ ) {
			Mat dstUltimate = Mat.eye(dstImg.size(), CvType.CV_8UC3);
			Scalar color = new Scalar(255,255,255  );
			Imgproc.drawContours(dstUltimate, contours, i, color );
		}
		while(contours.size() > 0) {
			createSetOfContours();
		}
		
		int k = 0;
		while (maxMin.size() != (contoursGlob.size())) {
			getMaxAndMin(k);
			k += 1;
		}

	}
	
	public int createSetOfContours() {
		MatOfPoint tempLast = contours.get(contours.size()-1);
		contours.remove(contours.size()-1);
		ArrayList<MatOfPoint> setOfSame = new ArrayList<MatOfPoint>(); 
		setOfSame.add(tempLast);
			
		for(int j = 0; j < tempLast.rows(); j += 1) {
			for(int i = 0; i < tempLast.cols(); i += 1) {
				double dat[] = tempLast.get(j, i);
				double x = dat[0];
				int res = -5;
				while (res != -1) {	
					res = compar(x);
					if(res >= 0) {
						setOfSame.add(contours.get(res));
						contours.remove(res);
					}
				}
			}
		}
		contoursGlob.add(setOfSame);
		return 0;
	}
	
	public void sortExtremeCoordinates() {
		Collections.sort(maxMin, new Comparator<Scalar>() {
		    @Override 
		    public int compare(Scalar p1, Scalar p2) {
	            return (int) (p1.val[0]- p2.val[0]);
	        }
		});
	}
		
	
	public void saveCharacter(Mat srcImgUltimate, int j, String s) {
		int m = 1;
		for(int i = 0; i < maxMin.size(); i += 1) {
			if((int)(maxMin.get(i).val[2] - maxMin.get(i).val[0]) != 0 && (int)(maxMin.get(i).val[3] - maxMin.get(i).val[1]) != 0) {
				Mat dst = new Mat(srcImgUltimate, new Rect((int)maxMin.get(i).val[0], (int)maxMin.get(i).val[1], 
						(int)(maxMin.get(i).val[2] - maxMin.get(i).val[0]), 
						(int)(maxMin.get(i).val[3] - maxMin.get(i).val[1]))).clone();
//				System.out.println(this.dst + "\\" + String.format("%03d", i) + ".png");
				Imgcodecs.imwrite(this.dst + "\\" + s + "_"+  String.format("%03d", m) + ".png", dst);
				System.gc();
	    	}
			m += 1;
	    }
	}
		
	
	public void getMaxAndMin(int k) {
		ArrayList<MatOfPoint> t = contoursGlob.get(k);
		double minX = t.get(0).get(0, 0)[0];
		double maxX = t.get(0).get(0, 0)[0];
		double minY = t.get(0).get(0, 0)[1];
		double maxY = t.get(0).get(0, 0)[1];
		for(int i = 0; i < t.size(); i += 1) {
			for(int j = 0; j < t.get(i).rows(); j += 1) {
				for(int m = 0; m < t.get(i).cols(); m += 1) {
					double dat[] = t.get(i).get(j, m);
					if(dat[0] > maxX) {
						maxX = dat[0]; 
					}
					if(dat[0] < minX) {
						minX = dat[0]; 
					}
					if(dat[1] < minY) {
						minY = dat[1]; 
					}
					if(dat[1] > maxY) {
						maxY = dat[1]; 
					}
				}
			}
		}
		maxMin.add(new Scalar (minX, minY, maxX, maxY));
	}
		
	public int compar(double x) {
		for(int m = contours.size() - 1; m >= 0; m -= 1) {
			MatOfPoint tempLastCont = contours.get(m);
			for(int jj = 0; jj < tempLastCont.rows(); jj += 1) {
				for(int ii = 0; ii < tempLastCont.cols(); ii += 1) {
					double datt[] = tempLastCont.get(jj, ii);
					double x1 = datt[0];
					if(x == x1) {
						return m;
					}
				}
			}
		}
		return -1;
	}

}
