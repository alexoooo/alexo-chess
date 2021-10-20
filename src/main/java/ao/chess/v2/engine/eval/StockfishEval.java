package ao.chess.v2.engine.eval;


import ao.chess.v2.engine.neuro.rollout.RolloutContext;
import ao.chess.v2.engine.stockfish.StockfishController;
import ao.chess.v2.engine.stockfish.StockfishInstance;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class StockfishEval implements PositionEvaluator, AutoCloseable
{
    //-----------------------------------------------------------------------------------------------------------------
    public static StockfishEval create(
            StockfishController controller,
            int processes,
            int hashMbPerThread,
            int nodesPerEval
    ) {
        BlockingQueue<StockfishInstance> available = new ArrayBlockingQueue<>(processes);

        for (int i = 0; i < processes; i++) {
            StockfishInstance instance = controller.start(1, hashMbPerThread, nodesPerEval);
            available.add(instance);
        }

        return new StockfishEval(available);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final List<StockfishInstance> all;
    private final BlockingQueue<StockfishInstance> available;


    //-----------------------------------------------------------------------------------------------------------------
    private StockfishEval(BlockingQueue<StockfishInstance> available) {
        all = new ArrayList<>(available);
        this.available = available;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double evaluate(int topLevelMoveCount, State state, RolloutContext context) {
        return evaluate(state);
    }


    public double evaluate(State state) {
        try {
            StockfishInstance instance = available.take();
            double value = instance.evaluate(state);
            available.add(instance);
            return value;
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        for (StockfishInstance instance : all) {
            instance.close();
        }
    }
}
