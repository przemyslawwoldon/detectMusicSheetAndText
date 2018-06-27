package img.proccesing;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

public class CutBlackArea {

	private String src;
	private String dst;
	private boolean cutOnlyHorizontal;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	private final int maxStdDev = 20;// 25
	private final int errorOY = 10;// 15

	public CutBlackArea(String src, String dst, boolean cutOnlyHorizontal) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.src = src;
		this.dst = dst;
		this.cutOnlyHorizontal = cutOnlyHorizontal;
	}

	private void getAndCreateDir() {
		dirSrc = new File(src);
		if (dirSrc.isDirectory()) {
			listOfFileDirSrc = dirSrc.listFiles();
		} else {
			listOfFileDirSrc = new File[1];
			listOfFileDirSrc[0] = dirSrc;
		}
		dirDst = new File(dst);

		if (!dirDst.exists()) {
			try {
				dirDst.mkdirs();
				System.out.println("DIR created" + dst);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public void cutImg() {
		getAndCreateDir();
		List<String> loadImageFailed = new ArrayList<String>();
		for (int j = 0; j < listOfFileDirSrc.length; j += 1) {
			Mat srcImg = new Mat();
			if (listOfFileDirSrc.length != 1)
				srcImg = Imgcodecs.imread(dirSrc + "\\" + listOfFileDirSrc[j].getName(),
						Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			else
				srcImg = Imgcodecs.imread(listOfFileDirSrc[j].getPath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

			if (srcImg.empty()) {
				System.out.println("Nie mozna zaladowac obrazu" + "\\" + listOfFileDirSrc[j].getName());
				loadImageFailed.add(listOfFileDirSrc[j].getName().toString());
				continue;
			}
			cut(srcImg, j);
		}
		System.out.println("Liczba niezaladowanych plikow " + loadImageFailed.size());
	}

	public void cut(Mat srcImg, int i) {
		int nameFile = 0;
		String[] fileNameAndExtension = listOfFileDirSrc[i].getName().split("\\.");
		Mat tempToHorizontalCut = cutOneImg(srcImg, true);
		Imgcodecs.imwrite(dirDst + "\\" + listOfFileDirSrc[i].getName(), tempToHorizontalCut);
		Mat tempToVerticalCut;
		if (!this.cutOnlyHorizontal) {
			tempToVerticalCut = cutOneImg(tempToHorizontalCut, false);
			Imgcodecs.imwrite(dirDst + "\\" + listOfFileDirSrc[i].getName(), tempToVerticalCut);
		} else {
			Imgcodecs.imwrite(dirDst + "\\" + listOfFileDirSrc[i].getName(), tempToHorizontalCut);
		}
		System.gc();
		System.out.println("Cut black area " + fileNameAndExtension[nameFile] + " img finish");
	}

	// cutVerticalOneImg() + cutHorizontalOneImg() -> cutOneImg()
	public Mat cutOneImg(Mat srcImg, boolean isHorizontal) {
		Mat toCut = srcImg;
		if (!isHorizontal) {
			Mat temp = new Mat((int) srcImg.size().height, (int) srcImg.size().width, 0);
			Core.transpose(srcImg, temp);
			Mat dstFlip = new Mat();
			Core.flip(temp, dstFlip, -1);
			toCut.release();
			toCut = dstFlip;
		}
		int width = (int) toCut.size().width;
		List<Double> stdDevList = calculateStdDev(toCut, width);
		if (stdDevList.size() > 0 && stdDevList != null) {
			List<Double> keyModeStdDev = calculateModeOfStdDev(stdDevList);
			if (keyModeStdDev.size() > 0 && keyModeStdDev != null) {
				int j = calculateFirstRowOfBlackArea(keyModeStdDev, stdDevList);
				if (j < (int) (srcImg.size().height / 2)) {
					j = (int) srcImg.size().height;
				}
				
//				System.out.println(toCut.size().height + " " + toCut.size().width);
//				System.out.println(width + " " + (j - errorOY));
//				System.out.println(errorOY);
				
				Mat newImage;
				if((j - errorOY) > (toCut.size().height-5)) {
					newImage  = new Mat(toCut, new Rect(0, 0, width, j)).clone();
				}else {
					newImage = new Mat(toCut, new Rect(0, 0, width, (j - errorOY))).clone();
				}
				
				
				if (!isHorizontal) {
					Core.flip(newImage, newImage, 1);
					Core.rotate(newImage, newImage, Core.ROTATE_90_CLOCKWISE);
					System.out.println("Done vertical");
				} else {
					System.out.println("Done horizontal");
				}
				return newImage;
			}
		}
		return srcImg;
	}

	public void toGrayScale(Mat srcImg) {
		for (int y = 0; y < srcImg.rows(); y += 1) {
			for (int x = 0; x < srcImg.cols(); x += 1) {
				double rgb[] = srcImg.get(y, x);
				double greyScale = (rgb[0] + rgb[1] + rgb[2]) / 3;
				srcImg.put(y, x, greyScale, greyScale, greyScale);
			}
		}
	}

	public List<Double> calculateStdDev(Mat srcImg, int width) {
		List<Double> stdDevList = new ArrayList<Double>();
		for (int j = 0; j < srcImg.rows(); j += 1) {
			Mat temp = new Mat(srcImg, new Rect(0, j, width, 1)).clone();
			MatOfDouble mean = new MatOfDouble();
			MatOfDouble stddev = new MatOfDouble();
			Core.meanStdDev(temp, mean, stddev);
			stdDevList.add(stddev.get(0, 0)[0]);
			System.gc();
		}
		return stdDevList;
	}

	public List<Double> calculateModeOfStdDev(List<Double> stdDevList) {
		Map<Double, Integer> calculateRepresentationOfValue = new HashMap<Double, Integer>();
		for (double d : stdDevList) {
			calculateRepresentationOfValue.compute(d, (k, v) -> v == null ? 1 : v + 1);
		}
		Map<Double, Integer> modeSpecificScope = calculateRepresentationOfValue.entrySet().stream()
				.filter(map -> map.getKey() < maxStdDev).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

		List<Double> keyModeStdDev = new ArrayList<Double>();
		if (modeSpecificScope.size() > 0 && modeSpecificScope != null) {
			long modeStdDev = modeSpecificScope.values().stream().max(Comparator.naturalOrder()).get();
			keyModeStdDev = modeSpecificScope.entrySet().stream().filter(e -> e.getValue() == modeStdDev)
					.map(Map.Entry::getKey).collect(Collectors.toList());
		}
		return keyModeStdDev;
	}

	public int calculateFirstRowOfBlackArea(List<Double> keyModeStdDev, List<Double> stdDevList) {
		int k = 0;
		if (!keyModeStdDev.isEmpty()) {
			for (k = stdDevList.size() - 1; k >= 0; k -= 1) {
				if (stdDevList.get(k).equals(keyModeStdDev.get(0))) {
					break;
				}
			}
			for (int a = k; a >= 0; a -= 1) {
				if (!stdDevList.get(a).equals(keyModeStdDev.get(0))) {
					k = a;
					break;
				}
			}
		}
		return k;
	}
}
