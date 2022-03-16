package spsbenchmark;

import java.io.*;

public final class BenchmarkLogger {

    private static String history = "";

    public static void clear() {
        history = "";
    }

    public static void log(String message) {
        history += message;
    }
    public static void logln(String message) {
        history += message;
        history += "\n";
    }

    public static void flushLogToFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(history);
        writer.close();
    }
}
