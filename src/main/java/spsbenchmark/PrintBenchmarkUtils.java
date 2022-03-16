package spsbenchmark;

import java.io.*;
import java.nio.file.Files;

/**
 * A collection of utilities for running banchmarks
 *
 */
public class PrintBenchmarkUtils {

    public static final int CONSOLE_WIDTH = 120;

    public static final String OUTPUT_PATH = "output";

    /**
     * pads a string to length (pad right)
     */
    public static String padString(String s, int targetLength) {
        targetLength = (targetLength - 3);
        return String.format("* %-" + targetLength + "s" + "*", s);
    }

    /**
     * pads a string to the console width (pad right)
     */
    public static String padString(String s) {
        return padString(s, CONSOLE_WIDTH);
    }

    /**
     * gives a long line
     */
    public static String separator() {
        return padString("", CONSOLE_WIDTH).replace(" ", "-");
    }

    /**
     * prints a long line
     */
    public static void printSeparator() {
        System.out.println(separator());
    }

    /**
     * prints the given config with nice formatting
     */
    public static void prettyPrintConfig(BenchmarkConfig config, SPSBenchmark.BenchmarkMode mode) {

        System.out.println(separator());

        String startMessage = String.format("Running %s benchmark with config... ",
                (mode == SPSBenchmark.BenchmarkMode.Counting) ? "[Counting]" : "[Timer]");

        System.out.println(padString(startMessage, CONSOLE_WIDTH));

        System.out.println(padString((mode == SPSBenchmark.BenchmarkMode.Counting) ?
                config.getCountingBGroup().getClass().getSimpleName() :
                config.getTimerBGroup().toString() ,CONSOLE_WIDTH));

        System.out.println(padString(config.toPrettyString(),CONSOLE_WIDTH));

        System.out.println(separator());

    }


    private static String GetBenchmarkDirectoryPath() {
        return new File("./" + OUTPUT_PATH).getAbsolutePath();
    }

    public static void flushLoggerToBenchmarkFile(String schemeName, SPSBenchmark.BenchmarkMode benchmarkMode) {
        try {
            String dir = GetBenchmarkDirectoryPath();
            new File(dir).mkdirs();

            String filename = String.format("%s\\%s_%s.txt", dir, schemeName, benchmarkMode);

            BenchmarkLogger.flushLogToFile(filename);
            System.out.println(String.format("BenchmarkFile for [%s] using %s created under: %s",
                    schemeName, benchmarkMode, filename));
        } catch (Exception e) {
            System.out.println(String.format("BenchmarkFile for [%s] using %s could not be created!",
                    schemeName, benchmarkMode));
        }
    }
}
