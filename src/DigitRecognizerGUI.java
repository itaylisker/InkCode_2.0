import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class DigitRecognizerGUI extends JFrame {
    private JLabel imageLabel;
    private JButton uploadButton, predictButton;
    private File selectedFile;
    private Perceptron[] perceptrons; // Array of perceptrons for digits 0-9

    public DigitRecognizerGUI(Perceptron[] trainedPerceptrons) {
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
                    int[] predictedDigits = predictDigit(selectedFile);
                    System.out.println(Arrays.toString(predictedDigits));
                    if (predictedDigits[0] > -1){
                        int counter = 0;
                        String displayedText = "Predicted Digits: ";
                        for (int i = 0; i < 10; i++){
                            System.out.println(predictedDigits[i]);
                            if (predictedDigits[i] > 0){
                                System.out.println("gothereeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                                counter++;
                                displayedText += i + ", ";
                                System.out.println(displayedText);
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
    private int[] predictDigit(File file) {
        int[] predictions = new int[11];
        try {
            BufferedImage img = ImageIO.read(file);
            double[] features = extractFeatures(img);
            int bestDigit = -1;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (int digit = 0; digit < 10; digit++) {
                int score = perceptrons[digit].predict(features);

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


    // Convert image to 28x28 grayscale feature array
    private double[] extractFeatures(BufferedImage img) throws IOException {
        int width = 28, height = 28;
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();

        double[] features = new double[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = resized.getRGB(x, y) & 0xFF; // Grayscale intensity
                features[y * width + x] = pixel / 255.0; // Normalize to [0,1]
            }
        }

        BufferedImage recreatedImage = recreateImage(features, 28, 28);
        File output = new File("recreated_image.png");
        ImageIO.write(recreatedImage, "png", output);
        System.out.println("Recreated image saved as recreated_image.png");

        return features;
    }


    public static void main(String[] args) {
        // Load trained perceptrons (assumes they are already trained)
        int numFeatures = 28 * 28;
        int numDigits = 10;
        Perceptron[] perceptrons = new Perceptron[numDigits];
        for (int i = 0; i < numDigits; i++) {
            perceptrons[i] = new Perceptron(numFeatures, 0.01);
            perceptrons[i].loadWeights("weights_digit_" + i + ".txt"); // Load weights from file
        }

        new DigitRecognizerGUI(perceptrons);
    }
}
