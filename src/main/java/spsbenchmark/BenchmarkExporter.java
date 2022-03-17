package spsbenchmark;

import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugGroup;

import java.io.*;
import java.util.Locale;

/**
 * Utility class that enables the export of benchmarks as a set of LaTeX commands
 */
public final class BenchmarkExporter {

    public static final String EXPORT_PATH = "output";

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
    public static void loglns(String[] messages) {
        for (String message : messages) {
            logln(message);
        }
    }

    public static void exportBenchmarkTeX(SPSBenchmark benchmark) {

        clear();

        String spsName = benchmark.getConfig().getSchemeName().replaceAll("[0-9]+", "");

        loglns(generateConfigTeXCommands(benchmark.getConfig(), benchmark.getMode(), spsName));

        if (benchmark.getMode() == BenchmarkMode.Time) {
            loglns(generateTimeResultTeXCommands("Setup", spsName, benchmark.getBenchmarkTimeResult("setup")));
            loglns(generateTimeResultTeXCommands("KeyGen", spsName, benchmark.getBenchmarkTimeResult("keyGen")));
            loglns(generateTimeResultTeXCommands("Sign", spsName, benchmark.getBenchmarkTimeResult("sign")));
            loglns(generateTimeResultTeXCommands("Verify", spsName, benchmark.getBenchmarkTimeResult("verify")));
        }
        if (benchmark.getMode() == BenchmarkMode.Counting) {
            loglns(generateCountingResultTeXCommands("Setup", "setup", spsName, benchmark.getConfig().getCountingBGroup()));
            loglns(generateCountingResultTeXCommands("KeyGen", "keyGen", spsName, benchmark.getConfig().getCountingBGroup()));
            loglns(generateCountingResultTeXCommands("Sign", "sign", spsName, benchmark.getConfig().getCountingBGroup()));
            loglns(generateCountingResultTeXCommands("Verify", "verify", spsName, benchmark.getConfig().getCountingBGroup()));
        }

        String filename = String.format("%s_%s_results.tex",
                benchmark.getMode(), benchmark.getConfig().getSchemeName()).toLowerCase(Locale.ROOT);
        flushLoggerToBenchmarkFile(filename);
    }


    public static void flushLogToFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(history);
        writer.close();
    }

    private static String GetExportDirectoryPath() {
        return new File("./" + EXPORT_PATH).getAbsolutePath();
    }

    public static void flushLoggerToBenchmarkFile(String filename) {
        String dir = GetExportDirectoryPath();
        String filepath = String.format("%s\\%s", dir, filename);

        try {
            new File(dir).mkdirs();
            flushLogToFile(filepath);
            System.out.println(String.format("BenchmarkFile created under: %s", filepath));
        } catch (Exception e) {
            System.out.println(String.format("BenchmarkFile could not be created unter: %s", filepath));
        }
    }

    /**
     * generates a set of LaTeX commands that store the config with which the benchmark was run
     */
    public static String[] generateConfigTeXCommands(BenchmarkConfig config, BenchmarkMode mode, String spsName) {
        if(!(spsName.matches("[a-zA-Z]+"))) {
            throw new IllegalArgumentException("LaTeX commands cannot contain non-alpha characters!");
        }

        //TODO
        String[] commands = new String[3];

        commands[0] = generateConfigValueTeXCommand(spsName, mode, "Iterations", config.getRunIterations());
        commands[1] = generateConfigValueTeXCommand(spsName, mode, "MessageLength", config.getMessageLength());
        commands[2] = generateConfigValueTeXCommand(spsName, mode, "PrewarmIterations", config.getPrewarmIterations());

        return commands;
    }

    /**
     * generates a .tex command to access a timing benchmark result
     * @param operation the operation to which the result belongs (setup | keyGen | sign | Verify)
     * @param spsName the name of the SPS scheme being output (e.g. Groth, KPW, AKOT)
     */
    public static String[] generateTimeResultTeXCommands(String operation, String spsName,
                                                          BenchmarkTimes bmTimes) {

        if(!(operation.matches("[a-zA-Z]+") && spsName.matches("[a-zA-Z]+"))) {
            throw new IllegalArgumentException("LaTeX commands cannot contain non-alpha characters!");
        }

        String[] commands = new String[4];

        commands[0] = generateTimeResultTeXCommand(operation, spsName, "Avg", BenchmarkTimes.milliFromNano(bmTimes.getAvgTime()));
        commands[1] = generateTimeResultTeXCommand(operation, spsName, "Min", BenchmarkTimes.milliFromNano(bmTimes.getMinTime()));
        commands[2] = generateTimeResultTeXCommand(operation, spsName, "Max", BenchmarkTimes.milliFromNano(bmTimes.getMaxTime()));
        commands[3] = generateTimeResultTeXCommand(operation, spsName, "Sum", BenchmarkTimes.milliFromNano(bmTimes.getSumTime()));

        return commands;
    }

    /**
     * generates a .tex command to access a benchmark config value
     * @param spsName the name of the SPS scheme being output (e.g. Groth, KPW, AKOT)
     * @param configName the display name of the config value
     * @param configValue the config value
     */
    private static String generateConfigValueTeXCommand(String spsName, BenchmarkMode mode,
                                                       String configName, int configValue) {

        if(!(configName.matches("[a-zA-Z]+") && spsName.matches("[a-zA-Z]+"))) {
            throw new IllegalArgumentException("LaTeX commands cannot contain non-alpha characters!");
        }

        return String.format("\\newcommand{\\%s %s Config %s}{%d}",
                spsName, mode, configName, configValue).replace(" ", "");
    }

    /**
     * generates a .tex command to access a timing benchmark result
     * @param operation the operation to which the result belongs (setup | keyGen | sign | Verify)
     * @param spsName the name of the SPS scheme being output (e.g. Groth, KPW, AKOT)
     * @param timerName the kind of time being stored ( min | max | avg | sum )
     * @param timerResultInMs result of the benchmark in this category (rounded to 2 digits)
     */
    private static String generateTimeResultTeXCommand(String operation, String spsName,
                                                       String timerName, double timerResultInMs) {

        if(!(operation.matches("[a-zA-Z]+") && spsName.matches("[a-zA-Z]+")
                && timerName.matches("[a-zA-Z]+"))) {
            throw new IllegalArgumentException("LaTeX commands cannot contain non-alpha characters!");
        }

        return String.format("\\newcommand{\\%s Time %s %s}{%,.2f}",
                spsName, operation, timerName, timerResultInMs).replace(" ", "");
    }

    /**
     * generates a .tex command to access a counting benchmark result
     * @param operation the operation to which the result belongs (setup | keyGen | sign | Verify)
     * @param spsName the name of the SPS scheme being output (e.g. Groth, KPW, AKOT)
     */
    private static String[] generateCountingResultTeXCommands(String operation, String groupOperation, String spsName,
                                                              DebugBilinearGroup bGroup) {

        if(!(operation.matches("[a-zA-Z]+") && spsName.matches("[a-zA-Z]+"))) {
            throw new IllegalArgumentException("LaTeX commands cannot contain non-alpha characters!");
        }

        String[] commands = new String[3];
        commands[0] = generateCountingResultTeXCommand(operation, groupOperation, spsName, "G", (DebugGroup)bGroup.getG1());
        commands[1] = generateCountingResultTeXCommand(operation, groupOperation, spsName, "H", (DebugGroup)bGroup.getG2());
        commands[2] = generateCountingResultTeXCommand(operation, groupOperation, spsName, "T", (DebugGroup)bGroup.getGT());

        return commands;
    }

    /**
     * generates a .tex command to access a counting benchmark result for a single group
     * @param operation the operation to which the result belongs (setup | keyGen | sign | Verify)
     * @param spsName the name of the SPS scheme being output (e.g. Groth, KPW, AKOT)
     */
    private static String generateCountingResultTeXCommand(String operation, String groupOperation, String spsName,
                                                           String groupName, DebugGroup group) {
        return String.format("\\newcommand{\\%s Count %s %s}{%d}",
                spsName, operation, groupName, group.getNumOpsTotal(groupOperation)).replace(" ", "");
    }

}
