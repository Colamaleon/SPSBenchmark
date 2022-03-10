package spsbenchmark;

import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;

/**
 * stores a set of benchmark settings that should be shared among all schemes
 * to be compared
 */
public class BenchmarkConfig {

    /**
     * the bilinear map to use for timer benchmarks
     */
    private BilinearGroup timerBGroup;

    /**
     * the bilinear map to use for counting benchmarks
     */
    private DebugBilinearGroup countingBGroup;

    /**
     * the amount of pre-warm iterations to use when benchmarking
     */
    private int prewarmIterations;

    /**
     * the amount of iterations to use for calculating benchmark times
     */
    private int runIterations;

    /**
     * defines the target length of messageBlocks for benchmarks.
     */
    private int messageLength;


    public BenchmarkConfig(BilinearGroup timerBGroup, DebugBilinearGroup countingBGroup, int prewarmIterations, int runIterations, int messageLength) {
        this.timerBGroup = timerBGroup;
        this.countingBGroup = countingBGroup;

        // not that clean, but catches index issues when pre-warming
        if(prewarmIterations > runIterations) {
            throw new IllegalArgumentException("the amount of pre-warm iterations may not be larger than the amount "
            + "of measured iterations");
        }

        this.prewarmIterations = prewarmIterations;
        this.runIterations = runIterations;
        this.messageLength = messageLength;
    }

    public BilinearGroup getTimerBGroup() {
        return timerBGroup;
    }

    public DebugBilinearGroup getCountingBGroup() { return countingBGroup; }

    public int getPrewarmIterations() {
        return prewarmIterations;
    }

    public int getRunIterations() {
        return runIterations;
    }

    public int getMessageLength() {
        return messageLength;
    }


    public String toPrettyString() {
        return String.format("%s iterations :: messageLength %s :: %s pre-warm",
                runIterations,
                messageLength,
                prewarmIterations);
    }

}
