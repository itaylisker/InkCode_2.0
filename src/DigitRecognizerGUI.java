import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class DigitRecognizerGUI extends JFrame {
    private JLabel imageLabel;
    private JButton uploadButton, predictButton;
    private File selectedFile;
    private MLP[] perceptrons; // Array of perceptrons for digits 0-9

    public static void printSortedIndexValueMap(double[] array) {
        // Create a map of index -> value
        Map<Integer, Double> indexValueMap = new HashMap<>();
        for (int i = 0; i < array.length; i++) {
            indexValueMap.put(i, array[i]);
        }

        // Sort entries by value (ascending)
        List<Map.Entry<Integer, Double>> sortedEntries = new ArrayList<>(indexValueMap.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());

        // Print nicely
        System.out.println("Sorted Index -> Value Map:");
        for (Map.Entry<Integer, Double> entry : sortedEntries) {
            System.out.printf("Index %d: %.4f\n", entry.getKey(), entry.getValue());
        }
    }

    public DigitRecognizerGUI(MLP[] trainedPerceptrons) {
        this.perceptrons = trainedPerceptrons;

        setTitle("Handwritten Digit Recognizer");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Image display area
        imageLabel = new JLabel("No Image Uploaded", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(280, 280));
        add(imageLabel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        uploadButton = new JButton("Upload Image");
        predictButton = new JButton("Predict");

        buttonPanel.add(uploadButton);
        buttonPanel.add(predictButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Upload button action
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    displayImage(selectedFile);
                }
            }
        });

        // Predict button action
        predictButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    String displayedText;
                    int predictedDigit = predictDigit(selectedFile);
                    if (predictedDigit > -1) {
                        displayedText = "Predicted Digit: " + predictedDigit;
                    } else {
                        displayedText = "couldn't predict number";
                    }
                    JOptionPane.showMessageDialog(null, displayedText);
                }

                else {
                JOptionPane.showMessageDialog(null, "Please upload an image first.");
            }
        }
        });

        setVisible(true);
    }

    // Display uploaded image
    private void displayImage(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            Image scaledImage = img.getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error loading image.");
        }
    }

    // Predict digit from uploaded image
    private int predictDigit(File file) {
        double[] scores = new double[10];
        try {
            BufferedImage img = FeatureExtractor.centerDigit(ImageIO.read(file));
            double[] features = FeatureExtractor.extractFeatures(img);
            BufferedImage recreatedImage = FeatureExtractor.recreateImage(features, 28, 28);
        File output = new File("recreated_image_gui.png");
        ImageIO.write(recreatedImage, "png", output);
            int bestDigit = -1;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (int digit = 0; digit < 10; digit++) {
                double[] hiddenLayer = new double[perceptrons[digit].GetHiddenSize()];
                double score = perceptrons[digit].forward(features, hiddenLayer);
                scores[digit] = score;
                if (score > bestScore){
                    bestScore = score;
                    bestDigit = digit;
                }
            }
            printSortedIndexValueMap(scores);

            return bestDigit;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error processing image.");
            return -1;
        }
    }

    public static void main(String[] args) {
        // Load trained perceptrons (assumes they are already trained)
        int numFeatures = 28 * 28;
        int numDigits = 10;
        MLP[] models = new MLP[10];
        for (int i = 0; i < numDigits; i++) {
            models[i] = new MLP(numFeatures, 128, 0.01);
            models[i].loadWeights("4EpochsModel\\mlp_digit_" + i + ".txt"); // Load weights from file
        }

        new DigitRecognizerGUI(models);
    }
}

