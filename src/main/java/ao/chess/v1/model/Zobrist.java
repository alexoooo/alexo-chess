package ao.chess.v1.model;

import java.util.Random;

/**
 * Handles the creation and updating of zobrist keys 
 * 
 * @author Jonatan Pettersson (mediocrechess@gmail.com)
 *
 */

public class Zobrist implements Definitions
{
	public static final long[][][] PIECES;
	public static final long[] W_CASTLING_RIGHTS;
	public static final long[] B_CASTLING_RIGHTS;
	public static final long[] EN_PASSANT;
	public static final long SIDE;

	/**
	 * Creates a zobrist key from scratch using the given board
	 * 
	 * @param board The board to create a zobrist key from
	 * @return long The zobrist key for the board
	 */
	public static long getZobristKey(Board board)
	{
		long zobristKey = 0L;


		for(int index = 0; index < 120; index++)
		{
			if((index & 0x88) == 0)
			{
				int piece = board.boardArray[index];

				if(piece > 0) // White piece
				{
					zobristKey ^= PIECES[Math.abs(piece)-1][0][index];
				}
				else if(piece < 0) // Black piece
				{
					zobristKey ^= PIECES[Math.abs(piece)-1][1][index];
				}
			}
			else index += 7;

		}

		zobristKey ^= W_CASTLING_RIGHTS[board.white_castle];
		zobristKey ^= B_CASTLING_RIGHTS[board.black_castle];

		if(board.enPassant != -1) zobristKey ^= EN_PASSANT[board.enPassant];

		if(board.toMove == -1) zobristKey ^= SIDE;


		return zobristKey;
	}
	// END getZobristKey()
	
	/**
	 * Creates a pawn zobrist key from scratch using the given board
	 * 
	 * We're only interested in the position of the pawns, so castling rights etc is not hashed here
	 * 
	 * @param board The board to create a zobrist key from
	 * @return long The zobrist key for the board
	 */
	public static long getPawnZobristKey(Board board)
	{
		long zobristKey = 0L;


		for(int index = 0; index < 120; index++)
		{
			if((index & 0x88) == 0)
			{
				int piece = board.boardArray[index];

				if(piece == W_PAWN) // White piece
				{
					zobristKey ^= PIECES[Math.abs(piece)-1][0][index];
				}
				else if(piece == B_PAWN) // Black piece
				{
					zobristKey ^= PIECES[Math.abs(piece)-1][1][index];
				}
			}
			else index += 7;

		}

		return zobristKey;
	}
	// END getPawnZobristKey()


	/**
	 * The following lines creates the random numbers used
	 * for zobrist key creation
	 */
	static
	{
		Random rnd = new Random(17L);
		SIDE = Math.abs(rnd.nextLong());

		W_CASTLING_RIGHTS = new long[4];
		B_CASTLING_RIGHTS = new long[4];

		for(int i = 0; i < 4; i++)
		{
			W_CASTLING_RIGHTS[i] = Math.abs(rnd.nextLong());
			B_CASTLING_RIGHTS[i] = Math.abs(rnd.nextLong());			
		}


		EN_PASSANT = new long[120];
		PIECES = new long[6][2][120]; // piece, side to move, square

		for(int square = 0; square<120; square++)
		{
			if((square & 0x88) == 0)
			{
				EN_PASSANT[square] = Math.abs(rnd.nextLong());
				for(int piece = 0; piece<6; piece++)
				{
					PIECES[piece][0][square] = Math.abs(rnd.nextLong());
					PIECES[piece][1][square] = Math.abs(rnd.nextLong());
				}
			}
			else square +=7;
		}
	}
}
