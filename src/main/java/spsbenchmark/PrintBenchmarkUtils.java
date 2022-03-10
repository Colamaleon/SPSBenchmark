package spsbenchmark;

/**
 * A collection of utilities for running banchmarks
 *
 */
public class PrintBenchmarkUtils {

    public static final int CONSOLE_WIDTH = 120;

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

}
