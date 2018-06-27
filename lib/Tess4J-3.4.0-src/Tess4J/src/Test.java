import java.io.File;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Test {

	public static void main(String[] args) {
		File imageFile = new File("C:\\Users\\Przemyslaw\\Desktop\\eurotext.bmp");
		ITesseract instance = new Tesseract();
		instance.setLanguage("pol");
		try {
			String result = instance.doOCR(imageFile);
			System.out.println(result);
		} catch (TesseractException e) {
			System.out.println(":(");
		}

	}

}
