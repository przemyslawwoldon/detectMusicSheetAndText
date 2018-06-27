package img.proccesing;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DivideToPage {

	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	private List<Point> listOfPointVerticalLine;
	private int errorOX = 10;
	private int errorCenter = 125;
	
	public DivideToPage(String src, String dst) {
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
	
	public void divideToTwoPage() {
		getAndCreateDir();
		int nameFile = 0;
		List<String> loadImageFailed = new ArrayList<String>();
		for (int j = 0; j < listOfFileDirSrc.length; j += 1) {
			String[] fileNameAndExtension = listOfFileDirSrc[j].getName().split("\\.");
			Mat srcImg = new Mat();
			if(listOfFileDirSrc.length != 1)
				srcImg = Imgcodecs.imread(dirSrc + "\\" + listOfFileDirSrc[j].getName(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			else
				srcImg = Imgcodecs.imread(listOfFileDirSrc[j].getPath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
	
			if (srcImg.empty()) {
				System.out.println("Nie mozna zaladowac obrazu" + "\\" + listOfFileDirSrc[j].getName());
				loadImageFailed.add(listOfFileDirSrc[j].getName().toString());
				continue;
				// System.exit(-1);
			}

			getHoughLinesP(srcImg);
			List<Point> listOfPointVerticalLineCenterOfImg = getVerticalLine(srcImg);
			divideImg(listOfPointVerticalLineCenterOfImg, srcImg, j);
			listOfPointVerticalLine.clear();
			System.out.println("Divide to page " + fileNameAndExtension[nameFile] + " img finish");
			System.gc();
		}
		System.out.println("Liczba niezaladowanych plikow " + loadImageFailed.size());
	}

	public void getHoughLinesP(Mat srcImg) {
		Mat edgesImg = new Mat();
		Imgproc.Canny(srcImg, edgesImg, 170, 255);
		Mat detectLinesImg = new Mat();
		Imgproc.HoughLinesP(edgesImg, detectLinesImg, 1, Math.PI/180, 100, 30, 25);//70, 25
		listOfPointVerticalLine = new ArrayList<Point>();
		for (int i = 0; i < detectLinesImg.rows(); i++) {
			double data[] = detectLinesImg.get(i, 0);
			if (data[0] == data[2] || (data[0] < (data[2] + errorOX) && data[0] > (data[2] - errorOX))) {
				listOfPointVerticalLine.add(new Point(data[0], data[1]));
				listOfPointVerticalLine.add(new Point(data[2], data[3]));
			}
		}
	}
	
	public List<Point> getVerticalLine(Mat srcImg) {
		double center = srcImg.size().width / 2;
		List<Point> listOfPointVerticalLineCenterOfImg = new ArrayList<Point>(); 
		for(int i = 0; i < listOfPointVerticalLine.size(); i += 2) { 
			Point p = listOfPointVerticalLine.get(i); 
			if(p.x == center || (p.x < (center + errorCenter) && p.x > (center - errorCenter))) { 
				listOfPointVerticalLineCenterOfImg.add(p);
				listOfPointVerticalLineCenterOfImg.add(listOfPointVerticalLine.get(i + 1)); 
			}
		}
		return listOfPointVerticalLineCenterOfImg; 
	}
	
	public void divideImg(List<Point> listOfPointVerticalLineCenterOfImg, Mat srcImg, int imgIndex) {
		if(listOfPointVerticalLineCenterOfImg.size() > 0) {
			double x = listOfPointVerticalLineCenterOfImg.get(0).x;
			int widthImg = (int)(srcImg.size().width - x); 
			int heightImg = (int)(srcImg.size().height);	
			 	 
			Mat firstPage = new Mat(srcImg, new Rect(0, 0, (int)x, heightImg)).clone(); 
			Mat secondPage = new Mat(srcImg, new Rect((int)x, 0, widthImg, heightImg)).clone(); 
	
			Imgcodecs.imwrite(dirDst + "\\" + "fP_" + listOfFileDirSrc[imgIndex].getName(), firstPage);
			Imgcodecs.imwrite(dirDst + "\\" + "sP_" + listOfFileDirSrc[imgIndex].getName(), secondPage);
		}else {
			bruteForceDivideImg(srcImg, imgIndex);
		}
	}

	public void bruteForceDivideImg(Mat srcImg, int imgIndex){
		int centerImg = (int)((srcImg.size().width / 2) - 10);
		int widthImg = (int)(srcImg.size().width - centerImg); 
		int heightImg = (int)(srcImg.size().height);	
		 	 
		Mat firstPage = new Mat(srcImg, new Rect(0, 0, centerImg, heightImg)).clone(); 
		Mat secondPage = new Mat(srcImg, new Rect(centerImg, 0, widthImg, heightImg)).clone(); 

		Imgcodecs.imwrite(dirDst + "\\" + "bruteForce_fP_" + listOfFileDirSrc[imgIndex].getName(), firstPage);
		Imgcodecs.imwrite(dirDst + "\\" + "bruteForce_sP_" + listOfFileDirSrc[imgIndex].getName(), secondPage);
	}
}
