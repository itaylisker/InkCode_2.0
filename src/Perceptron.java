import java.io.*;
public class Perceptron {
    private double[] weights;
    private double bias;
    private double learningRate;

    public Perceptron(int numFeatures, double learningRate) {
        this.weights = new double[numFeatures];
        this.learningRate = learningRate;
        this.bias = Math.random() * 0.01; // Small random bias
        for (int i = 0; i < weights.length; i++) {
            weights[i] = Math.random() * 0.01; // Small random weights
        }
    }

    // Perceptron activation function (step function)
    public int predict(double[] inputs) {
        double sum = bias;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i] * inputs[i];
        }
        return (sum >= 0) ? 1 : 0;
    }

    // Load pre-trained weights
    public void loadWeights(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] = Double.parseDouble(br.readLine());
            }
            bias = Double.parseDouble(br.readLine());
            System.out.println("Loaded weights from " + filename);
        } catch (IOException e) {
            System.out.println("No saved weights found for " + filename);
        }
    }

    // Save trained weights
    public void saveWeights(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (double weight : weights) {
                bw.write(weight + "\n");
            }
            bw.write(bias + "\n");
            System.out.println("Saved weights to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving weights.");
        }
    }

    // Training with multiple examples
    public void trainMultiple(double[][] featureSet, int[] labels, int epochs, String savePath) {
        int numSamples = featureSet.length;

        for (int epoch = 1; epoch <= epochs; epoch++) {
            int correct = 0;

            for (int i = 0; i < numSamples; i++) {
                int prediction = predict(featureSet[i]);
                int error = labels[i] - prediction;

                if (error != 0) {
                    for (int j = 0; j < weights.length; j++) {
                        double oldWeight = weights[j];
                        weights[j] += learningRate * error * featureSet[i][j];

                    }
                    bias += learningRate * error;
                } else {
                    correct++;
                }
            }

            double accuracy = (correct / (double) numSamples) * 100;
            System.out.printf("Epoch %d - Accuracy: %.2f%%\n", epoch, accuracy);
        }
        saveWeights(savePath);
        System.out.println("weights saved to: " + savePath);
    }

}
