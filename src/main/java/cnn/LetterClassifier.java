package cnn;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
//import org.deeplearning4j.examples.utilities.DataUtilities;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
//import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class LetterClassifier {

  private String basePath; //System.getProperty("user.dir") +  "/enimst";

  public LetterClassifier(String basePath) {
	  this.basePath = basePath;
  }
  
  public void createTrainingCNNEmnist() throws Exception {
    int height = 50;
    int width = 25;
    int channels = 1; // single channel for grayscale images
    int outputNum = 64; // 10 digits classification
    int batchSize = 128;
    int nEpochs = 64;
    //int iterations = 1024;
    int seed = 1234;
    Random randNumGen = new Random(seed);

    System.out.println("Data load and vectorization...");
    File trainData = new File(basePath + "/img/training");
    FileSplit trainSplit = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
    ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator(); // parent path as the image label
    ImageRecordReader trainRR = new ImageRecordReader(height, width, channels, labelMaker);
    trainRR.initialize(trainSplit);
    DataSetIterator trainIter = new RecordReaderDataSetIterator(trainRR, batchSize, 1, outputNum);

    DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
    scaler.fit(trainIter);
    trainIter.setPreProcessor(scaler);

    File testData = new File(basePath + "/img/testing");
    FileSplit testSplit = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
    ImageRecordReader testRR = new ImageRecordReader(height, width, channels, labelMaker);
    testRR.initialize(testSplit);
    DataSetIterator testIter = new RecordReaderDataSetIterator(testRR, batchSize, 1, outputNum);
    testIter.setPreProcessor(scaler); // same normalization for better results

    System.out.println("Network configuration and training...");
    Map<Integer, Double> lrSchedule = new HashMap<>();
    lrSchedule.put(0, 0.001); // iteration #, learning rate
    lrSchedule.put(100, 0.0015);
    lrSchedule.put(200, 0.0001);
    lrSchedule.put(400, 0.00015);
    lrSchedule.put(800, 0.00001);
    lrSchedule.put(1200, 0.000015);
    lrSchedule.put(1500, 0.0001);
    
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(seed)
        .l2(0.0015 * 0.005)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .updater(new Nesterovs(new MapSchedule(ScheduleType.ITERATION, lrSchedule)))
        .weightInit(WeightInit.XAVIER)
        .list()
        .layer(0, new ConvolutionLayer.Builder(5, 5)
            .nIn(channels)
            .stride(1, 1)
            .nOut(20)
            .activation(Activation.TANH)
            .build())
        .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
            .kernelSize(4, 4)
            .stride(2, 2)
            .build())
        .layer(2, new ConvolutionLayer.Builder(3, 3)
            .stride(1, 1) // nIn need not specified in later layers
            .nOut(100)
            .activation(Activation.TANH)
            .build())
        .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
            .kernelSize(2, 2)
            .stride(1, 1)
            .build())
        .layer(4, new ConvolutionLayer.Builder(1, 1)
            .stride(1, 1) // nIn need not specified in later layers
            .nOut(200)
            .activation(Activation.TANH)
            .build())
        .layer(5, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
            .kernelSize(1, 1)
            .stride(1, 1)
            .build())
        .layer(6, new DenseLayer.Builder().activation(Activation.RELU)
            .nOut(500).build())
        .layer(7, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
            .nOut(outputNum)
            .activation(Activation.SOFTMAX)
            .build())
        .setInputType(InputType.convolutionalFlat(50, 25, 1)) // InputType.convolutional for normal image
        .backprop(true).pretrain(false).build();

    MultiLayerNetwork net = new MultiLayerNetwork(conf);
    net.init();
    net.setListeners(new ScoreIterationListener(10));
    System.out.println("Total num of params: {} " + net.numParams());

    // evaluation while training (the score should go down)
    for (int i = 0; i < nEpochs; i++) {
      net.fit(trainIter);
    }
    
    System.out.println("Completed train");
    trainIter.reset();
    Evaluation eval = net.evaluate(testIter);
    System.out.println(eval.stats(false, true));
    System.out.println(eval.confusionMatrix());
    testIter.reset();
    ModelSerializer.writeModel(net, new File(basePath + "/enimst-model.zip"), false);
  }

}