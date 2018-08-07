package mainApp.view;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

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
import javafx.fxml.FXML;
import mainApp.App;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

public class MainAppController {
	 
	@FXML
	private Button inPath;
	
	@FXML
	private Button outFile;
	
	@FXML
	private TextArea console;
	
	@FXML
	private ProgressBar progress;
	
	@FXML
	private CheckBox cutVertical;
	
	@FXML
	private CheckBox bass;
	
	@FXML
	private CheckBox violinBass;
	
	
	private App mainApp;
	private File file;
	private String dstTemp;
	
    @FXML
    private void initialize() {
    	outFile.setDisable(true);
    	console.setText("");
    	console.setDisable(true);
    	progress.setProgress(0);
    	violinBass.setSelected(true);
	}

    @FXML
    private void chosseMusicSheetBass() {
    	if(bass.isSelected()) {
    		violinBass.setSelected(false);
    	}else {
    		violinBass.setSelected(true);
    	}
    }
    
    @FXML
    private void chosseMusicSheetViolin() {
    	if(violinBass.isSelected()) {
    		bass.setSelected(false);
    	}else {
    		bass.setSelected(true);
    	}
    }
    
    
    
    @FXML
    private void handleGetPath() throws Exception {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Resource File");
    	file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
    	if(file != null) {
	    	this.dstTemp = System.getProperty("java.io.tmpdir");
	    	String fromPdf = this.dstTemp + "\\img\\fromPdf";
	    	
	    	double stepProgres = 0.1;
	    	double progres = 0.0;
	    	
	    	createDir(fromPdf);
	    	getImgFromPdf(file.getAbsolutePath(), fromPdf, file.getName());
	    	progress.setProgress(progres += stepProgres);
	    	String impr = this.dstTemp + "\\img\\improveImg";
	    	improveImg(fromPdf, impr);
	    	deleteDir(fromPdf);
	    	
	    	String cutBlackArea = this.dstTemp + "\\img\\cutBlackArea";
	    	if(this.cutVertical.isSelected()) {
	    		cutBackArea(impr, cutBlackArea, false);
	    	}else {
	    		cutBackArea(impr, cutBlackArea, true);
	    	}
	    	deleteDir(impr);
	    	progress.setProgress(progres += stepProgres);
	    	
	    	String dividePage = this.dstTemp + "\\img\\divideToPage"; 
	    	divideToPage(cutBlackArea, dividePage);
	    	deleteDir(cutBlackArea);
	    	progress.setProgress(progres += stepProgres);
	    	String straightenUp = this.dstTemp + "\\img\\straightenUp"; 
	    	toStraightenUp(dividePage, straightenUp);
	    	deleteDir(dividePage);
	    	progress.setProgress(progres += stepProgres);
	    	String majorProc = this.dstTemp + "\\img\\majorProcessing";
	    	if(bass.isSelected()) {
	        	new MajorProcessing(straightenUp, majorProc).detectMusicSheetViolin();
	    	} else{
	    		new MajorProcessing(straightenUp, majorProc).detectMusicSheetViolinAndBass();
	    	}
	    	this.console.appendText("Major processing Img finish.\n");
	    	progress.setProgress(progres += stepProgres);
	    	
	    	deleteDir(straightenUp);	
	    	
	     	createCnnText();
	     	progress.setProgress(progres += stepProgres);
	    	String detectTxt = this.dstTemp  + "\\img\\detectTxt";
	    	if(bass.isSelected()) {
	    		detectText(majorProc, detectTxt, false);
	    	} else{
	    		detectText(majorProc, detectTxt, true);
	    	}
	    	deleteDir(majorProc);
	    	progress.setProgress(progres += stepProgres);
	    	createCnnLetter();
	    	progress.setProgress(progres += stepProgres);
	    	String detectChar = this.dstTemp  + "\\img\\detectChar";
	    	detectLetter(detectTxt, detectChar);
	    	deleteDir(detectTxt);
	    	progress.setProgress(progres += stepProgres);
	    	String readLetter = this.dstTemp  + "\\img\\read";
	    	readLetter(detectChar, readLetter);
	    	deleteDir(detectChar);
	    	progress.setProgress(progres += stepProgres);
	    	this.console.appendText("Path" + this.dstTemp  + "\\img\\read .\n");
    	}
    }
    
    public void getImgFromPdf(String src, String dst, String name) throws InterruptedException {
    	new GetImgFromPdf(src, dst).getImage();
    	this.console.setText("Get Img from pdf finish " + name + " file.\n");
    }
    
    public void improveImg(String src, String dst) throws InterruptedException {
    	new ImproveImg(src, dst).improveQualityImg();
    	this.console.appendText("Improve Img finish.\n");
    }
    
    public void cutBackArea(String src, String dst, boolean b) throws InterruptedException {
    	new CutBlackArea(src, dst, b).cutImg();
    	this.console.appendText("Cut black area Img finish.\n");
	} 
     
    public void divideToPage(String src, String dst) throws InterruptedException {
    	new DivideToPage(src, dst).divideToTwoPage();
    	this.console.appendText("Divide to page Img finish.\n");
    }
    
    public void toStraightenUp(String src, String dst) throws InterruptedException {
    	new ToStraightenUp(src, dst).straightenUp();
    	this.console.appendText("Straighten up Img finish.\n");
    }
    
    public void createCnnText() throws IOException {
    	File cnnLineCalssifier = new File(dstTemp + "\\img\\musicSheetText\\textAndMusicSheet-model.zip");
    	if(!cnnLineCalssifier.exists()) { 
    		this.console.appendText("Create cnn (text) start.\n");
    		MusicSheetClassifier msc = new MusicSheetClassifier(dstTemp + "\\img\\musicSheetText");
    		msc.createTrainingCNNTextMusicSheet();
    		this.console.appendText("Create cnn (text) end.\n");
    	}
    }
    
    public void createCnnLetter() throws Exception {
    	File cnnLetterCalssifier = new File(dstTemp + "\\img\\enimst\\enimst-model.zip");
    	if(!cnnLetterCalssifier.exists()) { 
    		this.console.appendText("Create cnn (char) start.\n");    		
    		LetterClassifier letterC = new LetterClassifier(dstTemp + "\\img\\enimst");
    		letterC.createTrainingCNNEmnist();
    		this.console.appendText("Create cnn (char) end.\n");
    	}
    }
    
    public void detectText(String src, String dst, boolean b) throws IOException {
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
    	this.console.appendText("Detect text finish.\n");  
    }
    
    public void detectLetter(String src, String dst) {
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
    	this.console.appendText("Detect letter finish.\n");
    }
    
    public void readLetter(String src, String dst) throws IOException {
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
    	this.console.appendText("Read letter finish.\n");
    }
    
    public void createDir(String s) {
    	File dirDst= new File(s);
		if (!dirDst.exists()) {
			try {
				dirDst.mkdirs();
				System.out.println("DIR created" + s);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
    }
    
    public void deleteDir(String s) throws IOException {
		File dirDst= new File(s);
		if (dirDst.exists()) {
			FileUtils.deleteDirectory(dirDst);
			System.out.println("Del: " + s);
		}
    }
    
    public void setMainApp(App mainApp) {
        this.mainApp = mainApp;
    }
}
