package ao.chess.v2.state;


public record StateUndo(
        int moveUndo,
        byte reversibleMoves,
        byte castles,
        long castlePath,
        byte enPassant
) {}
