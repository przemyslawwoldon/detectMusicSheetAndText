

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import cnn.LetterClassifier;
import cnn.MusicSheetClassifier;
import cnn.classifier.ReadLetterClassifier;
import img.proccesing.CutBlackArea;
import img.proccesing.DetectLetter;
import img.proccesing.DetectText;
import img.proccesing.DivideToPage;
import img.proccesing.GetImgFromPdf;
import img.proccesing.ImproveImg;
import img.proccesing.MajorProcessing;
import img.proccesing.ToStraightenUp;

/**
 * Hello world!
 *
 */
public class App {
	
    public static void main( String[] args ) throws Exception {
    	
//    	String srcFromPdf01 = System.getProperty("user.dir") + "\\pdf\\spiewniki_skany_pdf\\spiewnik_01";	
//    	String srcFromPdf02 = System.getProperty("user.dir") + "\\pdf\\spiewniki_skany_pdf\\spiewnik_02";	
//    	
//    	String dstFromPdf01 = System.getProperty("user.dir") + "\\img\\spiewnik_01";	
//    	String dstFromPdf02 = System.getProperty("user.dir") + "\\img\\spiewnik_02";	
//    	getImgFromPdf(srcFromPdf01, dstFromPdf01);
//    	getImgFromPdf(srcFromPdf02, dstFromPdf02);
//    	
//    	String dstImproveImg01 = System.getProperty("user.dir") + "\\img\\spiewnik_03";	
//    	String dstImproveImg02 = System.getProperty("user.dir") + "\\img\\spiewnik_04";	
//    	improveImg(dstFromPdf01, dstImproveImg01);
//    	improveImg(dstFromPdf02, dstImproveImg02);
//    	
//    	String dstCutBlackArea01 = System.getProperty("user.dir") + "\\img\\spiewnik_05";	
//    	String dstCutBlackArea02 = System.getProperty("user.dir") + "\\img\\spiewnik_06";
//    	cutBackArea(dstImproveImg01, dstCutBlackArea01, true);
//    	cutBackArea(dstImproveImg02, dstCutBlackArea02, false);
//
//    	String dstDivide01 = System.getProperty("user.dir") + "\\img\\spiewnik_07";	
//    	String dstDivide02 = System.getProperty("user.dir") + "\\img\\spiewnik_08";	
//    	divideToPage(dstCutBlackArea01, dstDivide01);
//    	divideToPage(dstCutBlackArea02, dstDivide02);
//    	
//    	String dstToStraightenUp01 = System.getProperty("user.dir") + "\\img\\spiewnik_11";	
//    	String dstToStraightenUp02 = System.getProperty("user.dir") + "\\img\\spiewnik_12";	
//    	toStraightenUp(dstDivide01, dstToStraightenUp01);
//    	toStraightenUp(dstDivide02, dstToStraightenUp02);
//    	
//    	String dstMajor01 = System.getProperty("user.dir") + "\\img\\spiewnik_13";	
//    	String dstMajor02 = System.getProperty("user.dir") + "\\img\\spiewnik_14";	
//    	new MajorProcessing(dstToStraightenUp01, dstMajor01).detectMusicSheetViolinAndBass();
//    	new MajorProcessing(dstToStraightenUp02, dstMajor02).detectMusicSheetViolin();
    	
    	createCnnText();
    	
//    	String dstDetectText01 = System.getProperty("user.dir") + "\\img\\spiewnik_15";
//    	String dstDetectText02 = System.getProperty("user.dir") + "\\img\\spiewnik_16";
//    	detectText(dstMajor01, dstDetectText01, true);
//    	detectText(dstMajor02, dstDetectText02, false);
    	
    	createCnnLetter();
    	
    	String dstLetter01 = System.getProperty("user.dir") + "\\img\\spiewnik_17";
    	String dstLetter02 = System.getProperty("user.dir") + "\\img\\spiewnik_18";
//    	detectLetter(dstDetectText01, dstLetter01);
//    	detectLetter(dstDetectText02, dstLetter02);
    	
    	String dstReadLetter01 = System.getProperty("user.dir") + "\\img\\text_01";
    	String dstReadLetter02 = System.getProperty("user.dir") + "\\img\\text_02";
    	readLetter(dstLetter01, dstReadLetter01);
    	readLetter(dstLetter02, dstReadLetter02);
    }
    
    public static void getImgFromPdf(String src, String dst) {
    	new GetImgFromPdf(src, dst).getImage();
    }
    
    public static void improveImg(String src, String dst) {
    	new ImproveImg(src, dst).improveQualityImg();
    }
    
    public static void cutBackArea(String src, String dst, boolean b) {
    	new CutBlackArea(src, dst, b).cutImg();
    } 
     
    public static void divideToPage(String src, String dst) {
    	new DivideToPage(src, dst).divideToTwoPage();
    }
    
    public static void toStraightenUp(String src, String dst) {
    	new ToStraightenUp(src, dst).straightenUp();
    }
    
    public static void createCnnText() throws IOException {
    	File cnnLineCalssifier = new File(System.getProperty("user.dir") + "\\musicSheetText\\textAndMusicSheet-model.zip");
    	if(!cnnLineCalssifier.exists()) { 
    		MusicSheetClassifier msc = new MusicSheetClassifier(System.getProperty("user.dir") + "\\musicSheetText");
    		msc.createTrainingCNNTextMusicSheet();
    	}
    }
    
    public static void detectText(String src, String dst, boolean b) throws IOException {
     	File parent01 = new File(src);
    	String path01[] = parent01.list(new FilenameFilter() {
    		@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
    		}
		});
    	
    	for(String s : path01) {
    		new DetectText(src + "\\" + s, dst + "\\" + s, b).detectLineWithText();
    	}
    }

    public static void createCnnLetter() throws Exception {
    	File cnnLetterCalssifier = new File(System.getProperty("user.dir") + "\\enimst\\enimst-model.zip");
    	if(!cnnLetterCalssifier.exists()) { 
    		System.out.println("k");
    		LetterClassifier letterC = new LetterClassifier(System.getProperty("user.dir") + "\\enimst");
    		letterC.createTrainingCNNEmnist();
    	}
    }
    
    public static void detectLetter(String src, String dst) {
    	File parent11 = new File(src);
    	String path11[] = parent11.list(new FilenameFilter() {
    		@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
    		}
		});
    	for(String	s : path11) {
    		new DetectLetter(src + "\\" + s, dst + "\\" + s).detectLetterUseContours();
    	}
    }
    
    public static void readLetter(String src, String dst) throws IOException {
    	File parent11 = new File(src);
    	String path11[] = parent11.list(new FilenameFilter() {
    		@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
    		}
		});
    	for(String	s : path11) {
    		new ReadLetterClassifier(src + "\\" + s, dst).detectAndRecognizeLetter();
    	}
    }
}
