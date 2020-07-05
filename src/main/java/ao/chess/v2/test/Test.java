package ao.chess.v2.test;

import ao.chess.v1.ai.Node;
import ao.chess.v1.model.Board;
import ao.chess.v2.data.MovePicker;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.state.Status;

/**
 * Date: Feb 6, 2009
 * Time: 5:57:42 PM
 */
public class Test
{
    //--------------------------------------------------------------------
    private static GameBranch fenA = new GameBranch();
    private static GameBranch fenB = new GameBranch();

    public static void main(String[] args)
    {
//        new State("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p" +
//                        "/PPPBBPPP/R3K2R w KQkq -");


//        int nodesA = buildTree(new State(), 5, fenA);
//        int nodesA = buildTree(new State(
//                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p" +
//                        "/PPPBBPPP/R3K2R w KQkq -"), 4, fenA);
//        int nodesA = buildTree(new State(
//                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -"), 5, fenA);
//        int nodesA = buildTree(new State(
//                "8/PPP4k/8/8/8/8/4Kppp/8 w - -"), 5, fenA);
//        int nodesA = buildTree(new State(
//                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -"), 6, fenA);
//        System.out.println("nodesA "  + nodesA);
//        System.out.println("caps  "  + caps);
//        System.out.println("en passants " + enPassants);
//        System.out.println("castles " + castles);
//        System.out.println("promotions " + promotions);
//        System.out.println("checks " + checks);
//        System.out.println("mates "  + mates);
//        System.out.println("draws "  + draws);


//        System.out.println("building Mediocre");
//        int nodesB = buildMediocreTree((String) null, 5, fenB);
////        int nodesB = buildMediocreTree(
////                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p" +
////                        "/PPPBBPPP/R3K2R w KQkq -", 4, fenB);
////        int nodesB = buildMediocreTree(
////                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 6, fenB);
//        System.out.println("nodesB "  + nodesB);
//
//        System.out.println();
//        System.out.println(fenA.equals( fenB ));
//        System.out.println();
//        System.out.println(fenB.minus(  fenA ));
//        System.out.println();
//        System.out.println(fenA.minus(  fenB ));
//        System.out.println();
//        System.out.println(fenA);
//        System.out.println();
//        System.out.println(fenB);

//
        testRandom();
    }


    //--------------------------------------------------------------------
    public static void testRandom()
    {
        System.out.println("warming up");
        for (int i = 0; i < 15001; i++)
        {
            playOutRandom( State.initial() );
//            playOutMediocre();
        }
        System.out.println("done warm-up");

        while (true) {
            int  count  = 100000;
            long before = System.currentTimeMillis();
            for (int i = 0; i < count; i++)
            {
                //System.out.println(i);
//                playOutRandom( new State() );
                playOutMediocre();
            }
            long delta = System.currentTimeMillis() - before;
            System.out.println(
                    count + " at " +
                   (count / (delta / 1000)) + " per second");
        }
    }


    //--------------------------------------------------------------------
//    private static int mobs   = 0;
    private static int caps       = 0;
    private static int checks     = 0;
    private static int draws      = 0;
    private static int mates      = 0;
    private static int enPassants = 0;
    private static int castles    = 0;
    private static int promotions = 0;

    private static int buildTree(
            State state, int ply, GameBranch check)
    {
        int[] moves = state.legalMoves();
        int nMoves  = moves == null ? -1 : moves.length;
        if (nMoves == 0) {
            if (ply == 0) {
                if (state.isInCheck(state.nextToAct())) {
                    mates++;
                } else {
                    draws++;
                }
                return 1;
            }
            return 0;
        }
        if (ply == 0) return 1;

        int sum = 0;
        for (int i = 0; i < nMoves; i++) {
            State proto = state.prototype();
//            State proto;
//            if (ply == 1) {
//                proto = state;
//            } else {
//                proto = state.prototype();
//            }


//            State proto2 = state.prototype();
//            int   move2  = Move.apply(moves[ i ], proto2);
//            if (truncate(proto2.toFen()).equals(
//                    "r3k2r/p1ppqPb1/bn4p1/4N3/1p2n3/2N2Q1p/" +
//                            "PPPBBPPP/R3K2R b KQkq - 0")) {
////                System.out.println("wtf");
//                System.out.println(
//                        Move.toString(moves[ i ]) + " -> " +
//                        proto2.toFen() );
//            }

            int   move  = Move.apply(moves[ i ], proto);

            if (ply == 1) {
                if (proto.isInCheck(proto.nextToAct())) {
                    checks++;
                }
                if (Move.isCapture(move)) {
                    caps++;
                }
                if (Move.isEnPassant(move)) {
                    caps++;
                    enPassants++;
                }
                if (Move.isCastle(move)) {
                    castles++;
                }
                if (Move.isPromotion(move)) {
                    promotions++;
                }
            }

            sum += buildTree(
                    proto, ply - 1,
                    check.add(
                            truncate(proto.toFen()),
                            Move.toString(move))
                    );
            Move.unApply(move, proto);

//            System.out.println(
//                    Move.toString(move));
        }

        return sum;
    }

//    private static int buildTree(
//            State state, int ply)
//    {
//        if (ply == 0) return 1;
//
//        int moves[] = new int[256];
//        int nMoves  = state.moves(moves);
//        if (nMoves == -1) return 0;
//
//        int sum = 0;
//        for (int i = 0; i < nMoves; i++) {
//            State proto = state.prototype();
//            int move    = Move.apply(moves[ i ], proto);
//            int subMove = buildTree(proto, ply - 1);
//            sum += subMove;
//
//            if (subMove != 0) {
//                boolean inCheck = proto.isInCheck( proto.nextToAct() );
//                if (inCheck) checks++;
//
//                if (Move.isCapture(move)) {
//                    caps++;
//                }
////                else if (Move.moveType(move) == MoveType.MOBILITY) {
////                    mobs++;
////                }
//            }
//        }
//        if (sum == 0) {
//            if (state.isInCheck( state.nextToAct() )) {
//                mates++;
//            } else {
//                draws++;
//            }
//            return 1;
//        }
//
//        return sum;
//    }


    //--------------------------------------------------------------------
//    private static int buildMediocreTree(
//            String fen, int ply, GameBranch check)
//    {
//        Board board = new Board();
//        if (fen == null) {
//            board.setupStart();
//        } else {
//            board.inputFEN(fen);
//        }
//
//        return buildMediocreTree(board, ply, check);
//    }
//    private static int buildMediocreTree(
//            Board board, int ply, GameBranch check)
//    {
//        if (ply == 0) return 1;
//
//        FullOutcome out = AlexoChess.outcome(board, null, 0);
//        if (out.isDraw()) {
////            draws++;
//            return 0;
//        } else if (out != FullOutcome.UNDECIDED) {
////            mates++;
//            return 0;
//        }
//
//        int moves[] = new int[256];
//        int nMoves  = board.generateMoves(false, moves, 0);
//
//        int sum = 0;
//        for (int i = 0; i < nMoves; i++)
//        {
//            int move = moves[ i ];
//            board.makeMove(move);
//
//            sum += buildMediocreTree(board, ply - 1,
//                    check.add(truncate(board.getFEN()),
//                              model.Move.notation( move )));
//
//            board.unmakeMove(move);
//        }
//
//        return sum;
//    }


    //--------------------------------------------------------------------
    private static Outcome playOutRandom(State state)
    {
        int    nextCount = 0;
        int[]  nextMoves = new int[ 128 ];
        int[]  moves     = new int[ 128 ];
        int    nMoves    = state.moves(moves);

        do
        {
//            state.checkPieces();

            int     move;
            boolean madeMove = false;

            int[] moveOrder = MovePicker.pickRandom(nMoves);
            for (int moveIndex : moveOrder)
            {
//                if (! state.check(moves[ moveIndex ])) {
//                    state.check(moves[ moveIndex ]);
//                }

                move = Move.apply(moves[ moveIndex ], state);

                // generate opponent moves
                nextCount = state.moves(nextMoves);
//                for (int i = 0; i < nextCount; i++) {
//                    if (! state.check(nextMoves[ i ])) {
//                        state.check(nextMoves[ i ]);
//                    }
//                }

                if (nextCount < 0) { // it lead to mate
//                    System.out.println("Unmaking " + Move.toString(move));
                    Move.unApply(move, state);
                } else {
                    madeMove = true;
                    break;
                }
            }
            if (! madeMove) {
                return state.isInCheck(state.nextToAct())
                       ? Outcome.loses(state.nextToAct())
                       : Outcome.DRAW;
            }

//            System.out.println(Move.toString(move));
//            System.out.println(state);

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        while (! state.isDrawnBy50MovesRule());
        return Outcome.DRAW;
    }


    //--------------------------------------------------------------------
//    private static boolean fenEqual(String fenA, String fenB)
//    {
//        String sansCountA = fenA.replaceAll(" \\w+$", "");
//        String sansCountB = fenB.replaceAll(" \\w+$", "");
//
//        return sansCountA.equals(
//                sansCountB);
//    }
    private static String truncate(String fen)
    {
        return fen.substring(0, fen.lastIndexOf(" "));
//        return fen.replaceAll("(.*) (\\d|n)+$", "\\1");
    }


    //--------------------------------------------------------------------
//    private static final Map<Position, Node>
//            transposition = new HashMap<Position, Node>();
    private static void playOutMediocre()
    {
        Board board = new Board();
        board.setupStart();
        Node.randomBench(board);
    }
}
