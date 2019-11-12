package ao.chess.v2.move;


/**
 * User: alexo
 * Date: Feb 26, 2009
 * Time: 10:15:41 PM
 */
public interface BoardPiece
{
    //--------------------------------------------------------------------
    long moves(
            long pieceLocation,
            long occupied,
            long notOccupied,
            long proponent,
            long notProponent,
            long opponent);
}
