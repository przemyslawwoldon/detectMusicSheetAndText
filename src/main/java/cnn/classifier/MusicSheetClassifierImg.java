package cnn.classifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class MusicSheetClassifierImg {

	private String src;
	
	public MusicSheetClassifierImg(String src) {
		this.src = src;
	}
	
	public boolean isText() throws IOException  {
		// TODO Auto-generated method stub
		int height = 600;
		int width = 15;
		int channels = 1;

		ArrayList<String> labelList = (ArrayList<String>) Stream.of("ms", "t").collect(Collectors.toList());

		String filechooser = src;
		String path = System.getProperty("user.dir") + "\\musicSheetText" + "\\textAndMusicSheet-model.zip";

		File locationToSave = new File(path);
		MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

		File f = new File(filechooser);
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
//		System.out.println("1 " + maxEntry.getKey() + " " + maxEntry.getValue() + " " + entryOrderList.size());
		
		ArrayList<ArrayList<String>> maxEntryList = new ArrayList<ArrayList<String>>();
		
		for (HashMap.Entry<String, Float> entry : entryOrderList) {
			ArrayList<String> maxEntryListTemp = (ArrayList<String>) Stream
					.of(entry.getKey(), entry.getValue().toString()).collect(Collectors.toList());
			maxEntryList.add(maxEntryListTemp);
		}

		if((maxEntryList.get(0).get(0)).equals("t")) {
			if(Float.valueOf(maxEntryList.get(0).get(1)) > 0.9) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
//		log.info("## The file chosen was " + filechooser);
//		System.out.println(out.toString());
//		System.out.println(labelList.toString());
//		return false;
	}

}
