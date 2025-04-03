import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TrainDigits {

    public static BufferedImage recreateImage(double[] features, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Scale the normalized value back to [0, 255]
                int value = (int) (features[y * width + x] * 255);
                int grayscale = (value << 16) | (value << 8) | value; // Set RGB to the same value
                image.setRGB(x, y, grayscale);
            }
        }
        return image;
    }

    public static void main(String[] args) {
        int numFeatures = 28 * 28; // 784 pixels per image
        int numDigits = 10; // Digits 0-9
        int epochs = 10;
        double learningRate = 0.01;

        Perceptron[] perceptrons = new Perceptron[numDigits];

        for (int i = 0; i < numDigits; i++) {
            perceptrons[i] = new Perceptron(numFeatures, learningRate);
        }

        try {
            // Load training data
            String mnistTrainPath = "training_data\\mnist_train.csv";
            double[][] trainFeatures = MNISTLoader.loadFeatures(mnistTrainPath);
            int[] trainLabels = MNISTLoader.loadLabels(mnistTrainPath);

            // Load separate test data
            String mnistTestPath = "training_data\\mnist_test.csv";
            double[][] testFeatures = MNISTLoader.loadFeatures(mnistTestPath);
            int[] testLabels = MNISTLoader.loadLabels(mnistTestPath);

            BufferedImage recreatedImage = recreateImage(testFeatures[10],28,28);
            File output = new File("recreated_image.png");
            ImageIO.write(recreatedImage, "png", output);
            System.out.println("Recreated image saved as recreated_image.png");

            // Train perceptrons
            for (int digit = 0; digit < numDigits; digit++) {
                int[] binaryLabels = new int[trainLabels.length];
                String weightsPath = "weights_digit_" + digit + ".txt";

                for (int i = 0; i < trainLabels.length; i++) {
                    binaryLabels[i] = (trainLabels[i] == digit) ? 1 : 0;
                }

                System.out.println("Training perceptron for digit " + digit + "...");
                perceptrons[digit].trainMultiple(trainFeatures, binaryLabels, epochs, weightsPath);
                System.out.println("Perceptron for digit " + digit + " trained!\n");

                // 🔹 Evaluate on unseen test data
                int correct = 0;
                for (int i = 0; i < testFeatures.length; i++) {
                    int prediction = perceptrons[digit].predict(testFeatures[i]);
                    if (prediction == (testLabels[i] == digit ? 1 : 0)) {
                        correct++;
                    }
                }

                double testAccuracy = (correct / (double) testFeatures.length) * 100;
                System.out.printf("Test Accuracy for digit %d: %.2f%%\n", digit, testAccuracy);
            }

        } catch (IOException e) {
            System.err.println("Error loading MNIST dataset: " + e.getMessage());
        }

    }
}
