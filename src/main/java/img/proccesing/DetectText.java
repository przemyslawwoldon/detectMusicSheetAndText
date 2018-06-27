package img.proccesing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import cnn.classifier.MusicSheetClassifierImg;

public class DetectText {

	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	private boolean twoMusicSheet;
	int k = -1;
	int j = -1;
	
	public DetectText(String src, String dst, boolean twoMusicSheet) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.src = src;
		this.dst = dst;
		this.twoMusicSheet = twoMusicSheet;
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
	
	public void detectLineWithText() throws IOException {
		getAndCreateDir();
//		int nameFile = 0;
		List<String> loadImageFailed = new ArrayList<String>();
		for (int j = 0; j < listOfFileDirSrc.length; j += 1) {
//			String[] fileNameAndExtension = listOfFileDirSrc[j].getName().split("\\.");
			Mat srcImg = new Mat();
			if(listOfFileDirSrc.length != 1) {
				srcImg = Imgcodecs.imread(dirSrc + "\\" + listOfFileDirSrc[j].getName(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//				System.out.println("1 " + dirSrc + "\\" + listOfFileDirSrc[j].getName());
			}else {
				srcImg = Imgcodecs.imread(listOfFileDirSrc[j].getPath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//				System.out.println("2 " + listOfFileDirSrc[j].getPath());
			}
			if (srcImg.empty()) {
				System.out.println("Nie mozna zaladowac obrazu" + "\\" + listOfFileDirSrc[j].getName());
				loadImageFailed.add(listOfFileDirSrc[j].getName().toString());
				continue;
			}
			List<Double> stdDevList = calculateStdDev(srcImg);
			srcImg = expandBoundingBoxFromTop(srcImg, stdDevList);
			if(twoMusicSheet) {
				srcImg = expandBoundingBoxFromBottom(srcImg, stdDevList);
			}
			stdDevList.clear();
			stdDevList = calculateStdDev(srcImg);
			findProbablyText(srcImg, stdDevList, j);
			System.out.println("Detect text " + listOfFileDirSrc[j].getName() + " img finish");
		}
	}
	
	public Mat toGrayScale(Mat src) {
		for (int y = 0; y < src.rows(); y += 1) {
			for (int x = 0; x < src.cols(); x += 1) {
				double rgb[] = src.get(y, x);
				double greyScale = (rgb[0] + rgb[1] + rgb[2]) / 3;
				src.put(y, x, greyScale, greyScale, greyScale);
			}
		}
		return src;
	}
	
	public List<Double> calculateStdDev(Mat src){
		int width = (int) src.size().width;
		List<Double> stdDevList = new ArrayList<Double>();
		for (int j = 0; j < src.rows(); j += 1) {
			Mat temp = new Mat(src, new Rect(0, j, width, 1)).clone();
			MatOfDouble mean = new MatOfDouble();
			MatOfDouble stddev = new MatOfDouble();
			Core.meanStdDev(temp, mean, stddev);
			stdDevList.add(stddev.get(0, 0)[0]);
			System.gc();
		}	
		return stdDevList;
	}

	public Mat expandBoundingBoxFromTop(Mat src, List<Double> stdDevList) {
		k = 0;
		int width = (int) src.size().width;
		for (k = 0; k < stdDevList.size(); k += 1) {
			if (stdDevList.get(k) == 0)
				break;
			Imgproc.rectangle(src, new Point(0, k), new Point(width, k), new Scalar(255, 0, 0), -1, 4, 0);
		}
		while (stdDevList.get(k++) == 0) {}; //k to ostatnia linia wycieta od gory
		
		for (int i = -5; i <= 20; i += 1) {
			Imgproc.rectangle(src, new Point(0, k + i), new Point(width, k + i), new Scalar(255, 0, 0), -1, 4, 0);
		}
		k += (20 - (-5));
		return src;
	}
	
	public Mat expandBoundingBoxFromBottom(Mat src, List<Double> stdDevList) {
		j = 0;
		int width = (int) src.size().width;
		for (j = stdDevList.size() - 1; j >= 0; j -= 1) {
			if (stdDevList.get(j) == 0)
				break;
			Imgproc.rectangle(src, new Point(0, j), new Point(width, j), new Scalar(255, 0, 0), -1, 4, 0);
		}
		while (stdDevList.get(j--) == 0) {};
		for (int i = 5; i >= -20; i -= 1) {
			Imgproc.rectangle(src, new Point(0, j + i), new Point(width, j + i), new Scalar(255, 0, 0), -1, 4, 0);
		}
		j += -20 + 5;
		return src;
	}
	
	public void findProbablyText(Mat src, List <Double> stdDevList, int j) throws IOException {
		double max = stdDevList.get(0);
		int indMax = 0;
		for (int b = 1; b < stdDevList.size(); b++) {
			double p = stdDevList.get(b);
			if (p > max) {
				max = p;
				indMax = b;
			}
		}
		int width = (int) src.size().width;
		
		Mat dstTemp;
		if((indMax - 30) >= 0 && (indMax + 30) <= (int) src.size().height) {
			dstTemp = new Mat(src, new Rect(0, (indMax - 30), width, ((indMax + 30)-(indMax - 30)))).clone();
			Imgcodecs.imwrite(dirDst + "\\" + String.format("%03d", j) + ".png", dstTemp);
			boolean isText = new MusicSheetClassifierImg(dirDst + "\\" + String.format("%03d", j) + ".png").isText();
			if(!isText) {
				File fd = new File(dirDst + "\\" + String.format("%03d", j) + ".png");
				fd.delete();
				dstTemp = src;
			}
		}else {
			dstTemp = src;
		}
		Imgcodecs.imwrite(dirDst + "\\" + String.format("%03d", j) + ".png", dstTemp);
	}
	


}
