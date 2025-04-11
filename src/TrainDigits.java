import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TrainDigits {

    public static void main(String[] args) throws IOException {
        int inputSize = 28 * 28; // 784 pixels
        int hiddenSize = 128; // Hidden neurons
        double learningRate = 0.01;
        int digitsNum = 10;
        int epochs = 1;

        MLP[] perceptrons = new MLP[digitsNum]; // One model per digit

        // Load MNIST dataset
        double[][] trainingData = MNISTLoader.loadFeatures("training_data\\mnist_train.csv"); // 2D array of images
        int[] labels = MNISTLoader.loadLabels("training_data\\mnist_train.csv"); // Corresponding digit labels
        double[][] testingData = MNISTLoader.loadFeatures("training_data\\mnist_test.csv"); // 2D array of images
        int[] testinglabels = MNISTLoader.loadLabels("training_data\\mnist_test.csv"); // Corresponding digit labels

        BufferedImage recreatedImage = FeatureExtractor.recreateImage(trainingData[100], 28, 28);
        File output = new File("recreated_image.png");
        ImageIO.write(recreatedImage, "png", output);

        for (int digit = 0; digit < digitsNum; digit++) {
            perceptrons[digit] = new MLP(inputSize, hiddenSize, learningRate);
            perceptrons[digit].loadWeights("mlp_digit_" + digit + ".txt");  // Load weights if available
        }

        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Epoch " + (epoch + 1));

            // === Training Phase ===
            for (int i = 0; i < trainingData.length; i++) {
                int label = labels[i];
                double[] image = trainingData[i];

                for (int digit = 0; digit < digitsNum; digit++) {
                    int target = (label == digit) ? 1 : 0;
                    perceptrons[digit].train(image, target);
                }
            }

            // === Evaluation Phase ===
            int correct = 0;
            for (int i = 0; i < trainingData.length; i++) {
                double[] image = trainingData[i];
                int label = labels[i];

                double maxScore = Double.NEGATIVE_INFINITY;
                int predictedDigit = -1;

                for (int digit = 0; digit < (digitsNum); digit++) {
                    double[] hiddenLayer = new double[perceptrons[digit].GetHiddenSize()];
                    double score = perceptrons[digit].forward(image, hiddenLayer);
                    if (score > maxScore) {
                        maxScore = score;
                        predictedDigit = digit;
                    }
                }

                if (predictedDigit == label) {
                    correct++;
                }
            }

            double accuracy = (double) correct / trainingData.length * 100;
            System.out.printf("Accuracy after epoch %d: %.2f%%\n", (epoch + 1), accuracy);
        }

// Save the trained weights and test
        for (int digit = 0; digit < digitsNum; digit++) {
            perceptrons[digit].saveWeights("mlp_digit_" + digit + ".txt");
        }

        int count = 0;
        for (int i = 0; i < testingData.length; i++) {
            int label = testinglabels[i];
            double[] image = testingData[i];
            double maxScore = Double.NEGATIVE_INFINITY;
            int prediction = -1;
            for (int digit = 0; digit < digitsNum; digit++){
                double[] hiddenLayer = new double[perceptrons[digit].GetHiddenSize()];
                double score = perceptrons[digit].forward(image, hiddenLayer);
                if (score > maxScore){
                    //System.out.println("CURMAX: " + maxScore + " ,NEWMAX: " + score + " ,CURPREDICTION: " + prediction + " ,NEWPREDICTION: " + digit);
                    maxScore = score;
                    prediction = digit;
                }
            }
            if (prediction == label){
                count++;
                System.out.println("COUNTGOESUPPPPPPPPPPPPPP: " + count);
            }
        }
        System.out.println("Success rate of model: " + (double)count/10000);

    }
}
