package ao.chess.v1.ai;


import ao.chess.v1.model.Board;
import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;


public class V2Bot
        implements Bot
{
    private final Player player;
    private final int moveMillis;


    public V2Bot(Player player, int moveMillis)
    {
        this.player = player;
        this.moveMillis = moveMillis;
    }



    @Override
    public void init() {
        Io.display("Initiating: " + player);
        player.move(State.initial(), 1, 1, 1);
        Io.display("Done...");
    }


    @Override
    public int act(Board board)
    {
        State position = State.fromFen(board.getFEN());
        Io.display("Act:\n" + position);

        int move = player.move(
                position, moveMillis, moveMillis, moveMillis);

        Io.display("Move: " + Move.toString(move));

        int[] legalMoves = new int[Move.MAX_PER_PLY];
        int count = board.generateMoves(false, legalMoves, 0);

        String targetInputNotation = Move.toInputNotation(move);

        for (int i = 0; i < count; i++) {
            int legalMove = legalMoves[i];
            String inputNotation = ao.chess.v1.model.Move.inputNotation(legalMove);

            if (inputNotation.equals(targetInputNotation)) {
                Io.write("Playing: " + inputNotation);
                return legalMove;
            }
        }

        Io.write("Move not found!");
        throw new IllegalStateException(position.toFen());
    }
}
