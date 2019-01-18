package ao.chess.v1.model;

import ao.chess.v1.ai.Position;

import java.util.Arrays;

/**
 * class Board
 *
 * Represents a position on the board and also contains methods for making
 * and unmaking a move, as well as generating all possible (legal) moves on
 * the board.
 *
 * @author Jonatan Pettersson (mediocrechess@gmail.com)
 * Date: 2006-12-14
 * Last update: 2006-12-25
 */

public class Board implements Definitions
{
	// ***
	// Variables used to describe the position
	// ***
	
	public int[] boardArray; 		// Represents the 0x88 board
	public int[] boardArrayUnique;	// Keeps track of what index a piece on a certain square has in the corresponding piece list 

	public int toMove; 			// Side to move on the board

	public int enPassant;  		// Index on boardArray where en passant is
						// available, -1 if none

	public int white_castle;		// White's ability to castle
	public int black_castle; 		// Black's ability to castle

	public int movesFifty;			// Keeps track of half-moves since last capture
						// or pawn move
						
	public int movesFull; 			// Keeps track of total full moves in the game
	public int[] history;			// Keeps track of previous positions for unmake move
	public int[] captureHistory;
	public long[] zobristHistory;	// Same as history but keeps tracks of passed zobrist keys
	public long[] pawnZobristHistory;
	
	public int historyIndex;		// Keeps track of number of takebacks in the history arrays
	
	
	public long zobristKey;			// The zobrist key for the position
	public long pawnZobristKey;		// The zobrist key for the pawns

	//public int wking_index;
	//public int bking_index;
	
	//public int[] pieces;
	//public static final int PIECE_UNIQUE_SHIFT = 4;
	//public static final int PIECE_TYPE_MASK = 15;
	
	public PieceList w_pawns;
	public PieceList b_pawns;
	public PieceList w_knights;
	public PieceList b_knights;
	public PieceList w_bishops;
	public PieceList b_bishops;
	public PieceList w_rooks;
	public PieceList b_rooks;
	public PieceList w_queens;
	public PieceList b_queens;
	public PieceList w_king;
	public PieceList b_king;
	
	
	//END Variables

	/**
	 *  Creates an empty board
	 *
	 *  @return An empty Board
	 */
	public Board()
	{
		this.boardArray = new int[128];
		this.boardArrayUnique = new int[128];
		this.toMove = 1; // White to move
		this.enPassant = -1; // No en passants available
		this.white_castle = Definitions.CASTLE_NONE;
		this.black_castle = Definitions.CASTLE_NONE;
		this.history = new int[4096];
		this.captureHistory = new int[4096];
		this.zobristHistory = new long[4096];
		this.pawnZobristHistory = new long[4096];
		this.zobristKey = 0;
		this.pawnZobristKey = 0;
        this.w_pawns = new PieceList(this);
		this.b_pawns = new PieceList(this);
		this.w_knights = new PieceList(this);
		this.b_knights = new PieceList(this);
		this.w_bishops = new PieceList(this);
		this.b_bishops = new PieceList(this);
		this.w_rooks = new PieceList(this);
		this.b_rooks = new PieceList(this);
		this.w_queens = new PieceList(this);
		this.b_queens = new PieceList(this);
		this.w_king =  new PieceList(this);
		this.b_king = new PieceList(this);
    }
	// END Board()

    private Board(int boardArray[],
                  int boardArrayUnique[],
                  int toMove, int enPassant,
                  int white_castle, int black_castle,
                  PieceList w_pawns, PieceList b_pawns,
                  PieceList w_knights, PieceList b_knights,
                  PieceList w_bishops, PieceList b_bishops,
                  PieceList w_rooks, PieceList b_rooks,
                  PieceList w_queens, PieceList b_queens,
                  PieceList w_king, PieceList b_king,
                  int movesFull,
                  int history[],
                  int captureHistory[],
                  long zobristHistory[],
                  long pawnZobristHistory[],
                  long zobristKey,
                  long pawnZobristKey)
    {
        this.boardArray = boardArray;
		this.boardArrayUnique = boardArrayUnique;
		this.toMove = toMove;
		this.enPassant = enPassant;
		this.white_castle = white_castle;
		this.black_castle = black_castle;
		this.movesFull = movesFull;
        this.history = history;
		this.captureHistory = captureHistory;
		this.zobristHistory = zobristHistory;
		this.pawnZobristHistory = pawnZobristHistory;
		this.zobristKey = zobristKey;
		this.pawnZobristKey = pawnZobristKey;
        this.w_pawns = w_pawns.prototype(this);
		this.b_pawns = b_pawns.prototype(this);
		this.w_knights = w_knights.prototype(this);
		this.b_knights = b_knights.prototype(this);
		this.w_bishops = w_bishops.prototype(this);
		this.b_bishops = b_bishops.prototype(this);
		this.w_rooks = w_rooks.prototype(this);
		this.b_rooks = b_rooks.prototype(this);
		this.w_queens = w_queens.prototype(this);
		this.b_queens = b_queens.prototype(this);
		this.w_king = w_king.prototype(this);
		this.b_king = b_king.prototype(this);
    }

    public Board prototype()
    {
        return new Board(boardArray.clone(),
                         boardArrayUnique.clone(),
                         toMove, enPassant, white_castle, black_castle,
                         w_pawns, b_pawns,
                         w_knights, b_knights,
                         w_bishops, b_bishops,
                         w_rooks, b_rooks,
                         w_queens, b_queens,
                         w_king, b_king,
                         movesFull,
                         history.clone(), captureHistory.clone(),
                         zobristHistory.clone(), pawnZobristHistory.clone(),
                         zobristKey, pawnZobristKey);
    }

    public int pieceCount()
    {
        return w_pawns.count +
               b_pawns.count +

               w_knights.count +
               b_knights.count +

               w_bishops.count +
               b_bishops.count +

               w_rooks.count +
               b_rooks.count +

               w_queens.count +
               b_queens.count +

               w_king.count +
               b_king.count;
    }


    /**
	 * The general class for the piece lists
	 * 
	 * This class is quite fragile so it has to be used right,
	 * for example editing the pieces array in any way but using the internal methods
	 * is dangerous (we have to remember updating the boardArraUnique etc)
	 * 
	 * Also trying to remove a piece if count==0 will not be pleasant, we need to be careful to never do things like this
	 * 
	 * It is possible to write this class in a safer way, but it costs a little time and should not be nescessary
	 * 
	 * We only work with boardArrayUnique here and never touch the boardArray, that is done elsewhere (in makeMove and unmakeMove)
	 * 
	 * For promotions you need to remove the pawn/promoted piece (make/unmake) from the corresponding
	 * list and add it to the other. Make sure you always add AFTER removing the first piece
	 * since boardArrayUnique is reset after removing a piece (making it impossible to find the added piece
	 * if it is not added after the remove)
	 */
	public static class PieceList
	{
		public int pieces[]; // The indexes the white of the certain type is on
		public int count; // The number of pieces (how many slots in the array are filled with indexes)
		private final Board BOARD;

        public PieceList(Board board)
		{
			this.pieces = new int[10];
			this.count = 0;

            BOARD = board;
        }
        private PieceList(int pieces[], int count, Board board)
		{
            this.pieces = pieces;
            this.count  = count;
            BOARD = board;
        }

        /**
		 * Removes a piece from the list and updates the boardArrayUnique accordingly
		 * 
		 * Used when a capture is made
		 * 
		 * @param boardIndex The index where the captured piece resided
		 */
		public void removePiece(int boardIndex)
		{
			count--; // We now have one less piece in the array
			int listIndex = BOARD.boardArrayUnique[boardIndex]; // Get the place in the pieces list where the particular piece resides
			BOARD.boardArrayUnique[boardIndex] = -1; // Remove the piece from the board array
			
			pieces[listIndex] = pieces[count]; // Overwrite the removed piece with the last slot in the array (we decremented count above so 'count' is the previous last slot in the array)
			BOARD.boardArrayUnique[pieces[count]] = listIndex; // Update the boardArrayUnique so we get the changed index of the moved slot
			pieces[count] = 0; // Erase the last slot 
		}
		// END removePiece()
		
		/**
		 * Adds a piece to the list
		 * 
		 * @param boardIndex Index where the new piece should be
		 */
		public void addPiece(int boardIndex)
		{
			BOARD.boardArrayUnique[boardIndex] = count; // Record the list index for the piece ('count' works here as last filled index +1)
			pieces[count] = boardIndex; // Record the board index in the list			
			count++; // Now we can increment the number of pieces
		}
		// END addPiece()
		
		/**
		 * Updates the index of a piece in the list, used when a piece is moving
		 * 
		 * If the to-square is occupied already, find out by what piece and remove it from the corresponding list
		 * 
		 * @param from The square the piece was on before the change
		 * @param to The new index the piece should have
		 */
		public void updateIndex(int from, int to)
		{

			int listIndex = BOARD.boardArrayUnique[from]; // Get the place in the pieces list where the particular piece resides
			BOARD.boardArrayUnique[from] = -1; // Reset the square it was moving from

			if(BOARD.boardArray[to] != 0) // The to-square was not empty so remove whatever piece was on it from the corresponding list
			{
				//System.out.println(from + " . " + to);
				switch(BOARD.boardArray[to])
				{
				case Definitions.W_PAWN: BOARD.w_pawns.removePiece(to); break;
				case Definitions.B_PAWN: BOARD.b_pawns.removePiece(to); break;
				case Definitions.W_KNIGHT: BOARD.w_knights.removePiece(to); break;
				case Definitions.B_KNIGHT: BOARD.b_knights.removePiece(to); break;
				case Definitions.W_BISHOP: BOARD.w_bishops.removePiece(to); break;
				case Definitions.B_BISHOP: BOARD.b_bishops.removePiece(to); break;
				case Definitions.W_ROOK: BOARD.w_rooks.removePiece(to); break;
				case Definitions.B_ROOK: BOARD.b_rooks.removePiece(to); break;
				case Definitions.W_QUEEN: BOARD.w_queens.removePiece(to); break;
				case Definitions.B_QUEEN: BOARD.b_queens.removePiece(to); break;
				}

			}
			BOARD.boardArrayUnique[to] = listIndex; // Record the new position of the piece
			
			pieces[listIndex] = to;
		}
		// END updateIndex()

        public PieceList prototype(Board board)
        {
            return new PieceList(pieces.clone(), count, board);
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PieceList pieceList = (PieceList) o;

            if (count != pieceList.count) return false;
            if (!Arrays.equals(pieces, pieceList.pieces)) return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = Arrays.hashCode(pieces);
            result = 31 * result + count;
            return result;
        }
    }
	// END PieceList

	/**
	 *  Makes a Move on the board and updates the zobrist key accordingly
	 *
	 *  @param move move
	 */
	public void makeMove(int move)
	{
	
		// Backup information about the position for use in unmake
		history[historyIndex] = 0;
		if(enPassant != -1)
		{
			history[historyIndex] =
				enPassant;
		}		
		
		history[historyIndex] = history[historyIndex] |
			  (white_castle << 7)
			| (black_castle << 9)
			| (movesFifty << 16);
		
		captureHistory[historyIndex] = boardArray[Move.toIndex(move)]; // If the move is an en passant this will be 0 here, but changed when we catch en passant below
		
		zobristHistory[historyIndex] = zobristKey;
		pawnZobristHistory[historyIndex] = pawnZobristKey;
		
		
		// Done with backing up, continue
		
		if(enPassant != -1) zobristKey ^= Zobrist.EN_PASSANT[enPassant]; // Unmake the enpassant square that is on the board now, and replace it below when it is set again

		zobristKey ^= Zobrist.SIDE; // Toggles the side to move
		
		// Remove the castling rights now on the board, and replace them below when they are set again
		zobristKey ^= Zobrist.W_CASTLING_RIGHTS[white_castle];
		zobristKey ^= Zobrist.B_CASTLING_RIGHTS[black_castle];

							
		enPassant = -1; // Set the en passant square to none, will be reset below if it becomes available

		toMove *= -1; // Switch side to move (multiply toMove by -1 simply switches from negative to positive
				// and vice versa)
		
		// Update the king index
	//	if(Move.pieceMoving(move) == W_KING) wking_index = Move.toIndex(move);
		//if(Move.pieceMoving(move) == B_KING) bking_index = Move.toIndex(move);
		
		
		// Set the piece list index of the piece that is moving
		switch(Move.pieceMoving(move))
		{
		case Definitions.W_PAWN: w_pawns.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.B_PAWN: b_pawns.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.W_KNIGHT: w_knights.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.B_KNIGHT: b_knights.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.W_BISHOP: w_bishops.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.B_BISHOP: b_bishops.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.W_ROOK: w_rooks.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.B_ROOK: b_rooks.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.W_QUEEN: w_queens.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.B_QUEEN: b_queens.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.W_KING: w_king.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		case Definitions.B_KING: b_king.updateIndex(Move.fromIndex(move), Move.toIndex(move)); break;
		}
		
		
		
		if(Move.pieceMoving(move) != Definitions.W_PAWN && Move.pieceMoving(move) != Definitions.B_PAWN && Move.capture(move) == 0)
			movesFifty++; // Increment the moves fifty if not a pawn moving or a capture
		else
			movesFifty = 0; // If a pawned moved or it was a capture, reset the movesFifty count

		if(Move.pieceMoving(move) < 0)
			movesFull++; // Increment the total of full moves if the moving piece is black

		// Now find out what kind of move it is and act accordingly
		switch(Move.moveType(move))
		{
		case Definitions.ORDINARY_MOVE:
		{
			// Remove and replace the piece in the zobrist key
			if(toMove == -1) // We have switched side so black means a white piece is moving
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.toIndex(move)];
				
				if(Move.pieceMoving(move) == Definitions.W_PAWN)
				{
					pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.fromIndex(move)];
					pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.toIndex(move)];
				}
			}
			else
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.toIndex(move)];
				
				if(Move.pieceMoving(move) == Definitions.B_PAWN)
				{
					pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.fromIndex(move)];
					pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.toIndex(move)];
				}
			}	

			boardArray[Move.toIndex(move)] = boardArray[Move.fromIndex(move)]; // Set target square
			boardArray[Move.fromIndex(move)] = Definitions.EMPTY_SQUARE; // Clear the original square

			// Check for en passant
			// If the piece moving is a white or black pawn, and it has moved
			// 2 squares (the Math.abs line checks that)
			// set the en passant square to the right index and exit the move
			if((Move.pieceMoving(move) == Definitions.W_PAWN || Move.pieceMoving(move) == Definitions.B_PAWN)
					&& Math.abs(Move.toIndex(move) - Move.fromIndex(move)) == 32)
			{
				enPassant = Move.fromIndex(move) + (Move.toIndex(move) - Move.fromIndex(move))/2;
				break;
			}
			break;
		}

		case Definitions.SHORT_CASTLE:
		{
			if (Move.pieceMoving(move) == Definitions.W_KING) // White king castles short
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.toIndex(move)];

				// And the rook
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][0][7];
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][0][5];

				// Change the place of the rook in the piece list as well
				w_rooks.updateIndex(Definitions.H1, Definitions.F1);

				boardArray[6] = boardArray[4]; // Put white king
				boardArray[5] = boardArray[7]; // Put white rook
				boardArray[7] = Definitions.EMPTY_SQUARE; // Empty the rook square
				boardArray[4] = Definitions.EMPTY_SQUARE; // Empty the king square

				white_castle = Definitions.CASTLE_NONE; // Make further castling impossible for white
			}
			else // Black king castles short
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.toIndex(move)];

				// And the rook, stils W_ROOK since '1' takes care of color
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][1][119];
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][1][117];

				// Change the place of the rook in the piece list as well
				b_rooks.updateIndex(Definitions.H8, Definitions.F8);

				boardArray[118] = boardArray[116]; // Put black king
				boardArray[117] = boardArray[119]; // Put black rook
				boardArray[119] = Definitions.EMPTY_SQUARE; // Empty the rook square
				boardArray[116] = Definitions.EMPTY_SQUARE; // Empty the king square

				black_castle = Definitions.CASTLE_NONE; // Make further castling impossible for black
			}				
			break;
		}

		case Definitions.LONG_CASTLE:
		{
			if (Move.pieceMoving(move) == Definitions.W_KING) // White king castles long
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.toIndex(move)];

				// And the rook
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][0][0];
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][0][3];

				// Change the place of the rook in the piece list as well
				w_rooks.updateIndex(Definitions.A1, Definitions.D1);

				boardArray[2] = boardArray[4]; // Put white king
				boardArray[3] = boardArray[0]; // Put white rook
				boardArray[0] = Definitions.EMPTY_SQUARE; // Empty the rook square
				boardArray[4] = Definitions.EMPTY_SQUARE; // Empty the king square

				white_castle = Definitions.CASTLE_NONE; // Make further castling impossible for white
			}
			else // Black king castles long
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.toIndex(move)];

				// And the rook, still W_ROOK since '1' takes care of color
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][1][112];
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][1][115];

				// Change the place of the rook in the piece list as well
				b_rooks.updateIndex(Definitions.A8, Definitions.D8);

				boardArray[114] = boardArray[116]; // Put black king
				boardArray[115] = boardArray[112]; // Put black rook
				boardArray[112] = Definitions.EMPTY_SQUARE; // Empty the rook square
				boardArray[116] = Definitions.EMPTY_SQUARE; // Empty the king square

				black_castle = Definitions.CASTLE_NONE; // Make further castling impossible for black
			}		
			break;
		}

		case Definitions.EN_PASSANT:
		{
			// Remove and replace the piece in the zobrist key
			if(toMove == -1) // We have switched side so this means white is moving
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.toIndex(move)];

				pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.fromIndex(move)];
				pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][0][Move.toIndex(move)];

				// Remove the pawn from the piece list as well
				b_pawns.removePiece(Move.toIndex(move) - 16); // It is a black pawn that was captured en passant

				captureHistory[historyIndex] = boardArray[Move.toIndex(move) - 16];

				// Since it's an en passant capture we also need to remove the captured pawn
				// which resides one square up/down from the target square
				// If it's a white pawn capturing, clear the square below it
				boardArray[Move.toIndex(move) - 16] = Definitions.EMPTY_SQUARE;



				// Black pawn is to be removed
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.toIndex(move) - 16];
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.toIndex(move) - 16];

			}
			else
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.fromIndex(move)];
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.toIndex(move)];

				pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.fromIndex(move)];
				pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.pieceMoving(move))-1][1][Move.toIndex(move)];
				
				// Remove the pawn from the piece list as well
				w_pawns.removePiece(Move.toIndex(move) + 16); // It is a white pawn that was captured en passant

				captureHistory[historyIndex] = boardArray[Move.toIndex(move) + 16];

				boardArray[Move.toIndex(move) + 16] = Definitions.EMPTY_SQUARE;

				// White pawn is to be removed
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.toIndex(move) + 16];
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.toIndex(move) + 16];
			}				
			boardArray[Move.toIndex(move)] = boardArray[Move.fromIndex(move)]; // Set target square
			boardArray[Move.fromIndex(move)] = Definitions.EMPTY_SQUARE; // Clear the original square
			break;
		}


		// Put a promoted piece of the right color on the target square
		// We use the toMove property to get the right color but since
		// we already changed side to move we need the opposite color
		// 
		// E.g. promotion to queen and white to move would result in
		// 2*(-(-1)) = 2 and for black 2*(-(1)) = -2
		case Definitions.PROMOTION_QUEEN:
		{
			boardArray[Move.toIndex(move)] = Definitions.W_QUEEN *(-toMove);

			if(toMove == -1)
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_QUEEN -1][0][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				
				w_pawns.removePiece(Move.toIndex(move));
				w_queens.addPiece(Move.toIndex(move));
				
			}
			else
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_QUEEN -1][1][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				
				b_pawns.removePiece(Move.toIndex(move));
				b_queens.addPiece(Move.toIndex(move));
			}
			boardArray[Move.fromIndex(move)] = Definitions.EMPTY_SQUARE; // Empty the square it moved from
			break;
		}
		case Definitions.PROMOTION_ROOK:
		{
			boardArray[Move.toIndex(move)] = Definitions.W_ROOK *(-toMove);


			if(toMove == -1)
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][0][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				
				w_pawns.removePiece(Move.toIndex(move));
				w_rooks.addPiece(Move.toIndex(move));
			}
			else
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_ROOK -1][1][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				
				b_pawns.removePiece(Move.toIndex(move));
				b_rooks.addPiece(Move.toIndex(move));
			}
			boardArray[Move.fromIndex(move)] = Definitions.EMPTY_SQUARE; // Empty the square it moved from
			break;
		}
		case Definitions.PROMOTION_BISHOP:
		{
			boardArray[Move.toIndex(move)] = Definitions.W_BISHOP *(-toMove);


			if(toMove == -1)
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_BISHOP -1][0][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				
				w_pawns.removePiece(Move.toIndex(move));
				w_bishops.addPiece(Move.toIndex(move));				
			}
			else
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_BISHOP -1][1][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				
				b_pawns.removePiece(Move.toIndex(move));
				b_bishops.addPiece(Move.toIndex(move));
			}
			boardArray[Move.fromIndex(move)] = Definitions.EMPTY_SQUARE; // Empty the square it moved from
			break;
		}
		case Definitions.PROMOTION_KNIGHT:
		{
			boardArray[Move.toIndex(move)] = Definitions.W_KNIGHT *(-toMove);


			if(toMove == -1)
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_KNIGHT -1][0][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][0][Move.fromIndex(move)];
				w_pawns.removePiece(Move.toIndex(move));
				w_knights.addPiece(Move.toIndex(move));
			}
			else
			{
				zobristKey ^= Zobrist.PIECES[Definitions.W_KNIGHT -1][1][Move.toIndex(move)];
				zobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				pawnZobristKey ^= Zobrist.PIECES[Definitions.W_PAWN -1][1][Move.fromIndex(move)];
				b_pawns.removePiece(Move.toIndex(move));
				b_knights.addPiece(Move.toIndex(move));
			}
			boardArray[Move.fromIndex(move)] = Definitions.EMPTY_SQUARE; // Empty the square it moved from
			break;
		}

		}
		
		// Check for castling rights changes
		if(white_castle != Definitions.CASTLE_NONE)
		{
			if(Move.pieceMoving(move) == Definitions.W_KING) white_castle = Definitions.CASTLE_NONE;
			else if(Move.toIndex(move) == Definitions.A1 || Move.fromIndex(move) == Definitions.A1)
			{
				if(white_castle == Definitions.CASTLE_BOTH || white_castle == Definitions.CASTLE_SHORT) white_castle = Definitions.CASTLE_SHORT;
				else white_castle = Definitions.CASTLE_NONE;
			}
			else if(Move.toIndex(move) == Definitions.H1 || Move.fromIndex(move) == Definitions.H1)
			{
				if(white_castle == Definitions.CASTLE_BOTH || white_castle == Definitions.CASTLE_LONG) white_castle = Definitions.CASTLE_LONG;
				else white_castle = Definitions.CASTLE_NONE;
			}
			
		}
		if(black_castle != Definitions.CASTLE_NONE)
		{
			if(Move.pieceMoving(move) == Definitions.B_KING) black_castle = Definitions.CASTLE_NONE;
			else if(Move.toIndex(move) == Definitions.A8 || Move.fromIndex(move) == Definitions.A8)
			{
				if(black_castle == Definitions.CASTLE_BOTH || black_castle == Definitions.CASTLE_SHORT)	black_castle = Definitions.CASTLE_SHORT;
				else black_castle = Definitions.CASTLE_NONE;
			}
			else if(Move.toIndex(move) == Definitions.H8 || Move.fromIndex(move) == Definitions.H8)
			{
				if(black_castle == Definitions.CASTLE_BOTH || black_castle == Definitions.CASTLE_LONG) black_castle = Definitions.CASTLE_LONG;
				else black_castle = Definitions.CASTLE_NONE;
			}			
		}		
		
		// If the move is a capture, remove the captured piece from zobrist as well
		if(toMove == -1) // We have switched side so black means a white piece is moving
		{
			if(Move.capture(move) != 0 && Move.moveType(move) != Definitions.EN_PASSANT)
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.capture(move))-1][1][Move.toIndex(move)];
				
				if(Move.capture(move) == Definitions.B_PAWN)
					pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.capture(move))-1][1][Move.toIndex(move)];
			}
				
		}
		else
		{
			if(Move.capture(move) != 0 && Move.moveType(move) != Definitions.EN_PASSANT)
			{
				zobristKey ^= Zobrist.PIECES[Math.abs(Move.capture(move))-1][0][Move.toIndex(move)];
				
				if(Move.capture(move) == Definitions.W_PAWN)
					pawnZobristKey ^= Zobrist.PIECES[Math.abs(Move.capture(move))-1][0][Move.toIndex(move)];
			}
		}	

		// Apply the now changed castling rights to zobrist key
		zobristKey ^= Zobrist.W_CASTLING_RIGHTS[white_castle];
		zobristKey ^= Zobrist.B_CASTLING_RIGHTS[black_castle];

		// Apply the now changed en passant square to zobrist key
		if(enPassant != -1) zobristKey ^= Zobrist.EN_PASSANT[enPassant];
		
		historyIndex++;
		
	}
	// END makeMove()
	
	/**
	 *  Just like switching side, but toggles the zobrist as well
	 */
	public void nullmoveToggle()
	{
		toMove *= -1;
		zobristKey ^= Zobrist.SIDE;
	}
	// END makeNullmove()

	/**
	 *  Unmakes a Move on the board
	 *
	 *  @param move move
	 */
	public void unmakeMove(int move)
	{
		historyIndex--; // Go back one step in the history so we find the right unmake variables

		// Use the history to reset known variables
		if(((history[historyIndex]) & 127) == 0)
		{
			enPassant = -1;
		}
		else
		{
			enPassant = ((history[historyIndex]) & 127);
		}
		white_castle = ((history[historyIndex] >> 7) & 3);
		black_castle = ((history[historyIndex] >> 9) & 3);
		movesFifty = ((history[historyIndex] >> 16) & 127);
		zobristKey = zobristHistory[historyIndex];
		pawnZobristKey = pawnZobristHistory[historyIndex];

		// We wait with resetting the capture until we know if it is an en passant or not



		toMove *= -1; // Switch side to move

		// Update the king index

		//if(Move.pieceMoving(move) == W_KING) wking_index = Move.fromIndex(move);
		//if(Move.pieceMoving(move) == B_KING) bking_index = Move.fromIndex(move);

		//switch(Move.pieceMoving(move))
		switch(boardArray[Move.toIndex(move)])
		{
		case Definitions.W_PAWN: w_pawns.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.B_PAWN: b_pawns.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.W_KNIGHT: w_knights.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.B_KNIGHT: b_knights.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.W_BISHOP: w_bishops.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.B_BISHOP: b_bishops.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.W_ROOK: w_rooks.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.B_ROOK: b_rooks.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.W_QUEEN: w_queens.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.B_QUEEN: b_queens.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.W_KING: w_king.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		case Definitions.B_KING: b_king.updateIndex(Move.toIndex(move), Move.fromIndex(move)); break;
		}


		// If the move we're taking back was a black piece moving
		// decrement the movesFull.
		if(Move.pieceMoving(move) < 0)
			movesFull--;

		switch(Move.moveType(move))
		{
		case Definitions.SHORT_CASTLE:

			if (Move.pieceMoving(move) == Definitions.W_KING) // White king castles short
			{	
				w_rooks.updateIndex(Definitions.F1, Definitions.H1); // Put back the rook in the piece list

				boardArray[4] = boardArray[6]; // Put back white king
				boardArray[7] = boardArray[5]; // Put back white rook
				boardArray[5] = Definitions.EMPTY_SQUARE; // Empty castling squares
				boardArray[6] = Definitions.EMPTY_SQUARE; // .



			}
			else if (Move.pieceMoving(move) == Definitions.B_KING) // Black king castles short
			{	
				b_rooks.updateIndex(Definitions.F8, Definitions.H8); // Put back the rook in the piece list

				boardArray[116] = boardArray[118]; // Put back black king
				boardArray[119] = boardArray[117]; // Put back black rook
				boardArray[117] = Definitions.EMPTY_SQUARE; // Empty castling squares
				boardArray[118] = Definitions.EMPTY_SQUARE; // .

			}				
			break; // Done with unmake


		case Definitions.LONG_CASTLE:

			if (Move.pieceMoving(move) == Definitions.W_KING) // White king castles long
			{		
				w_rooks.updateIndex(Definitions.D1, Definitions.A1); // Put back the rook in the piece list

				boardArray[4] = boardArray[2]; // Put back white king
				boardArray[0] = boardArray[3]; // Put back white rook
				boardArray[2] = Definitions.EMPTY_SQUARE; // Empty castling squares
				boardArray[3] = Definitions.EMPTY_SQUARE; // .

			}
			else if (Move.pieceMoving(move) == Definitions.B_KING) // Black king castles lon
			{
				b_rooks.updateIndex(Definitions.D8, Definitions.A8); // Put back the rook in the piece list

				boardArray[116] = boardArray[114]; // Put back black king
				boardArray[112] = boardArray[115]; // Put back black rook
				boardArray[114] = Definitions.EMPTY_SQUARE; // Empty castling squares
				boardArray[115] = Definitions.EMPTY_SQUARE; // .

			}		
			break; // Done with unmake

		case Definitions.EN_PASSANT:

			// Remove and replace the piece in the piece lists
			if(toMove == 1)
			{
				// Put back the black pawn
				boardArray[Move.toIndex(move) - 16] = Definitions.B_PAWN;
				b_pawns.addPiece(Move.toIndex(move) - 16);
			}
			else
			{
				// Put back the white pawn
				boardArray[Move.toIndex(move) + 16] = Definitions.W_PAWN;
				w_pawns.addPiece(Move.toIndex(move) + 16);
			}								

			boardArray[Move.fromIndex(move)] = boardArray[Move.toIndex(move)]; // Put back the pawn
			boardArray[Move.toIndex(move)] = Definitions.EMPTY_SQUARE; // Clear the original square


			break;

		default:
		{
			boardArray[Move.fromIndex(move)] = Move.pieceMoving(move); // Put back the piece (white/black pawn)
			boardArray[Move.toIndex(move)] = captureHistory[historyIndex]; // Put back the captured piece, 0 if it wasn't a capture

			// If it was a capture put it back in the piece list
			if(captureHistory[historyIndex] != 0)
			{
				switch(boardArray[Move.toIndex(move)])
				{
				case Definitions.W_PAWN: w_pawns.addPiece(Move.toIndex(move)); break;
				case Definitions.B_PAWN: b_pawns.addPiece(Move.toIndex(move)); break;
				case Definitions.W_KNIGHT: w_knights.addPiece(Move.toIndex(move)); break;
				case Definitions.B_KNIGHT: b_knights.addPiece(Move.toIndex(move)); break;
				case Definitions.W_BISHOP: w_bishops.addPiece(Move.toIndex(move)); break;
				case Definitions.B_BISHOP: b_bishops.addPiece(Move.toIndex(move)); break;
				case Definitions.W_ROOK: w_rooks.addPiece(Move.toIndex(move)); break;
				case Definitions.B_ROOK: b_rooks.addPiece(Move.toIndex(move)); break;
				case Definitions.W_QUEEN: w_queens.addPiece(Move.toIndex(move)); break;
				case Definitions.B_QUEEN: b_queens.addPiece(Move.toIndex(move)); break;
				}
			}
			
			// If it was a promotion, remove the promoted piece from the corresponding list
			// and put back the pawn in the pawns list
			if(Move.moveType(move) >= Definitions.PROMOTION_QUEEN)
			{
				if(toMove == 1) // The move we're unmaking was white promoting
				{
					switch(Move.moveType(move))
					{
					case Definitions.PROMOTION_QUEEN:
						w_queens.removePiece(Move.fromIndex(move));
						w_pawns.addPiece(Move.fromIndex(move));
						
						break;
					case Definitions.PROMOTION_ROOK:
						w_rooks.removePiece(Move.fromIndex(move));
						w_pawns.addPiece(Move.fromIndex(move));
						
						break;
					case Definitions.PROMOTION_BISHOP:
						w_bishops.removePiece(Move.fromIndex(move));
						w_pawns.addPiece(Move.fromIndex(move));
						
						break;
					case Definitions.PROMOTION_KNIGHT:
						w_knights.removePiece(Move.fromIndex(move));
						w_pawns.addPiece(Move.fromIndex(move));
						
						break;					
					}

				}
				else // Black promoted
				{
					switch(Move.moveType(move))
					{
					case Definitions.PROMOTION_QUEEN:
						b_queens.removePiece(Move.fromIndex(move));
						b_pawns.addPiece(Move.fromIndex(move));
						break;
					case Definitions.PROMOTION_ROOK:
						b_rooks.removePiece(Move.fromIndex(move));
						b_pawns.addPiece(Move.fromIndex(move));
						
						break;
					case Definitions.PROMOTION_BISHOP:
						b_bishops.removePiece(Move.fromIndex(move));
						b_pawns.addPiece(Move.fromIndex(move));
						
						break;
					case Definitions.PROMOTION_KNIGHT:
						b_knights.removePiece(Move.fromIndex(move));
						b_pawns.addPiece(Move.fromIndex(move));
						
						break;					
					}

				}
			}
			
			break;
		}

		}
	}
	// END unmakeMove()
	
	/**
	 * Takes a move fromt the killers in the search and checks
	 * if it is possible to play on the board.
	 * 
	 * Killers can't be captures so we do not have to check that
	 * 
	 * @param move The move to verify
	 * @return true if the move can be played, false if not
	 */
	public boolean validateKiller(int move)
	{
		if(move == 0) return false;
		
		int from = Move.fromIndex(move);
		int to = Move.toIndex(move);
		int piece = Move.pieceMoving(move);
		int type = Move.moveType(move);
		
		if(boardArray[from] != piece) return false; // Check if the piece exists on the index
		if(boardArray[to] != Definitions.EMPTY_SQUARE) return false; // Make sure is not a capture
	
		if(toMove == Definitions.WHITE_TO_MOVE)
		{
			if(piece < 0) return false; // Make sure it is the right side moving
			if(piece == Definitions.W_PAWN)
			{
				if(to == from + 16 && boardArray[from+16] == Definitions.EMPTY_SQUARE) return true;
				else if(rank(from) == 1 && to == from + 32 && boardArray[from+16] == Definitions.EMPTY_SQUARE && boardArray[from+32] == Definitions.EMPTY_SQUARE) return true;
				else return false;
			}
			else if(type == Definitions.SHORT_CASTLE)
			{
				if(white_castle == Definitions.CASTLE_SHORT || white_castle == Definitions.CASTLE_BOTH)
				{
					if(boardArray[Definitions.F1] == Definitions.EMPTY_SQUARE && boardArray[Definitions.G1] == Definitions.EMPTY_SQUARE)
					{
						if(!isAttacked(Definitions.E1, Definitions.BLACK) && !isAttacked(Definitions.F1, Definitions.BLACK) && !isAttacked(Definitions.G1, Definitions.BLACK)) return true;
					}
				}				
			}
			else if(type == Definitions.LONG_CASTLE)
			{
				if(white_castle == Definitions.CASTLE_LONG || white_castle == Definitions.CASTLE_BOTH)
				{
					if(boardArray[Definitions.D1] == Definitions.EMPTY_SQUARE && boardArray[Definitions.C1] == Definitions.EMPTY_SQUARE && boardArray[Definitions.B1] == Definitions.EMPTY_SQUARE)
					{
						if(!isAttacked(Definitions.E1, Definitions.BLACK) && !isAttacked(Definitions.D1, Definitions.BLACK) && !isAttacked(Definitions.C1, Definitions.BLACK)) return true;
					}
				}
			}
			else
			{
				if(traverseDelta(from,to)) return true;				
			}
		}
		else // Black to move
		{
			if(piece > 0) return false; // Make sure it is the right side moving
			if(piece == Definitions.B_PAWN)
			{
				if(to == from + 16 && boardArray[from-16] == Definitions.EMPTY_SQUARE) return true;
				else if(rank(from) == 6 && to == from - 32 && boardArray[from-16] == Definitions.EMPTY_SQUARE && boardArray[from-32] == Definitions.EMPTY_SQUARE) return true;
				else return false;
			}
			else if(type == Definitions.SHORT_CASTLE)
			{
				if(black_castle == Definitions.CASTLE_SHORT || black_castle == Definitions.CASTLE_BOTH)
				{
					if(boardArray[Definitions.F8] == Definitions.EMPTY_SQUARE && boardArray[Definitions.G8] == Definitions.EMPTY_SQUARE)
					{
						if(!isAttacked(Definitions.E8, Definitions.WHITE) && !isAttacked(Definitions.F8, Definitions.WHITE) && !isAttacked(Definitions.G8, Definitions.WHITE)) return true;
					}
				}				
			}
			else if(type == Definitions.LONG_CASTLE)
			{
				if(black_castle == Definitions.CASTLE_LONG || black_castle == Definitions.CASTLE_BOTH)
				{
					if(boardArray[Definitions.D8] == Definitions.EMPTY_SQUARE && boardArray[Definitions.C8] == Definitions.EMPTY_SQUARE && boardArray[Definitions.B8] == Definitions.EMPTY_SQUARE)
					{
						if(!isAttacked(Definitions.E8, Definitions.WHITE) && !isAttacked(Definitions.D8, Definitions.WHITE) && !isAttacked(Definitions.C8, Definitions.WHITE)) return true;
					}
				}
			}
			else
			{
				if(traverseDelta(from,to)) return true;				
			}
		}
		return false;
	}
	// END validateKiller()
	
	/**
	 *  Sets the board to the starting position
	 *  rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	 */
	public void setupStart()
	{
		inputFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		Zobrist.getZobristKey(this); // Get the inital zobrist key
	}
	// END setupStart()

	/**
	 *  Returns the current position in FEN-notation
	 *
	 *  @return A string with FEN-notation
	 */
	public String getFEN()
	{
		String fen_string = ""; // This holds the FEN-string

		// ***
		// The following lines adds the pieces and empty squares to the FEN
		// ***

		int index = 112; // Keeps track of the index on the board
		int empties = 0; // Number of empty squares in a row

		while (index >= 0) // Run until end of the real board
		{
			if((index & 0x88) != 0) // Reached the end of a rank
			{
				if(empties != 0)
				{
					fen_string += empties; // Add the empties number if it's not 0
					empties = 0;
				}
				index -= 24; // Jump to the next rank
				if(index >= 0) fen_string += "/"; // Add to mark a new rank, if we're not at the end
			}	      
			else // The index is on the real board
			{
				if(boardArray[index] != Definitions.EMPTY_SQUARE) // If a piece is on the square
					// i.e. the square it not empty
				{
					if(empties != 0) fen_string += empties; // Add the empty square number
					// if it's not 0
					empties = 0; // Reset empties (since we now have a piece coming)
				}

				switch(boardArray[index])
				{
					// Add the piece on the square
					case Definitions.W_KING: fen_string += "K";	break;
					case Definitions.W_QUEEN: fen_string += "Q"; break;
					case Definitions.W_ROOK: fen_string += "R";	break;
					case Definitions.W_BISHOP: fen_string += "B"; break;
					case Definitions.W_KNIGHT: fen_string += "N"; break;
					case Definitions.W_PAWN: fen_string += "P"; break;
					case Definitions.B_KING: fen_string += "k";	break;
					case Definitions.B_QUEEN: fen_string += "q"; break;
					case Definitions.B_ROOK: fen_string += "r"; break;
					case Definitions.B_BISHOP: fen_string += "b"; break;
					case Definitions.B_KNIGHT: fen_string += "n"; break;
					case Definitions.B_PAWN: fen_string += "p";	break;
					default: empties++; // If no piece, increment the empty square count
				}
				index++; // Go to the next square
			}


		}

		// END Adding pieces
		
		fen_string += " "; // Add space for next part

		// Adds side to move (important space before the letter here)
		if(toMove == Definitions.WHITE_TO_MOVE) fen_string += "w"; // White's move
		else fen_string += "b"; // Black's move

		fen_string += " "; // Add space for next part

		// Castling rights
		if(white_castle == Definitions.CASTLE_NONE && black_castle == Definitions.CASTLE_NONE) fen_string += "-"; // Neither
		else // Atleast one side can castle one way
		{
			switch(white_castle) // Check white's castling rights
			{
				case Definitions.CASTLE_SHORT: fen_string += "K"; break;
				case Definitions.CASTLE_LONG: fen_string += "Q"; break;
				case Definitions.CASTLE_BOTH: fen_string += "KQ"; break;
			}

			switch(black_castle) // Check black's castling rights
			{
				case Definitions.CASTLE_SHORT: fen_string += "k"; break;
				case Definitions.CASTLE_LONG: fen_string += "q"; break;
				case Definitions.CASTLE_BOTH: fen_string += "kq"; break;
			}
		}

		fen_string += " "; // Add space for next part

		// En passant square
		
		if(enPassant == -1) fen_string += "-"; // If no en passant is available
		else // An en passant is available
		{
			switch(enPassant%16) // Find the row
			{
				case 0: fen_string += "a"; break;
				case 1: fen_string += "b"; break;
				case 2: fen_string += "c"; break;
				case 3: fen_string += "d"; break;
				case 4: fen_string += "e"; break;
				case 5: fen_string += "f"; break;
				case 6: fen_string += "g"; break;
				case 7: fen_string += "h"; break;
				default: fen_string += "Error in ep square";
			}
			switch((enPassant-(enPassant%16))/16) // Find the rank
			{
				case 2: fen_string += "3"; break;
				case 5: fen_string += "6"; break;
				default: fen_string += "Error in ep square"; // Since en passants only can occur
					 				     // on 3rd and 6th rank, any other
									     // rank is an error
			}
		}

		fen_string += " "; // Add space for next part
		fen_string += movesFifty; // Add half-moves since last capture/pawn move
		fen_string += " ";
		fen_string += movesFull; // Add number of full moves in the game so far

		return fen_string; // Returns the finished FEN-string
	}
	// END getFEN()

	/**
	 *  Takes a FEN-string and sets the board accordingly
	 *
	 * @param fen fen
	 */
	public void inputFEN(String fen)
	{
		historyIndex = 0; // Reset to make sure we start from the beginning
		String trimmedFen = fen.trim(); // Removes any white spaces in front or behind the string
		boardArray = new int[128]; // Empties the board from any pieces
		
		
		for(int i = 0; i <128; i++)
		{
			boardArrayUnique[i] = -1;
		}
		// Reset the piece lists
		this.w_pawns = new PieceList(this);
		this.b_pawns = new PieceList(this);
		this.w_knights = new PieceList(this);
		this.b_knights = new PieceList(this);
		this.w_bishops = new PieceList(this);
		this.b_bishops = new PieceList(this);
		this.w_rooks = new PieceList(this);
		this.b_rooks = new PieceList(this);
		this.w_queens = new PieceList(this);
		this.b_queens = new PieceList(this);
		this.w_king = new PieceList(this);
		this.b_king = new PieceList(this);
		
		String currentChar; // Holds the current character in the fen

		int i = 0; // Used to go through the fen-string character by character

		int boardIndex = 112; // Keeps track of current index on the board (while adding pieces)
				      // Starts at "a8" (index 112) since the fen string starts on this square

		int currentStep = 0; // This will be incremented when a space is detected in the string
				     // 0 - Pieces
				     // 1 - Side to move
				     // 2 - Castling rights
				     // 3 - En passant square
				     // 4 - Half-moves (for 50 move rule) and full moves

		white_castle = Definitions.CASTLE_NONE; // Resetting, will be changed below if castling rights are found
		black_castle = Definitions.CASTLE_NONE;
		boolean fenFinished = false; // Set to true when we're at the end of the fen-string
		while(!fenFinished && i < trimmedFen.length())
		{
			currentChar = trimmedFen.substring(i,i+1); // Gets the current character from the fen-string

			// If a space is detected, get the next character, and move to next step
			if(" ".equals( currentChar )) 
			{
				i++;
				currentChar = trimmedFen.substring(i,i+1);
				currentStep++;
			}


			switch(currentStep) // Determine what step we're on
			{
				case 0: // Pieces
				{
					switch(currentChar.charAt(0)) // See what piece is on the square
					{
						// If character is a '/' move to first row on next rank
						case '/': boardIndex -= 24; break;

						// If the character is a piece, add it and move to next square
						case 'K':
							boardArray[boardIndex] = Definitions.W_KING;
							w_king.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'Q':
							boardArray[boardIndex] = Definitions.W_QUEEN;
							w_queens.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'R':
							boardArray[boardIndex] = Definitions.W_ROOK;
							w_rooks.addPiece(boardIndex);														
							boardIndex++;
							break;
						case 'B':
							boardArray[boardIndex] = Definitions.W_BISHOP;
							w_bishops.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'N':
							boardArray[boardIndex] = Definitions.W_KNIGHT;
							w_knights.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'P':
							boardArray[boardIndex] = Definitions.W_PAWN;
							w_pawns.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'k':
							boardArray[boardIndex] = Definitions.B_KING;
							b_king.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'q':
							boardArray[boardIndex] = Definitions.B_QUEEN;
							b_queens.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'r':
							boardArray[boardIndex] = Definitions.B_ROOK;
							b_rooks.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'b':
							boardArray[boardIndex] = Definitions.B_BISHOP;
							b_bishops.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'n':
							boardArray[boardIndex] = Definitions.B_KNIGHT;
							b_knights.addPiece(boardIndex);
							boardIndex++;
							break;
						case 'p':
							boardArray[boardIndex] = Definitions.B_PAWN;
							b_pawns.addPiece(boardIndex);
							boardIndex++;
							break;

						// If no piece was found, it has to be a number of empty squares
						// so move to that board index
						default: boardIndex += Integer.parseInt(currentChar);
					}
					break;
				}
				case 1: // Side to move
				{
					if("w".equals( currentChar )) toMove = Definitions.WHITE_TO_MOVE;
					else toMove = Definitions.BLACK_TO_MOVE;
					break;
				}
				case 2: // Castling rights
				{
					// '-' states that no castling is available so we simply keep the values
					// we set before the while-loop, and don't need to do anything here.
					switch(currentChar.charAt(0))
					{
						// White can atleast castle short
						case 'K': white_castle = Definitions.CASTLE_SHORT; break;

						case 'Q': // White can atleast castle long
						{
							// If white already can castle short, do both, else only long
							if(white_castle == Definitions.CASTLE_SHORT) white_castle = Definitions.CASTLE_BOTH;
							else white_castle = Definitions.CASTLE_LONG;
							break;
						}
						// Black can atleast castle short
						case 'k': black_castle = Definitions.CASTLE_SHORT; break;

						case 'q': // Black can atleast castle long
						{
							// If black already can castle short, do both, else only long
							if(black_castle == Definitions.CASTLE_SHORT) black_castle = Definitions.CASTLE_BOTH;
							else black_castle = Definitions.CASTLE_LONG;
							break;
						}
					}
					break;					
				}
				case 3: // En passant
				{
					if("-".equals( currentChar )) enPassant = -1;
					else
					{
						switch(currentChar.charAt(0)) // Find the row
						{
							case 'a': enPassant = 0; break;
							case 'b': enPassant = 1; break;
							case 'c': enPassant = 2; break;
							case 'd': enPassant = 3; break;
							case 'e': enPassant = 4; break;
							case 'f': enPassant = 5; break;
							case 'g': enPassant = 6; break;
							case 'h': enPassant = 7; break;
						}
						// Get the next character (the rank)
						i++;
						currentChar = trimmedFen.substring(i,i+1);

						// On rank 3 or else rank 6
						if("3".equals( currentChar )) enPassant += 32; // Add 2 ranks to index
						else enPassant += 80; // Add 5 ranks to index
					}
					break;
				}
				case 4: // Half-moves (50 move rule) and full moves
				{
					// If the next character is a space, we're done with half-moves and
					// can insert them
					if(" ".equals( trimmedFen.substring(i + 1,i + 2) ))
					{
						movesFifty = Integer.parseInt(currentChar);
					}
					// If the next character is not a space, we know it's a number
					// and since half-moves can't be higher than 50 (or it can, but the game
					// is drawn so there's not much point to it), we can assume
					// there are two numbers and then we're done with half-moves.
					else
					{
						movesFifty = Integer.parseInt(trimmedFen.substring(i,i+2));
						i++;
					}
					i += 2;
					movesFull = Integer.parseInt(trimmedFen.substring(i));
					fenFinished = true; // We're done with the fen-string and can exit the loop
					break;
				}
			}
			i++; // Move to the next character in the fen-string
		}		
		zobristKey = Zobrist.getZobristKey(this); // The board is now setup so we can get the inital zobrist key
		pawnZobristKey = Zobrist.getPawnZobristKey(this);
	}
	// END inputFEN()
	
	/**
	 * Takes an integer array and fills it with possible legal moves
	 * starting at the startIndex, if onlyCaptures is true it only
	 * fills the array with captures, else all legal moves
	 * 
	 * @param onlyCaptures Should we only get captures
	 * @param moves The array to fill
	 * @param startIndex The index we start to fill the array from
	 * @return totalMoves The total number of moves that were inserted in the array
	 */
	public int generateMoves(boolean onlyCaptures, int[] moves, int startIndex)
	{
		int totalMoves = 0;
		
		if(onlyCaptures)
		{
			totalMoves = gen_caps(moves, startIndex);
			totalMoves = filterQuis(moves, startIndex, (startIndex + totalMoves));
		}
		else
		{
			totalMoves = gen_caps(moves, startIndex);
			totalMoves += gen_noncaps(moves, (startIndex + totalMoves));
			totalMoves = filterMoves(moves, startIndex, (startIndex + totalMoves));
		}
		
		return totalMoves;		
	}
	// END getLegalMoves()
	
	/**
	 * Filters out the illegal move (leaving king in check etc), also filters
	 * out non-captures
	 * 
	 * Also puts a preliminary value of the capture (used for MVA/LVV)
	 * 
	 * @param pseudoMoves The array to filter
	 * @param startIndex Index we start filtering from
	 * @param endIndex The index we end filtering at
	 * @return totalMoves The total number of moves left when filtering is done
	 */
	private int filterQuis(int[] pseudoMoves, int startIndex, int endIndex)
	{
		int totalMoves = 0;

		int king_square;		
		if(toMove == Definitions.WHITE_TO_MOVE) king_square = w_king.pieces[0];
		else king_square = b_king.pieces[0];

		boolean willBeAdded;
		
		// Now go through every move
		for(int i = startIndex; i < endIndex; i++)
		{
			willBeAdded = false; // Start with assuming the move will not be added
			int currentMove = pseudoMoves[i]; // Get a move from the vector

			if(Move.capture(currentMove) != 0)
			{

				makeMove(currentMove); // Make the move on the board

				// We have already determined the king square, but if the king
				// is moving we need to call isAttacked with its new square
				// and not where it stood before
				if(Move.pieceMoving(currentMove) == Definitions.W_KING || Move.pieceMoving(currentMove) == Definitions.B_KING)
				{
					if(!isAttacked(Move.toIndex(currentMove), toMove))
						willBeAdded = true;
				}
				// If it's not the king moving, we use where it's standing as
				// determined before
				else
				{
					// If the king can't be attacked add the move
					if(!isAttacked(king_square, toMove))
						willBeAdded = true;
				}

				unmakeMove(currentMove); // Unmake the move to reset the board


				if(willBeAdded) // The move passed the checks and will be added
				{

					// See what type of move it is and give it an orderingValue
					// Give capture points
					if(Move.capture(currentMove) != 0) // If the move is a capture
					{
						
						boolean willStillBeAdded = false;
						currentMove = Move.setOrderingValue(currentMove,(See.see(this, currentMove)/100) + 31);
						if(Move.orderingValue(currentMove) - 31 >=0)
						{
							willStillBeAdded = true;
						}
						
						if(willStillBeAdded)
						{
							pseudoMoves[i] = 0; // Empty the spot it had
							pseudoMoves[startIndex + totalMoves] = currentMove; // Move it to its new spot (might be the same)
							totalMoves++;
						}
					}
				}
				else
				{
					pseudoMoves[i] = 0; // Empty the spot since the move is illegal
				}
			}
			else
			{
				pseudoMoves[i] = 0; // Empty the spot since the move is not a capture
			}
			
		}
			

		return totalMoves; // We are now done and can return the filtered moves
	}
	//END filterQuis()
	
	/**
	 * Filters out the illegal move (leaving king in check etc)
	 * 
	 * Also sets a preliminary value if the move is a capture
	 * 
	 * @param pseudoMoves The array to filter
	 * @param startIndex Index we start filtering from
	 * @param endIndex The index we end filtering at
	 * @return totalMoves The total number of moves left when filtering is done
	 */
	private int filterMoves(int[] pseudoMoves, int startIndex, int endIndex)
	{
		
		int totalMoves = 0;

		int king_square;		
		if(toMove == Definitions.WHITE_TO_MOVE) king_square = w_king.pieces[0];
		else king_square = b_king.pieces[0];
		
		boolean willBeAdded;
		// Now go through every move
		for(int i = startIndex; i < endIndex; i++)
		{
			willBeAdded = false; // Start with assuming the move will not be added
			int currentMove = pseudoMoves[i]; // Get a move from the vector

			// If the move is a castle we also need to check for castling in check
			// and over attacked squares, so check the type of move
			switch(Move.moveType(currentMove))
			{
				case Definitions.SHORT_CASTLE:
				{
					makeMove(currentMove); // Make the move on the board

					// Now check if the king's original square or the two to the right
					// are attacked.
					// This catches all three posibilities; castle in check, castle over
					// attacked square and castle into check.
					//
					// If neither is attacked we can add it to legal moves.
					if(!isAttacked(Move.toIndex(currentMove), toMove))
						willBeAdded = true;

					unmakeMove(currentMove); // Unmake the move to reset the board
					break;
				}
				case Definitions.LONG_CASTLE:
				{
					makeMove(currentMove); // Make the move on the board

					// Now check if the king's original square or the two to the left
					// are attacked.
					// This catches all three posibilities; castle in check, castle over
					// attacked square and castle into check.
					//
					// If neither is attacked we can add it to legal moves.
					if(!isAttacked(Move.toIndex(currentMove), toMove))
						willBeAdded = true;

					unmakeMove(currentMove); // Unmake the move to reset the board
					break;
				}				

				default: // Catches all other types of moves
				{
					makeMove(currentMove); // Make the move on the board

					// We have already determined the king square, but if the king
					// is moving we need to call isAttacked with its new square
					// and not where it stood before
					if(Move.pieceMoving(currentMove) == Definitions.W_KING || Move.pieceMoving(currentMove) == Definitions.B_KING)
					{
						if(!isAttacked(Move.toIndex(currentMove), toMove))
							willBeAdded = true;
					}
					// If it's not the king moving, we use where it's standing as
					// determined before
					else
					{
						// If the king can't be attacked add the move
						if(!isAttacked(king_square, toMove))
							willBeAdded = true;
					}

					unmakeMove(currentMove); // Unmake the move to reset the board
				}
			}

			if(willBeAdded) // The move passed the checks and will be added
			{
				// TODO Add non-capture ordering values depending on where the piece moves to
				// use the board in Definitions for this


				// Give capture points
				if(Move.capture(currentMove) != 0) // If the move is a capture
				{
					switch(Math.abs(Move.capture(currentMove)))
					{
					case Definitions.W_QUEEN:
					{
						switch(Math.abs(Move.pieceMoving(currentMove)))
						{
						case Definitions.W_PAWN: currentMove = Move.setOrderingValue(currentMove, 30); break;
						case Definitions.W_KNIGHT: currentMove = Move.setOrderingValue(currentMove, 29); break;
						case Definitions.W_BISHOP: currentMove = Move.setOrderingValue(currentMove, 28); break;
						case Definitions.W_ROOK: currentMove = Move.setOrderingValue(currentMove, 27); break;
						case Definitions.W_QUEEN: currentMove = Move.setOrderingValue(currentMove, 26); break;
						case Definitions.W_KING: currentMove = Move.setOrderingValue(currentMove, 25); break;
						}
						break;
					}
					case Definitions.W_ROOK:
					{
						switch(Math.abs(Move.pieceMoving(currentMove)))
						{
						case Definitions.W_PAWN: currentMove = Move.setOrderingValue(currentMove, 24); break;
						case Definitions.W_KNIGHT: currentMove = Move.setOrderingValue(currentMove, 23); break;
						case Definitions.W_BISHOP: currentMove = Move.setOrderingValue(currentMove, 22); break;
						case Definitions.W_ROOK: currentMove = Move.setOrderingValue(currentMove, 21); break;
						case Definitions.W_QUEEN: currentMove = Move.setOrderingValue(currentMove, 20); break;
						case Definitions.W_KING: currentMove = Move.setOrderingValue(currentMove, 19); break;
						}
						break;
					}
					case Definitions.W_BISHOP:
					{
						switch(Math.abs(Move.pieceMoving(currentMove)))
						{
						case Definitions.W_PAWN: currentMove = Move.setOrderingValue(currentMove, 18); break;
						case Definitions.W_KNIGHT: currentMove = Move.setOrderingValue(currentMove, 17); break;
						case Definitions.W_BISHOP: currentMove = Move.setOrderingValue(currentMove, 16); break;
						case Definitions.W_ROOK: currentMove = Move.setOrderingValue(currentMove, 15); break;
						case Definitions.W_QUEEN: currentMove = Move.setOrderingValue(currentMove, 14); break;
						case Definitions.W_KING: currentMove = Move.setOrderingValue(currentMove, 13); break;
						}
						break;
					}
					case Definitions.W_KNIGHT:
					{
						switch(Math.abs(Move.pieceMoving(currentMove)))
						{
						case Definitions.W_PAWN: currentMove = Move.setOrderingValue(currentMove, 12); break;
						case Definitions.W_KNIGHT: currentMove = Move.setOrderingValue(currentMove, 11); break;
						case Definitions.W_BISHOP: currentMove = Move.setOrderingValue(currentMove, 10); break;
						case Definitions.W_ROOK: currentMove = Move.setOrderingValue(currentMove, 9); break;
						case Definitions.W_QUEEN: currentMove = Move.setOrderingValue(currentMove, 8); break;
						case Definitions.W_KING: currentMove = Move.setOrderingValue(currentMove, 7); break;
						}
						break;
					}
					case Definitions.W_PAWN:
					{
						switch(Math.abs(Move.pieceMoving(currentMove)))
						{
						case Definitions.W_PAWN: currentMove = Move.setOrderingValue(currentMove, 6); break;
						case Definitions.W_KNIGHT: currentMove = Move.setOrderingValue(currentMove, 5); break;
						case Definitions.W_BISHOP: currentMove = Move.setOrderingValue(currentMove, 4); break;
						case Definitions.W_ROOK: currentMove = Move.setOrderingValue(currentMove, 3); break;
						case Definitions.W_QUEEN: currentMove = Move.setOrderingValue(currentMove, 2); break;
						case Definitions.W_KING: currentMove = Move.setOrderingValue(currentMove, 1); break;
						}
						break;
					}					
					}
				}

				pseudoMoves[i] = 0; // Empty the spot it had
				pseudoMoves[startIndex + totalMoves] = currentMove; // Move it to its new spot (might be the same)
				totalMoves++;

			}
			else
			{
				pseudoMoves[i] = 0; // Empty the spot since the move is illegal
			}
		}
			

		return totalMoves; // We are now done and can return the filtered moves
	}
	// END filterMoves()
	


	
	/**
	 *  Takes an index and checks if the index can be attacked by 'side'
	 *
	 *  @param attacked The attacked index
	 *  @param side The side that is attacking (white: 1, black: -1)
	 *  @return boolean True it can be attacked, false it can't
	 */
	public boolean isAttacked(int attacked, int side) // add side here
	{
		int pieceAttack;
		
		if(side == Definitions.WHITE) // White is attacking
		{
			// Pawns, only two possible squares
			if(((attacked-17)& 0x88) == 0 && boardArray[attacked-17] == Definitions.W_PAWN) return true;
			if(((attacked-15)& 0x88) == 0 && boardArray[attacked-15] == Definitions.W_PAWN) return true;
			
			// Knights
			for(int i = 0; i < w_knights.count; i++)
			{
				if(Definitions.ATTACK_ARRAY[attacked - w_knights.pieces[i] +128] == Definitions.ATTACK_N) return true;
			}
			
			// Bishops
			for(int i = 0; i < w_bishops.count; i++)
			{
				pieceAttack = Definitions.ATTACK_ARRAY[attacked - w_bishops.pieces[i] +128];
				if(pieceAttack == Definitions.ATTACK_KQBwP || pieceAttack == Definitions.ATTACK_KQBbP || pieceAttack == Definitions.ATTACK_QB)
				{
					if(traverseDelta(w_bishops.pieces[i],attacked)) return true;
				}
			}
			// Rooks
			for(int i = 0; i < w_rooks.count; i++)
			{
				pieceAttack = Definitions.ATTACK_ARRAY[attacked - w_rooks.pieces[i] +128];
				if(pieceAttack == Definitions.ATTACK_KQR || pieceAttack == Definitions.ATTACK_QR)
				{
					if(traverseDelta(w_rooks.pieces[i],attacked)) return true;
				}
			}
			// Queen
			for(int i = 0; i < w_queens.count; i++)
			{
				pieceAttack = Definitions.ATTACK_ARRAY[attacked - w_queens.pieces[i] +128];
				if(pieceAttack != Definitions.ATTACK_NONE && pieceAttack != Definitions.ATTACK_N)
				{
					if(traverseDelta(w_queens.pieces[i],attacked)) return true;
				}
			}
			// King
			pieceAttack = Definitions.ATTACK_ARRAY[attacked - w_king.pieces[0] +128];
			if(pieceAttack == Definitions.ATTACK_KQBwP || pieceAttack == Definitions.ATTACK_KQBbP || pieceAttack == Definitions.ATTACK_KQR)
			{
				return true;
			}
		}
		else // Black is attacking
		{
			// Pawns, only two possible squares
			// No need for out of bounds checks here since we add to the index (can never get below zero)
			if(((attacked+17)& 0x88) == 0 &&boardArray[attacked+17] == Definitions.B_PAWN) return true;
			if(((attacked+15)& 0x88) == 0 &&boardArray[attacked+15] == Definitions.B_PAWN) return true;
			
			// Knights
			for(int i = 0; i < b_knights.count; i++)
			{
				if(Definitions.ATTACK_ARRAY[attacked - b_knights.pieces[i] +128] == Definitions.ATTACK_N) return true;
			}
			
			// Bishops
			for(int i = 0; i < b_bishops.count; i++)
			{
				pieceAttack = Definitions.ATTACK_ARRAY[attacked - b_bishops.pieces[i] +128];
				if(pieceAttack == Definitions.ATTACK_KQBwP || pieceAttack == Definitions.ATTACK_KQBbP || pieceAttack == Definitions.ATTACK_QB)
				{
					if(traverseDelta(b_bishops.pieces[i],attacked)) return true;
				}
			}
			// Rooks
			for(int i = 0; i < b_rooks.count; i++)
			{
				pieceAttack = Definitions.ATTACK_ARRAY[attacked - b_rooks.pieces[i] +128];
				if(pieceAttack == Definitions.ATTACK_KQR || pieceAttack == Definitions.ATTACK_QR)
				{
					if(traverseDelta(b_rooks.pieces[i],attacked)) return true;
				}
			}
			// Queen
			for(int i = 0; i < b_queens.count; i++)
			{
				pieceAttack = Definitions.ATTACK_ARRAY[attacked - b_queens.pieces[i] +128];
				if(pieceAttack != Definitions.ATTACK_NONE && pieceAttack != Definitions.ATTACK_N)
				{
					if(traverseDelta(b_queens.pieces[i],attacked)) return true;
				}
			}
			// King
			pieceAttack = Definitions.ATTACK_ARRAY[attacked - b_king.pieces[0] +128];
			if(pieceAttack == Definitions.ATTACK_KQBwP || pieceAttack == Definitions.ATTACK_KQBbP || pieceAttack == Definitions.ATTACK_KQR)
			{
				return true;
			}
		}
		return false; // If the loops didn't return true, no piece can attack the square
	}
	// END isAttacked()
	
	/**
	 *  Used by isAttacked() to traverse a piece's delta to
	 *  see if it runs in to any pieces on the way to the attacked square
	 *  
	 *  Important: May not be called with an attacker that can't reach the
	 *  attacked square by its delta. Will cause endless loop. The safety
	 *  measures are commented out below for a small gain in time.
	 *
	 *  @param attacker The attacking square
	 *  @param attacked The attacked square
	 *  @return boolean True if the piece can reach the attacked square, false if not
	 */
	public boolean traverseDelta(int attacker, int attacked)
	{
		int deltaIndex = attacker; // Initialize from first square
		int delta = Definitions.DELTA_ARRAY[attacked - attacker + 128]; // Find the delta needed

		//while((deltaIndex & 0x88) == 0) // Traverse until off the board
		while(true)
		{
			deltaIndex += delta; // Add the delta to move to the next square

			// We reached the attacked square, so we return true
			if(deltaIndex == attacked) return true;

			// A piece was found on the way, so return false
			if(boardArray[deltaIndex] != Definitions.EMPTY_SQUARE) return false;
		}
		//return false; // If we for some reason missed the attacked square and wandered off the board
			      // return false. This shouldn't happen.
	}
	// END traverseDelta()
	
	/**
	 * @param index The index to check
	 * @return The rank the index is located on (index 18 gives (18-(18%16))/16 = rank 1)
	 */
	public final int rank(int index)
	{
		return (index-(index%16))/16;		
	}
	// END rank()
	
	/**
	 * @param index The index to check
	 * @return The row (file) the index is located on (index 18 gives 18%16 = row 2)
	 */
	public final int row(int index)
	{
		return index%16;		
	}
	// END row()
	
	
	/**
	 * Returns the shortest distance between two squares
	 * 
	 * @param squareA
	 * @param squareB
	 * @return distance The distance between the squares
	 */
	public final int distance(int squareA, int squareB)
	{
		int distance = Math.max(Math.abs(row(squareA)-row(squareB)), Math.abs(rank(squareA)-rank(squareB)));
		
		return distance;		
	}
	// END distance()
	
	public int gen_noncaps(int[] moves, int startIndex)
	{
		int totalMovesAdded = 0;
		int from, to;
		int pieceType;
		
		if(toMove == Definitions.WHITE_TO_MOVE)
		{
			// Castling

			// Short, we can assume king and rook are in the right places or else there wouldn't be castling rights
			if(white_castle == Definitions.CASTLE_SHORT || white_castle == Definitions.CASTLE_BOTH)
			{
				// Squares between king and rook needs to be empty
				if((boardArray[Definitions.F1] == Definitions.EMPTY_SQUARE) && (boardArray[Definitions.G1] == Definitions.EMPTY_SQUARE))
				{
					// King and the square that is castled over can't be attacked, castling into check is handled like an ordinary move into check move
					if(!isAttacked(Definitions.E1, Definitions.BLACK) && !isAttacked(Definitions.F1, Definitions.BLACK))
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_KING, Definitions.E1, Definitions.G1, 0, Definitions.SHORT_CASTLE, 0);
						totalMovesAdded++;						
					}
				}				
			}
			
			// Long
			if(white_castle == Definitions.CASTLE_LONG || white_castle == Definitions.CASTLE_BOTH)
			{
				if((boardArray[Definitions.D1] == Definitions.EMPTY_SQUARE) && (boardArray[Definitions.C1] == Definitions.EMPTY_SQUARE) && (boardArray[Definitions.B1]) == Definitions.EMPTY_SQUARE)
				{
				    if(!isAttacked(Definitions.E1, Definitions.BLACK) && !isAttacked(Definitions.D1, Definitions.BLACK))
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_KING, Definitions.E1, Definitions.C1, 0, Definitions.LONG_CASTLE, 0);
						totalMovesAdded++;						
					}
				}				
			}
			
			// Pawns
			for(int i = 0; i < w_pawns.count; i++)
			{
				from = w_pawns.pieces[i]; // Index the current pawn is on

				// TODO: Queen promotions should perhaps be in gen_caps instead

				to = from + 16; // Up
				pieceType = boardArray[to];
				if(pieceType == Definitions.EMPTY_SQUARE) // Pawn can move forward
				{
					if(rank(to) == 7) // Reached the last rank add promotions
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, 0, Definitions.PROMOTION_QUEEN, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, 0, Definitions.PROMOTION_ROOK, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, 0, Definitions.PROMOTION_BISHOP, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, 0, Definitions.PROMOTION_KNIGHT, 0);
						totalMovesAdded++;
					}
					else // Ordinary
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, 0, Definitions.ORDINARY_MOVE, 0);
						totalMovesAdded++;					

						if(rank(from) == 1) // First move by the pawn so it can move two squares as well
						{
							to += 16; // Move another square
							if(boardArray[to] == Definitions.EMPTY_SQUARE) // The square is also empty so we can add it
							{
								moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, 0, Definitions.ORDINARY_MOVE, 0);
								totalMovesAdded++;	
							}
						}
					}

				}
			}
			
			// Knights
			for(int i = 0; i < w_knights.count; i++)
			{
				totalMovesAdded += gen_noncaps_delta(w_knights.pieces[i], Definitions.knight_delta, false, moves, (startIndex + totalMovesAdded));
			}
			// Bishops
			for(int i = 0; i < w_bishops.count;i++)
			{
				totalMovesAdded += gen_noncaps_delta(w_bishops.pieces[i], Definitions.bishop_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Rooks
			for(int i = 0; i < w_rooks.count;i++)
			{
				totalMovesAdded += gen_noncaps_delta(w_rooks.pieces[i], Definitions.rook_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Queen
			for(int i = 0; i < w_queens.count;i++)
			{
				totalMovesAdded += gen_noncaps_delta(w_queens.pieces[i], Definitions.queen_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// King
			totalMovesAdded += gen_noncaps_delta(w_king.pieces[0], Definitions.king_delta, false, moves, (startIndex + totalMovesAdded));
			
		}
		else // Black to move
		{
			// Castling

			// Short, we can assume king and rook are in the right places or else there wouldn't be castling rights
			if(black_castle == Definitions.CASTLE_SHORT || black_castle == Definitions.CASTLE_BOTH)
			{
				// Squares between king and rook needs to be empty
				if((boardArray[Definitions.F8] == Definitions.EMPTY_SQUARE) && (boardArray[Definitions.G8] == Definitions.EMPTY_SQUARE))
				{
					// King and the square that is castled over can't be attacked, castling into check is handled like an ordinary move into check move
					if(!isAttacked(Definitions.E8, Definitions.WHITE) && !isAttacked(Definitions.F8, Definitions.WHITE))
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_KING, Definitions.E8, Definitions.G8, 0, Definitions.SHORT_CASTLE, 0);
						totalMovesAdded++;						
					}
				}				
			}
			
			// Long
			if(black_castle == Definitions.CASTLE_LONG || black_castle == Definitions.CASTLE_BOTH)
			{
				if((boardArray[Definitions.D8] == Definitions.EMPTY_SQUARE) && (boardArray[Definitions.C8] == Definitions.EMPTY_SQUARE) && (boardArray[Definitions.B8]) == Definitions.EMPTY_SQUARE)
				{
					if(!isAttacked(Definitions.E8, Definitions.WHITE) && !isAttacked(Definitions.D8, Definitions.WHITE))
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_KING, Definitions.E8, Definitions.C8, 0, Definitions.LONG_CASTLE, 0);
						totalMovesAdded++;						
					}
				}				
			}
			
			for(int i = 0; i < b_pawns.count; i++)
			{
				from = b_pawns.pieces[i]; // Index the current pawn is on

				to = from - 16; // Down
				pieceType = boardArray[to];
				if(pieceType == Definitions.EMPTY_SQUARE)
				{
					if(rank(to) == 0)
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, 0, Definitions.PROMOTION_QUEEN, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, 0, Definitions.PROMOTION_ROOK, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, 0, Definitions.PROMOTION_BISHOP, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, 0, Definitions.PROMOTION_KNIGHT, 0);
						totalMovesAdded++;
					}
					else // Ordinary capture
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, 0, Definitions.ORDINARY_MOVE, 0);
						totalMovesAdded++;	

						if(rank(from) == 6) // First move by the pawn so it can move two squares as well
						{
							to -= 16; // Move another square
							if(boardArray[to] == Definitions.EMPTY_SQUARE) // The square is also empty so we can add it
							{
								moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, 0, Definitions.ORDINARY_MOVE, 0);
								totalMovesAdded++;	
							}
						}
					}
				}

			}
			// Knights
			for(int i = 0; i < b_knights.count; i++)
			{
				totalMovesAdded += gen_noncaps_delta(b_knights.pieces[i], Definitions.knight_delta, false, moves, (startIndex + totalMovesAdded));
			}
			// Bishops
			for(int i = 0; i < b_bishops.count;i++)
			{
				totalMovesAdded += gen_noncaps_delta(b_bishops.pieces[i], Definitions.bishop_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Rooks
			for(int i = 0; i < b_rooks.count;i++)
			{
				totalMovesAdded += gen_noncaps_delta(b_rooks.pieces[i], Definitions.rook_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Queen
			for(int i = 0; i < b_queens.count;i++)
			{
				totalMovesAdded += gen_noncaps_delta(b_queens.pieces[i], Definitions.queen_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// King
			totalMovesAdded += gen_noncaps_delta(b_king.pieces[0], Definitions.king_delta, false, moves, (startIndex + totalMovesAdded));
		}
		
		return totalMovesAdded;		
	}
	// END gen_noncaps()
	
	/**
	 * Fill the moves array from startIndex with pseudo legal captures
	 * 
	 * @param moves The array to fill
	 * @param startIndex Where to start filling
	 * @return totalMovesAdded The number of captures added
	 */
	public int gen_caps(int[] moves, int startIndex)
	{
		int totalMovesAdded = 0;
		int from, to;
		int pieceType; // Holds the piece type of the index

		if(toMove == Definitions.WHITE_TO_MOVE)
		{

			// Loop through the pawn indexes, if the index does not contain a pawn, the pawn was
			// promoted and we add the moves for that piece instead
			for(int i = 0; i < w_pawns.count; i++)
			{
				from = w_pawns.pieces[i]; // Index the current pawn is on

				// Generate moves for the pawn in the two different capture directions
				// Note: we do not have to check for out of bounds since there will never be
				// anything but 0 (empty square) outside of the board

				// TODO: Queen promotions without capture might belong here so we check them in quiescent search and early in ordinary search

				to = from + 17; // Up right
				if((to & 0x88) == 0)
				{
				pieceType = boardArray[to];
				if(pieceType < 0) // Black piece
				{
					if(rank(to) == 7) // Reached the last rank with the capture so add promotions
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_QUEEN, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_ROOK, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_BISHOP, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_KNIGHT, 0);
						totalMovesAdded++;
					}
					else // Ordinary capture
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.ORDINARY_MOVE, 0);
						totalMovesAdded++;						
					}
				}
				}

				to = from + 15; // Up left
				if((to & 0x88) == 0)
				{
				pieceType = boardArray[to];
				if(pieceType < 0) // Black piece
				{
					if(rank(to) == 7) // Reached the last rank with the capture so add promotions
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_QUEEN, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_ROOK, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_BISHOP, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.PROMOTION_KNIGHT, 0);
						totalMovesAdded++;
					}
					else // Ordinary capture
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, to, pieceType, Definitions.ORDINARY_MOVE, 0);
						totalMovesAdded++;						
					}
				}	
				}
			}
			
			
			// Now add any possible en passant
			if(enPassant != -1 && rank(enPassant) == 5)
			{
				// Check the both squares where an en passant capture is possible from
				from = enPassant - 17;
				if((from & 0x88) == 0)
				{
				if(boardArray[from] == Definitions.W_PAWN)
				{
					moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, enPassant, Definitions.B_PAWN, Definitions.EN_PASSANT, 0);
					totalMovesAdded++;
				}
				}
				
				from = enPassant - 15;
				if((from & 0x88) == 0)
				{
				if(boardArray[from] == Definitions.W_PAWN)
				{
					moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.W_PAWN, from, enPassant, Definitions.B_PAWN, Definitions.EN_PASSANT, 0);
					totalMovesAdded++;
				}
				}
			}
			
			// Knights
			for(int i = 0; i < w_knights.count; i++)
			{
				totalMovesAdded += gen_caps_delta(w_knights.pieces[i], Definitions.knight_delta, false, moves, (startIndex + totalMovesAdded));
			}
			// Bishops
			for(int i = 0; i < w_bishops.count;i++)
			{
				totalMovesAdded += gen_caps_delta(w_bishops.pieces[i], Definitions.bishop_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Rooks
			for(int i = 0; i < w_rooks.count;i++)
			{
				totalMovesAdded += gen_caps_delta(w_rooks.pieces[i], Definitions.rook_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Queen
			for(int i = 0; i < w_queens.count;i++)
			{
				totalMovesAdded += gen_caps_delta(w_queens.pieces[i], Definitions.queen_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// King
			totalMovesAdded += gen_caps_delta(w_king.pieces[0], Definitions.king_delta, false, moves, (startIndex + totalMovesAdded));
		}
		else // Black to move
		{

			// Loop through the pawn indexes, if the index does not contain a pawn, the pawn was
			// promoted and we add the moves for that piece instead
			for(int i = 0; i < b_pawns.count; i++)
			{
				from = b_pawns.pieces[i]; // Index the current pawn is on


				// Generate moves for the pawn in the two different capture directions
				// Note: we do not have to check for out of bounds since there will never be
				// anything but 0 (empty square) outside of the board

				// TODO: Queen promotions without capture might belong here so we check them in quiescent search and early in ordinary search

				to = from - 17; // Down right
				if((to & 0x88) == 0)
				{
				pieceType = boardArray[to];
				if(pieceType > 0) // White piece
				{
					if(rank(to) == 0) // Reached the last rank with the capture so add promotions
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_QUEEN, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_ROOK, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_BISHOP, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_KNIGHT, 0);
						totalMovesAdded++;
					}
					else // Ordinary capture
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.ORDINARY_MOVE, 0);
						totalMovesAdded++;						
					}
				}
				}

				to = from - 15; // Down left
				if((to & 0x88) == 0)
				{
				pieceType = boardArray[to];
				if(pieceType > 0) // White piece
				{
					if(rank(to) == 0) // Reached the last rank with the capture so add promotions
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_QUEEN, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_ROOK, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_BISHOP, 0);
						totalMovesAdded++;
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.PROMOTION_KNIGHT, 0);
						totalMovesAdded++;
					}
					else // Ordinary capture
					{
						moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, to, pieceType, Definitions.ORDINARY_MOVE, 0);
						totalMovesAdded++;						
					}
				}	
				}

			}

			
			// Now add any possible en passant
			if(enPassant != -1 && rank(enPassant) == 2)
			{
				// Check the both squares where an en passant capture is possible from
				from = enPassant + 17;
				if((from & 0x88) == 0)
				{
				if(boardArray[from] == Definitions.B_PAWN)
				{
					moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, enPassant, Definitions.W_PAWN, Definitions.EN_PASSANT, 0);
					totalMovesAdded++;
				}
				}

				from = enPassant + 15;
				if((from & 0x88) == 0)
				{
				if(boardArray[from] == Definitions.B_PAWN)
				{
					moves[startIndex + totalMovesAdded] = Move.createMove(Definitions.B_PAWN, from, enPassant, Definitions.W_PAWN, Definitions.EN_PASSANT, 0);
					totalMovesAdded++;
				}
				}
			}
			// Knights
			for(int i = 0; i < b_knights.count; i++)
			{
				totalMovesAdded += gen_caps_delta(b_knights.pieces[i], Definitions.knight_delta, false, moves, (startIndex + totalMovesAdded));
			}
			// Bishops
			for(int i = 0; i < b_bishops.count;i++)
			{
				totalMovesAdded += gen_caps_delta(b_bishops.pieces[i], Definitions.bishop_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Rooks
			for(int i = 0; i < b_rooks.count;i++)
			{
				totalMovesAdded += gen_caps_delta(b_rooks.pieces[i], Definitions.rook_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// Queen
			for(int i = 0; i < b_queens.count;i++)
			{
				totalMovesAdded += gen_caps_delta(b_queens.pieces[i], Definitions.queen_delta, true, moves, (startIndex + totalMovesAdded));
			}
			// King
			totalMovesAdded += gen_caps_delta(b_king.pieces[0], Definitions.king_delta, false, moves, (startIndex + totalMovesAdded));
		}

		return totalMovesAdded;
	}
	// END gen_caps()
	
	/**
	 *  Takes an index, a delta, sliding/non-sliding boolean and the an array
	 *  and fills the array with all possible non captures for the piece
	 *
	 *  @param index The index the piece is on
	 *  @param delta The piece's delta
	 *  @param sliding Sliding/non-sliding piece
	 *  @param moves The array to be filled
	 *  @param startIndex The index where to start filling the array
	 *  @return totalMovesAdded The number of moves that were added to the array
	 */
	private int gen_noncaps_delta(int index, int[] delta, boolean sliding, int[] moves, int startIndex)
	{
		int totalMovesAdded = 0;
		// Record the board's en passant square, white/black castling rights and half-moves
		for(int i = 0; i < 8; i++) // Loop through the 8 possible deltas
		{
			// Get the index of a square one step away from the orignal square by using the current delta
			int deltaIndex = index;
			deltaIndex += delta[i];
			while(delta[i] != 0) // Run the loop if we have a valid delta
			{
				if((deltaIndex & 0x88) == 0) // Target square is on the board
				{
					if(boardArray[deltaIndex] == Definitions.EMPTY_SQUARE) // The target square is empty
					{
						// Add the non capture
						moves[startIndex + totalMovesAdded] = Move.createMove(boardArray[index], index, deltaIndex, 0, Definitions.ORDINARY_MOVE, 0);
						totalMovesAdded++;
						
						if(!sliding) break; // If not sliding, stop calculating this delta
						deltaIndex += delta[i]; // If the moving piece is sliding, add the next square
					}
					else // The target square is not empty
					{
						break; // Can't go further in this direction
					}
				}
				else break; // We've moved off the board, so stop checking this direction
			}
		}
		return totalMovesAdded;		
	}
	// END gen_noncaps_delta()
	
	/**
	 *  Takes an index, a delta, sliding/non-sliding boolean and the an array
	 *  and fills the array with all possible captures for the piece
	 *
	 *  @param index The index the piece is on
	 *  @param delta The piece's delta
	 *  @param sliding Sliding/non-sliding piece
	 *  @param moves The array to be filled
	 *  @param startIndex The index where to start filling the array
	 *  @return totalMovesAdded The number of moves that were added to the array
	 */
	private int gen_caps_delta(int index, int[] delta, boolean sliding, int[] moves, int startIndex)
	{
		int totalMovesAdded = 0;
		// Record the board's en passant square, white/black castling rights and half-moves
		for(int i = 0; i < 8; i++) // Loop through the 8 possible deltas
		{
			// Get the index of a square one step away from the orignal square by using the current delta
			int deltaIndex = index;
			deltaIndex += delta[i];
			while(delta[i] != 0) // Run the loop if we have a valid delta
			{
				if((deltaIndex & 0x88) == 0) // Target square is on the board
				{
					if(boardArray[deltaIndex] == Definitions.EMPTY_SQUARE) // The target square is empty
					{
						if(!sliding) break; // If not sliding, stop calculating this delta
						deltaIndex += delta[i]; // If the moving piece is sliding, add the next square
					}
					else // The target square is not empty
					{
						if((boardArray[index] * boardArray[deltaIndex]) < 0) // Opposite colored piece
						{
							// Add the capture
							moves[startIndex + totalMovesAdded] = Move.createMove(boardArray[index], index, deltaIndex, boardArray[deltaIndex], Definitions.ORDINARY_MOVE, 0);
							totalMovesAdded++;
						}
						break; // Can't go further in this direction
					}
				}
				else break; // We've moved off the board, so stop checking this direction
			}
		}
		return totalMovesAdded;		
	}
	// END gen_caps_delta()


    //--------------------------------------------------------------------
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board board = (Board) o;

        if (black_castle != board.black_castle) return false;
        if (enPassant != board.enPassant) return false;
        if (historyIndex != board.historyIndex) return false;
        if (movesFifty != board.movesFifty) return false;
        if (movesFull != board.movesFull) return false;
        if (pawnZobristKey != board.pawnZobristKey) return false;
        if (toMove != board.toMove) return false;
        if (white_castle != board.white_castle) return false;
        if (zobristKey != board.zobristKey) return false;
        if (!b_bishops.equals(board.b_bishops)) return false;
        if (!b_king.equals(board.b_king)) return false;
        if (!b_knights.equals(board.b_knights)) return false;
        if (!b_pawns.equals(board.b_pawns)) return false;
        if (!b_queens.equals(board.b_queens)) return false;
        if (!b_rooks.equals(board.b_rooks)) return false;
        if (!Arrays.equals(boardArray, board.boardArray)) return false;
        if (!Arrays.equals(boardArrayUnique, board.boardArrayUnique)) return false;
        if (!Arrays.equals(captureHistory, board.captureHistory)) return false;
        if (!Arrays.equals(history, board.history)) return false;
        if (!Arrays.equals(pawnZobristHistory, board.pawnZobristHistory)) return false;
        if (!w_bishops.equals(board.w_bishops)) return false;
        if (!w_king.equals(board.w_king)) return false;
        if (!w_knights.equals(board.w_knights)) return false;
        if (!w_pawns.equals(board.w_pawns)) return false;
        if (!w_queens.equals(board.w_queens)) return false;
        if (!w_rooks.equals(board.w_rooks)) return false;
        if (!Arrays.equals(zobristHistory, board.zobristHistory)) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = Arrays.hashCode(boardArray);
        result = 31 * result + Arrays.hashCode(boardArrayUnique);
        result = 31 * result + toMove;
        result = 31 * result + enPassant;
        result = 31 * result + white_castle;
        result = 31 * result + black_castle;
        result = 31 * result + movesFifty;
        result = 31 * result + movesFull;
        result = 31 * result + Arrays.hashCode(history);
        result = 31 * result + Arrays.hashCode(captureHistory);
        result = 31 * result + Arrays.hashCode(zobristHistory);
        result = 31 * result + Arrays.hashCode(pawnZobristHistory);
        result = 31 * result + historyIndex;
        result = 31 * result + (int) (zobristKey ^ (zobristKey >>> 32));
        result = 31 * result + (int) (pawnZobristKey ^ (pawnZobristKey >>> 32));
        result = 31 * result + w_pawns.hashCode();
        result = 31 * result + b_pawns.hashCode();
        result = 31 * result + w_knights.hashCode();
        result = 31 * result + b_knights.hashCode();
        result = 31 * result + w_bishops.hashCode();
        result = 31 * result + b_bishops.hashCode();
        result = 31 * result + w_rooks.hashCode();
        result = 31 * result + b_rooks.hashCode();
        result = 31 * result + w_queens.hashCode();
        result = 31 * result + b_queens.hashCode();
        result = 31 * result + w_king.hashCode();
        result = 31 * result + b_king.hashCode();
        return result;
    }
    public Position transpositionKey()
    {
//        return zobristKey;
        long result;
//        result = Arrays.hashCode(boardArray);
//        result = 31 * result + Arrays.hashCode(boardArrayUnique);
        result = toMove;
        result = 31 * result + enPassant;
        result = 31 * result + white_castle;
        result = 31 * result + black_castle;
//        result = 31 * result + movesFifty;
        result = 31 * result + movesFull;
        result = 31 * result + zobristKey;
        result = 31 * result + pawnZobristKey;
//        result = 31 * result + w_pawns.hashCode();
//        result = 31 * result + b_pawns.hashCode();
//        result = 31 * result + w_knights.hashCode();
//        result = 31 * result + b_knights.hashCode();
//        result = 31 * result + w_bishops.hashCode();
//        result = 31 * result + b_bishops.hashCode();
//        result = 31 * result + w_rooks.hashCode();
//        result = 31 * result + b_rooks.hashCode();
//        result = 31 * result + w_queens.hashCode();
//        result = 31 * result + b_queens.hashCode();
//        result = 31 * result + w_king.hashCode();
//        result = 31 * result + b_king.hashCode();
        //return result;

        return new Position(Arrays.hashCode(boardArray),
                               zobristKey, pawnZobristKey, movesFifty,
                               result);
    }
}
