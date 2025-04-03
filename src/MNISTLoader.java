import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MNISTLoader {
    public static double[][] loadFeatures(String csvFile) throws IOException {
        ArrayList<double[]> featuresList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                double[] features = new double[784]; // 28x28 pixels

                // Normalize pixel values (0-255 → 0-1)
                for (int i = 1; i < values.length; i++) {
                    features[i - 1] = 1 - Double.parseDouble(values[i]) / 255.0;
                }

                featuresList.add(features);
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
}