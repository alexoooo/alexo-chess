package ao.chess.v2.move;

import static ao.chess.v2.move.SlidingPieces.slide;

/**
 * Date: Feb 6, 2009
 * Time: 3:35:53 AM
 */
public class Rook implements BoardPiece
{
    //--------------------------------------------------------------------
    private Rook() {}

    public static final BoardPiece MOVES = new Rook();


//    //--------------------------------------------------------------------
//    public static long attacks(long rook,
//                               long proponentPieces,
//                               long  opponentPieces) {
//        return slide(rook,  1,  0, proponentPieces, opponentPieces) |
//               slide(rook, -1,  0, proponentPieces, opponentPieces) |
//               slide(rook,  0,  1, proponentPieces, opponentPieces) |
//               slide(rook,  0, -1, proponentPieces, opponentPieces);
//    }


    //--------------------------------------------------------------------
    public long moves(long rook,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return slide(rook,  1,  0, notOccupied, opponent) |
               slide(rook, -1,  0, notOccupied, opponent) |
               slide(rook,  0,  1, notOccupied, opponent) |
               slide(rook,  0, -1, notOccupied, opponent);
    }
}
