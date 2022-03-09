package spsbenchmark;

import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.math.structures.groups.Group;

import java.util.stream.IntStream;

public class MessageGenerator {

    /**
     * Precomputes a set of {@param blockCount} messages in {@param targetGroup} for benchmarking.
     * Each MessageBlock will be of length {@param messageLength} and contain GroupElementPlainTexts.
     */
    public static MessageBlock[] prepareMessages(Group targetGroup, int blockCount, int messageLength) {

        MessageBlock[] messages = new MessageBlock[blockCount];

        // generate blocks
        for (int i = 0; i < blockCount; i++) {
            messages[i] = new MessageBlock(IntStream.range(0, messageLength).mapToObj(
                    x -> new GroupElementPlainText(targetGroup.getUniformlyRandomElement().precomputePow())
                ).toArray(GroupElementPlainText[]::new)
            );
        }

        return messages;
    }

}
