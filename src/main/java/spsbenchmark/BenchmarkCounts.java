package spsbenchmark;

/**
 * holds a set of operations counted during a benchmark
 */
public class BenchmarkCounts {

    //TODO
    private int groupOpsSetup;

    /**
     * count of group operations in generateKeys()
     */
    private int groupOpsKeyGen;

    /**
     * count of group operations in sign()
     */
    private int groupOpsSign;

    /**
     * count of group operations in verify() (does not include bMap.apply())
     */
    private int groupOpsVerify;


    /**
     * count of applied pairings
     */
    private int bMapApply;

    public BenchmarkCounts(int groupOpsSetup, int groupOpsKeyGen, int groupOpsSign, int groupOpsVerify, int bMapApply) {
        this.groupOpsSetup = groupOpsSetup;
        this.groupOpsKeyGen = groupOpsKeyGen;
        this.groupOpsSign = groupOpsSign;
        this.groupOpsVerify = groupOpsVerify;
        this.bMapApply = bMapApply;
    }

    public int getGroupOpsSetup() {
        return groupOpsSetup;
    }

    public int getGroupOpsKeyGen() {
        return groupOpsKeyGen;
    }

    public int getGroupOpsSign() {
        return groupOpsSign;
    }

    public int getGroupOpsVerify() {
        return groupOpsVerify;
    }

    public int getbMapApply() {
        return bMapApply;
    }

}
