import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TrainDigits {

    public static void main(String[] args) throws IOException {
        int inputSize = 28 * 28; // 784 pixels
        int hiddenSize = 128; // Hidden neurons
        double learningRate = 0.01;
        int epochs = 5;

        MLP[] perceptrons = new MLP[10]; // One model per digit

        // Load MNIST dataset
        double[][] trainingData = MNISTLoader.loadFeatures("training_data\\mnist_train.csv"); // 2D array of images
        int[] labels = MNISTLoader.loadLabels("training_data\\mnist_train.csv"); // Corresponding digit labels

        for (int digit = 0; digit < 10; digit++) {
            perceptrons[digit] = new MLP(inputSize, 128, learningRate);
            perceptrons[digit].loadWeights("mlp_digit_" + digit + ".txt");  // Load weights if available
        }

// Train the models
        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Epoch " + (epoch + 1));

            for (int i = 0; i < trainingData.length; i++) {
                int label = labels[i];
                double[] image = trainingData[i];

                for (int digit = 0; digit < 10; digit++) {
                    int target = (label == digit) ? 1 : 0;
                    perceptrons[digit].train(image, target);
                }
            }
        }

// Save the trained weights
        for (int digit = 0; digit < 10; digit++) {
            perceptrons[digit].saveWeights("mlp_digit_" + digit + ".txt");
        }

    }
}
