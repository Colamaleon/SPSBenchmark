package spsbenchmark;

/**
 * holds a set of benchmark times
 */
public class BenchmarkTimes {

    private double minTime;

    private double maxTime;

    private double sumTime;

    private double avgTime;

    public BenchmarkTimes(double minTime, double maxTime, double sumTime, double avgTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.sumTime = sumTime;
        this.avgTime = avgTime;
    }

    public static String formatTimeInMs(double timeInNanoSeconds)
    {
        return (timeInNanoSeconds) / 1000000 + " ms";
    }

    public String getPrettyString() {
        return PrintBenchmarkUtils.padString(String.format("*** Times measured :: avg: %s  |  min: %s  |  max: %s  | total: %s",
                formatTimeInMs(avgTime),
                formatTimeInMs(minTime),
                formatTimeInMs(maxTime),
                formatTimeInMs(sumTime)), PrintBenchmarkUtils.CONSOLE_WIDTH);
    }

}
