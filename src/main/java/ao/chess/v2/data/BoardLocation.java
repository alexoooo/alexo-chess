package ao.chess.v2.data;

/**
 * User: AO
 * Date: 10/31/10
 * Time: 6:15 PM
 */
public enum BoardLocation
{
    //------------------------------------------------------------------------
    A1(0, 0), A2(1, 0), A3(2, 0), A4(3, 0),
    A5(4, 0), A6(5, 0), A7(6, 0), A8(7, 0),

    B1(0, 1), B2(1, 1), B3(2, 1), B4(3, 1),
    B5(4, 1), B6(5, 1), B7(6, 1), B8(7, 1),

    C1(0, 2), C2(1, 2), C3(2, 2), C4(3, 2),
    C5(4, 2), C6(5, 2), C7(6, 2), C8(7, 2),

    D1(0, 3), D2(1, 3), D3(2, 3), D4(3, 3),
    D5(4, 3), D6(5, 3), D7(6, 3), D8(7, 3),

    E1(0, 4), E2(1, 4), E3(2, 4), E4(3, 4),
    E5(4, 4), E6(5, 4), E7(6, 4), E8(7, 4),

    F1(0, 5), F2(1, 5), F3(2, 5), F4(3, 5),
    F5(4, 5), F6(5, 5), F7(6, 5), F8(7, 5),
    
    G1(0, 6), G2(1, 6), G3(2, 6), G4(3, 6),
    G5(4, 6), G6(5, 6), G7(6, 6), G8(7, 6),
    
    H1(0, 7), H2(1, 7), H3(2, 7), H4(3, 7),
    H5(4, 7), H6(5, 7), H7(6, 7), H8(7, 7);


    //------------------------------------------------------------------------
//    public static final int RANKS = 8;
//    public static final int FILES = 8;


    private static final BoardLocation[]   INDEX_VALUES = values();

    private static final BoardLocation[][] RANK_FILE_VALUES;
    static {
        RANK_FILE_VALUES = new BoardLocation[ 8 ][ 8 ];

        int nextIndex = 0;
        for (int r = 0; r < Location.RANKS; r++) {
            for (int f = 0; f < Location.FILES; f++) {
                RANK_FILE_VALUES[ r ][ f ] =
                        INDEX_VALUES[ nextIndex++ ];
            }
        }
    }


    //------------------------------------------------------------------------
    public static BoardLocation get(int rankFile)
    {
        return INDEX_VALUES[ rankFile ];
    }

    public static BoardLocation get(int rank, int file)
    {
        return RANK_FILE_VALUES[ rank ][ file ];
    }


    //------------------------------------------------------------------------
    private final int rank;
    private final int file;


    //------------------------------------------------------------------------
    BoardLocation(int rank, int file)
    {
        this.rank = rank;
        this.file = file;
    }


    //------------------------------------------------------------------------
    public int rank()
    {
        return rank;
    }

    public int file()
    {
        return file;
    }
}
