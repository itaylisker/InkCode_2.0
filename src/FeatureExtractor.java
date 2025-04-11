import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class FeatureExtractor {

    // Function to extract features from an image
    public static double[] extractFeatures(BufferedImage image) throws IOException {

        // Resize the image to 28x28
        BufferedImage resizedImage = resizeImage(centerDigit(image), 28, 28);

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

        return features;
    }

    // Function to resize an image to the desired dimensions
    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return resizedImage;
    }

    // Function to recreate an image from the feature array
    public static BufferedImage recreateImage(double[] features, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Scale the normalized value back to [0, 255]
                int value = (int) (features[y * width + x] * 255);
                int grayscale = (value << 16) | (value << 8) | value; // Set RGB to the same value
                image.setRGB(x, y, grayscale);
            }
        }
        File output = new File("recreated_image.png");
        ImageIO.write(image, "png", output);
        return image;
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


        public static void main (String[]args){
            // Example usage of the function
            try {
                // Replace with the path to your image
                String imagePath = "Images\\eight.jpeg";
                BufferedImage image = javax.imageio.ImageIO.read(new java.io.File(imagePath));

                // Extract features from the image
                double[] features = extractFeatures(image);

                // Print the features (28x28 pixel values flattened)
                System.out.println("Extracted Features:");
                for (int i = 0; i < 28; i++) { // Print 28 values per row
                    for (int j = 0; j < 28; j++) {
                        System.out.printf("%.3f ", features[i * 28 + j]);
                    }
                    System.out.println();

                    BufferedImage recreatedImage = recreateImage(features, 28, 28);
                    File output = new File("recreated_image.png");
                    ImageIO.write(recreatedImage, "png", output);
                    System.out.println("Recreated image saved as recreated_image.png");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
