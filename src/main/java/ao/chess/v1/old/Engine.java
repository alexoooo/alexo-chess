package ao.chess.v1.old;

import ao.chess.v1.model.Board;
import ao.chess.v1.model.Definitions;
import ao.chess.v1.model.Move;
import ao.chess.v1.model.See;
import ao.chess.v1.util.Io;

import java.io.IOException;

/**
 *  class Engine
 *
 *  This class holds static methods for finding the best move
 *  given a certain position.
 *
 *  @author Jonatan Pettersson (mediocrechess@gmail.com)
 *  Date: 2006-12-26
 */

public class Engine implements Definitions
{
	public static final int PLY = 16; // Represents a full ply
	public static final int ASPIRATION_SIZE = 10;
	public static final int TIME_CHECK_INTERVAL = 10000; // How often we should check if time is up inside alphaBeta
	public static final int CHECK_EXT = 16;
	public static final int P_TO_7TH_EXT =  10;
	public static final int MATE_THREAT_EXT =  10;
	
	public static int current_depth; // The depth we are currently searching to in the iterative deepening
	public static int nodesSearched; // The number of nodes search, incremented at every alphaBeta()-call
	public static int contemptFactor; // Added to the draw value to avoid easy draws
	public static int[][] historyMoves; // Keeps track of number of times a move has been played for better sorting (history heuristic)
	public static KillerMoves killers; // Keeps track of killer moves 
	public static boolean useFixedDepth; // Are we using a fixed depth
	public static boolean stopSearch; // Set to true if used too much time
	public static long startTime; // The time we started searching at
	public static int timeForThisMove; // Set if we are using a fixed time for every move
	public static int nextTimeCheck; // Keeps track of when to check the time
	public static boolean isUci; // Are we using uci or winboard output
	public static int gamePhase; // What phase the game is in (opening, ending etc.)
	public static int[] moves; // The moves array, keeps track of moves at every ply
	public static int[] movesSort; // The ordering values for the moves
	public static int moveMemoryIndex; // Where we currently are in the moves array
	public static int globalBestMove;
	
	
	/**
	 * Starts the search by doing iterative deepening calls to alphaBeta()
	 * 
	 * @param board The position to search
	 * @param depth If !=0 we will search to this depth and ignore time
	 * @param uci If true we use uci thinking output, else we use winboard output
	 * @param timeLeft The time we have left
	 * @param increment The increment used in the game
	 * @param movetime If !=0 we search every move this amount of time and ignore the timeLeft and increment
	 * @param leftOpening This is the first search after the opening book ended, use more time on this move
	 * @return finalEval The best line and evaluation
	 */
	public static LineEval search(Board board, int depth, boolean uci, int timeLeft, int increment, int movetime, boolean leftOpening) throws IOException
	{
		moves = new int[4096];
		movesSort = new int[4096];
		moveMemoryIndex = 0;


		isUci = uci; // Store wether we are using uci or winboard
	
		
		
		// Decide the contempt factor by looking at the phase the game is in
		gamePhase = Evaluation.getGamePhase(board);
		
		if(gamePhase == PHASE_OPENING) contemptFactor = CONTEMPT_OPENING;
		else if(gamePhase == PHASE_ENDING || gamePhase == PHASE_PAWN_ENDING) contemptFactor = CONTEMPT_ENDING;
		else contemptFactor = CONTEMPT_MIDDLE;

        useFixedDepth = (depth != 0);

		LineEval finalEval = new LineEval(); // Stores the evaluation and principal valuation
		nodesSearched = 0; // Initialize nodes searched

		startTime = System.currentTimeMillis(); // Start the timer

		historyMoves = new int[120][120];
		killers = new KillerMoves(); // Initialize the killer move class
		
		int alpha = -INFINITY; // First iteration will be with ordinary values for alpha and beta
		int beta = INFINITY;

		stopSearch = false; // Initialize the hard stop

		if(movetime == 0) // Not fixed time per move
		{
			timeForThisMove = calculateTime(timeLeft, increment, leftOpening, board); // Calculate the time for this move
		}
		else // Fixed time per move
		{
			timeForThisMove = movetime;
		}
		
		// Generate the root moves, since they are the same throughout the search
		moveMemoryIndex += board.generateMoves(false, moves, 0);
		if(moveMemoryIndex == 0) return new LineEval();

		for(current_depth = 1; current_depth <= 64;) // Start the iterative deepening loop
		{	
			nextTimeCheck = TIME_CHECK_INTERVAL; // Reset the time check 
			
			int eval = searchRoot(board,alpha,beta);
			
			if(stopSearch) break;
						
			// If we miss the aspiration window, make a new search with full windows
			// Do this check before reporting any lines so we don't get double reports
			// for researchs
			if(eval <= alpha || eval>=beta)
			{
				alpha = -INFINITY;
				beta = INFINITY;
				continue;
			}
			else // Only get eval and pv if we do not have to recall
			{
				// If the score dropped by atleast the value of a pawn
				// on the last iteration something dangerous is happening
				// so try use more time on this move
				// Won't be used if we are used fix time per move
				/*if(movetime == 0 && eval+100 < finalEval.eval)
				{
					timeForThisMove = needMoreTime(timeLeft, timeForThisMove, (int)(System.currentTimeMillis() - startTime));
				}*/
				
				// Record the evaluation
				finalEval = new LineEval(Mediocre.transpositionTable.collectPV(board, current_depth), eval); // Record the evaluation and principal variation, flip the eval if it's black to move so black ahead is always negative
				finalEval.line[0] = globalBestMove;
			}

			Io.write(receiveThinking(startTime, finalEval)); // Get a thinking string and send

			alpha = eval -ASPIRATION_SIZE; // Get ready for a new search, with a new window
			beta = eval +ASPIRATION_SIZE;  // The current window equals 3/10 of a pawn
			
			if(alpha <= -INFINITY) alpha = -INFINITY;
			if(beta >= INFINITY) beta = INFINITY;
			
			

			if(useFixedDepth)
			{
				if(current_depth == depth || eval == -(MATE_VALUE +1)) break;
			}
			else if(movetime != 0)
			{
				if(System.currentTimeMillis() - startTime > timeForThisMove || eval == -(MATE_VALUE +1)) break; // We have reached the allocated time or found a mate, and exit
			}
			else
			{
				// If we used 90% of the time so far, we break here
				if(System.currentTimeMillis() - startTime > timeForThisMove*0.9 || eval == -(MATE_VALUE +1)) break; // We have reached the allocated time or found a mate, and exit
			}
			current_depth++; // Go to the next depth


		}
		return finalEval;
	}
	// END search()
	
	/**
	 * Used when evaluation make a big change after an iteration
	 * grants more time to use on the move
	 * 
	 * @param timeLeft Total time left in the game
	 * @param originalTimeForThisMove What was originally granted for the move
	 * @param timeUsed How much time we used so far
	 * @return Either the new time to use, or the same time if we don't grant new time
	 */
	/*private static int needMoreTime(int timeLeft, int originalTimeForThisMove, int timeUsed)
	{
		// Give half the total time for this move + what was left of the originally granted time
		int newTimeForThisMove = (originalTimeForThisMove-timeUsed) + (originalTimeForThisMove/2);
		
		// If we run out of time by doing this, don't grant new time
		if(newTimeForThisMove >= timeLeft)
		{
			return originalTimeForThisMove;
		}
		// Grant more time
		else
		{
			return newTimeForThisMove;
		}		
	}*/
	// END needMoreTime()
	
	/**
	 *  Takes the time left and calculates how much is to be
	 *  used on this move, increment adds to the thinking time
	 *
	 *  @param timeLeft The total time left
	 *  @param increment The games uses increments
	 *  @param leftOpening If true this is the first search after the opening moves so we should use more time
	 *  @param board Board is submitted so we can check for queens and such
	 *  @return timeForThisMoves The time to be used on this move
	 */
	private static int calculateTime(int timeLeft, int increment, boolean leftOpening, Board board)
	{
		int timeForThisMove; // The maximum time we are allowed to use on this move
		int percent; // How many percent of the time we will use; percent=20 -> 5%, percent=40 -> 2.5% etc. (formula is 100/percent, i.e. 100/40 =2.5)
		
		/*if(leftOpening)
		{
			// We left the opening so we should spend some time understanding the position
			percent = 10; // 10% of the time left		
		}
		else if ((gamePhase == PHASE_OPENING || gamePhase == PHASE_MIDDLE) && (board.pieces[30] != -1 || board.pieces[14] != -1))
		{
			// Queens are still on the board along with more pieces, so we need to be careful
			percent = 30; // 3.33% of the time left
		}
		else*/
		{
			// Did not leave opening and no queens, so use the standard amount
			percent = 40; // 2.5% of the time left
		}
		
		// Use the percent + increment for the move
		timeForThisMove = timeLeft/percent+(increment);

		// If the increment puts us above the total time left
		// use the timeleft - 0.5 seconds
		if(timeForThisMove >= timeLeft)
			timeForThisMove = timeLeft -500;

		// If 0.5 seconds puts us below 0
		// use 0.1 seconds
		if(timeForThisMove < 0)
			timeForThisMove = 100;


		return timeForThisMove;
	}
	// END calculateTime()	
	

	/**
	 *  Returns a thinking line
	 *
	 *  @param time The time the search began
	 *  @param finalEval The evaluation and pv
	 *  @return String The thinking string
	 */
	private static String receiveThinking(long time, LineEval finalEval)
	{
		
		if(isUci) // We are using uci protocol
		{

			// Built the pv line
			String pvString = "";
			for(int i = 0; i < 128; i++)
			{
				if(i == 0)
				{
					pvString += (Move.inputNotation(globalBestMove) + " ");
				}
				else if(finalEval.line[i] == 0) break;
				else
				{
					pvString += (Move.inputNotation(finalEval.line[i]) + " ");					
				}				
			}

			// Calculate the nodes per second, we need decimal values
			// to get the most accurate result.
			// If we have searched less than 1 second return the nodesSearched
			// since the numbers tend to get crazy at lower times

			long splitTime = (System.currentTimeMillis() - time);
			int nps;
			if((splitTime / 1000) < 1) nps = nodesSearched;
			else
			{
				Double decimalTime = nodesSearched/(splitTime/1000D);
				nps = decimalTime.intValue();
			}

			// Send the info to the uci interface
			if(finalEval.eval >= MATE_BOUND)
			{
				int rest = ((-MATE_VALUE) - finalEval.eval)%2;
				int mateInN = (((-MATE_VALUE)-finalEval.eval)-rest)/2+rest;
				return "info score mate " + mateInN + " depth " + current_depth + " nodes " + nodesSearched + " nps " + nps + " time " + splitTime + " pv " + pvString;				
			}
			else if(finalEval.eval <= -MATE_BOUND)
			{
				int rest = ((-MATE_VALUE) + finalEval.eval)%2;
				int mateInN = (((-MATE_VALUE)+finalEval.eval)-rest)/2+rest;
				return "info score mate " + -mateInN + " depth " + current_depth + " nodes " + nodesSearched + " nps " + nps + " time " + splitTime + " pv " + pvString;				
			}
			return "info score cp " + finalEval.eval + " depth " + current_depth + " nodes " + nodesSearched + " nps " + nps + " time " + splitTime + " pv " + pvString;


		}
		else // We are using winboard protocol
		{
			// Start building the thinking output
			String stringEval = "";
			String stringLine = "";

			// Insert the principal variation into the stringEval
			for(int i = 0; i < 128; i++)
			{
				if(i == 0)
				{
					stringLine += (Move.notation(globalBestMove) + " ");
				}
				else if(finalEval.line[i] == 0)
				{
					stringEval = current_depth + " " + finalEval.eval + " " + (System.currentTimeMillis() - time) + " " + nodesSearched + " ";
					break;
				}
				else
				{
					stringLine += (Move.notation(finalEval.line[i]) + " ");
				}

				
			}
			
			stringEval += " " + stringLine;
			

			return stringEval; // Send the stringEval to the gui
		}		

	}
	// END receiveThinking()

	/**
	 *  class LineEval
	 *
	 *  Holds a line and evaluation number used to return in search
	 */
	public static class LineEval
	{
		public int[] line;
		public int eval;

		public LineEval()
		{
			this.line = new int[128];
			this.eval = 0;
		}

		public LineEval(int[] line, int eval)
		{
			this.line = line;
			this.eval = eval;
		}
	}
	// END class LineEval
	
	/**
	 *  class KillerMoves
	 *
	 *  Holds killer moves for the alphaBeta search
	 */
	public static class KillerMoves
	{
		public int[] primaryKillers; // The index corresponds to the ply the killer move is located in
		public int[] secondaryKillers;
		public int[][][] everyKiller; // ply, from index, to index

		public KillerMoves()
		{
			primaryKillers = new int[100]; // Assuming we never search over 100 plies deep
			secondaryKillers = new int[100];
			everyKiller = new int[100][120][120]; // Assuming we never search over 100 plies and indexes are on the 0x88 board
		}

		/**
		 *  Inserts a new killer move into either primary or secondary array
		 *
		 *  @param move The killer move
		 *  @param depth The ply the killer move exists in
		 */
		public void addKiller(int move, int depth)
		{
			if(primaryKillers[depth] == 0) primaryKillers[depth] = move; // Spot was empty so take it
			else if(secondaryKillers[depth] == 0) secondaryKillers[depth] = move; // Primary was taken, but secondary empty so take it

			// If the move isn't the same as in primary killers and it has a higher count
			// put it to primaryKillers and move down the primary killer to secondary
			else if(Move.fromIndex(primaryKillers[depth]) != Move.fromIndex(move) && Move.toIndex(primaryKillers[depth]) != Move.toIndex(move) && everyKiller[depth][Move.fromIndex(primaryKillers[depth])][Move.toIndex(primaryKillers[depth])] < everyKiller[depth][Move.fromIndex(move)][Move.toIndex(move)])
			{
				secondaryKillers[depth] = primaryKillers[depth];
				primaryKillers[depth] = move;
			}
			// If the move isn't the same as in secondaryKiller and has a higher count
			else if(Move.fromIndex(secondaryKillers[depth]) != Move.fromIndex(move) && Move.toIndex(secondaryKillers[depth]) != Move.toIndex(move) && everyKiller[depth][Move.fromIndex(secondaryKillers[depth])][Move.toIndex(secondaryKillers[depth])] < everyKiller[depth][Move.fromIndex(move)][Move.toIndex(move)])
			{
				secondaryKillers[depth] = move;
			}
		}
		// END addKiller()

		/**
		 *  Increments the number a certain killer has occured
		 *
		 *  @param move The move
		 *  @param depth The ply the move exists in
		 */
		public void incrementKiller(int move, int depth)
		{
			// Increment the place that corresponds to the ply and from/to index
			everyKiller[depth][Move.fromIndex(move)][Move.toIndex(move)]++;
		}
		// END incrementKiller()
	}
	// END KillerMoves

	
	
	/**
	 *  Checks wether the search should stop or not,
	 *  can be due to out of time, or 'stop'/'?' has been sent
	 *  by the interface (uci: 'stop' winboard: '?')
	 *  
	 *  return boolean true for stop, false for not
	 */
	public static boolean shouldWeStop() throws IOException
	{
		
		if(((System.currentTimeMillis() - startTime) > timeForThisMove))
		{
			return true;

		}
		if(Mediocre.reader.ready())
		{
			
			if(isUci)
			{
				if("stop".equals( Mediocre.reader.readLine() ))
				{
					return true;
				}
			}
			else
			{
				if(Mediocre.reader.readLine().startsWith("?"))
				{
					return true;
				}
			}
		}

		return false;
		
	}
	// END shouldWeStop()

	/**
	 * Searches the root moves. The search is done slightly differently at the root
	 */
	public static int searchRoot(Board board, int alpha, int beta) throws IOException
	{
		int eval = 0;
		int bestMove = 0;
		int bestEval = -INFINITY;
		int eval_type = HASH_ALPHA;

		int firstMoveIndex = 0;
		sortRootMoves(moves, 0, moveMemoryIndex, current_depth, board);
		for(int i = firstMoveIndex; i < moveMemoryIndex; i++)
		{						
			board.makeMove(moves[i]); // Make the move on the board

			if(eval_type == HASH_EXACT) // We have found a good move, so do the pv search
			{   
				eval = -alphaBeta(board, (current_depth-1)*PLY, -alpha -1, -alpha, true, 1);

				if(eval > alpha && eval < beta)
				{
					eval = -alphaBeta(board, (current_depth-1)*PLY, -beta, -alpha, true, 1);

				}
			}
			else //  We have not found a good move yet, so do ordinary search.
			{
				eval = -alphaBeta(board, (current_depth-1)*PLY, -beta, -alpha, true, 1);
			}

			board.unmakeMove(moves[i]); // Reset the board
			
			if(stopSearch)
			{
				break;
			}

			if(eval > bestEval)
			{
				if(eval >= beta) // If the evalatuion is bigger than beta, cutoff
				{
					if(!stopSearch)
						Mediocre.transpositionTable.record(board.zobristKey, current_depth, HASH_BETA, eval, moves[i]);
					//killers.incrementKiller(moves[i], current_depth); // Increment the possible killer
					//killers.addKiller(moves[i], current_depth); // Try to add the killer

					//historyMoves[(Move.fromIndex(moves[i]))][Move.toIndex((moves[i]))]++;

					
					return eval;
				}

				
				bestEval = eval;
				bestMove = moves[i];
				
				if(eval > alpha) // If the evaluation is bigger than alpha (but less than beta) this is our new best move
				{
					eval_type = HASH_EXACT;
					alpha = eval;
				}
			}
		}
	
		
		if(!stopSearch)
		{
			Mediocre.transpositionTable.record(board.zobristKey, current_depth, eval_type, bestEval, bestMove);
		}
		
		if(bestMove != 0) globalBestMove = bestMove;
		
		return alpha;		
	}
	// END searchRoot()
	
	/**
	 * The alpha-beta search
	 * 
	 * @param board The position we're at
	 * @param depth The current ply we're at (starts at the current depth and goes down to 0)
	 * @param alpha The alpha value
	 * @param beta The beta value
	 * @param allowNull If a null move is allowed
	 * @param ply The actual number of plies we've searched
	 * @return int Depending on where in the search we are returns alpha, beta or exact evaluation
	 */
	public static int alphaBeta(Board board, int depth, int alpha, int beta, boolean allowNull, int ply) throws IOException
	{
		int bestMove = 0;
		nodesSearched++; // Add a node searched

		if(!useFixedDepth)
		{
			nextTimeCheck--;
			if(nextTimeCheck == 0) // Time to check the time
			{
				nextTimeCheck = TIME_CHECK_INTERVAL;
				
				if(shouldWeStop())
				{
					stopSearch = true;
					return 0;
				}
			}
		}
		
		if((depth != current_depth*PLY && Mediocre.repTable.repExists(board.zobristKey)) || board.movesFifty >= 100)
		{
			//Mediocre.transpositionTable.record(board.zobristKey, ply, HASH_EXACT, (DRAW_VALUE + contemptFactor), 0);
			return (DRAW_VALUE + contemptFactor);
			
			// TODO: Contempt factor still not right. Should be - for white and + for black
		}
		
		Mediocre.repTable.recordRep(board.zobristKey);
		
		if(Mediocre.transpositionTable.entryExists(board.zobristKey) && Mediocre.transpositionTable.getDepth(board.zobristKey) >= depth) // Check if the value in the hashtable was found at same or higher depth search
		{
			if(Mediocre.transpositionTable.getFlag(board.zobristKey) == HASH_EXACT)
			{
				Mediocre.repTable.removeRep(board.zobristKey);
				return Mediocre.transpositionTable.getEval(board.zobristKey);
			}
			else if(Mediocre.transpositionTable.getFlag(board.zobristKey) == HASH_ALPHA && Mediocre.transpositionTable.getEval(board.zobristKey) <= alpha)
			{
				Mediocre.repTable.removeRep(board.zobristKey);
				return Mediocre.transpositionTable.getEval(board.zobristKey);
			}
			else if(Mediocre.transpositionTable.getFlag(board.zobristKey) == HASH_BETA && Mediocre.transpositionTable.getEval(board.zobristKey) >= beta)
			{
				Mediocre.repTable.removeRep(board.zobristKey);
				return Mediocre.transpositionTable.getEval(board.zobristKey);
			}
		}

		// If there is a check in the position, we want to extend the search one
		// ply to see if there is something dangerous with the check
		boolean isInCheck = false;
		if(isInCheck(board))
		{
			depth += CHECK_EXT;
			isInCheck = true;
		}
		
		int eval = 0;



		if(depth/PLY <= 0)
		{
			//eval = Evaluation.evaluate(board);
			eval = quiescentSearch(board, alpha, beta);
			if(eval >= beta)
				Mediocre.transpositionTable.record(board.zobristKey, 0, HASH_BETA, eval, 0);
			else if(eval <= alpha)
				Mediocre.transpositionTable.record(board.zobristKey, 0, HASH_ALPHA, eval, 0);
			else {
				Mediocre.transpositionTable.record(board.zobristKey, 0, HASH_EXACT, eval, 0);
			}
			Mediocre.repTable.removeRep(board.zobristKey);
			return eval;
		}

		// Start with trying a null-move if allowed
		// Do not use null moves if he search was extended since that means
		// something interesting might be about to happen
		boolean mate_threat = false;
		if(allowNull && !isInCheck && gamePhase != PHASE_PAWN_ENDING)
		{
			int R = (depth > 6*PLY) ? PLY*3 : PLY*2;
			board.nullmoveToggle(); // Making a null-move
			eval = -alphaBeta(board, depth-PLY-R, -beta, -beta+1, false, ply+1);

			board.nullmoveToggle(); // Unmaking the null-move

			if(eval >= beta)
			{
				if(!stopSearch)
					Mediocre.transpositionTable.record(board.zobristKey, depth, HASH_BETA, eval, 0);
				Mediocre.repTable.removeRep(board.zobristKey);
				return eval; // Cutoff
			}
			if(eval <= -MATE_BOUND)
			{
				mate_threat = true;
			}
		}

		if(stopSearch)
		{
			Mediocre.repTable.removeRep(board.zobristKey);
			return 0; // Stop the search
		}
		

		// Internal iterative deepening if we don't have a hash move
		int hashMove = Mediocre.transpositionTable.getMove(board.zobristKey);

		int iidDepth = depth/2;
		if(hashMove == 0 && depth > 3*PLY && iidDepth >= Mediocre.transpositionTable.getDepth(board.zobristKey))
		{
			alphaBeta(board, iidDepth, alpha, beta, false, ply+1);
			hashMove = Mediocre.transpositionTable.getMove(board.zobristKey);			
		}

		
		
		int generationState = GEN_HASH;
		int rememberMoveMemoryIndex = moveMemoryIndex;
		int firstMoveIndex = moveMemoryIndex;
		int tempMove;
		int losingCaptureIndexStart = firstMoveIndex; // Keeps track of where the losing captures start, initialized here to make java compiler happy will change below
		int losingCaptureIndexEnd = moveMemoryIndex;
		int startIndex = firstMoveIndex;
		int endIndex = moveMemoryIndex; // Tells the for-loop between what indexes to get moves
		int bestEval = -INFINITY;
		int eval_type = HASH_ALPHA;
		boolean atleastOneLegal = false; // Is set to true if we find atleast one legal move, if left false after all generation is done, there are no legal moves and the positions is mate or stalemate
		int searchedMoves = 0;
		
		// For every generation change, make sure we reset the futility settings
		boolean fprune = false;
		int fmargin = 0;
		int materialEval = 0;
	
		// Futility pruning, if we're at a frontier node
		// check if the current evaluation + 200 (500 for pre frontier nodes)
		// reaches up to alpha, if it doesn't the node is poor and we
		// do not search it if it's not a checking move (determined below)
		if(depth <= 2*PLY && !isInCheck)
		{
			materialEval = Evaluation.evaluate(board,false);
			if(depth == 1*PLY && (materialEval +200)  <= alpha)
			{
				fmargin = 200;
				fprune = true;
			}
			else if(depth == 2*PLY && materialEval + 500 <= alpha)
			{
				fmargin = 500;
				fprune = true;
			}
		}
		
		// Outer loop, this loops 6 times, one time for every time of move generation
		while(generationState < GEN_END)
		{		

			
			// Generate part of the moves depending on how many times we have looped
			switch(generationState)
			{
			case GEN_HASH:
				// If there is a hash move (which we got before or from iid), record it and give it a very high ordering value
				if(hashMove != 0)
				{
					moves[moveMemoryIndex] = hashMove;
					movesSort[moveMemoryIndex] = 10000;
					moveMemoryIndex++;
				}
				startIndex = firstMoveIndex;
				endIndex = moveMemoryIndex;
				break;
			case GEN_CAPS:
				// Start generating captures
				
				firstMoveIndex = moveMemoryIndex;
				
				// Generate all pseudo legal captures on the board
				moveMemoryIndex += board.gen_caps(moves, firstMoveIndex);

				// Go through the moves and assign the ordering values according to see
				for(int i = firstMoveIndex; i < moveMemoryIndex; i++)
				{
					tempMove = moves[i];

					// If the capture is the same as the hashMove, we have already searched it
					// so give it a very low value, it will be skipped when run into below.
					if(tempMove == hashMove)
					{
						movesSort[i] = -10000;
					}
					else
					{
						// The move is not a duplicate so give it a see value
						movesSort[i] = See.see(board, tempMove);
					}
				}

				// We now have ordering values for all the captures so order them with bubblesort
				losingCaptureIndexStart = sortCaps(firstMoveIndex, moveMemoryIndex);
				losingCaptureIndexEnd = moveMemoryIndex;
				
				startIndex = firstMoveIndex;
				if(losingCaptureIndexStart == -1)
					endIndex = moveMemoryIndex;
				else
					endIndex = losingCaptureIndexStart;
				break;
			case GEN_KILLERS:
				firstMoveIndex = moveMemoryIndex;
				if(hashMove != killers.primaryKillers[depth/PLY])
				{
					if(board.validateKiller(killers.primaryKillers[depth/PLY]))
					{
						moves[moveMemoryIndex] = killers.primaryKillers[depth/PLY];
						movesSort[moveMemoryIndex] = 5000;
						moveMemoryIndex++;
					}
				}
				if(hashMove != killers.secondaryKillers[depth/PLY])
				{
					if(board.validateKiller(killers.secondaryKillers[depth/PLY]))
					{
						moves[moveMemoryIndex] = killers.secondaryKillers[depth/PLY];
						movesSort[moveMemoryIndex] = 2500;
						moveMemoryIndex++;
					}
				}
				startIndex = firstMoveIndex;
				endIndex = moveMemoryIndex;
				break;
			case GEN_NONCAPS:				
				// Start generating non captures
				
				firstMoveIndex = moveMemoryIndex;
				
				// Generate all pseudo legal captures on the board
				moveMemoryIndex += board.gen_noncaps(moves, firstMoveIndex);

				// Go through the moves and assign the ordering values according to see
				for(int i = firstMoveIndex; i < moveMemoryIndex; i++)
				{
					tempMove = moves[i];

					if(tempMove == hashMove)
					{
						movesSort[i] = -10000; // Hash move is already searched so skip it
					}
					else if(tempMove == killers.primaryKillers[depth/PLY])
					{
						movesSort[i] = -10000;
					}
					else if(tempMove == killers.secondaryKillers[depth/PLY])
					{
						movesSort[i] = -10000;
					}
					else
					{
						// Add ordering value from the history
						movesSort[i] = historyMoves[Move.fromIndex(moves[i])][Move.toIndex(moves[i])];
					}
				}

				// Sort the non-captures
				sortNoncaps(firstMoveIndex, moveMemoryIndex);
				
				startIndex = firstMoveIndex;
				endIndex = moveMemoryIndex;
				break;
			case GEN_LOSINGCAPS:
				// Here we go back and use the losing captures generated in GEN_CAPS if there were any				
				if(losingCaptureIndexStart == -1)
				{
					// No losing captures so skip looping
					startIndex = moveMemoryIndex;
					endIndex = moveMemoryIndex;
				}
				else
				{
					// Select the captures that were losing
					startIndex = losingCaptureIndexStart;
					endIndex = losingCaptureIndexEnd;					
				}
				
				break;
				
			}
			

			
			
			// Go through the generated moves one by one
			for(int i = startIndex; i < endIndex; i++)
			{

				if(movesSort[i] == -10000) continue;

			
				
				board.makeMove(moves[i]); // Make the move on the board
				
				// Make sure we don't leave the king in check when making this move
				if(board.toMove == BLACK_TO_MOVE && board.isAttacked(board.w_king.pieces[0], BLACK))
				{
					board.unmakeMove(moves[i]);
					continue;
				}
				else if(board.toMove == WHITE_TO_MOVE && board.isAttacked(board.b_king.pieces[0], WHITE))
				{
					board.unmakeMove(moves[i]);
					continue;
				}
				
				atleastOneLegal = true; // The move was legal, so we have atleast one legal move
				
				
				// Futility pruning, if we decided that we could not reach alpha
				// above, see if the move is a checking move, if it isn't just
				// set the score to whatever the eval was and continue with the next move
				if(fprune && !isInCheck(board))
				{
					// If the move was a capture we add the value of the captured piece
					// if the move was not a capture this will add 0 (leaving materialEval unchanged)
					materialEval += Math.abs(PIECE_VALUE_ARRAY[Move.capture(moves[i])+7]);
					if((materialEval+fmargin) <= alpha)
					{
						if(materialEval > bestEval) bestEval = materialEval;
						board.unmakeMove(moves[i]);
						continue;
					}
				}
				
				int extend = 0;
				if(Move.pieceMoving(moves[i]) == W_PAWN  && board.rank(Move.toIndex(moves[i])) == 6) extend += P_TO_7TH_EXT;
				else if(Move.pieceMoving(moves[i]) == B_PAWN  && board.rank(Move.toIndex(moves[i])) == 1) extend += P_TO_7TH_EXT;
				if(mate_threat)	extend += MATE_THREAT_EXT;
				if(extend > PLY) extend = PLY;	
				
				if(searchedMoves > 0) // We have found a good move, so do the pv search
				{   

					// Late move reduction
					if(generationState >= GEN_NONCAPS && depth >= 3*PLY && extend == 0 && Move.capture(moves[i]) == 0 && !isInCheck(board))
					{
						eval = -alphaBeta(board, depth-(2*PLY), -alpha-1,-alpha, true, ply+1);
					}
					else eval = alpha +1; // If the move was not reduced make sure we enter the ordinary searches below



					if(eval > alpha)
					{
						// Pvs search
						eval = -alphaBeta(board, depth-PLY+extend, -alpha -1, -alpha, true, ply+1);

						if(eval > alpha && eval < beta)
						{
							// Full depth search
							eval = -alphaBeta(board, depth-PLY+extend, -beta, -alpha, true, ply+1);

						}
					}
				
				}
				else // We have not found a good move yet, so do ordinary search.
				{
					eval = -alphaBeta(board, depth-PLY+extend, -beta, -alpha, true, ply+1);
				}

				searchedMoves++;
				board.unmakeMove(moves[i]); // Reset the board

				if(eval > bestEval)
				{
					if(eval >= beta) // If the evalatuion is bigger than beta, cutoff
					{
						if(!stopSearch)
							Mediocre.transpositionTable.record(board.zobristKey, depth, HASH_BETA, eval, moves[i]);
						if(Move.capture(moves[i]) == 0)
						{
							killers.incrementKiller(moves[i], depth/PLY); // Increment the possible killer
							killers.addKiller(moves[i], depth/PLY); // Try to add the killer
						}
						
						historyMoves[(Move.fromIndex(moves[i]))][Move.toIndex((moves[i]))]++;

						moveMemoryIndex = rememberMoveMemoryIndex;

						Mediocre.repTable.removeRep(board.zobristKey);
						return eval;
					}


					bestEval = eval;
					bestMove = moves[i];

					if(eval > alpha) // If the evaluation is bigger than alpha (but less than beta) this is our new best move
					{
						eval_type = HASH_EXACT;
						alpha = eval;
					}
				}
			}// End for loop
			
			generationState++; // We have gone through all the generated moves from the last state so move to the next
		}// End while loop
	
		if(!atleastOneLegal)
		{
			moveMemoryIndex = rememberMoveMemoryIndex;
			Mediocre.repTable.removeRep(board.zobristKey);
			return mateCheck(board, ply); // No legal moves, check for mate/stalemate
		}
		
		moveMemoryIndex = rememberMoveMemoryIndex;
		
		
		
		if(!stopSearch)
		{
			Mediocre.transpositionTable.record(board.zobristKey, depth, eval_type, bestEval, bestMove);
		}
		
		
		Mediocre.repTable.removeRep(board.zobristKey);
		return alpha;
	}
	//END alphaBeta()
	
	/**
	 *  Quiescent search, looks at captures passed the search depth
	 *
	 *  @param board The board to search
	 *  @param alpha alpha value
	 *  @param beta beta value
	 *  @return int The expected evaluation
	 */
	private static int quiescentSearch(Board board, int alpha, int beta)
	{
        // Start with evaluating the current position
        int eval = Evaluation.evaluate(board,false);

		if(eval >= beta)
		{ // Check if we get a cutoff, the move is too good
			return beta;
		}


		if(eval > alpha)
		{
			
			alpha = eval;
		}
		
		
		int firstMoveIndex = moveMemoryIndex;
		moveMemoryIndex += board.gen_caps(moves, firstMoveIndex); // Get captures

		
		// Go through the moves and assign the ordering values according to see
		for(int i = firstMoveIndex; i < moveMemoryIndex; i++)
		{
				movesSort[i] = See.see(board, moves[i]);
		}
		
		int losingCapturesIndexStart = sortCaps(firstMoveIndex, moveMemoryIndex);

		if(losingCapturesIndexStart == -1) losingCapturesIndexStart = moveMemoryIndex;
		
		for(int i = firstMoveIndex; i < losingCapturesIndexStart; i++) // Loop through all the captures
		{
			nodesSearched++;
			board.makeMove(moves[i]); // Make first capture on the board
			
			// Make sure we don't leave the king in check when making this move
			if(board.toMove == BLACK_TO_MOVE && board.isAttacked(board.w_king.pieces[0], BLACK))
			{
				board.unmakeMove(moves[i]);
				continue;
			}
			else if(board.toMove == WHITE_TO_MOVE && board.isAttacked(board.b_king.pieces[0], WHITE))
			{
				board.unmakeMove(moves[i]);
				continue;
			}
			
			eval = -quiescentSearch(board, -beta, -alpha); // Call recursively just like in alphabeta
			board.unmakeMove(moves[i]); // Reset the board


			if(eval >= beta)// The move is too good
			{
				moveMemoryIndex = firstMoveIndex;
				return beta;
			}

			if(eval > alpha)// Best move so far
			{
				alpha = eval;
							

			}
		}			
		
		moveMemoryIndex = firstMoveIndex;
		return alpha;
	}
	// END quiescentSearch()
	
	/**
	 * Same as sortMoves but includes checking for hash move since it's not automatic at root
	 * 
	 * @param moves The moves we want to order
	 * @param startIndex Where the moves we want start
	 * @param endIndex Where the moves we want end
	 * @param depth What ply in the search are we at, used to find killer moves
	 * @param board The current position, used to find the transposition table entry
	 */
	private static void sortRootMoves(int[] moves, int startIndex, int endIndex, int depth, Board board) 
	{
		// TODO: See if we can move the 'sortMoves' into the generate moves algorithms,
		// this method doesn't really sort the moves it only puts values on them which
		// could be done when generating instead

		if(depth <0) depth = 0;

		//TranspositionTable.Hashentry hashentry = Mediocre.transpositionTable.getEntry(board.zobristKey);

		// Go through the moves and add orderingValues according to the type of move
		for(int i = startIndex; i < endIndex; i++)
		{
			movesSort[i] = 0;
			try{


				if(moves[i] == Mediocre.transpositionTable.getMove(board.zobristKey))
				{
					movesSort[i] = 5000;
					continue;
				}

				if(Move.capture(moves[i]) != 0)
				{
					int tempSee = See.see(board, moves[i]);
					if(tempSee > 0)
					{
						movesSort[i] = 4000;
						continue;
					}
					else if(tempSee == 0)
					{
						movesSort[i] = 1000;
						continue;
					}
				}
				if(moves[i] == killers.primaryKillers[depth])
				{
					movesSort[i] = 3000;	
					continue;
				}
				else if(moves[i] == killers.secondaryKillers[depth])
				{
					movesSort[i] = 2000;
					continue;
				}

				movesSort[i] += Move.orderingValue(moves[i]) - 31;
				movesSort[i] += historyMoves[Move.fromIndex(moves[i])][Move.toIndex(moves[i])];
			}
			catch (ArrayIndexOutOfBoundsException ignored){}

		}		
		bubbleSortMoves(moves, startIndex, endIndex); // Order the moves according to their ordering values

	}
	// END sortMoves()

	/**
	 * Used to sort non-captures, simply sorts the moves-array
	 * according to the information in the movesSort-array
	 * 
	 * @param startIndex Where the non-captures begin in the moves array
	 * @param endIndex Where the non-captures end
	 */
	public static void sortNoncaps(int startIndex, int endIndex)
	{
		boolean done = false;
		
		for(int i = startIndex; i < endIndex; i++)
		{
			if(done) break;
			done = true;
			

			for(int j = endIndex - 1; j > i; j--)
			{				
				if(movesSort[j] > movesSort[j-1])
				{
					// Switch the moves' places
					int temp = moves[j];
					moves[j] = moves[j-1];
					moves[j-1] = temp;
					
					// Switch the sort places
					temp = movesSort[j];
					movesSort[j] = movesSort[j-1];
					movesSort[j-1] = temp;
					
					done = false;
				}
			}
		}
	}
	// END bubbleSortMoves()
	
	/**
	 * Used to sort captures, different from the other sorts since it
	 * returns the index where the captures start having negative ordering
	 * values (i.e. losing captures)
	 * 
	 * @param startIndex Where the captures begin in the moves array
	 * @param endIndex Where the captures end
	 * @return Index where captures start to be losing, or -1 for no losing captures
	 */
	public static int sortCaps(int startIndex, int endIndex)
	{
		boolean done = false;
		
		for(int i = startIndex; i < endIndex; i++)
		{
			if(done) break;
			done = true;
			

			for(int j = endIndex - 1; j > i; j--)
			{				
				if(movesSort[j] > movesSort[j-1])
				{
					// Switch the moves' places
					int temp = moves[j];
					moves[j] = moves[j-1];
					moves[j-1] = temp;
					
					// Switch the sort places
					temp = movesSort[j];
					movesSort[j] = movesSort[j-1];
					movesSort[j-1] = temp;
					
					done = false;
				}
			}
			
			// Check if we have negative ordering value for the last move
			// If we do we skip sorting the rest of the moves, and return
			// the index where the negative moves start
			if(movesSort[i] < 0)
			{
				return i;
			}
		}
		return -1; // No losing captures
	}
	// END bubbleSortMoves()
	
	/**
	 * Sorts the moves in the array according to their ordering values
	 * using the bubbleSort algorithm
	 * 
	 * The ordering values are in a separate array, but both arrays
	 * has to be sorted
	 * 
	 * TODO: We probably won't have to send the toSort array since it is the
	 * global moves[] array anyway
	 * 
	 * @param toSort The array to sort
	 * @param startIndex Where to start
	 * @param endIndex Where to stop
	 */
	public static void bubbleSortMoves(int[] toSort, int startIndex, int endIndex)
	{
		boolean done = false;
		for(int i = startIndex; i < endIndex; i++)
		{
			if(done) break;
			done = true;
			

			for(int j = endIndex - 1; j > i; j--)
			{				
				if(movesSort[j] > movesSort[j-1])
				{
					// Switch the moves' places
					int temp = toSort[j];
					toSort[j] = toSort[j-1];
					toSort[j-1] = temp;
					
					// Switch the sort places
					temp = movesSort[j];
					movesSort[j] = movesSort[j-1];
					movesSort[j-1] = temp;
					
					done = false;
				}
			}
		}
	}
	// END bubbleSortMoves()
	
	/**
	 *  Same as bubbleSortMoves but only uses the ordering value located in the move
	 *  and not the movesSort array
	 *
	 *  @param toSort The moves to sort
	 *  @param startIndex Where to start
	 *  @param endIndex Where to end
	 */
	public static void bubbleSortQuis(int[] toSort, int startIndex, int endIndex)
	{
		boolean done = false;
		for(int i = startIndex; i < endIndex; i++)
		{
			if(done) break;
			done = true;

			for(int j = endIndex - 1; j > i; j--)
			{
				if(Move.orderingValue(toSort[j]) > Move.orderingValue(toSort[j-1]))
				{
					int temp = toSort[j];
					toSort[j] = toSort[j-1];
					toSort[j-1] = temp;
					done = false;
				}
			}
		}
	}
	// END bubbleSortMoves()
	
	/**
	 *  Takes a board and checks for mate/stalemate, this method assumes
	 *  that there are no legal moves for the side on the move
	 *
	 *  @param board The board to check
	 *  @param ply What ply the mate is found on
	 *  @return int The evaluation
	 */
	public static int mateCheck(Board board, int ply)
	{
		int king_square;		
		if(board.toMove == WHITE_TO_MOVE) king_square = board.w_king.pieces[0];
		else king_square = board.b_king.pieces[0];

		board.toMove *= -1; // Switch side to move on the board to get the other side's pieces to be the attacker
		if(board.isAttacked(king_square, board.toMove))
		{
			board.toMove *= -1; // Switch back side to move	
			return (MATE_VALUE + ply); 
		}
		else
		{
			board.toMove *= -1; // Switch back side to move
			return -DRAW_VALUE;
		}	



	}
	//END mateCheck()
	
	/**
	 *  Similar to mateCheck but only reports if the king of the side moving
	 *  is in check or not
	 *
	 *  @param board The board to check
	 *  @return boolean true if the king of the side moving is in check, false if is not
	 */
	public static boolean isInCheck(Board board)
	{
		int king_square;		
		if(board.toMove == WHITE_TO_MOVE) king_square = board.w_king.pieces[0];
		else king_square = board.b_king.pieces[0];
		
		board.toMove *= -1; // Switch side to move on the board to get the other side's pieces to be the attacker
		if(board.isAttacked(king_square, board.toMove))
		{
			board.toMove *= -1; // Switch back side to move	
			return true; // The king is in check
		}		
		board.toMove *= -1; // Switch back side to move	
		return false; // The king is not in check
	}
	//END isInCheck()
}
