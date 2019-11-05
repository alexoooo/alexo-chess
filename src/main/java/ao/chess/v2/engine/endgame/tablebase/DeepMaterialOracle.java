package ao.chess.v2.engine.endgame.tablebase;

import ao.chess.v2.state.State;

import java.io.Serializable;

/**
 * User: alex
 * Date: 18-Oct-2009
 * Time: 5:59:21 PM
 */
public interface DeepMaterialOracle extends Serializable
{
    DeepOutcome see(long staticHash);

    DeepOutcome see(State state);
}