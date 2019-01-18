package ao.chess.v1.old;

import ao.chess.v1.engine.Book;
import ao.chess.v1.engine.TranspositionTable;
import ao.chess.v1.model.Board;
import ao.chess.v1.model.Definitions;
import ao.chess.v1.model.Move;
import ao.chess.v1.util.Io;

import java.io.*;

/**
 *  class Mediocre
 *
 *  This is the main class of Mediocre
 *  which is used to connect to Winboard etc.
 *
 *  @author Jonatan Pettersson (mediocrechess@gmail.com)
 *  Date: 2006-12-27
 */

public class Mediocre implements Definitions
{
	public static BufferedReader reader;
	private static String command;
	public static Book book; // Holds the methods for using the book
	public static TranspositionTable transpositionTable;
	public static TranspositionTable repTable;
	public static TranspositionTable evalHash;
	public static TranspositionTable pawnHash;
	public static int ancientNodeSwitch; // Keeps track of ancient nodes in the transposition table, switches between -1 and 1 every time a move is made

	public static String bookLocation;
	public static boolean useOwnBook;
	
	public static int tt_size;
	public static final int REP_SIZE = 16384; //2^14 
	public static int eval_size;
	public static int pawn_size;

	public static final String VERSION = "v0.332";


    //--------------------------------------------------------------------
    /**
	 *  The main method, initializes the reader
	 *  and calls winBoard.
	 *
	 *  @param args arguments
	 */
	public static void main(String args[]) throws IOException
	{		
		initSettings();
		
		if(useOwnBook)
		{
			book = Book.getInstance(); // Initialize the opening book
			book.readBook(); // Read in the book
		}
		
		initHashes();
		ancientNodeSwitch = 1; // Initialize the ancient nodes switch

		reader = new BufferedReader(new InputStreamReader(System.in)); // Initialize the reader
		lineInput();
		/*command = reader.readLine();
		
		if(command.equals("uci")) uci();
		else if(command.equals("xboard")) winBoard();
		else lineInput();*/

	}
	//END main()
    public static void initHashes()
    {
        repTable = new TranspositionTable(REP_SIZE, true); // Initialize the history
		transpositionTable = new TranspositionTable(tt_size, 6);
		evalHash = new TranspositionTable(eval_size, 2);
		pawnHash = new TranspositionTable(pawn_size, 3);
    }


    /**
	 *  Handles input from the UCI application
	 *  and also sends moves and settings from the engine
	 *
	 *  I had some much appreciated help with this from Yves Catineau
	 */
	private static void uci() throws IOException
	{
		Board board = new Board(); // Create a board on which we will be playing
		board.setupStart(); // Start position

		boolean useBook = useOwnBook; // Initilize using the book to what is set in the settings file
		String openingLine = ""; // Holds the opening line so far
		int searchDepth = 0; // Initialize search depth
		int movetime = 0; // Initialize fixed time per move
		System.out.println("");
		System.out.println("id name Mediocre " + VERSION);
		System.out.println("id author Jonatan Pettersson");
		System.out.println("uciok");

		// This is the loop in which we look for incoming commands from Uci
		for(;;)
		{ 
			command = reader.readLine(); // Receive the input

			if ("uci".equals( command ))
			{
				System.out.println("id name Mediocre " + VERSION);
				System.out.println("id author Jonatan Pettersson");
				System.out.println("uciok");

			}


			if ("isready".equals( command ))
			{
				System.out.println("readyok");

			}

			if("quit".equals( command ))
				System.exit(0);


			// A new game is starting, can be both from start and inserted position
			if("ucinewgame".equals( command )) 
			{
				repTable.repClear(); // Reset the history
				transpositionTable.clear(); // Reset transposition table
				useBook = true; // We can potentially use the book in the new game
				searchDepth = 0;
				movetime = 0;
			}

			// Using the UCI protocol we receive the moves by the opponent
			// in a 'position' string, this string comes with a FEN-string (or states "startpos")
			// followed by the moves played on the board.
			// 
			// The UCI protocol states that the position should be set on the board
			// and all moves played
			if(command.startsWith("position"))
			{
				// Set the position on the board

				if(command.indexOf("startpos") != -1) // Start position
				{
					openingLine = ""; // Initialize opening line
					board.setupStart(); // Insert start position
				}
				else // Fen string
				{
					String fen = extractFEN(command);

					useBook = false; // The position was not played from the start so don't attempt to use the opening book
					openingLine = "none"; // Make sure we can't receive a book move
					if(!"".equals( fen )) // If fen == "" there was an error in the position-string
					{
						board.inputFEN(fen); // Insert the FEN
					}
				}					


				// Play moves if there are any

				String[] moves = extractMoves(command);

				if(moves != null) // There are moves to be played
				{
					openingLine = ""; // Get ready for new input
                    for (String move : moves)
                    {

                        int moveToMake = receiveMove(move, board);
                        if (moveToMake == 0)
                        {
                            System.out.println("Error in position string. Move " + move + " could not be found.");
                        }
                        else
                        {
                            board.makeMove(moveToMake); // Make the move on the board
                            repTable.recordRep(board.zobristKey);
                            if (useBook) openingLine += move; // Update opening line
                        }
                    }
				}



			}

			// The GUI has told us to start calculating on the position, if the
			// opponent made a move this will have been caught in the 'position' string
			if(command.startsWith("go")) 
			{
				int wtime = 0; // Initialize the times
				int btime = 0;
				int winc = 0;
				int binc = 0; 
				
				// If infinite time, set the times to 'infinite'
				if("infinite".equals( command.substring(3) ))
				{
					wtime = 99990000;
					btime = 99990000;
					winc = 0;
					binc = 0;
				}
				else if("depth".equals( command.substring(3,8) ))
				{
					try
					{
						searchDepth = Integer.parseInt(command.substring(9));
					}
					catch(NumberFormatException ignored) {}
				}
				else if("movetime".equals( command.substring(3,11) ))
				{
					try
					{
						movetime = Integer.parseInt(command.substring(12));
					}
					catch(NumberFormatException ignored) {}
				}
				else // Extract the times since it's not infinite time controls
				{
					String[] splitGo = command.split(" ");
					for(int goindex = 0; goindex < splitGo.length; goindex++)
					{

						try
						{
							if("wtime".equals( splitGo[goindex] ))
								wtime = Integer.parseInt(splitGo[goindex +1]);
							else if("btime".equals( splitGo[goindex] ))
								btime = Integer.parseInt(splitGo[goindex +1]);
							else if("winc".equals( splitGo[goindex] ))
								winc = Integer.parseInt(splitGo[goindex +1]);
							else if("binc".equals( splitGo[goindex] ))
								binc = Integer.parseInt(splitGo[goindex +1]);	
						}

						// Catch possible errors so the engine doesn't crash
						// if the go command is flawed
						catch(ArrayIndexOutOfBoundsException ignored) {}
						catch(NumberFormatException ignored) {}
					}
				}
				
				// We now have the times so set the engine's time and increment
				// to whatever side he is playing (the side to move on the board)

				int engineTime;
				int engineInc;
				if(board.toMove == 1)
				{
					engineTime = wtime;
					engineInc = winc;
				}
				else
				{
					engineTime = btime;
					engineInc = binc;
				}
			

				// The engine's turn to move, so find the best line
				Engine.LineEval bestLine = new Engine.LineEval();

				if(useBook)
				{
					String bookMove = Book.getBestMove(openingLine);

					if("".equals( bookMove ))
					{
						useBook = false;
						bestLine = Engine.search(board, searchDepth, true, engineTime, engineInc, movetime, true);
					}
					else
					{
						openingLine += bookMove;
						bestLine.line[0] = receiveMove(bookMove, board);
					}
				}			
				else
				{
					bestLine = Engine.search(board, searchDepth, true, engineTime, engineInc, movetime, false);
				}
				if(bestLine.line[0] != 0) // We have found a move to make
				{
					board.makeMove(bestLine.line[0]); // Make best move on the board
					
					repTable.recordRep(board.zobristKey);
					
					System.out.println("bestmove " + (Move.inputNotation(bestLine.line[0])));
					ancientNodeSwitch *= -1; // Switch the ancient nodes

				}				
			}
		}
	}
	// END uci()

	/**
	 *  Used by uci mode
	 *
	 *  Extracts the fen-string from a position-string, do not call
	 *  if the position string has 'startpos' and not fen
	 *
	 *  Throws 'out of bounds' exception so faulty fen string won't crash the program
	 *
	 *  @param position The position-string
	 *  @return String Either the start position or the fen-string found
	 */
	public static String extractFEN(String position) throws ArrayIndexOutOfBoundsException
	{

		String[] splitString = position.split(" "); // Splits the string at the spaces

		String fen = "";
		if(splitString.length < 6)
		{
			System.out.println("Error: position fen command faulty");
		}
		else
		{
			fen += splitString[2] + " "; // Pieces on the board
			fen += splitString[3] + " "; // Side to move
			fen += splitString[4] + " "; // Castling rights
			fen += splitString[5] + " "; // En passant
			if(splitString.length >= 8)
			{
				fen += splitString[6] + " "; // Half moves
				fen += splitString[7];       // Full moves
			}
		}
		

		return fen;
	}
	// END extractFEN()	

	/** 
	 *  Used by uci mode
	 *
	 *  Extracts the moves at the end of the 'position' string
	 *  sent by the UCI interface
	 *
	 *  Originally written by Yves Catineau and modified by Jonatan Pettersson
	 *
	 *  @param position The 'position' string
	 *  @return moves The last part of 'position' that contains the moves
	 */
	private static String[] extractMoves(String position)
	{	
		String pattern = " moves ";
		int index = position.indexOf(pattern);
		if(index == -1) return null; // No moves found

		String movesString = position.substring(index + pattern.length());
		String[] moves = movesString.split(" "); // Create an array with the moves
		return moves;
	}
	// END extractMoves()		

	/**
	 *  Handles input from the winBoard application
	 *  and also sends moves and settings from the engine.
	 *
	 */
	private static void winBoard() throws IOException
	{
		Board board = new Board(); // Create a board on which we will be playing
		board.setupStart(); // Start position
		Io.write("");
		Io.write("feature myname=\"Mediocre " + VERSION + "\" usermove=1 setboard=1 colors=0 analyze=0 done=1");
		boolean useBook = useOwnBook; // Initilize using the book to what is set in the settings file
		String openingLine = ""; // Holds the opening line so far
		boolean force = false; // Start with not being in force mode
		int increment = 0; // Initialize the increment
		int timeLeft = 0; // Initialize the timeLeft
		int searchDepth = 0; // Initialize search depth
		int movetime = 0; // Initialize fixed time per move
		
		int[] gameHistory = new int[4096];
		int gameHistoryIndex = 0;

		// This is the loop in which we look for incoming commands from WinBoard
		for(;;)
		{ 
			command = Io.read();

			if("xboard".equals( command )) // We are using the xboard interface
			{
				// feature - A few special commands that can be sent at startup, more in Tim Mann's guide
				// myname - The name of the engine \" since we need the quotation marks inside the string
				// usermove - Makes winBoard send "usermove Ne4" instead of just "Ne4"
				// setboard - Uses the setboard feature instead of edit
				// colors - Disables the white/black commands since Mediocre does not use the anyway
				// analyze - Mediocre does not support analyze mode in winboard yet
				// done - We are now done with starting up and can begin
				Io.write("feature myname=\"Mediocre " + VERSION + "\" usermove=1 setboard=1 colors=0 analyze=0 done=1");
			}

			else if("new".equals( command )) // A new game is starting so reset the board
			{
				force = false;
				repTable.repClear(); // Reset the history
				transpositionTable.clear(); // Reset transposition table
				gameHistory = new int[4096];
				gameHistoryIndex = 0;
				useBook = true; // Use book
				openingLine = ""; // Initilize the opening line
				board.setupStart(); // Insert start position
				increment = 0; // Initialize the increment
				timeLeft = 0; // Initialize the timeLeft
				searchDepth = 0; // Initialize search depth
				movetime = 0;
			}

			else if("quit".equals( command )) // Quit the program
			{
				System.exit(0); // Exit the program
			} 

			else if(command.startsWith("setboard")) // Sets the board
			{
				repTable.repClear(); // Reset the history
				transpositionTable.clear(); // Reset transposition table
				gameHistory = new int[4096];
				gameHistoryIndex = 0;
				useBook = false; // Stop using book
				board.inputFEN(command.substring(9)); // Insert the submitted position
				increment = 0; // Initialize the increment
				timeLeft = 0; // Initialize the timeLeft
				searchDepth = 0; // Initialize search depth
				movetime = 0;
			}

			// Receive the time left
			else if(command.startsWith("time"))
			{
				// Winboard reports time left in centiseconds, transform to milliseconds
				try
				{
					timeLeft = Integer.parseInt(command.substring(5)) * 10;
				}
				catch (NumberFormatException ex) {timeLeft = 0;}
			}

			// Receive the time controls, we are only interested if there is an increment
			// since we will receive the time left before every move
			else if(command.startsWith("level"))
			{
				String[] splitString = command.split(" ");
				try
				{
					// Winboard reports increment in full seconds, transform to milliseconds
					increment = Integer.parseInt(splitString[3]) *1000;
				}
				catch(ArrayIndexOutOfBoundsException ignored)
				{
					increment = 0; // If something is wrong with the level string, make increment = 0
				}
				catch(NumberFormatException ignored) {}
			}

			// In force mode the engine should only register and make the moves
			// but not think or make moves of its own
			else if("force".equals( command ))
			{
				force = true;
			}

			else if(command.startsWith("st"))
			{
				try
				{
					movetime = Integer.parseInt(command.substring(3)) * 1000;
				}
				catch(NumberFormatException ex) {movetime = 0;}
			}
			
			// Tells the engine to search to a certain depth and then move
			else if(command.startsWith("sd"))
			{
				try
				{
					searchDepth = Integer.parseInt(command.substring(3));
				}
				catch(NumberFormatException ex) {}
			}

			// Opponent played a move or told us to play from the position		
			else if("go".equals( command ) || command.startsWith("usermove"))
			{
				if(command.equals("go")) force = false; // Exit force mode
				if(command.startsWith("usermove"))
				{
					// Receive the move and play it on the board	
					int usermove = receiveMove(command.substring(9), board);
					openingLine += command.substring(9);
					if(usermove == 0)
					{
						Io.write("The move " + command.substring(9) +
                                 " could not be found. Waiting for new command.");
						continue;
				 	}
					else
					{
						board.makeMove(usermove); // Make the move on the board
						repTable.recordRep(board.zobristKey);
						gameHistory[gameHistoryIndex] = (int)(board.zobristKey >> 32);
						gameHistoryIndex++;
					}
				}
				
				if(!force)
				{
					// The engine's turn to move, so find the best line
					Engine.LineEval bestLine = new Engine.LineEval();

					if(useBook)
					{
						String bookMove = Book.getBestMove(openingLine); // Get next book move

						if("".equals( bookMove )) // No book move found
						{
							useBook = false; // Stop using book
							bestLine =
                                    Engine.search(
                                            board, searchDepth,
                                            false, timeLeft, increment,
                                            movetime, true);
						}
						else
						{
							openingLine += bookMove; // Space is added from the substring
							bestLine.line[0] = receiveMove(bookMove, board);
						}
					}
					
					else
					{
						// If searchDepth = 0 the times will be used, else searchDepth will limit the search
						bestLine =
                                Engine.search(
                                        board, searchDepth,
                                        false, timeLeft, increment,
                                        movetime, false);
						
					}

					

					if(bestLine.line[0] != 0) // We have found a move to make
					{
						board.makeMove(bestLine.line[0]); // Make best move on the board
						repTable.recordRep(board.zobristKey);
						Io.write("move " + (Move.inputNotation(bestLine.line[0]))); // Tell winboard to make the move
						ancientNodeSwitch *= -1; // Switch the ancient nodes
						gameHistory[gameHistoryIndex] = (int)(board.zobristKey >> 32);
						gameHistoryIndex++;
					}
					
					String isItOver = isGameOver(board,gameHistory,gameHistoryIndex);
					if(!isItOver.equals("no"))
					{
						Io.write(isItOver);
					}

				}
			}
		}
	}
	// END winBoard()

	/**
	 * Tries to find mediocre.ini and use the settings from there,
	 * if mediocre.ini is not found default settings will be used.
	 * 
	 * If mediocre.ini is found but missing some setting the default value
	 * will be used for that setting
	 */
	public static void initSettings()
	{
		BufferedReader in;
		boolean ub = false;
		boolean mts = false;
		boolean ets = false;
		boolean pts = false;
		String str = null;
		boolean useDefault = false;
		for(int i = 0; i < 3; i++)
		{
		try
		{
			if(i == 0) in = new BufferedReader(new FileReader("mediocre.ini"));
			else if(i == 1) in = new BufferedReader(new FileReader("bin/mediocre.ini"));
			else in = new BufferedReader(new FileReader("../mediocre.ini"));


			
			while ((str = in.readLine()) != null)
			{
				if(str.startsWith("ub true"))
				{
					useOwnBook = true;
					ub = true;
				}
				else if(str.startsWith("ub false"))
				{
					useOwnBook = false;
					ub = true;
				}
				else if(str.startsWith("mts "))
				{
					tt_size = Integer.parseInt(str.substring(4));
					tt_size = tt_size*1024*1024*8/32/6;
					mts = true;
				}
				else if(str.startsWith("ets "))
				{
					eval_size = Integer.parseInt(str.substring(4));
					eval_size = eval_size*1024*1024*8/32/2;
					ets = true;
				}
				else if(str.startsWith("pts "))
				{
					pawn_size = Integer.parseInt(str.substring(4));
					pawn_size = pawn_size*1024*1024*8/32/3;
					pts = true;
				}
			}
			in.close();

			break;
		}
		catch(NullPointerException ex)
		{
			System.out.println("Error: Can't recognize; " + str);
			break;
		}
		catch(NumberFormatException ex)
		{
			System.out.println("Error: Can't recognize; " + str);
			break;
		}
		catch (FileNotFoundException e)
		{
			if(i == 2)
			{
				System.out.println("Error: mediocre.ini not found; using default setup");
				useDefault = true;
			}
			
		}
		catch (IOException e)
		{
			System.out.println("Error: mediocre.ini was found but an error occured while reading it; using default setup");
			useDefault = true;
		}
		}

		if(!useDefault && (!ub || !mts || !ets || !pts))
		{
			if(!ub) System.out.println("Error: missing \"ub [true/false]\" in mediocre.ini; using default setup");
			if(!mts) System.out.println("Error: missing \"mts [size]\" in mediocre.ini; using default setup");
			if(!ets) System.out.println("Error: missing \"ets [size]\" in mediocre.ini; using default setup");
			if(!pts) System.out.println("Error: missing \"pts [size]\" in mediocre.ini; using default setup");
			useDefault = true;
		}
		
		if(useDefault)
		{
			// Own book will not be used by default
			useOwnBook = false;
			
			tt_size = 1048576; // 2^20, will translate to 24mb hash
			eval_size = 524288; // 2^19, will translate to 6mb hash with two slots per entry
			pawn_size = 524288;	// 2^19, will translate to 6mb hash with two slots per entry (subject to change)		
		}
	}
	// END initSettings()

	/**
	 *  Takes an inputted move-string and matches it with a legal move generated
	 *  from the board
	 *
	 *  @param move The inputted move
	 *  @param board The board on which to find moves
	 *  @return int The matched move
	 */
	public static int receiveMove(String move, Board board) throws IOException
	{


		int[] legalMoves = new int[128];
		int totalMoves = board.generateMoves(false, legalMoves, 0); // All moves

		for(int i = 0; i < totalMoves; i++)
		{
			if(Move.inputNotation(legalMoves[i]).equals(move))
			{
				return legalMoves[i];
			}
		}

		// If no move was found return null
		return 0;
	}
	// END receiveMove()
	
	/**
	 * Checks the board and the repetition table if the game is over
	 * 
	 */
	public static String isGameOver(Board board, int[] gameHistory, int gameHistoryIndex)
	{
		int[] legalMoves = new int[128];
		if(board.generateMoves(false, legalMoves, 0) == 0)
		{
			if(Engine.isInCheck(board))
			{
				if(board.toMove == WHITE_TO_MOVE)
				{
					return "0-1 (Black mates)";
				}
				else
				{
					return "1-0 (White mates)";
				}
			}
			else
			{
				return "1/2-1/2 (Stalemate)";
			}
		}
		
		if(board.movesFifty >= 100)
		{
			return "1/2-1/2 (50 moves rule)";
		}
		
		for(int i = 0; i < gameHistoryIndex; i++)
		{
			int repetitions = 0;
			for(int j = i+1; j < gameHistoryIndex; j++)
			{
				if(gameHistory[i] == gameHistory[j]) repetitions++;
				if(repetitions == 2)
				{
					return "1/2-1/2 (Drawn by repetition)";
				}
			}
		}
		
		if(Evaluation.drawByMaterial(board,0))
		{
			return "1/2-1/2 (Drawn by material)";
		}
		
		return "no";
	}
	// END isGameOver()


	/**
	 *  Starts a loop that checks for input,
	 *  used for testing mainly, when no engine is calling
	 *  for application (used in console window)
	 */
	public static void lineInput() throws IOException
	{
		Board board = new Board(); // Create
		board.setupStart(); // Start position


		System.out.println("\nWelcome to Mediocre " + VERSION + ". Type 'help' for commands.");
		System.out.print("\n->");
		for(;;)
		{
			command = Io.read();

            if(command.equals("xboard"))
			{
				winBoard();
				break;
			}
			
			if(command.equals("uci"))
			{
				uci();
				break;
			}

			if("quit".equals( command ))
				System.exit(0);

			else if("help".equals( command ))
			{
				System.out.println("\nCommands:\n");
				System.out.println("quit           ->  Exit the program");
				System.out.println("setboard [fen] ->  Set to board to the fen string");
				System.out.println("perft [depth]  ->  Run a perft check to [depth]");
				System.out.println("divide [depth] ->  Run a divide check to [depth]");
				System.out.println("searchd [depth]->  Search the position to [depth]");
				System.out.println("searcht [time] ->  Search the position for [time] (in milliseconds)");
				System.out.println("eval           ->  Evaluates the position");

				System.out.print("\n->");
			}

			else if(command.startsWith("setboard "))
			{
				board.inputFEN(command.substring(9)); // Insert the submitted position
				System.out.print("\n->");				
			}

			else if(command.startsWith("perft "))
			{
				try {
					if(Integer.parseInt(command.substring(6)) <= 0)
						System.out.println("Depth needs to be higher than 0.");
					else
						Perft.perft(board, Integer.parseInt(command.substring(6)), false);
				}
				catch(NumberFormatException ex)
				{
					System.out.println("Depth needs to be an integer.");
				}
				catch(StringIndexOutOfBoundsException ex)
				{
					System.out.println("Please enter a search depth.");
				}				
				System.out.print("\n->");

			}
			else if(command.startsWith("searchd "))
			{
				try {
					if(Integer.parseInt(command.substring(8)) <= 0)
						System.out.println("Depth needs to be higher than 0.");
					else
					{
						long time = System.currentTimeMillis();
						Engine.search(board, Integer.parseInt(command.substring(8)), false, 0, 0, 0, false);
						System.out.println("Time: " + Perft.convertMillis((System.currentTimeMillis() - time)));
					}
				}
				catch(NumberFormatException ex)
				{
					System.out.println("Depth needs to be an integer.");
				}	
				catch(StringIndexOutOfBoundsException ex)
				{
					System.out.println("Please enter a search depth.");
				}			
				System.out.print("\n->");				
			}
			else if(command.startsWith("searcht "))
			{
				try {
					if(Integer.parseInt(command.substring(8)) <= 0)
						System.out.println("Time needs to be higher than 0.");
					else
					{
						long time = System.currentTimeMillis();
						Engine.search(board, 0, false, 0, 0, Integer.parseInt(command.substring(8)), false);
						System.out.println("Time: " + Perft.convertMillis((System.currentTimeMillis() - time)));
					}
				}
				catch(NumberFormatException ex)
				{
					System.out.println("Time needs to be an integer.");
				}	
				catch(StringIndexOutOfBoundsException ex)
				{
					System.out.println("Please enter a time.");
				}			
				System.out.print("\n->");				
			}
			else if(command.equals("eval"))
			{
				Evaluation.evaluate(board,true);

				System.out.print("\n->");				
			}	
			else if(command.startsWith("divide "))
			{
				try {
					if(Integer.parseInt(command.substring(7)) <= 0)
						System.out.println("Depth needs to be higher than 0,");
					else
						Perft.perft(board, Integer.parseInt(command.substring(7)), true);
				}
				catch(NumberFormatException ex)
				{
					System.out.println("Depth needs to be an integer.");
				}	
				catch(StringIndexOutOfBoundsException ex)
				{
					System.out.println("Please enter a search depth.");
				}			
				System.out.print("\n->");				
			}			
			else
			{
				System.out.println("Command not found.");
				System.out.print("\n->");
			}

		}
	}
}

