import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Assig4 {

    private PrintWriter writeFile;

    // Simple QuickSort
    private double totalTime1;
    private double avgTime1;

    // Median of Three (5)
    private double totalTime2;
    private double avgTime2;

    // Median of Three (20)
    private double totalTime3;
    private double avgTime3;

    //  Median of Three (100)
    private double totalTime4;
    private double avgTime4;

    // Random Pivot (5)
    private double totalTime5;
    private double avgTime5;

    // MergeSort
    private double totalTime6;
    private double avgTime6;

    // timer init
    private long start;
    private long finish;
    private long delta;


    public Assig4() {

        Integer[] random;
        Integer[] sorted;
        Integer[] reversed;

        Integer[] testClone; //used to test data while keeping original data integrity

        Random rng = new Random();

        boolean isTrace = false;

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Array Size: ");
        int arraySize = sc.nextInt();

        System.out.print("Enter number of trials: ");
        int trialSize = sc.nextInt();

        System.out.print("Enter file name (ex. 'test25k.txt'): ");
        String file = sc.next();
        try {
            writeFile = new PrintWriter(new FileOutputStream(file, false));
        } catch (IOException e) {System.out.println("Throwing file write IOException!");}

        if (arraySize <= 20) isTrace = true;

        /** RANDOM **/
        random = new Integer[arraySize];

        for (int i = 0; i < trialSize; i++) {

            for (int j = 0; j < arraySize; j++) {
                Integer rand = rng.nextInt(arraySize);
                random[j] = rand;
            }
            /** Quick Sort **/
            testClone = random.clone();
            if (arraySize <= 100000) quickSort(testClone, arraySize, "Random", trialSize, isTrace, i);

            /** Median of Three (5) **/
            testClone = random.clone();
            quickSort5(testClone, arraySize, "Random", trialSize, isTrace, i);

            /** Median of Three (20) **/
            testClone = random.clone();
            quickSort20(testClone, arraySize, "Random", trialSize, isTrace, i);

            /** Median of Three (100) **/
            testClone = random.clone();
            quickSort100(testClone, arraySize, "Random", trialSize, isTrace, i);

            /** Random Pivot QuickSort **/
            testClone = random.clone();
            quickSortPivot(testClone, arraySize, "Random", trialSize, isTrace, i);

            /** Merge Sort **/
            testClone = random.clone();
            mergeSort(testClone, arraySize, "Random", trialSize, isTrace, i);

        }   reset();

        /** ALREADY SORTED **/
        sorted = new Integer[arraySize];

        for (int i = 0; i < trialSize; i++) {

            for (int j = 0; j < arraySize; j++) {
                sorted[j] = j;
            }

            /** Quick Sort **/
            testClone = sorted.clone();
            if (arraySize <= 100000) quickSort(testClone, arraySize, "Sorted", trialSize, isTrace, i);

            /** Median of Three (5) **/
            testClone = sorted.clone();
            quickSort5(testClone, arraySize, "Sorted", trialSize, isTrace, i);

            /** Median of Three (20) **/
            testClone = sorted.clone();
            quickSort20(testClone, arraySize, "Sorted", trialSize, isTrace, i);

            /** Median of Three (100) **/
            testClone = sorted.clone();
            quickSort100(testClone, arraySize, "Sorted", trialSize, isTrace, i);

            /** Random Pivot QuickSort **/
            testClone = sorted.clone();
            quickSortPivot(testClone, arraySize, "Sorted", trialSize, isTrace, i);

            /** Merge Sort **/
            testClone = sorted.clone();
            mergeSort(testClone, arraySize, "Sorted", trialSize, isTrace, i);

        }   reset();

        /** REVERSE SORTED **/
        reversed = new Integer[arraySize];

        for (int i = 0; i < trialSize; i++) {

            for (int j = 0; j < arraySize; j++) {
                reversed[j] = arraySize-j;
            }

            /** Quick Sort **/
            testClone = reversed.clone();
            if (arraySize <= 100000) quickSort(testClone, arraySize, "Reverse", trialSize, isTrace, i);

            /** Median of Three (5) **/
            testClone = reversed.clone();
            quickSort5(testClone, arraySize, "Reverse", trialSize, isTrace, i);

            /** Median of Three (20) **/
            testClone = reversed.clone();
            quickSort20(testClone, arraySize, "Reverse", trialSize, isTrace, i);

            /** Median of Three (100) **/
            testClone = reversed.clone();
            quickSort100(testClone, arraySize, "Reverse", trialSize, isTrace, i);

            /** Random Pivot QuickSort **/
            testClone = reversed.clone();
            quickSortPivot(testClone, arraySize, "Reverse", trialSize, isTrace, i);

            /** Merge Sort **/
            testClone = reversed.clone();
            mergeSort(testClone, arraySize, "Reverse", trialSize, isTrace, i);

        }   reset();
        writeFile.close();
    }

    void quickSort (Integer[] test, int arraySize, String order, int trialSize, boolean trace, int count) {

        // reinitialize timer
        start = 0;
        finish = 0;
        delta = 0;

        if (trace) {
            writeFile.write("\nAlgorithm: Simple QuickSort TRACE: " + count);
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nData before sorted: " + Arrays.toString(test));
        }

        start = System.nanoTime();
        Quick.quickSort(test, 0, arraySize - 1);
        finish = System.nanoTime();
        delta = finish - start;
        totalTime1 += delta;

        if (trace) {
            writeFile.write("\nData after sorted: " + Arrays.toString(test) + "\n");
        }

        if (count == trialSize-1) {
            avgTime1 = ((totalTime1 / trialSize) / 1000000000);
            writeFile.write("\nAlgorithm: Simple QuickSort");
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nNumber of trials: " + trialSize);
            writeFile.write("\nAverage Time: " + avgTime1 + " sec.\n");
        }
    }

    void quickSort5 (Integer[] test, int arraySize, String order, int trialSize, boolean trace, int count) {

        // reinitialize timer
        start = 0;
        finish = 0;
        delta = 0;

        if (trace) {
            writeFile.write("\nAlgorithm: Median of Three (5) TRACE: " + count);
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nData before sorted: " + Arrays.toString(test));
        }

        start = System.nanoTime();
        TextMergeQuick.quickSort5(test, 0, arraySize - 1);
        finish = System.nanoTime();
        delta = finish - start;
        totalTime2 += delta;

        if (trace) {
            writeFile.write("\nData after sorted: " + Arrays.toString(test) + "\n");
        }

        avgTime2 = ((totalTime2 / trialSize) / 1000000000);

        if (count == trialSize-1) {
            writeFile.write("\nAlgorithm: Median of Three (5)");
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nNumber of trials: " + trialSize);
            writeFile.write("\nAverage Time: " + avgTime2 + " sec.\n");
        }
    }

    void quickSort20 (Integer[] test, int arraySize, String order, int trialSize, boolean trace, int count) {

        // reinitialize timer
        start = 0;
        finish = 0;
        delta = 0;

        if (trace) {
            writeFile.write("\nAlgorithm: Median of Three (20) TRACE: " + count);
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nData before sorted: " + Arrays.toString(test));
        }

        start = System.nanoTime();
        TextMergeQuick.quickSort5(test, 0, arraySize - 1);
        finish = System.nanoTime();
        delta = finish - start;
        totalTime3 += delta;

        if (trace) {
            writeFile.write("\nData after sorted: " + Arrays.toString(test) + "\n");
        }

        avgTime3 = ((totalTime3 / trialSize) / 1000000000);

        if (count == trialSize-1) {
            writeFile.write("\nAlgorithm: Median of Three (20)");
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nNumber of trials: " + trialSize);
            writeFile.write("\nAverage Time: " + avgTime3 + " sec.\n");
        }
    }

    void quickSort100 (Integer[] test, int arraySize, String order, int trialSize, boolean trace, int count) {

        // reinitialize timer
        start = 0;
        finish = 0;
        delta = 0;

        if (trace) {
            writeFile.write("\nAlgorithm: Median of Three (100) TRACE: " + count);
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nData before sorted: " + Arrays.toString(test));
        }

        start = System.nanoTime();
        TextMergeQuick.quickSort5(test, 0, arraySize - 1);
        finish = System.nanoTime();
        delta = finish - start;
        totalTime4 += delta;

        if (trace) {
            writeFile.write("\nData after sorted: " + Arrays.toString(test) + "\n");
        }

        avgTime4 = ((totalTime4 / trialSize) / 1000000000);

        if (count == trialSize-1) {
            writeFile.write("\nAlgorithm: Median of Three (100)");
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nNumber of trials: " + trialSize);
            writeFile.write("\nAverage Time: " + avgTime4 + " sec.\n");
        }
    }

    void quickSortPivot (Integer[] test, int arraySize, String order, int trialSize, boolean trace, int count) {

        // reinitialize timer
        start = 0;
        finish = 0;
        delta = 0;

        if (trace) {
            writeFile.write("\nAlgorithm: Random Pivot (5) TRACE: " + count);
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nData before sorted: " + Arrays.toString(test));
        }

        start = System.nanoTime();
        TextMergeQuick.quickSortPivot(test, 0, arraySize - 1);
        finish = System.nanoTime();
        delta = finish - start;
        totalTime5 += delta;

        if (trace) {
            writeFile.write("\nData after sorted: " + Arrays.toString(test) + "\n");
        }

        avgTime5 = ((totalTime5 / trialSize) / 1000000000);

        if (count == trialSize-1) {
            writeFile.write("\nAlgorithm: Random Pivot (5)");
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nNumber of trials: " + trialSize);
            writeFile.write("\nAverage Time: " + avgTime5 + " sec.\n");
        }
    }

    void mergeSort (Integer[] test, int arraySize, String order, int trialSize, boolean trace, int count) {

        // reinitialize timer
        start = 0;
        finish = 0;
        delta = 0;

        if (trace) {
            writeFile.write("\nAlgorithm: MergeSort TRACE: " + count);
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nData before sorted: " + Arrays.toString(test));
        }

        start = System.nanoTime();
        TextMergeQuick.mergeSort(test, 0, arraySize - 1);
        finish = System.nanoTime();
        delta = finish - start;
        totalTime6 += delta;

        if (trace) {
            writeFile.write("\nData after sorted: " + Arrays.toString(test) + "\n");
        }

        avgTime6 = ((totalTime6 / trialSize) / 1000000000);

        if (count == trialSize-1) {
            writeFile.write("\nAlgorithm: MergeSort");
            writeFile.write("\nArray size: " + arraySize);
            writeFile.write("\nOrder: " + order);
            writeFile.write("\nNumber of trials: " + trialSize);
            writeFile.write("\nAverage Time: " + avgTime6 + " sec.\n");
        }
    }

    void reset() {
        totalTime1 = 0;
        avgTime1 = 0;
        totalTime2 = 0;
        avgTime2 = 0;
        totalTime3 = 0;
        avgTime3 = 0;
        totalTime4 = 0;
        avgTime4 = 0;
        totalTime5 = 0;
        avgTime5 = 0;
        totalTime6 = 0;
        avgTime6 = 0;
    }

    public static void main (String[] args) {new Assig4();}
}
