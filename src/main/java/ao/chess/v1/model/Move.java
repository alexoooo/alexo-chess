package ao.chess.v1.model;

/**
 * class Move
 *
 * Contains static methods to analyze a move integer
 *
 * @author Jonatan Pettersson (mediocrechess@gmail.com)
 * Date: 2006-02-11
 * Updated: 2006-02-11
 */

public class Move implements Definitions
{
	public static final int TO_SHIFT = 7; // Number of bits to shift to get the to-index
	public static final int PIECE_SHIFT = 14; // To get the piece moving
	public static final int CAPTURE_SHIFT = 18; // To get the capture
	public static final int TYPE_SHIFT = 22; // To the move type
	public static final int ORDERING_SHIFT = 25; // To get the ordering value
	
	public static final int SQUARE_MASK = 127; // 7 bits, masks out the rest of the int when it has been shifted so we only get the information we need
	public static final int PIECE_MASK  = 15; // 4 bits
	public static final int TYPE_MASK = 7; // 3 bits
	
	public static final int ORDERING_CLEAR = 0x1FFFFFF; // = 00000001111111111111111111111111 use with & which clears the ordering value
	
	/**
	 *  @return int Piece moving
	 */
	public static int pieceMoving(int move)
	{
		return ((move >> PIECE_SHIFT) & PIECE_MASK) - 7; // 7 is the offset, so we get negative values for black pieces		
	}
	// END pieceMoving()
	
	/**
	 *  @return int To-index
	 */
	public static int toIndex(int move)
	{
		return ((move >> TO_SHIFT) & SQUARE_MASK);		
	}
	// END toIndex()
	
	/**
	 *  @return int From-index
	 */
	public static int fromIndex(int move)
	{
		return (move & SQUARE_MASK); // Since the from-index is first in the integer it doesn't need to be shifted first		
	}
	// END fromIndex()
	
	/**
	 *  @return int Piece captured
	 */
	public static int capture(int move)
	{
		return ((move >> CAPTURE_SHIFT) & PIECE_MASK) -7;		
	}
	// END capture()
	
	/**
	 *  @return int Move type
	 */
	public static int moveType(int move)
	{
		return ((move >> TYPE_SHIFT) & TYPE_MASK);		
	}
	// END moveType()
	
	/**
	 *  @return int Ordering value
	 */
	public static int orderingValue(int move)
	{
		return (move >> ORDERING_SHIFT); // Since the ordering value is last in the integer it doesn't need a mask		
	}
	// END orderingValue()
	
	/**
	 *  Clears the ordering value and sets it to the new number
	 *  
	 *  Important: Ordering value in the move integer cannot be >127
	 *  
	 *  @param move The move to change
	 *  @param value The new ordering value
	 *  @return move The changed moved integer
	 */
	public static int setOrderingValue(int move, int value)
	{
		move = (move & ORDERING_CLEAR); // Clear the ordering value
		return (move | (value << ORDERING_SHIFT)); // Change the ordering value and return the new move integer
	}
	// END orderingValue()
	
	
	/**
	 *  Creates a move integer from the gives values
	 *  
	 *  @param pieceMoving
	 *  @param fromIndex
	 *  @param toIndex
	 *  @param capture
	 *  @param type
	 *  @param ordering If we want to assign an ordering value at creation time, probably won't be used much for now
	 *  @reutrn move The finished move integer
	 */
	public static int createMove(int pieceMoving, int fromIndex, int toIndex, int capture, int type, int ordering)
	{
		int move =
		      fromIndex	// from
			| (toIndex << TO_SHIFT) // to
			| ((pieceMoving + 7) << PIECE_SHIFT) // piece moving (offset 7)
			| ((capture + 7) << CAPTURE_SHIFT) //piece captured (offset 7)
			| (type << TYPE_SHIFT) // move type
			| (ordering << ORDERING_SHIFT); // ordering value
		return move;
	}
	// END createMove
	
	/**
	 *  Returns a string holding the short notation of the move
	 *
	 *  @return String Short notation
	 */
	public static String notation(int move)
	{
		int pieceMoving = ((move >> PIECE_SHIFT) & PIECE_MASK) - 7; // Remember the offset 7
		int fromIndex = (move & SQUARE_MASK);
		int toIndex = ((move >> TO_SHIFT) & SQUARE_MASK);
		int capture = ((move >> CAPTURE_SHIFT) & PIECE_MASK) -7;
		int moveType = ((move >> TYPE_SHIFT) & TYPE_MASK);	
		
		String notation = "";

		// Add the piece notation
		switch(pieceMoving)
		{
			case W_KING:
				{
					if(moveType == SHORT_CASTLE) return "0-0";
					if(moveType == LONG_CASTLE) return "0-0-0";
					notation += "K"; break;
				}	
			case B_KING:
				{
					if(moveType == SHORT_CASTLE) return "0-0";
					if(moveType == LONG_CASTLE) return "0-0-0";
					notation += "K"; break;
				}				
			case W_QUEEN: notation += "Q"; break;
			case B_QUEEN: notation += "Q"; break;
			case W_ROOK: notation += "R"; break;
			case B_ROOK: notation += "R"; break;
			case W_BISHOP: notation += "B"; break;
			case B_BISHOP: notation += "B"; break;
			case W_KNIGHT: notation += "N"; break;
			case B_KNIGHT: notation += "N"; break;
		}

		if(capture != 0) // The move is a capture
		{
			// If the moving piece is a pawn we need to add the row it's moving from
			if((pieceMoving == W_PAWN) || (pieceMoving == B_PAWN))
			{
				switch(fromIndex % 16) // Find the row
				{
					case 0: notation += "a"; break;
					case 1: notation += "b"; break;
					case 2: notation += "c"; break;
					case 3: notation += "d"; break;
					case 4: notation += "e"; break;
					case 5: notation += "f"; break;
					case 6: notation += "g"; break;
					case 7: notation += "h"; break;
				}
			}
			notation += "x";
		}

		switch(toIndex % 16) // Find the row
		{
			case 0: notation += "a"; break;
			case 1: notation += "b"; break;
			case 2: notation += "c"; break;
			case 3: notation += "d"; break;
			case 4: notation += "e"; break;
			case 5: notation += "f"; break;
			case 6: notation += "g"; break;
			case 7: notation += "h"; break;
		}

		notation += (toIndex-(toIndex%16))/16 + 1; // Add the rank
		
		if(moveType == EN_PASSANT)
			notation += " e.p.";

		// Add promotion
		switch(moveType)
		{
			case PROMOTION_QUEEN: notation += "=Q"; break;
			case PROMOTION_ROOK: notation += "=R"; break;
			case PROMOTION_BISHOP: notation += "=B"; break;
			case PROMOTION_KNIGHT: notation += "=N"; break;
		}
		
		return notation;
	}
	// END notation()
	
	/**
	 *  Returns the move on the form 'e2e4', that is
	 *  only the from and to square
	 *
	 *  @return String The input notation
	 */
	public static String inputNotation(int move)
	{
		
		// Gather the information from the move int

		int fromIndex = (move & SQUARE_MASK);
		int toIndex = ((move >> TO_SHIFT) & SQUARE_MASK);
		int moveType = ((move >> TYPE_SHIFT) & TYPE_MASK);
		
		String inputNotation = "";
		switch(fromIndex % 16)
		{
			case 0: inputNotation += "a"; break;
			case 1: inputNotation += "b"; break;
			case 2: inputNotation += "c"; break;
			case 3: inputNotation += "d"; break;
			case 4: inputNotation += "e"; break;
			case 5: inputNotation += "f"; break;
			case 6: inputNotation += "g"; break;
			case 7: inputNotation += "h"; break;
		}
		switch((fromIndex-(fromIndex%16))/16)
		{
			case 0: inputNotation += "1"; break;
			case 1: inputNotation += "2"; break;
			case 2: inputNotation += "3"; break;
			case 3: inputNotation += "4"; break;
			case 4: inputNotation += "5"; break;
			case 5: inputNotation += "6"; break;
			case 6: inputNotation += "7"; break;
			case 7: inputNotation += "8"; break;
		}
		switch(toIndex%16)
		{
			case 0: inputNotation += "a"; break;
			case 1: inputNotation += "b"; break;
			case 2: inputNotation += "c"; break;
			case 3: inputNotation += "d"; break;
			case 4: inputNotation += "e"; break;
			case 5: inputNotation += "f"; break;
			case 6: inputNotation += "g"; break;
			case 7: inputNotation += "h"; break;
		}
		switch((toIndex-(toIndex%16))/16)
		{
			case 0: inputNotation += "1"; break;
			case 1: inputNotation += "2"; break;
			case 2: inputNotation += "3"; break;
			case 3: inputNotation += "4"; break;
			case 4: inputNotation += "5"; break;
			case 5: inputNotation += "6"; break;
			case 6: inputNotation += "7"; break;
			case 7: inputNotation += "8"; break;
		}    
		switch(moveType) // Check for promotion
		{
			case PROMOTION_QUEEN: inputNotation += "q"; break;
			case PROMOTION_ROOK: inputNotation += "r"; break;
			case PROMOTION_BISHOP: inputNotation += "b"; break;
			case PROMOTION_KNIGHT: inputNotation += "n"; break;
		}		

		return inputNotation;

	}
	// END inputNotation()

	
}
