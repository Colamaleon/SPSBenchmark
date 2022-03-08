import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.Signature;
import org.cryptimeleon.craco.sig.SignatureKeyPair;
import org.cryptimeleon.craco.sig.sps.kpw15.*;

public class HelloWorld {

    SPSKPW15SignatureScheme s;

    public HelloWorld(){}

    public static void main(String[] args) {

        SPSKPW15PublicParameters pp = new SPSKPW15PublicParameterGen().generatePublicParameter(128, true, 1);
        SPSKPW15SignatureScheme scheme = new SPSKPW15SignatureScheme(pp);
        SignatureKeyPair keys = scheme.generateKeyPair(1);

        GroupElementPlainText message = new GroupElementPlainText(pp.getG1GroupGenerator().getStructure().getUniformlyRandomElement());
        Signature sig =  scheme.sign(new MessageBlock(message),keys.getSigningKey());
        boolean foo = scheme.verify(new MessageBlock(message), sig, keys.getVerificationKey());

        System.out.println("hello world! : " + foo);
    }

}
