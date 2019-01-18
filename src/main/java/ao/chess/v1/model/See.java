package ao.chess.v1.model;

/**
 * class See
 *
 * Contains static methods for statically determining the quality of
 * a capture.
 * 
 * That is if a certain square is attacked numerous times by both sides
 * we play out the sequence of captures and see what the gains were.
 * 
 * I have used ideas from the open source engines Glaurung and Scorpio here.
 *
 * @author Jonatan Pettersson (mediocrechess@gmail.com)
 * Date: 2007-03-13
 */

public class See implements Definitions
{
	private static int move_to; // Holds the square where the captures take place
	private static int move_from; // Holds the square from the initial attacker
	private static int b_attackers_total; // Keeps track of the number of white attackers
	private static int w_attackers_total; // Keeps track of the number of black attackers
	private static Board board; // A pointer to the inputBoard so we don't have to pass it around

	// The piece_values is similar to the PIECE_VALUE_ARRAY in Definitions
	// but we don't negate black piece values and we give kings a high value
	// so they always get ordered last
	// The values are full pawns and not centipawns, and we don't need to worry
	// about nuances like giving queens a slightly higher value than 9 pawns
	private static final int[] piece_values = {0,1,3,3,5,9,99,0,99,9,5,3,3,1};
	private static int[] w_attackers = new int[16];
	private static int[] b_attackers = new int[16];
	private static int[] scores = new int[32]; // Holds the values after each capture

	/**
	 * Find all attackers of the to-square in the 'move', including
	 * hidden attackers (pieces that are 'hiding' behind other pieces
	 * that can capture once the intervening piece does its capture)
	 * and simulate the capture sequence to determine what is
	 * gained/lost from the capture.
	 * 
	 * @param move The capture we want to checka
	 * @param inputBoard The board the move is made on
	 * @return capture_value What was gained/lost from the capture sequence
	 */
	public static int see(Board inputBoard, int move)
	{
		// A simple check to see we are actually trying a capturing move
		// if the move is not a capture there is not capture sequence
		// and we won't go any further
		if(Move.capture(move) == 0) return 0;
		
		// Initialize global variables
		move_to = Move.toIndex(move);
		move_from = Move.fromIndex(move);
		w_attackers_total = 0;
		b_attackers_total = 0;
		board = inputBoard;

		int score; // Is set after each capture in the sequence
		int capture_value = 0; // Will be set to the total gain of the caputre sequence
		int tempAttack; // Temporary variable used below
		int sideToMove; // Keeps track of what side is moving
		int attacked_piece_value; // Keeps track of the value of the piece that is standing on the attacked square after each capture

		// Add attacking pawns

		// If the square diagonally down to the right is a white pawn, and the pawn is not
		// the initial attacker, add it
		if(((move_to -15) & 0x88) == 0 && board.boardArray[move_to - 15] == W_PAWN && move_from != (move_to -15))
		{
			w_attackers[w_attackers_total] = (move_to -15);
			w_attackers_total++;
		}			
		if(((move_to -17) & 0x88) == 0 && board.boardArray[move_to - 17] == W_PAWN && move_from != (move_to -17)) // Same but diagonally down to the left
		{
			w_attackers[w_attackers_total] = (move_to -17);
			w_attackers_total++;
		}		
		if(((move_to + 15) & 0x88) == 0 && board.boardArray[move_to + 15] == B_PAWN && move_from != (move_to +15)) // Diagonally up to the left
		{
			b_attackers[b_attackers_total] = (move_to +15);
			b_attackers_total++;
		}	
		if(((move_to +17) & 0x88) == 0 && board.boardArray[move_to + 17] == B_PAWN && move_from != (move_to +17)) // Diagonally up to the right
		{
			b_attackers[b_attackers_total] = (move_to +17);
			b_attackers_total++;
		}

		// Add attacking knights

		// White knights
		// If there is a piece and it is not the initial attacker
		for(int i = 0; i < board.w_knights.count; i++)
		{
			if(board.w_knights.pieces[i] != move_from)
			{
				// If it is possible for the knight to attack the move_to square add it to attackers
				if(ATTACK_ARRAY[move_to - board.w_knights.pieces[i] + 128] == ATTACK_N)
				{
					w_attackers[w_attackers_total] = board.w_knights.pieces[i];
					w_attackers_total++;				 
				}			
			}			
		}

		// Black knights
		for(int i = 0; i < board.b_knights.count; i++)
		{
			if(board.b_knights.pieces[i] != move_from)
			{
				// If it is possible for the knight to attack the move_to square add it to attackers
				if(ATTACK_ARRAY[move_to - board.b_knights.pieces[i] + 128] == ATTACK_N)
				{
					b_attackers[b_attackers_total] = board.b_knights.pieces[i];
					b_attackers_total++;				 
				}			
			}			
		}
		
		// Add attacking kings
		
		// White king		
		// If the king wasn't the initial attacker
		if(board.w_king.pieces[0] != move_from)
		{
			// If it is possible for the king to attack the move_to square add it to attackers
			tempAttack = ATTACK_ARRAY[move_to - board.w_king.pieces[0] + 128];
			if(tempAttack == ATTACK_KQR || tempAttack == ATTACK_KQBwP || tempAttack == ATTACK_KQBbP)
			{
				w_attackers[w_attackers_total] = board.w_king.pieces[0];
				w_attackers_total++;				 
			}			
		}

		// Black king
		if(board.b_king.pieces[0] != move_from)
		{
			// If it is possible for the king to attack the move_to square add it to attackers
			tempAttack = ATTACK_ARRAY[move_to - board.b_king.pieces[0] + 128];
			if(tempAttack == ATTACK_KQR || tempAttack == ATTACK_KQBwP || tempAttack == ATTACK_KQBbP)
			{
				w_attackers[w_attackers_total] = board.b_king.pieces[0];
				w_attackers_total++;				 
			}			
		}
		
		// Add attacking sliders
		
		// Sliders that move diagonally
		addSlider(move_to, 17, W_BISHOP);
		addSlider(move_to, 15, W_BISHOP);
		addSlider(move_to, -15, W_BISHOP);
		addSlider(move_to, -17, W_BISHOP);
		
		// Sliders that move straight
		addSlider(move_to, 16, W_ROOK);
		addSlider(move_to, -16, W_ROOK);
		addSlider(move_to, 1, W_ROOK);
		addSlider(move_to, -1, W_ROOK);

		// All obvious attackers are now added to the arrays
		//**************************************************
		
		// Now we start with 'making' the initial move so we can find out if
		// there is a hidden piece behind it, that is a piece that is able
		// to capture on the move_to square if the inital piece captures
		// We do this to get the inital move out of the way since it will
		// always happen first and should not be ordered
		
		// Important: Below we don't actually carry out the moves on the board for each capture
		// we simply simulate it by toggling the sideToMove variable and
		// setting attacked_piece_value to the value of the piece that 'captured'
		// any reference to a piece on the move_to square below is simply the piece there after a simulated capture
		
		score = piece_values[Move.capture(move)+7]; // Get the value of the initally capture piece
		attacked_piece_value = piece_values[(Move.pieceMoving(move))+7]; // A new piece is now 'standing' on the attacked square (the move_to square)
		sideToMove = board.toMove*-1; // Toggle the side to move since we simulated a move here
		
		addHidden(Move.fromIndex(move)); // We now add any hidden attacker that was behind the inital attacker
		
		scores[0] = score; // We now have a first for the capture sequence, this is the value of the initally captured piece
		
		int scoresIndex = 1; // Keeps track of where in the sequence we are
		int w_attackers_count = 0; // Keeps track of how many white pieces we have analyzed below
		int b_attackers_count = 0; // Keeps track of how many black pieces we have analyzed below
		int lowestValueIndex; // Temporary variable to keep track of the index of the least valuable spot in the sequence
		int lowestValue; // Temporary variable to keep track of the value of the least valuable spot in the sequence
		int tempSwap; // Temporary variable used for swapping places in the attackers arrays
		
		// Start looping, when we run out of either black or white pieces to analyze we break out
		// Inside we always capture with the least valuable piece left first
		while(true)
		{
			// If we have run out of pieces for the side to move we are finished and break out
			if((sideToMove == WHITE_TO_MOVE && w_attackers_count == w_attackers_total) ||
			   (sideToMove == BLACK_TO_MOVE && b_attackers_count == b_attackers_total))
			{
				break;
			}
			
			// Set the next step in the sequence to the value of the piece now on the move_to square - the previous score in the sequence
			scores[scoresIndex] = attacked_piece_value - scores[scoresIndex - 1];
			scoresIndex++; // Move to the next step in the sequence
			
			if(sideToMove == WHITE_TO_MOVE)
			{
				lowestValueIndex = w_attackers_count; // Get the index for the piece we're now analyzing
				lowestValue = piece_values[board.boardArray[w_attackers[w_attackers_count]]+7]; // Get the value for that piece
				
				// Loop from the next attacker to the total number of attackers
				for(int i = w_attackers_count + 1; i < w_attackers_total; i++)
				{
					// If the value for this piece is less then the currently lowest value of a piece in the sequence
					// update the lowestValueIndex and lowestValue so they reflect the now lowest valued piece
					if(piece_values[board.boardArray[w_attackers[i]]+7] < lowestValue)
					{
						lowestValueIndex = i;
						lowestValue = piece_values[board.boardArray[w_attackers[i]]+7];
					}
				}
				
				// If the lowestValueIndex got updated above we have a new lowest value and we swap it
				if(lowestValueIndex != w_attackers_count)
				{
					// Swap the places
					tempSwap = w_attackers[lowestValueIndex];
					w_attackers[lowestValueIndex] = w_attackers[w_attackers_count];
					w_attackers[w_attackers_count] = tempSwap;
				}
				
				// We have now analyzed the piece on the w_attackers_count in the w_attackers array
				// so we can now look for hidden attackers behind it
				addHidden(w_attackers[w_attackers_count]);
				
				// We now simulate the move for the analyzed piece
				attacked_piece_value = lowestValue; // A new piece is now 'standing' on the attacked square (the move_to square)
				sideToMove = BLACK_TO_MOVE; // Since it was white to move, it is now black's turn
				
				w_attackers_count++; // On the next pass of the loop (when it's white's turn) we check the next attacking piece				
			}
			else // Black to move, works exactly as above only for black attackers
			{
				lowestValueIndex = b_attackers_count;
				lowestValue = piece_values[board.boardArray[b_attackers[b_attackers_count]]+7];				
				for(int i = b_attackers_count + 1; i < b_attackers_total; i++)
				{
					if(piece_values[board.boardArray[b_attackers[i]]+7] < lowestValue)
					{
						lowestValueIndex = i;
						lowestValue = piece_values[board.boardArray[b_attackers[i]]+7];
					}
				}
				if(lowestValueIndex != b_attackers_count)
				{
					tempSwap = b_attackers[lowestValueIndex];
					b_attackers[lowestValueIndex] = b_attackers[b_attackers_count];
					b_attackers[b_attackers_count] = tempSwap;
				}
				addHidden(b_attackers[b_attackers_count]);
				attacked_piece_value = lowestValue;
				sideToMove = WHITE_TO_MOVE;				
				b_attackers_count++;	
			}
		}
		
		// Loop through the scores array, starting from the end (scoresIndex kept track of how many entries it has)
		// This loop moves the smallest value to the front of the list
		while(scoresIndex > 1)
		{
			// Since scoresIndex holds the total number of sequences we start with decrementing it so we get the first place in the array
			scoresIndex--;
			// If the place before the scoresIndex is greater than the current place
			if(scores[scoresIndex-1] > -scores[scoresIndex])
			{
				// Set the place before scoresIndex to the value of scoresIndex
				scores[scoresIndex - 1] = -scores[scoresIndex];
			}		
		}

		// The value of the capture sequence is now in the front of the scores list
		capture_value = scores[0];
		
		// Return the value of the capture in centipawns
		return (capture_value*100);
	}

	/**
	 * Iterates through all empty squares from the startIndex
	 * in the direction of the given delta. If we run into a piece
	 * we check if it is the same type as 'piece'. If it is
	 * we increment the number of attackers (for the right side
	 * depending on the color of the found piece) and record the
	 * index it is standing on in the right array.
	 * 
	 * @param startIndex Where we start looking from
	 * @param delta What delta this piece uses
	 * @param piece What type of piece uses this delta
	 */
	private static void addSlider(int startIndex, int delta, int piece)
	{
		int square = startIndex + delta; // Initialize the square by moving to the next square from startIndex in the delta direction

		// Follow the delta until we exit the board or run into a piece
		while((square & 0x88) == 0 && board.boardArray[square] == EMPTY_SQUARE) square += delta;

		// If we didn't leave the board and we are not trying to add the initial attacker
		if((square & 0x88) == 0 && square != move_from)
		{
			// Catch both queen and the type of piece we submitted, if it matches save the index and increment the count
			if(board.boardArray[square] == W_QUEEN || board.boardArray[square] == piece)
			{
				w_attackers[w_attackers_total] = square;
				w_attackers_total++;
			}
			// Same but for black pieces, we negate 'piece' to get the black equivalent
			else if(board.boardArray[square] == B_QUEEN || board.boardArray[square] == -piece)
			{
				b_attackers[b_attackers_total] = square;
				b_attackers_total++;
			}

		}

	}

	/**
	 * Take a start index somewhere on the board and compare it to the
	 * to-index we got from the move in see(move), from this we find a
	 * delta and then call addSlider with the proper delta and pieces
	 * 
	 * Since only sliding pieces can be hiding behind other pieces
	 * we don't have to worry about the other kind (king/knight)
	 * 
	 * @param startIndex Where to start looking for the hidden piece
	 */
	private static void addHidden(int startIndex)
	{
		// Find out what kind of pieces can move in this delta
		int pieceType = ATTACK_ARRAY[move_to - startIndex + 128];

		// If rook is one of types, call addSlider with the right delta and rook as piece type
		// same if bishop is one of the types
		switch(pieceType)
		{
		case ATTACK_KQR:
		case ATTACK_QR:
			addSlider(startIndex, DELTA_ARRAY[startIndex - move_to + 128], W_ROOK);
			break;
		case ATTACK_KQBwP:
		case ATTACK_KQBbP:
		case ATTACK_QB:
			addSlider(startIndex, DELTA_ARRAY[startIndex -move_to +128], W_BISHOP);
			break;
		}

	}

}
