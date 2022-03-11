package spsbenchmark;

import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.*;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;

/**
 * Initializes a benchmark for a given scheme.
 * The Benchmark will either time each of the signature's functions
 * or
 * Run the scheme a single time, counting the group operations required in each step
 */
public class SPSBenchmark {

    public enum BenchmarkMode {Time,Counting}

    // these are given on start up

    /**
     * defines the shared parameters of the benchmark
     */
    private final BenchmarkConfig config;

    private final BenchmarkMode mode;

    /**
     * points to a function that constructs a new instance of the scheme
     * using a {@code BilinearGroup} and the intended messageLength.
     * This allows us to bundle the calculation of the public parameters and the initialization of the scheme-instance.
     */
    private final BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme> schemeSetupFunction;

    /**
     * the instance of the signature scheme used for benchmarks
     */
    private final MultiMessageStructurePreservingSignatureScheme schemeBlueprint;


    /**
     * the messages to run the timer benchmark with.
     */
    private final MessageBlock[] messages;


    // these are calculated during the benchmark

    private final MultiMessageStructurePreservingSignatureScheme[] bmSchemeInstances;

    private final SignatureKeyPair[] bmKeyPairs;

    private final Signature[] bmSignatures;


    /**
     * Sets up a benchmark for the scheme generated by {@param schemeSetupFunction}.
     * The {@param mode} determines whether to measure the time steps take, or the number of group operations
     */
    public SPSBenchmark(BenchmarkConfig config,
                        BenchmarkMode mode,
                        MessageBlock[] messages,
                        BiFunction<BilinearGroup,Integer,MultiMessageStructurePreservingSignatureScheme> schemeSetupFunction) {

        // print the config
        PrintBenchmarkUtils.prettyPrintConfig(config, mode);

        this.config = config;
        this.messages = messages;
        this.mode = mode;

        //set up function to generate new scheme instances
        this.schemeSetupFunction = schemeSetupFunction;

        // pick the appropriate bilinear group based on the current mode
        BilinearGroup bGroup = (mode == BenchmarkMode.Counting) ? config.getCountingBGroup() : config.getTimerBGroup();

        //instantiate the scheme
        this.schemeBlueprint = schemeSetupFunction.apply(bGroup, config.getMessageLength());

        //set up arrays for storage
        this.bmKeyPairs = new SignatureKeyPair[config.getRunIterations()];
        this.bmSignatures = new Signature[config.getRunIterations()];
        this.bmSchemeInstances = new MultiMessageStructurePreservingSignatureScheme[config.getRunIterations()];

        // run the appropriate benchmark
        autoRunBenchmark();

        //TODO flush to txt file here
    }


    /**
     * Run all steps required for a signature scheme (i.e. setup, keyGen, sign, verify)
     * the specified number of times (see {@code config.runIterations}) for timing and once for counting -
     * tracking the appropriate statistic
     */
    @SuppressWarnings("unchecked")
    private void autoRunBenchmark() {

        //pick either the timing or counting benchmark function
        BiConsumer<String,IntConsumer> benchmarkFunc = (mode == BenchmarkMode.Counting) ?
                this::runCountingBenchmark : this::runTimeBenchmark ;

        // setup
        benchmarkFunc.accept("setup", this::runSetup);

        // keyGen
        benchmarkFunc.accept("keyGen", this::runKeyGen);

        // sign
        benchmarkFunc.accept("sign", this::runSign);

        // verify
        benchmarkFunc.accept("verify", this::runVerify);

        if(mode == BenchmarkMode.Counting) {
            // print results of counting
            System.out.println(PrintBenchmarkUtils.padString(
                    String.format("[DONE][COUNT] [%s] benchmarks...",
                            schemeBlueprint.getClass().getSimpleName())));

            System.out.println(config.getCountingBGroup().formatCounterDataAllBuckets());
        }
    }




    // benchmark steps


    /**
     * initializes the scheme a single time.
     * Uses the provided bilinear group and the provided construction delegate to do so.
     */
    private void runSetup(int iterationNumber) {
        BilinearGroup targetGroup = (mode == BenchmarkMode.Counting) ? config.getCountingBGroup() : config.getTimerBGroup();

        MultiMessageStructurePreservingSignatureScheme tempSchemeInstance
                = schemeSetupFunction.apply(targetGroup, config.getMessageLength());

        bmSchemeInstances[iterationNumber] = tempSchemeInstance;
    }

    /**
     * generates a keyPair using the blueprint instance and stores it for later use
     * {@param iterationNumber} determines where to store the generated key.
     */
    private void runKeyGen(int iterationNumber) {
        SignatureKeyPair keyPair = bmSchemeInstances[0].generateKeyPair(config.getMessageLength());

        if(mode == BenchmarkMode.Time) {
            bmKeyPairs[iterationNumber] = keyPair;
        }
        else if(mode == BenchmarkMode.Counting) {
            //if we're running in counting mode, force serialization/deserialization once
            Representation skRepr = keyPair.getSigningKey().getRepresentation();
            Representation vkRepr = keyPair.getVerificationKey().getRepresentation();
            bmKeyPairs[iterationNumber] = new SignatureKeyPair(
                    bmSchemeInstances[iterationNumber].restoreVerificationKey(vkRepr),
                    bmSchemeInstances[iterationNumber].restoreSigningKey(skRepr));
        }
        else {
            // only support counting or timer mode
            throw new IllegalArgumentException("SPSBenchmark may only run in Time or Counting mode");
        }
    }

    /**
     * signs the {@param iterationNumber}s MessageBlock and stores the resulting signature for later use
     * {@param iterationNumber} determines where to store the generated key.
     */
    private void runSign(int iterationNumber) {
        // [!] signs using different scheme instances, but with same signing key for all messages
        Signature sigma = bmSchemeInstances[0]
                .sign(bmKeyPairs[0].getSigningKey(), messages[iterationNumber]);

        if(mode == BenchmarkMode.Time) {
            bmSignatures[iterationNumber] = sigma;
        }
        else if(mode == BenchmarkMode.Counting) {
            //if we're running in counting mode, force serialization/deserialization once
            Representation repr = sigma.getRepresentation();
            bmSignatures[iterationNumber] = bmSchemeInstances[iterationNumber].restoreSignature(repr);
        }
        else {
            // only support counting or timer mode
            throw new IllegalArgumentException("SPSBenchmark may only run in Time or Counting mode");
        }
    }

    /**
     * verifies the {@param iterationNumber}s MessageBlock
     * {@param iterationNumber} determines where to store the generated key.
     */
    private void runVerify(int iterationNumber) {

        // [!] verifies using different scheme instances, but with same verification key for all messages
        bmSchemeInstances[0].verify(messages[iterationNumber],
                bmSignatures[iterationNumber],
                bmKeyPairs[0].getVerificationKey());
    }




    // Timer benchmarks


    /**
     * Runs a benchmark step.
     * {@param bmName} name of the current benchmark function for printing
     * {@param isPrewarm} should the function measure times or just run to pre-warm the function
     */
    private void runTimeBenchmark(String bmName, IntConsumer targetFunction, boolean isPrewarm) {

        System.out.println(PrintBenchmarkUtils.padString(
                String.format("[START][TIME] %s %s [%s] benchmark...", (isPrewarm) ? "(pre-warm)" : "",
                        bmName,
                        schemeBlueprint.getClass().getSimpleName())));

        if(isPrewarm) {
            // run without measuring
            for (int i = 0; i < config.getPrewarmIterations(); i++) {
                targetFunction.accept(i);
            }
            System.out.println(PrintBenchmarkUtils.padString(
                    String.format("[DONE][TIME] (pre-warm) %s [%s] benchmark...",
                            bmName,
                            schemeBlueprint.getClass().getSimpleName())));
            System.out.println(PrintBenchmarkUtils.padString(""));
        }else {
            // run and measure times
            BenchmarkTimes results = measureStepTimes(targetFunction);


            // print results
            System.out.println(PrintBenchmarkUtils.padString(""));

            System.out.println(PrintBenchmarkUtils.padString(
                    String.format("[DONE][TIME] %s [%s] benchmark...",
                            bmName,
                            schemeBlueprint.getClass().getSimpleName())));
            System.out.println(results.getPrettyString());

            PrintBenchmarkUtils.printSeparator();
            PrintBenchmarkUtils.printSeparator();
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
     * Runs the given method {@code config.getRunIterations()} times and
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




    // Counting benchmarks


    /**
     * runs the target function using the debug group; counting the group operations used.
     */
    private void runCountingBenchmark(String bmName, IntConsumer targetFunction, boolean isPrewarm) {

        System.out.println(PrintBenchmarkUtils.padString(
                String.format("[START][COUNT] %s %s [%s] benchmark...", (isPrewarm) ? "(pre-warm)" : "",
                        bmName,
                        schemeBlueprint.getClass().getSimpleName())));

        if(isPrewarm) {
            for (int i = 0; i < config.getPrewarmIterations(); i++) {
                //run the function for pre-warming
                targetFunction.accept(i);
            }
        }
        else {

            //run the function, this time counting the group operations.
            // Only needs to run on once
            config.getCountingBGroup().setBucket(bmName);
            config.getCountingBGroup().resetCounters();
            targetFunction.accept(0);
        }
    }

    private void runCountingBenchmark(String bmName, IntConsumer targetFunction) {
        runCountingBenchmark(bmName,targetFunction,true);
        runCountingBenchmark(bmName,targetFunction,false);
    }


}
