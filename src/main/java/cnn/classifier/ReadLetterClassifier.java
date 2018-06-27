package cnn.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class ReadLetterClassifier {

	private String src;
	private String dst;
	private File dirSrc;
	private File[] listOfFileDirSrc;
	private File dirDst;
	
	public ReadLetterClassifier(String src, String dst) {
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
	
	public void detectAndRecognizeLetter() throws IOException {
		getAndCreateDir();
		int height = 50;
		int width = 25;
		int channels = 1;

		ArrayList<String> labelList = (ArrayList<String>) Stream
				.of("_a", "_aa", "_b", "_c", "_cc", "_d", "_e", "_ee", "_f", "_g", "_h", "_i", "_j", "_k", "_l", "_ll",
						"_m", "_n", "_nn", "_o", "_oo", "_p", "_r", "_s", "_ss", "_t", "_u", "_w", "_y", "_z", "_zz",
						"_zzz", "a", "aa", "b", "c", "cc", "d", "e", "ee", "f", "g", "h", "i", "j", "k", "l", "ll", "m",
						"n", "nn", "o", "oo", "p", "r", "s", "ss", "t", "u", "w", "y", "z", "zz", "zzz")
				.collect(Collectors.toList());
		String ss = "D:\\PracaDyplomowaEclipse\\eclipseWorkspace\\ImageProcessingDetectTextUltimate\\enimst\\enimst-model.zip";
		File locationToSave = new File(ss/*System.getProperty("user.dir") + "\\enimst" + "\\enimst-model.zip"*/);
		MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
		
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j <  listOfFileDirSrc.length; j += 1) {
			File f;
			if (listOfFileDirSrc.length != 1)
				f = new File(dirSrc + "\\" + listOfFileDirSrc[j].getName());
			else
				f = new File(listOfFileDirSrc[j].getPath());

			NativeImageLoader nil = new NativeImageLoader(height, width, channels);
			INDArray image = nil.asMatrix(f);
			DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
			scaler.transform(image);
			INDArray out = model.output(image);
			INDArray outToMap = out.transpose();
			HashMap<String, Float> labelAndOut = new HashMap<String, Float>();
			for (int i = 0; i < out.columns(); i += 1) {
				labelAndOut.put(labelList.get(i), outToMap.getFloat(i));
			}

			ArrayList<HashMap.Entry<String, Float>> entryOrderList = new ArrayList<HashMap.Entry<String, Float>>();

			HashMap.Entry<String, Float> maxEntry = null;
			for (HashMap.Entry<String, Float> entry : labelAndOut.entrySet()) {
				if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
					maxEntry = entry;
				}
			}
			entryOrderList.add(maxEntry);
//			System.out.println("1 " + maxEntry.getKey() + " " + maxEntry.getValue());
			labelAndOut.remove(maxEntry.getKey());

			maxEntry = null;
			for (HashMap.Entry<String, Float> entry : labelAndOut.entrySet()) {
				if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
					maxEntry = entry;
				}
			}
			entryOrderList.add(maxEntry);
//			System.out.println("2 " + maxEntry.getKey() + " " + maxEntry.getValue());
			labelAndOut.remove(maxEntry.getKey());

			maxEntry = null;
			for (HashMap.Entry<String, Float> entry : labelAndOut.entrySet()) {
				if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
					maxEntry = entry;
				}
			}
			entryOrderList.add(maxEntry);
//			System.out.println("3 " + maxEntry.getKey() + " " + maxEntry.getValue());

			// jesli jest podkreslnik to to upper
			Pattern compiledPattern = Pattern.compile("_");

			ArrayList<HashMap<String, Float>> entryOrderListReplace = new ArrayList<HashMap<String, Float>>();
			for (HashMap.Entry<String, Float> entry : entryOrderList) {
				Matcher matcher = compiledPattern.matcher(entry.getKey());
				if (matcher.find()) {
					HashMap<String, Float> temp = new HashMap<String, Float>();
					String key;
					if (entry.getKey().substring(1).length() > 1) {
						key = replaceForDiacriticalMark(entry.getKey().substring(1));
					} else {
						key = entry.getKey().substring(1);
					}
					temp.put(key.toUpperCase(), entry.getValue());
					entryOrderListReplace.add(temp);
				} else {
					HashMap<String, Float> temp = new HashMap<String, Float>();
					String key;
					if (entry.getKey().length() > 1) {
						key = replaceForDiacriticalMark(entry.getKey());
					} else {
						key = entry.getKey();
					}
					temp.put(key, entry.getValue());
					entryOrderListReplace.add(temp);
				}
			}

			ArrayList<ArrayList<String>> maxEntryList = new ArrayList<ArrayList<String>>();
			for (HashMap<String, Float> entry : entryOrderListReplace) {
				for (HashMap.Entry<String, Float> entryTemp : entry.entrySet()) {
//					System.out.println(entryTemp.getKey() + " " + entryTemp.getValue());
					ArrayList<String> maxEntryListTemp = (ArrayList<String>) Stream
							.of(entryTemp.getKey(), entryTemp.getValue().toString()).collect(Collectors.toList());
					maxEntryList.add(maxEntryListTemp);
				}
			}

			ITesseract instance = new Tesseract();
			instance = new Tesseract();
			instance.setDatapath(System.getProperty("user.dir") + "\\lib\\Tess4J-3.4.0-src\\Tess4J");
			instance.setLanguage("pol");

			try {
				String resultTes = instance.doOCR(f);
				String tesOk = resultTes.replaceAll("\\s", "").replaceAll("[-+.^:,\')(\\{!@#$%&*/}\"/?<>|_]", "").replaceAll("[\\[\\]\"]", "");
//				System.out.println("Result teseract " + tesOk.length() + tesOk);

				if (((tesOk.equals(""))) && Float.valueOf(maxEntryList.get(0).get(1)) < 0.5) {
					// DO NOTHING
					// prawdopodobne smieci lub spacja
					sb.append(" ");
				} else {
					if(tesOk.length() > 1) {
						sb.append(tesOk);
					}else if(tesOk.length() == 1) {
						if (Character.isLetter(tesOk.charAt(0))) {
							if (tesOk.equals(maxEntryList.get(0).get(0))) {
								sb.append(tesOk);
							} else if (tesOk.equals(maxEntryList.get(1).get(0))) {
								sb.append(tesOk);
							} else if (tesOk.equals(maxEntryList.get(2).get(0))) {
								sb.append(tesOk);
							} else if (Float.valueOf(maxEntryList.get(0).get(1)) > 0.9) {
								sb.append(maxEntryList.get(0).get(0));
							} else {
								sb.append(" ");
							}
						}else {
							if (Float.valueOf(maxEntryList.get(0).get(1)) > 0.9) {
								sb.append(maxEntryList.get(0).get(0));
							} else {
								sb.append(" ");
							}
						}
					}else {
						if (Float.valueOf(maxEntryList.get(0).get(1)) > 0.9) {
							sb.append(maxEntryList.get(0).get(0));
						} else {
							sb.append(" ");
						}
					}
				}
			} catch (TesseractException e) {
				System.err.println(e.getMessage());
			}
		}
		
		System.out.println("Read letter " + dirSrc + " dir finish");
		
		try (PrintWriter p = new PrintWriter(new FileOutputStream(dirDst + "\\" + dirSrc.getName() + ".txt", true))) {
		    p.println(sb.toString());
		    p.println("\n");
		} catch (FileNotFoundException e1) {
		    e1.printStackTrace();
		}
		
	}

	public String replaceForDiacriticalMark(String s) {
		switch (s) {
		case "aa":
			return "ą";
		case "cc":
			return "ć";
		case "ee":
			return "ę";
		case "ll":
			return "ł";
		case "nn":
			return "ń";
		case "oo":
			return "ó";
		case "ss":
			return "ś";
		case "zz":
			return "ż";
		case "zzz":
			return "ź";
		default:
			return "";
		}
	}

}
