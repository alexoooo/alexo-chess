package ao.chess.v2.engine.run;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.player.HeuristicPlayer;
import ao.chess.v2.engine.heuristic.impl.simple.SimpleWinTally;
import ao.chess.v2.engine.mcts.heuristic.MctsCaptureHeuristic;
import ao.chess.v2.engine.mcts.heuristic.MctsHeuristicImpl;
import ao.chess.v2.engine.mcts.node.MctsNodeImpl;
import ao.chess.v2.engine.mcts.player.MctsPlayer;
import ao.chess.v2.engine.mcts.rollout.MctsRolloutImpl;
import ao.chess.v2.engine.mcts.scheduler.MctsSchedulerImpl;
import ao.chess.v2.engine.mcts.transposition.NullTransTable;
import ao.chess.v2.engine.mcts.value.Ucb1TunedValue;
import ao.chess.v2.engine.simple.RandomPlayer;
import ao.chess.v2.engine.simple.SimPlayer;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.io.IOException;
import java.util.Arrays;

/**
 * User: aostrovsky
 * Date: 14-Sep-2009
 * Time: 4:02:37 PM
 *
"C:\Program Files\Java\jdk1.6.0_14\bin\java.exe -server -Xmx512m -jar C:\~\proj\personal\chess\chess.jar uct"
"C:\Program Files\Java\jdk1.6.0_14\bin\java.exe -server -Xmx512m -jar C:\~\proj\personal\chess\chess.jar sim"
"C:\Program Files\Java\jdk1.6.0_14\bin\java.exe -server -Xmx512m -jar C:\~\proj\personal\chess\chess.jar random"

"C:\Program Files\Java\jdk1.6.0_16\bin\java.exe -server -ea -Xmx512m -jar C:\~\proj\personal\chess\Chess.jar uct C:\~\proj\personal\chess\"
"C:\Program Files\Java\jdk1.6.0_16\bin\java.exe -server -ea -Xmx512m -jar C:\~\proj\personal\chess\Chess.jar uct_o C:\~\proj\personal\chess\"

"C:\Program Files\Java\jdk1.6.0_16\bin\java.exe -server -ea -Xmx512m -jar C:\~\proj\personal\chess\Chess.jar heu C:\~\proj\personal\chess\"

 *
 */
public class AoChess {
    //--------------------------------------------------------------------
    private static final String WINBOARD_TAG = "xboard";


    // feature - A few special commands that can be sent at startup,
    //              more in Tim Mann's guide
    // myname - The name of the engine \" since we need the quotation
    //              marks inside the string
    // usermove - Makes winBoard send "usermove Ne4" instead of just "Ne4"
    // setboard - Uses the setboard feature instead of edit
    // colors - Disables the white/black commands since Mediocre
    //              does not use the anyway
    // analyze - Mediocre does not support analyze mode in winboard yet
    // done - We are now done with starting up and can begin
    private static final String WINBOARD_INIT =
            "feature sigint=0 myname=\"alexo2\" usermove=1 " +
            "setboard=1 colors=0 analyze=0 done=1";

    private static final String WINBOARD_NEW      = "new";
    private static final String WINBOARD_QUIT     = "quit";
    private static final String WINBOARD_SETBOARD = "setboard";
    private static final String WINBOARD_TIME     = "time";
    private static final String WINBOARD_FORCE    = "force";
    private static final String WINBOARD_LEVEL    = "level";


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        if (args.length > 1) {
            Config.setWorkingDirectory( args[1] );
        }

        try
        {
            Io.display( "Hi Mabsy!!!!" );

            Io.display( Arrays.toString(args) );
            Player bot = new RandomPlayer();
            String botName = (args.length > 0 ? args[0] : "");
            if (botName.equals("random")) {
                bot = new RandomPlayer();
            } else if (botName.equals("uct")) {
                Io.display("UCT");
                bot = new MctsPlayer(
                        new MctsNodeImpl.Factory<>(),
                        new Ucb1TunedValue.Factory(),
                        new MctsRolloutImpl(false),
                        new Ucb1TunedValue.VisitSelector(),
                        new MctsHeuristicImpl(),
                        new MctsSchedulerImpl.Factory()
                );
//                bot = new MctsPlayer(
//                        new MctsNodeImpl.Factory<Ucb1Value>(),
//                        new Ucb1Value.Factory(),
//                        new MctsRolloutImpl(),
//                        new Ucb1Value.VisitSelector(),
//                        new MctsHeuristicImpl(),
//                        new NativeTransTable<Ucb1Value>(
//                                new Ucb1Value.Factory()),
//                        new MctsSchedulerImpl.Factory()
//                );
            } else if (botName.equals("uct_o")) {
                Io.display("Optimized UCT");
//                bot = new TransPlayer();
//                bot = new UctPlayer(true);
//                bot = new MctsPlayer(
//                        new MctsNodeImpl.Factory<UcbTuned2Value>(),
//                        new UcbTuned2Value.Factory(),
//                        new MctsRolloutImpl(),
//                        new UcbTuned2Value.VisitSelector(),
//                        new MctsHeuristicImpl(),
//                        new MctsSchedulerImpl.Factory()
//                );
//                bot = new MctsPlayer(
//                        new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                        new Ucb1TunedValue.Factory(),
//                        new MctsRolloutImpl(),
//                        new Ucb1TunedValue.VisitSelector(),
//                        new MctsFpuHeuristic(),
//                        new NullTransTable<Ucb1TunedValue>(),
//                        new MctsSchedulerImpl.Factory()
//                );
                Io.display("loading...");
                bot = new MctsPlayer(
                        new MctsNodeImpl.Factory<>(),
                        new Ucb1TunedValue.Factory(),
                        new MctsRolloutImpl(false),
                        new Ucb1TunedValue.VisitSelector(),
                        new MctsCaptureHeuristic(),
                        new MctsSchedulerImpl.Factory()
                );
                Io.display("done loading!");
//                bot = new MctsPlayer(
//                        new MctsNodeImpl.Factory<Ucb1Value2>(),
//                        new Ucb1Value2.Factory(),
//                        new MctsRolloutImpl(),
//                        new Ucb1Value2.VisitSelector(),
//                        new MctsHeuristicImpl(),
//                        new NativeTransTable<Ucb1Value2>(
//                                new Ucb1Value2.Factory()),
//                        new MctsSchedulerImpl.Factory()
//                );
            } else if (botName.equals("sim")) {
                bot = new SimPlayer(false);
            } else if (botName.equals("sim_o")) {
                bot = new SimPlayer(true);
            } else if (botName.equals("heu")) {
                Io.display("Heuristic Bot");
                bot = new HeuristicPlayer(
                        new SimpleWinTally("test"));
            }

//            if (botName.matches("\\d+"))
//            {
//                bot = new UctBot(
//                        Integer.parseInt(botName), true);
//            }
//            if (botName.equals("opt"))
//            {
//                bot = new UctBot(1024*16, true);
//            }

            while (winboard(bot)) {}
        }
        catch (Throwable t)
        {
            Io.display( t );
            Io.display( Arrays.toString(t.getStackTrace()) );
            t.printStackTrace();
        }
        System.exit(0);
    }


    //--------------------------------------------------------------------
    private static boolean winboard(Player bot) throws IOException
    {
        State state = State.initial();
//		board.setupStart();

//        long time = System.currentTimeMillis();
//        int mv = Mediocre.receiveMove("e2e4", board);
//        board.makeMove( mv );
//        new UctBot(1024).act( board );
//        System.out.println("took " + (System.currentTimeMillis() - time));


        String command = Io.read();
        assert command.equals(WINBOARD_TAG)
                : "must use WinBoard protocol";
        Io.write(WINBOARD_INIT);

        int     moveTime  = 5000;
        int     increment = 5000;
        int     timeLeft  = 5000;
        boolean force     = false;
        while (true)
		{
            command = Io.read();

            if(command.equals( WINBOARD_TAG ))
			{
				Io.write(WINBOARD_INIT);
			}

            else if (command.equals( WINBOARD_NEW ))
            {
                force = false;
                state = State.initial();
            }

            else if (command.equals( WINBOARD_QUIT ))
            {
                return false;
            }

            else if (command.startsWith( WINBOARD_SETBOARD ))
            {
                state = State.fromFen( command.substring(9) );
            }

            else if (command.startsWith( WINBOARD_TIME ))
            {
                // Winboard reports time left in centiseconds,
                //  transform to milliseconds
				try
				{
					timeLeft =
                            Integer.parseInt(command.substring(5)) * 10;
				}
				catch (NumberFormatException ex) {timeLeft = 0;}
            }

            else if(command.startsWith( WINBOARD_LEVEL ))
			{
				String[] splitString = command.split(" ");
				try
				{
					// Winboard reports increment in full seconds,
                    //  transform to milliseconds
					increment = Integer.parseInt(splitString[3]) * 1000;
				}
				catch(ArrayIndexOutOfBoundsException ex)
				{
					increment = 0;
                }
				catch(NumberFormatException ignored) {}
			}

            else if(command.equals( WINBOARD_FORCE ))
			{
				force = true;
			}

            else if(command.startsWith("st"))
			{
				try
				{
					moveTime =
                        Integer.parseInt(command.substring(3)) * 1000;
				}
				catch(NumberFormatException ex) {moveTime = 0;}
			}

//            else if(command.startsWith("sd"))
//			{
//				try
//				{
//					searchDepth = Integer.parseInt(command.substring(3));
//				}
//				catch(NumberFormatException ignored) {}
//			}

            // Opponent played a move or told us to play from the position
			else if("go".equals( command ) ||
                    command.startsWith("usermove"))
			{
				if(command.equals("go")) force = false;
				if(command.startsWith("usermove"))
				{
                    String moveCommand = command.substring(9);
                    forceMove(state, moveCommand);
                    if (gameIsDrawnBy50MovesRule(state)) return true;
				}

				if (! force)
				{
                    if (! playMove(state,
                            bot, timeLeft, moveTime, increment)) {
                        Io.display("game is done, I lose =(");
                        return true;
                    }
                    gameIsDrawnBy50MovesRule(state);
				}
			}
        }
    }


    //--------------------------------------------------------------------
    private static boolean playMove(
            State state, Player bot,
            int timeLeft, int moveTime, int timeIncrement)
    {
        Io.display("playing " +
                timeLeft + "\t" + moveTime + "\t" + timeIncrement);
        int move = bot.move(state, timeLeft, moveTime, timeIncrement);
        if (move != -1) // We have found a move to make
        {
            Move.apply(move, state);
            Io.write("move " + Move.toInputNotation(move));
            Io.display("playing " + Move.toInputNotation(move) +
                        " :: " + Move.toString(move));
            return true;
        }
        else {
            Io.display("could not move in:\n" + state);
            return false;
        }
    }


    //--------------------------------------------------------------------
    private static boolean gameIsDrawnBy50MovesRule(State state)
    {
        if (state.isDrawnBy50MovesRule()) {
            Io.write(Outcome.DRAW);
            Io.display(Outcome.DRAW);
            return true;
        }
        return false;
    }


    //--------------------------------------------------------------------
    private static void forceMove(State state, String moveCommand)
    {
        // Receive the move and play it on the board
        int move = asMove(state, moveCommand);
        if(move == -1)
        {
            Io.write(
                    "The move " + moveCommand +
                    " could not be found. " +
                        "Waiting for new command.");
            Io.display(
                    "The move " + moveCommand +
                    " could not be found. " +
                        "Waiting for new command.");
        }
        else
        {
            Move.apply(move, state);
            Io.display("forcing " + Move.toInputNotation(move) +
                        " :: " + Move.toString(move));
        }
    }



    //--------------------------------------------------------------------
    private static int asMove(
            State state, String moveCommand) {
        int[] legalMoves = new int[128];
        int nMoves = state.legalMoves(legalMoves);
        for (int n = 0; n < nMoves; n++) {
            int move = legalMoves[ n ];
            if (Move.toInputNotation(move)
                    .equals(moveCommand)) {
                return move;
            }
        }
        return -1;
    }
}
