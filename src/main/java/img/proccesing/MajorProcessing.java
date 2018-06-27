package img.proccesing;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MajorProcessing {

	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	private List<List<Point>> setPointOfOneLine = new ArrayList<List<Point>>();
	private List<String> imageProcessingFailed = new ArrayList<String>();
	
	private File dirDst2;
	private File dirDst3;
	private File dirDst4;
	
	public MajorProcessing(String src, String dst) {
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
//		createAdditionalDir();
	}
	
	private void createAdditionalDir() {
		dirDst3= new File(dst + "\\" + "fullImg");
		if (!dirDst3.exists()) {
			try {
				dirDst3.mkdirs();
				System.out.println("DIR created" + dirDst3);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		dirDst4= new File(dst + "\\" + "allMusicSheet");
		if (!dirDst4.exists()) {
			try {
				dirDst4.mkdirs();
				System.out.println("DIR created" + dirDst4);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createDirForOneScan(String str){
		dirDst2= new File(dst + "\\" + str);
		if (!dirDst2.exists()) {
			try {
				dirDst2.mkdirs();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void detectMusicSheetViolinAndBass() {
		getAndCreateDir();
		List<String> loadImageFailed = new ArrayList<String>();
		for (int abc = 0; abc < listOfFileDirSrc.length; abc += 1) {
			String[] fileNameAndExtension = listOfFileDirSrc[abc].getName().split("\\.");
			Mat srcImg = new Mat();
			if(listOfFileDirSrc.length != 1)
				srcImg = Imgcodecs.imread(dirSrc + "\\" + listOfFileDirSrc[abc].getName(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			else
				srcImg = Imgcodecs.imread(listOfFileDirSrc[abc].getPath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		
			if (srcImg.empty()) {
				System.out.println("Nie mozna zaladowac obrazu" + "\\" + listOfFileDirSrc[abc].getName());
				loadImageFailed.add(listOfFileDirSrc[abc].getName().toString());
				continue;
			}
											
			setPointOfOneLine = new PrepareImgToAnalize(srcImg).houghLines(true);
			List<Point> goodLines = createSetOfPointOneLine();
			Set<Integer> printedLine = createPointLine(goodLines);
			List<Scalar> printedLineScalar = collectPointOfLIne(goodLines, printedLine, srcImg);
			createDirForOneScan(fileNameAndExtension[0]);
			createBoundingBox(srcImg, printedLineScalar, fileNameAndExtension);
			System.out.println("Major processing " + fileNameAndExtension[0] + " img finish");
			setPointOfOneLine.clear();
			System.gc();
		}
		clearEmptyDir();
		System.out.println("Liczba niezaladowanych plikow " + loadImageFailed.size());
		System.out.println("Liczba nieprzetworzonych plikow " + imageProcessingFailed.size());
		System.out.println(imageProcessingFailed);
	}

	public void /*List<Point>*/ createBoundingBox(Mat srcImg, List<Scalar> printedLineScalar, String[] fileNameAndExtension) {
		int nextMusicScheet01 = 0;
		int nextMusicScheet02 = 0;
		int x_ax = srcImg.cols();
		int licznik02 = 0;
		List<Point> boundingBox = new ArrayList<Point>();
		for(int j = 0; j < printedLineScalar.size() - 1; j++) {
			double y01 = printedLineScalar.get(j).val[1];
			double y02 = printedLineScalar.get(j + 1).val[1];
			if(Math.abs(y02 - y01) > 32) {
				nextMusicScheet02 = j;	
				Imgproc.rectangle(srcImg, new Point(0,
				printedLineScalar.get(nextMusicScheet01).val[1]), 
				new Point(x_ax, printedLineScalar.get(nextMusicScheet02).val[3]), new Scalar(255, 0, 0), -1, 4, 0);
				
				if(licznik02 % 2 == 0) {
					boundingBox.add(new Point(0, printedLineScalar.get(nextMusicScheet01).val[1]));
				}else {
					boundingBox.add(new Point(x_ax, printedLineScalar.get(nextMusicScheet02).val[3]));
				}
				nextMusicScheet01 = (j + 1);
				licznik02 += 1;
			}
		}
		if(printedLineScalar.size() > 0) {
			divideImg(srcImg, printedLineScalar, fileNameAndExtension, boundingBox, nextMusicScheet01);
		}else {
			imageProcessingFailed.add(fileNameAndExtension[0]);
		}
	}
	
	public void divideImg(Mat srcImg, List<Scalar> printedLineScalar, String[] fileNameAndExtension, List<Point> boundingBox, int nextMusicScheet01) {
		int x_ax = srcImg.cols();
		
		Imgproc.rectangle(srcImg, new Point(0,
				printedLineScalar.get(nextMusicScheet01).val[1]), 
				new Point(x_ax, printedLineScalar.get(printedLineScalar.size() - 1).val[3]), new Scalar(255, 0, 0), -1, 4, 0);
		boundingBox.add(new Point(x_ax, printedLineScalar.get(printedLineScalar.size() - 1).val[3]));
	
			for(int j = 0; j < boundingBox.size() - 1; j++) {
				if(j % 2 == 0) {
					//przy pierwszym i ostatnim trzeba znalesc +20 i -50
					double maxWidthLastMusicSheet = 0;						
					if ((boundingBox.get(j).y - boundingBox.get(j + 1).y + 50) >= srcImg.size().height) {
						maxWidthLastMusicSheet = srcImg.size().height - (boundingBox.get(j).y);
					} else  {
						maxWidthLastMusicSheet = (boundingBox.get(j).y - boundingBox.get(j + 1).y + 50);
					}
					double minWidthLastMusicSheet = 0;
					if ((boundingBox.get(j + 1).y - 20) <= 0) {
						minWidthLastMusicSheet = 0;
					} else  {
						minWidthLastMusicSheet = (boundingBox.get(j + 1).y - 15);
					}
					Mat oneLineToScan = new Mat(srcImg, new Rect(0, (int)minWidthLastMusicSheet, x_ax, (int)maxWidthLastMusicSheet)).clone();
					Imgcodecs.imwrite(dirDst2 + "\\" + "oneLine_" + fileNameAndExtension[0] + "_" + j + ".png", oneLineToScan);
//					Imgcodecs.imwrite(dirDst4 + "\\" + "oneLine_" + fileNameAndExtension[0] + "_" + j + ".png", oneLineToScan);
				}
				System.gc();
			}	
//			Imgcodecs.imwrite(dirDst2 + "\\" + "box" + fileNameAndExtension[0] + "_.png", srcImg);
//			Imgcodecs.imwrite(dirDst3 + "\\" + "box" + fileNameAndExtension[0] + "_.png", srcImg);
		
	}
	
	public List<Scalar> collectPointOfLIne(List<Point> goodLines, Set<Integer> printedLine, Mat srcImg) {
		Iterator<Integer> itr = printedLine.iterator();
		List<Scalar> printedLineScalar = new ArrayList<Scalar>();
	
		while(itr.hasNext()) {
			int line = (Integer) itr.next();
			line *= 2;
			Imgproc.line(srcImg, goodLines.get(line), goodLines.get(line + 1), new Scalar(0, 0, 255), 2);
			printedLineScalar.add(new Scalar(goodLines.get(line).x, goodLines.get(line).y, goodLines.get(line + 1).x, goodLines.get(line + 1).y));
		}
		
		Collections.sort(printedLineScalar, new Comparator<Scalar>() {
			@Override public int compare(Scalar p1, Scalar p2) {
				return (int) (p2.val[1]- p1.val[1]);
			}
		});
		return printedLineScalar;
	}
	
	public List<Point> createSetOfPointOneLine() {
		List<Point> goodLines = new ArrayList<Point>();
		for(int i = 0; i < setPointOfOneLine.size(); i++) {
			List<Point> listOfPointTemp = setPointOfOneLine.get(i);				
			double minX = listOfPointTemp.get(0).x;
			double maxX = listOfPointTemp.get(0).x;
			for(int j = 1; j < listOfPointTemp.size(); j++) {
				Point p = listOfPointTemp.get(j);
			    if(p.x < minX) {
			    	minX = p.x;
			    }
			    if(p.x > maxX) {
			    	maxX = p.x;
			    }
			}	
			goodLines.add(new Point(minX, listOfPointTemp.get(0).y));
			goodLines.add(new Point(maxX, listOfPointTemp.get(0).y));
		}
		return goodLines;
	}
	
	public Set<Integer> createPointLine(List<Point> goodLines) {
		List<Double> distance = new ArrayList<Double>();
		for(int i = 0; i < goodLines.size() - 2; i++) {
			if(i % 2 == 0) {
				Point p01 = goodLines.get(i);
				Point p02 = goodLines.get(i+2);
				distance.add(Math.abs(p01.y - p02.y));
			}		
		}
				
		List<Integer> musicsheet = new ArrayList<Integer>();
		for(int i = 0; i < distance.size(); i++) {
			if(distance.get(i) >= 18  && distance.get(i) <= 30) 
				musicsheet.add(i);
		}
		Set<Integer> printedLine = new HashSet<Integer>();
		for(int j = 0; j < musicsheet.size() - 1; j++) {
			int i = musicsheet.get(j);
			printedLine.add((i));
			printedLine.add((i + 1));
		}
		return printedLine;
	}
	
	public void detectMusicSheetViolin() {
		getAndCreateDir();
		List<String> loadImageFailed = new ArrayList<String>();
		//List<String> imageProcessingFailed = new ArrayList<String>();
		for (int abc = 0; abc < listOfFileDirSrc.length; abc += 1) {
			String[] fileNameAndExtension = listOfFileDirSrc[abc].getName().split("\\.");
			Mat srcImg = new Mat();
			if(listOfFileDirSrc.length != 1)
				srcImg = Imgcodecs.imread(dirSrc + "\\" + listOfFileDirSrc[abc].getName());
			else
				srcImg = Imgcodecs.imread(listOfFileDirSrc[abc].getPath());
		
			if (srcImg.empty()) {
				System.out.println("Nie mozna zaladowac obrazu");
			}
											
			setPointOfOneLine = new PrepareImgToAnalize(srcImg).houghLines(true);

			List<Point> goodLines = createSetOfPointOneLine();
			Set<Integer> printedLine = createPointLine(goodLines);
			
			
			List<Scalar> printedLineScalar = collectPointOfLIne(goodLines, printedLine, srcImg);
			createDirForOneScan(fileNameAndExtension[0]);
			//createBoundingBox(srcImg, printedLineScalar, fileNameAndExtension);
					
			int nextMusicScheet01 = 0;
			int nextMusicScheet02 = 0;
			int x_ax = srcImg.cols();
			List<Point> boundingBox = new ArrayList<Point>();
			
			for(int j = 0; j < printedLineScalar.size() - 1; j++) {
				double y01 = printedLineScalar.get(j).val[1];
				double y02 = printedLineScalar.get(j + 1).val[1];
				if(Math.abs(y02 - y01) > 32) {
					nextMusicScheet02 = j;	
					Imgproc.rectangle(srcImg, new Point(0,
					printedLineScalar.get(nextMusicScheet01).val[1]), 
					new Point(x_ax, printedLineScalar.get(nextMusicScheet02).val[3]), new Scalar(255, 0, 0), -1, 4, 0);

					boundingBox.add(new Point(0, printedLineScalar.get(nextMusicScheet01).val[1]));
					boundingBox.add(new Point(x_ax, printedLineScalar.get(nextMusicScheet02).val[3]));

					nextMusicScheet01 = (j + 1);
				}
			}

			
			if(printedLineScalar.size() > 0) {
				Imgproc.rectangle(srcImg, new Point(0,
						printedLineScalar.get(nextMusicScheet01).val[1]), 
						new Point(x_ax, printedLineScalar.get(printedLineScalar.size() - 1).val[3]), new Scalar(255, 0, 0), -1, 4, 0);
				boundingBox.add(new Point(0, printedLineScalar.get(nextMusicScheet01).val[1]));
				boundingBox.add(new Point(x_ax, printedLineScalar.get(printedLineScalar.size() - 1).val[3]));
		
				Collections.sort(boundingBox, new Comparator<Point>() {
				    @Override public int compare(Point p1, Point p2) {
			            return (int) (p1.y - p2.y);
			        }
				});		
				
				int jTemp = 0;
				for(int j = 0; j < boundingBox.size() - 2; j++) {
						if(j % 2 == 0) {
							double sizeBeetwenMusicSheet = (boundingBox.get(j + 2).y - boundingBox.get(j).y);
							if(sizeBeetwenMusicSheet >= 500) {
								sizeBeetwenMusicSheet = 250;
							}
							
							
							
							Mat oneLineToScan = new Mat(srcImg, new Rect(0, (int)(boundingBox.get(j).y), x_ax, (int)sizeBeetwenMusicSheet)).clone();
							Imgcodecs.imwrite(dirDst2 + "\\" + "oneLine_" + fileNameAndExtension[0] + "_" + j + ".png", oneLineToScan);
		
//							Imgcodecs.imwrite(dirDst4 + "\\" + "oneLine_" + fileNameAndExtension[0] + "_" + j + ".png", oneLineToScan);
							
						
						}
						jTemp = j;
				}
				//ostatnie + 100 ogolni e+x trzeba znalesc x
				double maxWidthLastMusicSheet = 0;
				if (((boundingBox.get(boundingBox.size() - 2).y + (boundingBox.get(boundingBox.size() - 1).y - boundingBox.get(boundingBox.size() - 2).y) + 180)) >= srcImg.size().height) {
					maxWidthLastMusicSheet = srcImg.size().height - (boundingBox.get(boundingBox.size() - 2).y);
				} else  {
					maxWidthLastMusicSheet = ((boundingBox.get(boundingBox.size() - 1).y - boundingBox.get(boundingBox.size() - 2).y) + 180);
				}

				
				
				Mat oneLineToScan = new Mat(srcImg, new Rect(0, (int)(boundingBox.get(boundingBox.size() - 2).y), x_ax, (int)maxWidthLastMusicSheet)).clone();
				Imgcodecs.imwrite(dirDst2 + "\\" + "oneLine_" + fileNameAndExtension[0] + "_" + jTemp + ".png", oneLineToScan);
//				Imgcodecs.imwrite(dirDst4 + "\\" + "oneLine_" + fileNameAndExtension[0] + "_" + jTemp + ".png", oneLineToScan);
				
				System.gc();					
//				Imgcodecs.imwrite(dirDst2 + "\\" + "box" + fileNameAndExtension[0] + "_.png", srcImg);
//				Imgcodecs.imwrite(dirDst3 + "\\" + "box" + fileNameAndExtension[0] + "_.png", srcImg);
			}else {
				imageProcessingFailed.add(fileNameAndExtension[0]);
			}
			
			System.out.println(fileNameAndExtension[0] + "_ Finish major processing");
			setPointOfOneLine.clear();
			System.gc();
		}
		clearEmptyDir();
		System.out.println("Liczba niezaladowanych plikow " + loadImageFailed.size());
		System.out.println("Liczba nieprzetworzonych plikow " + imageProcessingFailed.size());
		System.out.println(imageProcessingFailed);
	}
	
	public void clearEmptyDir() {
		for(String s: imageProcessingFailed) {
			File listFileCheckedDir[]; 
			File dirCheck = new File(dst + "\\" + s);
			listFileCheckedDir = dirCheck.listFiles();
			if(listFileCheckedDir.length == 0) {
				dirCheck.delete();
			}
		}
	}
	
}
