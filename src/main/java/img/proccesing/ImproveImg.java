package img.proccesing;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImproveImg {
	
	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	
	public ImproveImg(String src, String dst) {
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
//				System.out.println("DIR created" + dst);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void improveQualityImg() {
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
//				System.out.println("Nie mozna zaladowac obrazu" + "\\" + listOfFileDirSrc[j].getName());
				loadImageFailed.add(listOfFileDirSrc[j].getName().toString());
				continue;
			}
			Mat srcImgBlur = new Mat();
			Imgproc.medianBlur(srcImg, srcImgBlur, 5); //>5 rozmyte zabardzo
			Mat dstImg = new Mat();
			Imgproc.threshold(srcImgBlur, dstImg, 170, 255, Imgproc.THRESH_BINARY); //160 :(
			Imgcodecs.imwrite(dirDst + "\\" + listOfFileDirSrc[j].getName(), dstImg);
//			System.out.println("Improve " + fileNameAndExtension[nameFile] + " img finish");
			System.gc();
		}
//		System.out.println("Liczba niezaladowanych plikow " + loadImageFailed.size());
	}
			
}
