package ao.chess.v2.state;

/**
 * User: alexo
 * Date: Feb 21, 2009
 * Time: 10:18:24 PM
 */
public enum CastleType
{
    //--------------------------------------------------------------------
    QUEEN_SIDE, KING_SIDE;


    //--------------------------------------------------------------------
    public static final CastleType[] VALUES = values();


    //--------------------------------------------------------------------
    public static class Set
    {
        //----------------------------------------------------------------
        public static final Set NONE = new Set((byte) 0);


        //----------------------------------------------------------------
        private final boolean whiteQueen;
        private final boolean whiteKing;
        private final boolean blackQueen;
        private final boolean blackKing;


        //----------------------------------------------------------------
        public Set(byte fromBits)
        {
            this((fromBits & State.WHITE_Q_CASTLE) != 0,
                 (fromBits & State.WHITE_K_CASTLE) != 0,
                 (fromBits & State.BLACK_Q_CASTLE) != 0,
                 (fromBits & State.BLACK_K_CASTLE) != 0
                );
        }

        public Set(boolean whiteQueenSide,
                   boolean whiteKingSide,
                   boolean blackQueenSide,
                   boolean blackKingSide)
        {
            whiteQueen = whiteQueenSide;
            whiteKing  = whiteKingSide;
            blackQueen = blackQueenSide;
            blackKing  = blackKingSide;
        }

        
        //----------------------------------------------------------------
        public byte toBits() {
            return (byte)(
                   (whiteQueen ? State.WHITE_Q_CASTLE : 0) |
                   (whiteKing  ? State.WHITE_K_CASTLE : 0) |
                   (blackQueen ? State.BLACK_Q_CASTLE : 0) |
                   (blackKing  ? State.BLACK_K_CASTLE : 0));
        }


        //----------------------------------------------------------------
        public boolean noneAvailable() {
            return ! (whiteQueen || whiteKing ||
                        blackQueen || blackKing );
        }

        public boolean whiteAvailable() {
            return whiteQueen || whiteKing;
        }

        public boolean allWhiteAvailable() {
            return whiteQueen && whiteKing;
        }

        public boolean blackAvailable() {
            return blackQueen || blackKing;
        }

        public boolean allBlackAvailable() {
            return blackQueen && blackKing;
        }

        public boolean whiteQueenSide() {
            return whiteQueen;
        }

        public boolean whiteKingSide() {
            return whiteKing;
        }

        public boolean blackQueenSide() {
            return blackQueen;
        }

        public boolean blackKingSide() {
            return blackKing;
        }


        //----------------------------------------------------------------
        public String toFen() {
            StringBuilder str = new StringBuilder();

            if (whiteKing) {
                str.append("K");
            }
            if (whiteQueen) {
                str.append("Q");
            }
            if (blackKing) {
                str.append("k");
            }
            if (blackQueen) {
                str.append("q");
            }

            return str.toString();
        }
    }
}
