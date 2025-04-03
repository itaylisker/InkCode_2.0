import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;

public class FeatureExtractor {

    // Function to extract features from an image
    public static double[] extractFeatures(BufferedImage image) {
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

        return features;
    }

    // Function to resize an image to the desired dimensions
    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return resizedImage;
    }

    // Function to recreate an image from the feature array
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