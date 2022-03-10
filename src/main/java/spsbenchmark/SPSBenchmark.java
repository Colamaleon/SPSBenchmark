package spsbenchmark;

import org.cryptimeleon.craco.common.PublicParameters;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.MultiMessageStructurePreservingSignatureScheme;
import org.cryptimeleon.craco.sig.Signature;
import org.cryptimeleon.craco.sig.SignatureKeyPair;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;

import javax.swing.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * Initializes a benchmark for a given scheme
 * and runs its benchmarks
 */
public class SPSBenchmark<SchemeType extends MultiMessageStructurePreservingSignatureScheme> {

    // these are given on start up

    /**
     * defines the shared parameters of the benchmark
     */
    private final BenchmarkConfig config;

    /**
     * points to a function that constructs a new instance of the scheme
     * using a {@code BilinearGroup} and the intended messageLength.
     * This allows us to bundle the calculation of the public parameters and the initialization of the scheme-instance.
     */
    private BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme> schemeSetupFunction;

    /**
     * the instance of the signature scheme to initialize copies with.
     */
    private MultiMessageStructurePreservingSignatureScheme schemeBlueprint;

    /**
     * the messages to run the benchmark with. might differ between schemes
     * to account for different message spaces.
     */
    private final MessageBlock[] messages;


    // these are calculated during the benchmark

    private SignatureKeyPair[] bmKeyPairs;

    private Signature[] bmSignatures;


    public SPSBenchmark(BenchmarkConfig config,
                        MessageBlock[] messages,
                        BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme> schemeSetupFunction) {
        this.config = config;
        this.messages = messages;

        //set up function to generate new scheme instances
        this.schemeSetupFunction = schemeSetupFunction;
        //build one initial instance of our scheme
        this.schemeBlueprint = schemeSetupFunction.apply(config.getbGroup(), config.getMessageLength());

        //set up arrays for storage
        this.bmKeyPairs = new SignatureKeyPair[config.getRunIterations()];
        this.bmSignatures = new Signature[config.getRunIterations()];

        runTimedBenchmarks();
    }

    /**
     * Run all steps required for a signature scheme (i.e. setup, keyGen, sign, verify)
     * the specified number of times (see {@code config.runIterations}) -
     * tracking the completion times for each step
     */
    private void runTimedBenchmarks() {

        // setup
        runTimeBenchmark("setup", this::runSetup);

        // keyGen
        runTimeBenchmark("keyGen", this::runKeyGen);

        // sign
        runTimeBenchmark("sign", this::runSign);

        // verify
        runTimeBenchmark("verify", this::runVerify);

    }

    /**
     * Runs the given method n times and
     * measures the min, max and average time to complete of all {@param iterations}.
     */
    private BenchmarkTimes measureStepTimes(IntConsumer targetMethod) {

        // store measurements
        double sumTime = 0;
        double minTime = Double.MAX_VALUE;
        double maxTime = Double.MIN_VALUE;

        // times used for tracking
        double refTime = -1;
        double finishTime = -1;

        for (int i = 0; i < config.getRunIterations(); i++) {
            //begin counting here
            refTime = System.nanoTime();

            // run method to benchmark
            targetMethod.accept(i);

            //check time
            finishTime = System.nanoTime();

            double delta = finishTime - refTime;

            if(delta < minTime) {
                minTime = delta;
            }

            if(delta > maxTime) {
                maxTime = delta;
            }

            sumTime += delta;
        }

        // output times
        return new BenchmarkTimes(minTime, maxTime, sumTime, sumTime / config.getRunIterations());
    }


    /**
     * Runs a benchmark step.
     * {@param bmName} name of the current benchmark function for printing
     * {@param isPrewarm} should the function measure times or just run to pre-warm the function
     */
    private void runTimeBenchmark(String bmName, IntConsumer targetFunction, boolean isPrewarm) {

        System.out.println(BenchmarkUtils.padString(
                String.format("[START][TIME] %s %s [%s] benchmark...", (isPrewarm) ? "(pre-warm)" : "",
                        bmName,
                        schemeBlueprint.getClass().getSimpleName())));

        if(isPrewarm) {
            // run without measuring
            for (int i = 0; i < config.getPrewarmIterations(); i++) {
                targetFunction.accept(i);
            }
            System.out.println(BenchmarkUtils.padString(
                    String.format("[DONE][TIME] %s %s [%s] benchmark...", (isPrewarm) ? "(pre-warm)" : "",
                            bmName,
                            schemeBlueprint.getClass().getSimpleName())));
            System.out.println(BenchmarkUtils.padString(""));
        }else {
            // run and measure times
            BenchmarkTimes results = measureStepTimes(targetFunction);


            // print results
            System.out.println(BenchmarkUtils.padString(""));

            System.out.println(BenchmarkUtils.padString(
                    String.format("[DONE][TIME] %s %s [%s] benchmark...", (isPrewarm) ? "(pre-warm)" : "",
                            bmName,
                            schemeBlueprint.getClass().getSimpleName())));
            System.out.println(results.getPrettyString());

            System.out.println(BenchmarkUtils.separator());
            System.out.println(BenchmarkUtils.separator());
        }
    }

    /**
     * Runs a benchmark step - first for pre-warm and then with measurements
     * */
    private void runTimeBenchmark(String bmName, IntConsumer targetFunction) {
        runTimeBenchmark(bmName, targetFunction, true);
        runTimeBenchmark(bmName, targetFunction, false);
    }

    /**
     * initializes the scheme a single time.
     * Uses the provided bilinear group and the provided construction delegate to do so.
     */
    private void runSetup(int iterationNumber) {
        MultiMessageStructurePreservingSignatureScheme tempSchemeInstance
                = schemeSetupFunction.apply(config.getbGroup(), config.getMessageLength());
    }

    /**
     * generates a keyPair using the blueprint instance and stores it for later use
     * {@param iterationNumber} determines where to store the generated key.
     */
    private void runKeyGen(int iterationNumber) {
        SignatureKeyPair keyPair = schemeBlueprint.generateKeyPair(config.getMessageLength());
        bmKeyPairs[iterationNumber] = keyPair;
    }

    /**
     * signs the {@param iterationNumber}s MessageBlock and stores the resulting signature for later use
     * {@param iterationNumber} determines where to store the generated key.
     */
    private void runSign(int iterationNumber) {
        Signature sigma = schemeBlueprint.sign(bmKeyPairs[iterationNumber].getSigningKey(), messages[iterationNumber]);
        bmSignatures[iterationNumber] = sigma;
    }

    /**
     * verifies the {@param iterationNumber}s MessageBlock
     * {@param iterationNumber} determines where to store the generated key.
     */
    private void runVerify(int iterationNumber) {

        // verify the signature
        schemeBlueprint.verify(messages[iterationNumber],
                bmSignatures[iterationNumber],
                bmKeyPairs[iterationNumber].getVerificationKey());
    }


}
