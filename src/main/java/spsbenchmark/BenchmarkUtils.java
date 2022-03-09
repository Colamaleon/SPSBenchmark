package spsbenchmark;

import org.cryptimeleon.craco.sig.SignatureScheme;

/**
 * A collection of utilities for running banchmarks
 *
 */
public class BenchmarkUtils {

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
     * prints the given config with nice formatting
     */
    public static void prettyPrintConfig(BenchmarkConfig config) {

        System.out.println(separator());

        String startMessage = "Running benchmark with config... ";
        System.out.println(padString(startMessage, CONSOLE_WIDTH));
        System.out.println(padString(config.toPrettyString(),CONSOLE_WIDTH));

        System.out.println(separator());

    }

}
