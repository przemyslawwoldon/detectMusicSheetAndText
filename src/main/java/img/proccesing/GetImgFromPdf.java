package img.proccesing;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class GetImgFromPdf {
	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;

	public GetImgFromPdf(String src, String dst) {
		this.src = src;
		this.dst = dst;
	}

	private boolean getAndCreateDir() {
		dirSrc = new File(src);
		if (dirSrc.isDirectory()) {
			listOfFileDirSrc = dirSrc.listFiles();
		} else {
			listOfFileDirSrc = new File[1];
			listOfFileDirSrc[0] = dirSrc;
			String[] fileNameAndExtension = listOfFileDirSrc[0].getName().split("\\.");
			int extentionOfFile = 1;
			if (!fileNameAndExtension[extentionOfFile].equals("pdf"))
				return false;
		}
		dirDst = new File(dst);
		if (!dirDst.exists()) {
			try {
				dirDst.mkdirs();
				System.out.println("DIR created " + dst);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public void getImage() {
		if (this.getAndCreateDir()) {
			final int dpi = 400;	
			int nameFile = 0;
			PDDocument document;
			int numberOfPages = 0;
			PDFRenderer renderer;
			BufferedImage image;
			for (int j = 0; j < listOfFileDirSrc.length; j += 1) {
				try {
					document = PDDocument.load(listOfFileDirSrc[j]);
					numberOfPages = document.getNumberOfPages();
					renderer = new PDFRenderer(document);
					String[] fileNameAndExtension = listOfFileDirSrc[j].getName().split("\\.");
					for (int i = 0; i < numberOfPages; i += 1) {
						image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
						ImageIO.write(image, "JPEG",
								new File(dst + "\\" + fileNameAndExtension[nameFile] + "_" + i + ".png"));
//						System.out.println(fileNameAndExtension[nameFile] + "_" + i);
					}
					System.out.println("Get Img from " + fileNameAndExtension[nameFile] + " pdf finish");
					document.close();
					System.gc();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
