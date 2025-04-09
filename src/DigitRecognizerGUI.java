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

    public static BufferedImage centerDigit(BufferedImage input) throws IOException {
            int width = input.getWidth();
            int height = input.getHeight();

            // Step 1: Find bounding box of the digit
            int top = height, bottom = 0, left = width, right = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = input.getRGB(x, y);
                    if (!isWhite(rgb)) {
                        top = Math.min(top, y);
                        bottom = Math.max(bottom, y);
                        left = Math.min(left, x);
                        right = Math.max(right, x);
                    }
                }
            }

            // Handle blank images
            if (top > bottom || left > right) return input;

            // Step 2: Crop the digit
            BufferedImage digit = input.getSubimage(left, top, right - left + 1, bottom - top + 1);
// Step 3: Create a new white image
            BufferedImage centered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = centered.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // Step 4: Draw the digit centered
            int offsetX = (width - digit.getWidth()) / 2;
            int offsetY = (height - digit.getHeight()) / 2;
            g.drawImage(digit, offsetX, offsetY, null);
            g.dispose();

            return centered;
        }

        private static boolean isWhite(int rgb) {
            Color color = new Color(rgb, true);
            return color.getAlpha() > 250 &&
                    color.getRed() > 250 &&
                    color.getGreen() > 250 &&
                    color.getBlue() > 250;
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
    private int predictDigit(File file) {
        double[] scores = new double[10];
        try {
            BufferedImage img = centerDigit(ImageIO.read(file));
            double[] features = extractFeatures(img);
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
        File output = new File("recreated_image_gui.png");
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

