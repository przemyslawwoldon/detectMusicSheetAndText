package img.proccesing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class PrepareImgToAnalize {

	private Mat srcImg;
	private final int errorOY = 5;
	private final int minWidthLine = 300; //!!!!!!!!!!!! przy np 230 klapa //spiewnik 2 - 200 i 20
	private final int errorInSetOfPoint = 5;//15 250
	private List<Point> listOfPoints;
	private List<List<Point>> setPointOfOneLine = new ArrayList<List<Point>>();
	//prostowanie 1 spiewnik 5 200 5
	//wczesniej 5 300 i 5
	
	public PrepareImgToAnalize(Mat srcImg) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.srcImg = srcImg;
	}
	
	public List<List<Point>> houghLines(boolean widthTerms) {
		Mat edgesImg = new Mat();
		Imgproc.Canny(srcImg, edgesImg, 170, 255);										
		Mat detectLines = new Mat();
		int horizontalSize = edgesImg.cols() / 100;
		Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalSize, 1));
		Imgproc.erode(edgesImg, edgesImg, horizontalStructure, new Point(-1, -1), 1);
		Imgproc.dilate(edgesImg, edgesImg, horizontalStructure, new Point(-1, -1), 7);
		Imgproc.HoughLinesP(edgesImg, detectLines, 1, Math.PI/180, 100, 70, 25);
		
		listOfPoints = new ArrayList<Point>();
		for (int i = 0; i < detectLines.rows(); i++){
			double data[] = detectLines.get(i, 0);
			if(data[1] == data[3] || (data[1] < (data[3] + errorOY) && data[1] > (data[3] - errorOY))) {
				if(widthTerms) {
					if( Math.abs(data[0] - data[2]) > minWidthLine){
						listOfPoints.add(new Point(data[0], data[1]));
						listOfPoints.add(new Point(data[2], data[3]));
					}
				}else {
					listOfPoints.add(new Point(data[0], data[1]));
					listOfPoints.add(new Point(data[2], data[3]));
				}
			}
		}
		createSetOfLine();
		return setPointOfOneLine;
	}
	
	public void createSetOfLine() {
		Collections.sort(listOfPoints, new Comparator<Point>() {
		    @Override public int compare(Point p1, Point p2) {
	            return (int) (p1.y- p2.y);
	        }
		});
		while(listOfPoints.size() != 0)
			createLine();
	}
	
	public void createLine() {
		Point firstPointInSet = listOfPoints.get(listOfPoints.size() - 1);
		listOfPoints.remove(listOfPoints.size() - 1);
		List<Point> setOfSamePoints = new ArrayList<Point>(); 
		setOfSamePoints.add(firstPointInSet);
		for(int i = listOfPoints.size() - 1; i >= 0; i -= 1) {
			Point p = listOfPoints.get(i);
			if(p.y == firstPointInSet.y || (p.y < (firstPointInSet.y + errorInSetOfPoint) && p.y > (firstPointInSet.y - errorInSetOfPoint))) {
					setOfSamePoints.add(p);
					listOfPoints.remove(i);
			}
		}
		setPointOfOneLine.add(setOfSamePoints);
		System.gc();
	}
	
}
