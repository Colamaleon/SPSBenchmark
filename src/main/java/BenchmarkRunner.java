import org.cryptimeleon.craco.common.PublicParameters;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.MultiMessageStructurePreservingSignatureScheme;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15PublicParameters;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15PublicParametersGen;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15SignatureScheme;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.mclwrap.bn254.MclBilinearGroup;
import spsbenchmark.BenchmarkConfig;
import spsbenchmark.BenchmarkUtils;
import spsbenchmark.MessageGenerator;
import spsbenchmark.SPSBenchmark;

import java.util.function.BiFunction;
import java.util.function.Function;

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
     *
     * Note: the java compiler doesn't like the unchecked casting of the constructionDelegate to the required type.
     *       The warning is disabled however, since this is the only context in which we use this function.
     */
    @SuppressWarnings("unchecked")
    private static void runGroth1Benchmark() {

        BenchmarkUtils.padString(String.format("Benchmark scheme %s", SPSGroth15SignatureScheme.class.getSimpleName()));
        System.out.println(BenchmarkUtils.separator());

        // defines a delegate function that constructs an instance of the scheme for us
        BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme> constructionDelegate
                = (bGroup, messageLength) -> {
                SPSGroth15PublicParameters params = new SPSGroth15PublicParametersGen().generatePublicParameter(
                        bGroup, SPSGroth15PublicParametersGen.Groth15Type.type1, messageLength);
                return new SPSGroth15SignatureScheme(params);
        };

        //run groth benchmark
        grothBM = new SPSBenchmark(sharedConfig, group1MessageBlocks, constructionDelegate);
    }


}
