package ao.chess.v1.ai;

import ao.chess.v1.model.Board;
import ao.chess.v1.model.Move;
import ao.chess.v1.old.Engine;
import ao.chess.v1.old.Evaluation;
import ao.chess.v1.old.Mediocre;
import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.neuro.NeuralNetworkPlayer;
import ao.chess.v2.engine.neuro.puct.PuctModel;
import ao.chess.v2.engine.neuro.puct.PuctPlayer;
import ao.chess.v2.engine.neuro.puct.PuctSingleModel;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Works with WinBoard
 */
public class AlexoChess
{
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
            "feature myname=\"alexo\" usermove=1 " +
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
        Path nnPath =
//                Paths.get("lookup/nn/res_4h_20191215.zip");
                Paths.get("lookup/nn/res_10_20191224.zip");

        PuctModel puctModel = new PuctSingleModel(
                nnPath, true);

        try
        {
            Io.display( Arrays.toString(args) );

            String botName = (args.length > 0 ? args[0] : "");

            int thinkMillis;
            if (botName.matches("\\d+")) {
                thinkMillis = Integer.parseInt(botName);
            }
            else {
                thinkMillis = 0;
            }

            Player player;
            if (thinkMillis == 0) {
                player = NeuralNetworkPlayer.load(
                        puctModel,
                        true);
            }
            else {
                player = new PuctPlayer(
                        puctModel,
                        12,
                        1.25,
                        65536,
                        true,
                        true,
                        0,
                        true);
            }

            Bot bot = new V2Bot(player, thinkMillis);
            bot.init();
            
            winboard(bot);
        }
        catch (Throwable t)
        {
            Io.display( t );
            Io.display( Arrays.toString(t.getStackTrace()) );
            t.printStackTrace();
        }
    }
    private static void winboard(Bot bot) throws IOException
    {
        Board board = new Board();
		board.setupStart();

//        long time = System.currentTimeMillis();
//        int mv = Mediocre.receiveMove("e2e4", board);
//        board.makeMove( mv );
//        new UctBot(1024).act( board );
//        System.out.println("took " + (System.currentTimeMillis() - time));

        String command = Io.read();
        assert command.equals(WINBOARD_TAG)
                : "must use WinBoard protocol";
        Io.write(WINBOARD_INIT);

        int[] history = new int[4096];
        int historyIndex = 0;

        int     movetime  = 0;
        int     increment = 0;
        int     timeLeft  = 0;
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
                board.setupStart();

                history      = new int[4096];
                historyIndex = 0;
            }

            else if (command.equals( WINBOARD_QUIT ))
            {
                System.exit( 1 );
            }

            else if (command.startsWith( WINBOARD_SETBOARD ))
            {
                board.inputFEN( command.substring(9) );

                history      = new int[4096];
                historyIndex = 0;
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
					movetime =
                        Integer.parseInt(command.substring(3)) * 1000;
				}
				catch(NumberFormatException ex) {movetime = 0;}
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

                    // Receive the move and play it on the board
					int usermove =
                            Mediocre.receiveMove(moveCommand, board);
					if(usermove == 0)
					{
						Io.write(
                                "The move " + moveCommand +
                                " could not be found. " +
                                    "Waiting for new command.");
						continue;
					}
					else
					{
						board.makeMove(usermove);
//						repTable.recordRep(board.zobristKey);
						history[ historyIndex++ ] =
                                (int)(board.zobristKey >> 32);
					}
				}

				if(!force)
				{
//                    Bot bot = new RandomBot();
//                    Bot bot = new LightUctBot(1024);

                    int move = bot.act(board);
//					Engine.LineEval bestLine =
//                            Engine.search(board,
//                                          searchDepth,
//                                          false,
//                                          timeLeft,
//                                          increment,
//                                          movetime,
//                                          false);


                    if (move != 0) // We have found a move to make
					{
						board.makeMove(move);
//						repTable.recordRep(board.zobristKey);
						Io.write(
                                "move " + Move.inputNotation(move));
//						ancientNodeSwitch *= -1; // Switch the ancient nodes

                        history[ historyIndex++ ] =
                                (int)(board.zobristKey >> 32);
					}

                    FullOutcome outcome = outcome(board, history, historyIndex);
					if (outcome != FullOutcome.UNDECIDED)
					{
						Io.write(outcome);
					}
				}
			}
        }
    }

    public static FullOutcome outcome(
            Board board,
            int[] history,
            int historyIndex)
    {
        int[] legalMoves = new int[258];
		if (board.generateMoves(false, legalMoves, 0) == 0)
		{
			if (Engine.isInCheck(board))
			{
				if (board.toMove == Board.WHITE_TO_MOVE)
				{
					return FullOutcome.BLACK_MATES;
				}
				else
				{
					return FullOutcome.WHITE_MATES;
				}
			}
			else
			{
				return FullOutcome.STALE_MATE;
			}
		}

		if (board.movesFifty >= 100)
		{
			return FullOutcome.FIFTY_MOVES;
		}

        if (historyIndex > 1)
        {
            int repetitions = 0;
            int lastHistory = history[historyIndex - 2];
            for (int i = historyIndex - 2; i >= 0; i--)
            {
                if (history[i] == lastHistory)
                {
                    repetitions++;
                    if (repetitions == 3)
                    {
                        return FullOutcome.REPETITION;
                    }
                }
            }
        }

		if (Evaluation.drawByMaterial(board, 0))
		{
			return FullOutcome.MATERIAL;
		}

		return FullOutcome.UNDECIDED;
    }
}
