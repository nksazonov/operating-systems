import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    private static List<Integer> readListOfInts(int listSize, BufferedReader reader) throws IOException {
        List<Integer> ints = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            int readValue = Character.getNumericValue(reader.read());
            while (readValue == -1) {
                readValue = Character.getNumericValue(reader.read());
            }
            ints.add(readValue);
        }
        return ints;
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int functsAmount;
        do {
            System.out.print("Enter number of functions (must be even and > 0): ");
            functsAmount = Integer.parseInt(reader.readLine());
        } while (functsAmount % 2 != 0 || functsAmount <= 0);

        System.out.println("Enter args for functions (one by line):");
        List<Integer> functsArgs = readListOfInts(functsAmount, reader);

        System.out.println("Computing...");

        ComputationManager cm = new ComputationManager(functsAmount, functsArgs);
        cm.run();
    }
}
