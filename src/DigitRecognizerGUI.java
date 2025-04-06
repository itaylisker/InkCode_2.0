import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class DigitRecognizerGUI extends JFrame {
    private JLabel imageLabel;
    private JButton uploadButton, predictButton;
    private File selectedFile;
    private MLP[] perceptrons; // Array of perceptrons for digits 0-9

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
                    double[] predictedDigits = predictDigit(selectedFile);
                    System.out.println(Arrays.toString(predictedDigits));
                    if (predictedDigits[0] > -1){
                        int counter = 0;
                        String displayedText = "Predicted Digits: ";
                        for (int i = 0; i < 10; i++){
                            if (predictedDigits[i] > 0){
                                System.out.println("gothereeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                                counter++;
                                displayedText += i + ",";
                                System.out.println(displayedText + "  counter is: " + counter);
                            }
                        }
                        if (counter > 0){
                            displayedText = displayedText.substring(0,17+counter*2);
                            System.out.println(displayedText);

                        }
                        else{
                            displayedText = "couldn't predict number";
                        }
                        JOptionPane.showMessageDialog(null, displayedText);
                    }
                } else {
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

    // Predict digit from uploaded image
    private double[] predictDigit(File file) {
        double[] predictions = new double[11];
        try {
            BufferedImage img = ImageIO.read(file);
            double[] features = extractFeatures(img);
            int bestDigit = -1;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (int digit = 0; digit < 10; digit++) {
                double[] hiddenLayer = new double[perceptrons[digit].GetHiddenSize()];
                double score = perceptrons[digit].forward(features, hiddenLayer);

                predictions[digit] = score;

                if (score ==1) {
                    System.out.println("score for " + digit + ": " + score);
                }
            }

            return predictions;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error processing image.");
            predictions[0] = -1;
            return predictions;
        }
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return resizedImage;
    }

    // Convert image to 28x28 grayscale feature array
    public static double[] extractFeatures(BufferedImage image) throws IOException {
        // Resize the image to 28x28
        BufferedImage resizedImage = resizeImage(image, 28, 28);

        // Flatten and normalize the pixel values
        double[] features = new double[28 * 28];
        int index = 0;

        for (int y = 0; y < resizedImage.getHeight(); y++) {
            for (int x = 0; x < resizedImage.getWidth(); x++) {
                // Get pixel value as grayscale
                int rgb = resizedImage.getRGB(x, y);
                Color color = new Color(rgb);

                // Convert to grayscale intensity (average of R, G, and B)
                double grayscale = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;

                // Normalize to range [0, 1]
                features[index++] = grayscale / 255.0;
            }
        }
        BufferedImage recreatedImage = recreateImage(features, 28, 28);
        File output = new File("recreated_image.png");
        ImageIO.write(recreatedImage, "png", output);
        return features;
    }




    public static void main(String[] args) {
        // Load trained perceptrons (assumes they are already trained)
        int numFeatures = 28 * 28;
        int numDigits = 10;
        MLP[] models = new MLP[10];
        for (int i = 0; i < numDigits; i++) {
            models[i] = new MLP(numFeatures, 128, 0.01);
            models[i].loadWeights("mlp_digit_" + i + ".txt"); // Load weights from file
        }

        new DigitRecognizerGUI(models);
    }
}

