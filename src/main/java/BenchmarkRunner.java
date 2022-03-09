import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.Signature;
import org.cryptimeleon.craco.sig.SignatureKeyPair;
import org.cryptimeleon.craco.sig.sps.groth15.*;
import org.cryptimeleon.craco.sig.sps.kpw15.*;
import org.cryptimeleon.mclwrap.bn254.MclBilinearGroup;

public class BenchmarkRunner
{

    SPSKPW15SignatureScheme s;

    static final int warmupIterations = 100;
    static final int testIterations = 5000;


    public static void main(String[] args)
    {
        // Run test setup
        System.out.println("Warmup Benchmark");
        for (int i = 0; i < warmupIterations; i++)
            TestCase(true);

        System.out.println("Run Benchmark");
        long totalEvaluationTime = 0;
        long minEvaluationTime = Long.MAX_VALUE;
        long maxEvaluationTime = 0;
        for (int i = 0; i < testIterations; i++) {
            long evalTime = TestCase(false);
            totalEvaluationTime += evalTime;
            minEvaluationTime = Math.min(minEvaluationTime, evalTime);
            maxEvaluationTime = Math.max(maxEvaluationTime, evalTime);
        }

        System.out.println("Benchmark finished!");
        System.out.println("Mean evaluation time: " + FormatEvaluationTimeInMs(totalEvaluationTime / testIterations));
        System.out.println("Min evaluation time: " + FormatEvaluationTimeInMs(minEvaluationTime));
        System.out.println("Max evaluation time: " + FormatEvaluationTimeInMs(maxEvaluationTime));
    }


    public static long TestCase(boolean silent)
    {
        long referenceTime = 0;
        if (!silent) {
            // Benchmark start
            referenceTime = System.nanoTime();
        }

        //TestKPW();
        TestGroth();

        // Benchmark stop
        long timeToEvaluate = 0;
        if (!silent) {
            timeToEvaluate = System.nanoTime() - referenceTime;
            //System.out.println("Time to evaluate: " + FormatEvaluationTimeInMs(timeToEvaluate));
        }

        return timeToEvaluate;
    }

    public static String FormatEvaluationTimeInMs(long timeInNanoSeconds)
    {
        return timeInNanoSeconds / 1e-6 + "ms";
    }

    // TEST CASES

    public static void TestKPW() {
        SPSKPW15PublicParameters pp = new SPSKPW15PublicParameterGen().generatePublicParameter(128, true, 1);

        SPSKPW15SignatureScheme scheme = new SPSKPW15SignatureScheme(pp);
        SignatureKeyPair keys = scheme.generateKeyPair(1);

        GroupElementPlainText message = new GroupElementPlainText(pp.getG1GroupGenerator().getStructure().getUniformlyRandomElement());
        Signature sig = scheme.sign(new MessageBlock(message), keys.getSigningKey());
        boolean foo = scheme.verify(new MessageBlock(message), sig, keys.getVerificationKey());
    }


    public static void TestGroth() {

        int securityParam = 128;
        SPSGroth15PublicParametersGen.Groth15Type type = SPSGroth15PublicParametersGen.Groth15Type.type1;
        int numberOfMessages = 10;

        // setup scheme
        SPSGroth15PublicParametersGen ppSetup = new SPSGroth15PublicParametersGen();

        SPSGroth15PublicParameters pp = ppSetup.generatePublicParameter(new MclBilinearGroup(), type, numberOfMessages);
        SPSGroth15SignatureScheme scheme = new SPSGroth15SignatureScheme(pp);

        // generate two different key pairs to test
        SignatureKeyPair<? extends SPSGroth15VerificationKey, ? extends SPSGroth15SigningKey> keyPair = scheme.generateKeyPair(
                numberOfMessages);

        // generate two different message blocks to test
        GroupElementPlainText[] messages = new GroupElementPlainText[numberOfMessages];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = new GroupElementPlainText(pp.getPlaintextGroupGenerator().getStructure().getUniformlyRandomElement());
        }
        MessageBlock messageBlock = new MessageBlock(messages);


        // TODO

        //return new SignatureSchemeParams(scheme, pp, messageBlock, wrongMessageBlock, keyPair, wrongKeyPair);
    }
}
