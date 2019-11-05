package ao.chess.v2.engine.endgame.bitbase;

import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.io.Serializable;

/**
 * User: alex
 * Date: 18-Oct-2009
 * Time: 5:59:21 PM
 */
public interface BitMaterialOracle extends Serializable
{
    Outcome see(long staticHash);

    Outcome see(State state);
}
