import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15PublicParameters;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15PublicParametersGen;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15SignatureScheme;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.mclwrap.bn254.MclBilinearGroup;
import spsbenchmark.BenchmarkConfig;
import spsbenchmark.BenchmarkUtils;
import spsbenchmark.MessageGenerator;
import spsbenchmark.SPSBenchmark;

/**
 * prepares schemes for benchmark and provides entry point
 */
public class BenchmarkRunner
{

    private static final int PREWARM_ITERATIONS = 20;
    private static final int BM_ITERATIONS = 100;
    private static final int MESSAGE_LENGTH = 32;

    // the bilinear group to use
    private static final MclBilinearGroup.GroupChoice GROUP_CHOICE = MclBilinearGroup.GroupChoice.BN254;
    private static BilinearGroup sharedBGroup;

    // messages to sing. Set in either G1 or G2
    private static MessageBlock[] group1MessageBlocks;
    private static MessageBlock[] group2MessageBlocks;

    /**
     * the general config. Does not contain any messages.
    */
    private static BenchmarkConfig sharedConfig;

    private static SPSBenchmark grothBM;

    // go, benchmarks, go!
    public static void main(String[] args)
    {
        prepareBenchmark();

        runGroth1Benchmark();

    }

    /**
     * prepares benchmarks by generating messages and initializing the first scheme instances
     */
    private static void prepareBenchmark() {

        sharedBGroup = new MclBilinearGroup(GROUP_CHOICE);
        sharedConfig = new BenchmarkConfig(null, sharedBGroup,
                PREWARM_ITERATIONS, BM_ITERATIONS, MESSAGE_LENGTH);

        // prepare message sets for both groups of {@code sharedBGroup}.
        // These precompute automatically
        group1MessageBlocks = MessageGenerator.prepareMessages(sharedBGroup.getG1(), BM_ITERATIONS, MESSAGE_LENGTH);
        group2MessageBlocks = MessageGenerator.prepareMessages(sharedBGroup.getG2(), BM_ITERATIONS, MESSAGE_LENGTH);

        BenchmarkUtils.prettyPrintConfig(sharedConfig);
        // set up complete
    }

    /**
     * runs a benchmark for the groth15 SPS scheme (signing G_1 elements)
     */
    private static void runGroth1Benchmark() {

        BenchmarkUtils.padString(String.format("Benchmark scheme %s", SPSGroth15SignatureScheme.class.getSimpleName()));
        System.out.println(BenchmarkUtils.separator());

        SPSGroth15PublicParameters pp = new SPSGroth15PublicParametersGen().generatePublicParameter(
                sharedBGroup, SPSGroth15PublicParametersGen.Groth15Type.type1, MESSAGE_LENGTH);

        SPSGroth15SignatureScheme groth1Scheme = new SPSGroth15SignatureScheme(pp);

        //run groth benchmark
        grothBM = new SPSBenchmark(sharedConfig, group1MessageBlocks, groth1Scheme);
    }

}
