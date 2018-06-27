package img.proccesing;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ToStraightenUp {

	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	private List<List<Point>> setPointOfOneLine;
	private List<Scalar> goodLines;
	
	public ToStraightenUp(String src, String dst) {
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
	
	public void straightenUp() {
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
			}
			setPointOfOneLine = new PrepareImgToAnalize(srcImg).houghLines(false);
			lineProcessing(srcImg, j);	
			setPointOfOneLine.clear();
			goodLines.clear();
			System.out.println("Straighten up " + fileNameAndExtension[nameFile] + " img finish");
			System.gc();
		}
		System.out.println("Liczba niezaladowanych plikow " + loadImageFailed.size());
	}
	
	public void lineProcessing(Mat srcImg, int imgIndex) {
		goodLines = new ArrayList<Scalar>();
		for(int i = 0; i < setPointOfOneLine.size(); i++) {
			List<Point> listOfPointTemp = setPointOfOneLine.get(i);				
			double minX = listOfPointTemp.get(0).x;
			double maxX = listOfPointTemp.get(0).x;
			double minY = listOfPointTemp.get(0).y;
			double maxY = listOfPointTemp.get(0).y;
			for(int j = 1; j < listOfPointTemp.size(); j++) {
				Point p = listOfPointTemp.get(j);
			    if(p.x < minX) {
			    	minX = p.x;
			    	minY = p.y;
			    }
			    if(p.x > maxX) {
			    	maxX = p.x;
			    	maxY = p.y;
			    }
			}	
			goodLines.add(new Scalar(minX, minY, maxX, maxY));
		}
		double angle = calculateAngle(srcImg.size().width);
		angle *= 10;
		System.out.println("angle:" + angle);
		Mat rotationMat2d = Imgproc.getRotationMatrix2D(new Point(srcImg.cols() / 2.0F, srcImg.rows() / 2.0F), angle, 1.0);
		Mat dstImg = new Mat();
		Imgproc.warpAffine(srcImg, dstImg, rotationMat2d, srcImg.size());
		Imgcodecs.imwrite(dirDst + "\\" + "sU_" + listOfFileDirSrc[imgIndex].getName(), dstImg);
	}
	
	public double calculateAngle(double width) {
		double meanRotate = 0;
		int counterLine = 0;
		for(Scalar p : goodLines) {
			meanRotate += Math.abs((p.val[3] - p.val[1]));
			counterLine += 1;
		}
		meanRotate /= counterLine;
		double angleRotTan = (/*-*/1 * meanRotate) / width;
		return Math.toDegrees(Math.atan(angleRotTan));	
		//return Math.toDegrees((angleRotTan));
		//return meanRotate;
	}
	
}
