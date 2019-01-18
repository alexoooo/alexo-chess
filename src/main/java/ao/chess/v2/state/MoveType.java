package ao.chess.v2.state;

/**
 * Date: Feb 6, 2009
 * Time: 6:28:35 PM
 */
public enum MoveType
{
    //--------------------------------------------------------------------
    MOBILITY,
    CAPTURE,
    EN_PASSANT,
    CASTLE,
//    PROMOTION
    ;


    //--------------------------------------------------------------------
    public static final MoveType[] VALUES = values();
}
