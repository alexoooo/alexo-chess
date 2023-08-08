package ao.chess.v2.test.solve;


import ao.chess.v2.engine.neuro.rollout.store.FileRolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStoreNode;
import ao.chess.v2.engine.neuro.rollout.store.compact.CompactFileRolloutStore;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


public class CompactPositionSolverMigration {
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(CompactPositionSolverMigration.class);

    private static long count = 0;
    private static Stopwatch stopwatch = Stopwatch.createStarted();


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws Throwable {
        FileRolloutStore sourceStore = new FileRolloutStore(
                PositionSolver.treeDir.resolve("root.bin"),
                null);

        CompactFileRolloutStore destinationStore = new CompactFileRolloutStore(
                Path.of("E:/compact/root.bin"),
                null);

        System.out.println("As of: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        migrate(sourceStore, 0, destinationStore, new IntArrayList());

        destinationStore.flush();
        destinationStore.close();
        sourceStore.close();
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static long migrate(
            FileRolloutStore source,
            long sourceIndex,
            CompactFileRolloutStore destination,
            IntList childPath
    ) {
        RolloutStoreNode sourceNode = source.load(sourceIndex);
        long destinationNodeIndex = destination.nextIndex();

        int moveCount = sourceNode.moveCount();
        long[] destinationChildIndexes = new long[moveCount];
        Arrays.fill(destinationChildIndexes, -1);

        RolloutStoreNode initialDestinationNode = new RolloutStoreNode(
                destinationNodeIndex,
                sourceNode.visitCount(),
                sourceNode.valueSum(),
                sourceNode.valueSquareSum(),
                sourceNode.knownOutcome(),
                destinationChildIndexes
        );
        destination.store(initialDestinationNode);

        boolean populatedChild = false;
        for (int i = 0; i < moveCount; i++) {
            long sourceChildIndex = sourceNode.childIndex(i);
            if (sourceChildIndex == -1) {
                continue;
            }

            childPath.add(i);
            long destinationChildIndex = migrate(source, sourceChildIndex, destination, childPath);
            childPath.removeElements(childPath.size() - 1, childPath.size());

            destinationChildIndexes[i] = destinationChildIndex;
            populatedChild = true;
        }

        if (populatedChild) {
            RolloutStoreNode finalDestinationNode = new RolloutStoreNode(
                    destinationNodeIndex,
                    sourceNode.visitCount(),
                    sourceNode.valueSum(),
                    sourceNode.valueSquareSum(),
                    sourceNode.knownOutcome(),
                    destinationChildIndexes
            );
            destination.store(finalDestinationNode);
        }

        if (++count % 10_000 == 0) {
            logger.info("Migrated {} | {} | {} -> {} | {}",
                    count, childPath, sourceIndex, destinationNodeIndex, stopwatch);
            stopwatch.reset().start();
        }

        return destinationNodeIndex;
    }
}
