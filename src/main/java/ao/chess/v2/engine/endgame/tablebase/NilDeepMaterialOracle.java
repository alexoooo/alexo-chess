package ao.chess.v2.engine.endgame.tablebase;

import ao.chess.v2.engine.endgame.bitbase.BitMaterialOracle;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 18-Oct-2009
 * Time: 5:59:38 PM
 */
public class NilDeepMaterialOracle implements DeepMaterialOracle
{
    @Override public DeepOutcome see(long staticHash) {
        return null;
    }

    @Override public DeepOutcome see(State state) {
        return null;
    }
}