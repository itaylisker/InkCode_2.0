import com.sun.source.doctree.SystemPropertyTree;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MNISTLoader {
    public static double[][] loadFeatures(String csvFile) throws IOException {
        ArrayList<double[]> featuresList = new ArrayList<>();
        int counter = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                double[] features;
                String[] values = line.split(",");
                BufferedImage image = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY), centered;
                for (int y = 0; y < 28; y++) {
                    for (int x = 0; x < 28; x++) {
                        int value = 255 - (int) Double.parseDouble(values[y * 28 + x]); // invert image (white on black --> black on white)
                        int grayscale = (value << 16) | (value << 8) | value; // Set RGB to the same value
                        image.setRGB(x, y, grayscale);
                    }
                }
                centered =  FeatureExtractor.centerDigit(image);

                features = FeatureExtractor.extractFeatures(centered); // 28x28 pixels
                featuresList.add(features);
                System.out.println("processing image number: " + ++counter);

                if(counter == 2){
                    System.out.println("centered and loaded a third of the pictures!");
                    File put = new File("recreated_image.png");
                    File output = new File("recreated_image_centered.png");
                    ImageIO.write(centered, "png", output);
                    ImageIO.write(image, "png", put);
                    break;
                }
            }
        }

        return featuresList.toArray(new double[0][0]); // Convert ArrayList to array
    }

    public static int[] loadLabels(String csvFile) throws IOException {
        ArrayList<Integer> labelsList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                labelsList.add(Integer.parseInt(values[0])); // First column = label
            }
        }

        return labelsList.stream().mapToInt(i -> i).toArray();
    }


public static void main(String[] args) throws IOException {
    loadFeatures("training_data\\mnist_train.csv");

}
}
