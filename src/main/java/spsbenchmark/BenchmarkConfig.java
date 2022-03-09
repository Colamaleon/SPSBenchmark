package spsbenchmark;

import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMap;

/**
 * stores a set of benchmark settings that should be shared among all schemes
 * to be compared
 */
public class BenchmarkConfig {

    /**
     * the bilinear map to use for benchmarking
     */
    private BilinearGroup bGroup;

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


    public BenchmarkConfig(MessageBlock[] messages, BilinearGroup bGroup, int prewarmIterations, int runIterations, int messageLength) {
        this.bGroup = bGroup;

        // not that clean, but catches index issues when pre-warming
        if(prewarmIterations > runIterations) {
            throw new IllegalArgumentException("the amount of pre-warm iterations may not be larger than the amount "
            + "of measured iterations");
        }

        this.prewarmIterations = prewarmIterations;
        this.runIterations = runIterations;
        this.messageLength = messageLength;
    }

    public BilinearGroup getbGroup() {
        return bGroup;
    }

    public int getPrewarmIterations() {
        return prewarmIterations;
    }

    public int getRunIterations() {
        return runIterations;
    }

    public int getMessageLength() {
        return messageLength;
    }


    /**
     * Creates a config with different messages based on an exsisting one.
     * This is useful to create equal configs for schemes with different message spaces
     */
    public BenchmarkConfig createIndividualCopy(MessageBlock[] messages) {
        return new BenchmarkConfig(
                messages,
                this.bGroup,
                this.prewarmIterations,
                this.runIterations,
                this.messageLength);
    }

    public String toPrettyString() {
        return String.format("%s @ %s iterations :: messageLength %s :: %s pre-warm",
                bGroup.toString(),
                runIterations,
                messageLength,
                prewarmIterations);
    }

}
