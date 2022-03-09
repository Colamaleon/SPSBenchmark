import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.*;
import org.cryptimeleon.craco.sig.sps.SPSPublicParameters;
import org.cryptimeleon.craco.sig.sps.groth15.*;
import org.cryptimeleon.craco.sig.sps.kpw15.*;


import org.cryptimeleon.craco.common.plaintexts.PlainText;
import org.cryptimeleon.craco.sig.Signature;
import org.cryptimeleon.craco.sig.SignatureKeyPair;
//import org.cryptimeleon.math.structures.groups.counting.CountingBilinearGroup;
import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;

public class GrothVerifyCountingBenchmark {

    // Testing setup
    public static final int[] messageLenghtSetups = new int[] { 100 };
    public static final int randomPrimeBitLength = 256;
    public static final int kpwSecurityParameter = 128;


    public static void main(String[] args)
    {
        for (int messageLength : messageLenghtSetups) {
            //benchmarkGroth(messageLength);
            benchmarkKpw(messageLength);
        }
    }

    // Groth
    // KPW

    // AKOT
    // AGHO

    public static void benchmarkGroth(int messageLength) {
        DebugBilinearGroup bilGroup = new DebugBilinearGroup(RandomGenerator.getRandomPrime(randomPrimeBitLength), BilinearGroup.Type.TYPE_3);
        SPSGroth15PublicParametersGen parameterGen = new SPSGroth15PublicParametersGen();
        SPSGroth15PublicParameters pp = parameterGen.generatePublicParameter(bilGroup, SPSGroth15PublicParametersGen.Groth15Type.type1, messageLength);
        runGroth(bilGroup, new SPSGroth15SignatureScheme(pp), pp, messageLength);
    }

    public static void benchmarkKpw(int messageLengh) {
        DebugBilinearGroup bilGroup = new DebugBilinearGroup(RandomGenerator.getRandomPrime(randomPrimeBitLength), BilinearGroup.Type.TYPE_3);
        SPSKPW15PublicParameterGen parameterGen = new SPSKPW15PublicParameterGen();
        SPSKPW15PublicParameters pp = parameterGen.generatePublicParameter(kpwSecurityParameter, true, messageLengh);
        runGeneric(bilGroup, new SPSKPW15SignatureScheme(pp), pp, messageLengh);
    }

    public static void benchmarkAkot(int messageLengh) {
        DebugBilinearGroup bilGroup = new DebugBilinearGroup(RandomGenerator.getRandomPrime(randomPrimeBitLength), BilinearGroup.Type.TYPE_3);
        // TODO

        SPSKPW15PublicParameterGen parameterGen = new SPSKPW15PublicParameterGen();
        SPSKPW15PublicParameters pp = parameterGen.generatePublicParameter(kpwSecurityParameter, true, messageLengh);
        runGeneric(bilGroup, new SPSKPW15SignatureScheme(pp), pp, messageLengh);
    }


    public static void runGroth(DebugBilinearGroup bilGroup, MultiMessageStructurePreservingSignatureScheme scheme, SPSGroth15PublicParameters pp, int numMessages)
    {
        PlainText plainText;
        Signature signature;
        VerificationKey verificationKey;

        SignatureKeyPair<? extends VerificationKey, ? extends SigningKey> keyPair =
                scheme.generateKeyPair(numMessages);
        verificationKey = keyPair.getVerificationKey();
        GroupElementPlainText[] messages = new GroupElementPlainText[numMessages];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = new GroupElementPlainText(pp.getBilinearMap().getG1().getUniformlyRandomElement());
        }
        plainText = new MessageBlock(messages);
        signature = scheme.sign(plainText, keyPair.getSigningKey());
        // Get all representations to force computation
        signature.getRepresentation();
        verificationKey.getRepresentation();
        bilGroup.resetCounters();
        System.out.println("********** Running regular benchmark with numMessages = " + numMessages + " *********");
        scheme.verify(plainText, signature, verificationKey);
        System.out.println(bilGroup.formatCounterData());
        bilGroup.resetCounters();
    }

    public static void runGeneric(DebugBilinearGroup bilGroup, MultiMessageStructurePreservingSignatureScheme scheme, SPSPublicParameters pp, int numMessages)
    {
        PlainText plainText;
        Signature signature;
        VerificationKey verificationKey;

        SignatureKeyPair<? extends VerificationKey, ? extends SigningKey> keyPair =
                scheme.generateKeyPair(numMessages);
        verificationKey = keyPair.getVerificationKey();
        GroupElementPlainText[] messages = new GroupElementPlainText[numMessages];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = new GroupElementPlainText(pp.getBilinearMap().getG1().getUniformlyRandomElement());
        }
        plainText = new MessageBlock(messages);
        signature = scheme.sign(plainText, keyPair.getSigningKey());
        // Get all representations to force computation
        signature.getRepresentation();
        verificationKey.getRepresentation();
        bilGroup.resetCounters();
        System.out.println("********** Running regular benchmark with numMessages = " + numMessages + " *********");
        scheme.verify(plainText, signature, verificationKey);
        System.out.println(bilGroup.formatCounterData());
        bilGroup.resetCounters();
    }
}
