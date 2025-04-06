import java.io.*;
import java.util.Random;

public class MLP {
    private int inputSize, hiddenSize;
    private double[][] weightsInputHidden;
    private double[] weightsHiddenOutput;
    private double[] hiddenBias;
    private double outputBias;
    private double learningRate;
    private Random random = new Random();

    public MLP(int inputSize, int hiddenSize, double learningRate) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.learningRate = learningRate;

        // Initialize weights and biases
        weightsInputHidden = new double[inputSize][hiddenSize];
        weightsHiddenOutput = new double[hiddenSize];
        hiddenBias = new double[hiddenSize];
        outputBias = (random.nextDouble() - 0.5) * 0.01;

        initWeights(weightsInputHidden);
        initWeights(weightsHiddenOutput);
        initBias(hiddenBias);
    }

    public void saveWeights(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Save input-to-hidden weights
            for (int i = 0; i < inputSize; i++) {
                for (int j = 0; j < hiddenSize; j++) {
                    writer.print(weightsInputHidden[i][j] + " ");
                }
                writer.println();
            }

            // Save hidden biases
            for (int i = 0; i < hiddenSize; i++) {
                writer.print(hiddenBias[i] + " ");
            }
            writer.println();

            // Save hidden-to-output weights
            for (int i = 0; i < hiddenSize; i++) {
                writer.print(weightsHiddenOutput[i] + " ");
            }
            writer.println();

            // Save output bias
            writer.println(outputBias);

            System.out.println("Weights saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadWeights(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Load input-to-hidden weights
            for (int i = 0; i < inputSize; i++) {
                String[] line = reader.readLine().split(" ");
                for (int j = 0; j < hiddenSize; j++) {
                    weightsInputHidden[i][j] = Double.parseDouble(line[j]);
                }
            }

            // Load hidden biases
            String[] hiddenBiasLine = reader.readLine().split(" ");
            for (int i = 0; i < hiddenSize; i++) {
                hiddenBias[i] = Double.parseDouble(hiddenBiasLine[i]);
            }

            // Load hidden-to-output weights
            String[] hiddenToOutputLine = reader.readLine().split(" ");
            for (int i = 0; i < hiddenSize; i++) {
                weightsHiddenOutput[i] = Double.parseDouble(hiddenToOutputLine[i]);
            }

            // Load output bias
            outputBias = Double.parseDouble(reader.readLine());

            System.out.println("Weights loaded from " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initWeights(double[][] weights) {
        for (int i = 0; i < weights.length; i++)
            for (int j = 0; j < weights[i].length; j++)
                weights[i][j] = (random.nextDouble() - 0.5) * 0.01;
    }

    private void initWeights(double[] weights) {
        for (int i = 0; i < weights.length; i++)
            weights[i] = (random.nextDouble() - 0.5) * 0.01;
    }

    private void initBias(double[] bias) {
        for (int i = 0; i < bias.length; i++)
            bias[i] = (random.nextDouble() - 0.5) * 0.01;
    }

    // ReLU Activation Function
    private double relu(double x) { return Math.max(0, x); }

    // Derivative of ReLU
    private double reluDerivative(double x) { return x > 0 ? 1 : 0; }

    // Forward Pass
    public double forward(double[] input, double[] hiddenLayer) {
        double output = outputBias;

        // Input to Hidden Layer
        for (int i = 0; i < hiddenSize; i++) {
            hiddenLayer[i] = hiddenBias[i];
            for (int j = 0; j < inputSize; j++) {
                hiddenLayer[i] += input[j] * weightsInputHidden[j][i];
            }
            hiddenLayer[i] = relu(hiddenLayer[i]);
        }

        // Hidden to Output Layer
        for (int i = 0; i < hiddenSize; i++) {
            output += hiddenLayer[i] * weightsHiddenOutput[i];
        }

        return output;
    }

    // Train using Backpropagation
    public void train(double[] input, int target) {
        double[] hiddenLayer = new double[hiddenSize];
        double output = forward(input, hiddenLayer);
        double prediction = output >= 0 ? 1 : 0; // Binary classification for OvA
        double error = target - prediction; // Compute error

        // Compute Gradients
        double outputGradient = error; // For output layer
        double[] hiddenGradients = new double[hiddenSize];

        for (int i = 0; i < hiddenSize; i++) {
            hiddenGradients[i] = outputGradient * weightsHiddenOutput[i] * reluDerivative(hiddenLayer[i]);
        }

        // Update Weights - Hidden to Output
        for (int i = 0; i < hiddenSize; i++) {
            weightsHiddenOutput[i] += learningRate * outputGradient * hiddenLayer[i];
        }
        outputBias += learningRate * outputGradient;

        // Update Weights - Input to Hidden
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] += learningRate * hiddenGradients[j] * input[i];
            }
        }

        // Update Bias - Hidden Layer
        for (int i = 0; i < hiddenSize; i++) {
            hiddenBias[i] += learningRate * hiddenGradients[i];
        }
    }

    public int GetHiddenSize(){return this.hiddenSize;}
}
