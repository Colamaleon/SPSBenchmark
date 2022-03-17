import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.MultiMessageStructurePreservingSignatureScheme;
import org.cryptimeleon.craco.sig.sps.SPSPublicParametersGen;
import org.cryptimeleon.craco.sig.sps.agho11.SPSAGHO11PublicParameters;
import org.cryptimeleon.craco.sig.sps.agho11.SPSAGHO11PublicParametersGen;
import org.cryptimeleon.craco.sig.sps.agho11.SPSAGHO11SignatureScheme;
import org.cryptimeleon.craco.sig.sps.akot15.AKOT15SharedPublicParameters;
import org.cryptimeleon.craco.sig.sps.akot15.fsp2.SPSFSP2SignatureScheme;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15PublicParameters;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15PublicParametersGen;
import org.cryptimeleon.craco.sig.sps.groth15.SPSGroth15SignatureScheme;
import org.cryptimeleon.craco.sig.sps.kpw15.SPSKPW15PublicParameters;
import org.cryptimeleon.craco.sig.sps.kpw15.SPSKPW15SignatureScheme;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.mclwrap.bn254.MclBilinearGroup;
import spsbenchmark.BenchmarkConfig;
import spsbenchmark.MessageGenerator;
import spsbenchmark.PrintBenchmarkUtils;
import spsbenchmark.SPSBenchmark;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * prepares schemes for benchmark and provides entry point
 */
public class BenchmarkRunner
{

    private static int PREWARM_ITERATIONS;
    private static int BM_ITERATIONS;
    private static int MESSAGE_LENGTH;

    // the bilinear group to use
    private static final MclBilinearGroup.GroupChoice GROUP_CHOICE = MclBilinearGroup.GroupChoice.BN254;

    private static BilinearGroup sharedTimerBGroup;

    private static DebugBilinearGroup sharedCountingBGroup;


    // messages to sing. Set in either G1 or G2
    private static MessageBlock[] group1MessageBlocks;
    private static MessageBlock[] group2MessageBlocks;

    private static MessageBlock[] group1CountingMessageBlocks;
    private static MessageBlock[] group2CountingMessageBlocks;


    /**
     * the general config. Does not contain any messages.
    */
    private static BenchmarkConfig sharedConfig;


    // go, benchmarks, go!
    /**
     * Runs a benchmark with the selected parameters
     * Usage: main (t|c) NameOfScheme messageLength prewarmIterations iterations
     */
    public static void main(String[] args) {
        // parse message length, pre-warm iterations and iterations from args

        MESSAGE_LENGTH      = Integer.parseInt(args[2]);
        PREWARM_ITERATIONS  = Integer.parseInt(args[3]);
        BM_ITERATIONS       = Integer.parseInt(args[4]);

        prepareBenchmark();

        //find appropriate benchmark to run via reflections
        SPSBenchmark.BenchmarkMode mode = (args[0].equals("t")) ? SPSBenchmark.BenchmarkMode.Time : SPSBenchmark.BenchmarkMode.Counting;
        String benchmarkMethodName = "run" + args[1] + "Benchmark";

        try{
            Method benchmarkMethod = BenchmarkRunner.class.getMethod(benchmarkMethodName, SPSBenchmark.BenchmarkMode.class);
            benchmarkMethod.invoke(BenchmarkRunner.class, mode);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * prepares benchmarks by generating messages and initializing the first scheme instances
     */
    private static void prepareBenchmark() {

        sharedTimerBGroup = new MclBilinearGroup(GROUP_CHOICE);
        sharedCountingBGroup = new DebugBilinearGroup(sharedTimerBGroup.size(), BilinearGroup.Type.TYPE_3);

        sharedConfig = new BenchmarkConfig(sharedTimerBGroup, sharedCountingBGroup,
                PREWARM_ITERATIONS, BM_ITERATIONS, MESSAGE_LENGTH);

        // prepare message sets for both groups of {@code sharedBGroup}.
        // These precompute automatically
        group1MessageBlocks = MessageGenerator.prepareMessages(sharedTimerBGroup.getG1(), BM_ITERATIONS, MESSAGE_LENGTH);
        group2MessageBlocks = MessageGenerator.prepareMessages(sharedTimerBGroup.getG2(), BM_ITERATIONS, MESSAGE_LENGTH);

        group1CountingMessageBlocks = MessageGenerator.prepareMessages(
                sharedCountingBGroup.getG1(), BM_ITERATIONS, MESSAGE_LENGTH);
        group2CountingMessageBlocks = MessageGenerator.prepareMessages(
                sharedCountingBGroup.getG2(), BM_ITERATIONS, MESSAGE_LENGTH);

        // set up complete
    }

    /**
     * runs a benchmark for the Groth15 SPS scheme (signing G_1 elements)
     */
    public static void runGroth1Benchmark(SPSBenchmark.BenchmarkMode mode) {

        PrintBenchmarkUtils.padString(
                String.format("Benchmark scheme %s", SPSGroth15SignatureScheme.class.getSimpleName()));
        PrintBenchmarkUtils.printSeparator();

        // defines a delegate function that constructs an instance of the scheme for us
        BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme> constructionDelegate
                = (bGroup, messageLength) -> {
                SPSGroth15PublicParameters params = new SPSGroth15PublicParametersGen().generatePublicParameter(
                        bGroup, SPSGroth15PublicParametersGen.Groth15Type.type1, messageLength);
                return new SPSGroth15SignatureScheme(params);
        };

        if(mode == SPSBenchmark.BenchmarkMode.Time) {
            //run Groth1 benchmark in timing mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Time,
                    group1MessageBlocks, constructionDelegate);
        }
        else {
            //run Groth1 benchmark in counting mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Counting,
                    group1CountingMessageBlocks, constructionDelegate);
        }
    }


    /**
     * runs a benchmark for the AGHO11 SPS scheme (signing G_1 elements)
     */
    public static void runAGHO11Benchmark(SPSBenchmark.BenchmarkMode mode) {

        PrintBenchmarkUtils.padString(
                String.format("Benchmark scheme %s", SPSAGHO11SignatureScheme.class.getSimpleName()));
        PrintBenchmarkUtils.printSeparator();

        //Note: for this scheme, we need to wrap the messages within a second messageBlock
        MessageBlock[] wrappedMessages = Arrays.stream(group1MessageBlocks).map(
                x -> new MessageBlock(x, new MessageBlock()))
                .toArray(MessageBlock[]::new);

        MessageBlock[] wrappedCountingMessages = Arrays.stream(group1CountingMessageBlocks).map(
                        x -> new MessageBlock(x, new MessageBlock()))
                .toArray(MessageBlock[]::new);


        // defines a delegate function that constructs an instance of the scheme for us
        BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme> constructionDelegate
                = (bGroup, messageLength) -> {
            SPSAGHO11PublicParameters params = SPSAGHO11PublicParametersGen.generateParameters(
                    bGroup, new Integer[] {messageLength, 0}
            );
            return new SPSAGHO11SignatureScheme(params);
        };

        if(mode == SPSBenchmark.BenchmarkMode.Time) {
            //run AGHO benchmark in timing mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Time,
                    wrappedMessages, constructionDelegate);
        }
        else {
            //run AGHO benchmark in counting mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Counting,
                    wrappedCountingMessages, constructionDelegate);
        }
    }


    /**
     * runs a benchmark for the AKOT15 SPS scheme (signing G_2 elements)
     */
    public static void runAKOT15Benchmark(SPSBenchmark.BenchmarkMode mode) {

        PrintBenchmarkUtils.padString(
                String.format("Benchmark scheme %s", SPSFSP2SignatureScheme.class.getSimpleName()));
        PrintBenchmarkUtils.printSeparator();

        // defines a delegate function that constructs an instance of the scheme for us
        BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme>
                constructionDelegate = (bGroup, messageLength) -> {
            AKOT15SharedPublicParameters params
                    = SPSPublicParametersGen.generateParameters(
                    AKOT15SharedPublicParameters::new, bGroup, messageLength
            );
            return new SPSFSP2SignatureScheme(params);
        };

        if(mode == SPSBenchmark.BenchmarkMode.Time) {
            //run AKOT15 benchmark in timing mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Time,
                    group2MessageBlocks, constructionDelegate);
        }
        else {
            //run AKOT15 benchmark in counting mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Counting,
                    group2CountingMessageBlocks, constructionDelegate);
        }
    }

    /**
     * runs a benchmark for the KPW15 SPS scheme (signing G_1 elements)
     */
    public static void runKPW15Benchmark(SPSBenchmark.BenchmarkMode mode) {

        PrintBenchmarkUtils.padString(
                String.format("Benchmark scheme %s", SPSKPW15SignatureScheme.class.getSimpleName()));
        PrintBenchmarkUtils.printSeparator();

        // defines a delegate function that constructs an instance of the scheme for us
        BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme>
                constructionDelegate = (bGroup, messageLength) -> {
            SPSKPW15PublicParameters params
                    = SPSPublicParametersGen.generateParameters(
                    SPSKPW15PublicParameters::new, bGroup, messageLength
            );
            return new SPSKPW15SignatureScheme(params);
        };

        if(mode == SPSBenchmark.BenchmarkMode.Time) {
            //run KPW15 benchmark in timing mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Time,
                    group1MessageBlocks, constructionDelegate);
        }
        else {
            //run KPW15 benchmark in counting mode
            new SPSBenchmark(sharedConfig, SPSBenchmark.BenchmarkMode.Counting,
                    group1CountingMessageBlocks, constructionDelegate);
        }
    }
    

}
