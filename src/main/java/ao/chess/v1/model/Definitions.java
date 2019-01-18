package ao.chess.v1.model;

/**
 * interface Definitions
 *
 * Contains constants used by multiple classes (like values of pieces)
 *
 * @author: Jonatan Pettersson (mediocrechess@gmail.com)
 * Created:	2006-12-14
 * Last update: 2006-12-18
 */

public interface Definitions
{
	// Constants for type of move
	public static final int ORDINARY_MOVE = 0;
	public static final int SHORT_CASTLE = 1;
	public static final int LONG_CASTLE = 2;
	public static final int EN_PASSANT = 3;
	public static final int PROMOTION_QUEEN = 4;
	public static final int PROMOTION_ROOK = 5;
	public static final int PROMOTION_BISHOP = 6;
	public static final int PROMOTION_KNIGHT = 7;
	// END special moves constans

	// Side to move
	public static final int WHITE_TO_MOVE = 1;
	public static final int BLACK_TO_MOVE = -1;
	// END side to move
	
	// Side
	public static final int WHITE = 1;
	public static final int BLACK = -1;
	// END side

	// Constants for squares as they are represented on the 0x88 board
	public static final int A1 = 0;   public static final int A2 = 16;
	public static final int B1 = 1;   public static final int B2 = 17;
	public static final int C1 = 2;   public static final int C2 = 18;
	public static final int D1 = 3;   public static final int D2 = 19;
	public static final int E1 = 4;   public static final int E2 = 20;
	public static final int F1 = 5;   public static final int F2 = 21;
	public static final int G1 = 6;   public static final int G2 = 22;
	public static final int H1 = 7;   public static final int H2 = 23;
	
	public static final int A3 = 32;  public static final int A4 = 48;
	public static final int B3 = 33;  public static final int B4 = 49;
	public static final int C3 = 34;  public static final int C4 = 50;
	public static final int D3 = 35;  public static final int D4 = 51;
	public static final int E3 = 36;  public static final int E4 = 52;
	public static final int F3 = 37;  public static final int F4 = 53;
	public static final int G3 = 38;  public static final int G4 = 54;
	public static final int H3 = 39;  public static final int H4 = 55;
	
	public static final int A5 = 64;  public static final int A6 = 80;
	public static final int B5 = 65;  public static final int B6 = 81;
	public static final int C5 = 66;  public static final int C6 = 82;
	public static final int D5 = 67;  public static final int D6 = 83;
	public static final int E5 = 68;  public static final int E6 = 84;
	public static final int F5 = 69;  public static final int F6 = 85;
	public static final int G5 = 70;  public static final int G6 = 86;
	public static final int H5 = 71;  public static final int H6 = 87;
		
	public static final int A7 = 96;  public static final int A8 = 112;
	public static final int B7 = 97;  public static final int B8 = 113;
	public static final int C7 = 98;  public static final int C8 = 114;
	public static final int D7 = 99;  public static final int D8 = 115;
	public static final int E7 = 100; public static final int E8 = 116;
	public static final int F7 = 101; public static final int F8 = 117;
	public static final int G7 = 102; public static final int G8 = 118;
	public static final int H7 = 103; public static final int H8 = 119;
	// END squares
	

	// Constants for pieces
	public static final int W_KING = 1;
	public static final int W_QUEEN = 2;
	public static final int W_ROOK = 3;
	public static final int W_BISHOP = 4;
	public static final int W_KNIGHT = 5;
	public static final int W_PAWN = 6;

	public static final int B_KING = -1;
	public static final int B_QUEEN = -2;
	public static final int B_ROOK = -3;
	public static final int B_BISHOP = -4;
	public static final int B_KNIGHT = -5;
	public static final int B_PAWN = -6;

	public static final int EMPTY_SQUARE = 0;
	// END piece constants

	// Evaluation constants
	public static final int QUEEN_VALUE = 975;
	public static final int ROOK_VALUE = 500;
	public static final int BISHOP_VALUE = 325;
	public static final int KNIGHT_VALUE = 305;
	public static final int PAWN_VALUE = 70;
	public static final int MATE_VALUE = -31999;
	public static final int MATE_BOUND = 31000;
	public static final int DRAW_VALUE = 0;
	public static final int INFINITY = 32000;
	public static final int EVALNOTFOUND = 32001;
	// END evaluation constants
	
	// Move generation states
	public static final int GEN_HASH = 0;
	public static final int GEN_CAPS = 1;
	public static final int GEN_KILLERS = 2;
	public static final int GEN_NONCAPS = 3;
	public static final int GEN_LOSINGCAPS = 4;
	public static final int GEN_END = 5;
	// END move generation states
	
	// Game phase constans
	public static final int PHASE_OPENING = 0;
	public static final int PHASE_MIDDLE = 1;
	public static final int PHASE_ENDING = 2;
	public static final int PHASE_PAWN_ENDING = 3; // No null-moves in this phase
	// Contempt factor values
	public static final int CONTEMPT_OPENING = 50;
	public static final int CONTEMPT_MIDDLE = 25;
	public static final int CONTEMPT_ENDING = 0;
	// Hashtable constants
	public static final int HASH_EXACT = 0;
	public static final int HASH_ALPHA = 1;
	public static final int HASH_BETA  = 2;
	// END hashtable constants

	// This array is used for fast evaluation of pieces.
	//
	// Take the integer representing the piece + 7 to get the value
	//
	// E.g. Found black queen
	// -2 + 7 = 5 (looking up in the array will give -QUEEN_VALUE = black queen)
	public static final int[] PIECE_VALUE_ARRAY =
	{ 0,			// Not used
		-PAWN_VALUE,		// Black pawn
		-KNIGHT_VALUE,	// Black knight
		-BISHOP_VALUE,	// Black bishop
		-ROOK_VALUE,		// Black rook
		-QUEEN_VALUE,		// Black queen
		0,			// Black king
		0,			// Empty square
		0,			// White king
		QUEEN_VALUE,		// White queen
		ROOK_VALUE,		// White rook
		BISHOP_VALUE,		// White bishop
		KNIGHT_VALUE,		// White knight
		PAWN_VALUE };	  	// White pawn


	// END evaluation constants

	// Constants for castling availability
	public static final int CASTLE_NONE = 0;
	public static final int CASTLE_SHORT = 1;
	public static final int CASTLE_LONG = 2;
	public static final int CASTLE_BOTH = 3;
	// END Castling availability constans

	// Piece deltas
	public static int[] bishop_delta = {-15, -17, 15, 17, 0, 0, 0, 0};
	public static int[] rook_delta = {-1, -16, 1, 16, 0, 0, 0, 0};
	public static int[] queen_delta = {-15, -17, 15, 17, -1, -16, 1, 16};
	public static int[] king_delta = {-15, -17, 15, 17, -1, -16, 1, 16};
	public static int[] knight_delta = {18, 33, 31, 14, -31, -33, -18, -14};
	public static int[] pawn_delta = {16, 32, 17, 15, 0, 0, 0, 0};
	// END piece deltas

	// Attack- and delta-array and constants
	public static int ATTACK_NONE = 0; // Deltas that no piece can move
	public static int ATTACK_KQR = 1; // One square up down left and right
	public static int ATTACK_QR = 2; // More than one square up down left and right
	public static int ATTACK_KQBwP = 3; // One square diagonally up
	public static int ATTACK_KQBbP = 4; // One square diagonally down
	public static int ATTACK_QB = 5; // More than one square diagonally
	public static int ATTACK_N = 6; // Knight moves

	// Formula: attacked_square - attacking_square + 128 = pieces able to attack

	public static final int[] ATTACK_ARRAY =
	{0,0,0,0,0,0,0,0,0,5,0,0,0,0,0,0,2,0,0,0,     //0-19
		0,0,0,5,0,0,5,0,0,0,0,0,2,0,0,0,0,0,5,0,     //20-39
		0,0,0,5,0,0,0,0,2,0,0,0,0,5,0,0,0,0,0,0,     //40-59
		5,0,0,0,2,0,0,0,5,0,0,0,0,0,0,0,0,5,0,0,     //60-79
		2,0,0,5,0,0,0,0,0,0,0,0,0,0,5,6,2,6,5,0,     //80-99
		0,0,0,0,0,0,0,0,0,0,6,4,1,4,6,0,0,0,0,0,     //100-119
		0,2,2,2,2,2,2,1,0,1,2,2,2,2,2,2,0,0,0,0,     //120-139
		0,0,6,3,1,3,6,0,0,0,0,0,0,0,0,0,0,0,5,6,     //140-159
		2,6,5,0,0,0,0,0,0,0,0,0,0,5,0,0,2,0,0,5,     //160-179
		0,0,0,0,0,0,0,0,5,0,0,0,2,0,0,0,5,0,0,0,     //180-199
		0,0,0,5,0,0,0,0,2,0,0,0,0,5,0,0,0,0,5,0,     //200-219
		0,0,0,0,2,0,0,0,0,0,5,0,0,5,0,0,0,0,0,0,     //220-239
		2,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0         }; //240-256


	// Same as attack array but gives the delta needed to get to the square

	public static final int[] DELTA_ARRAY =
	{  0,   0,   0,   0,   0,   0,   0,   0,   0, -17,   0,   0,   0,   0,   0,   0, -16,   0,   0,   0,
		0,   0,   0, -15,   0,   0, -17,   0,   0,   0,   0,   0, -16,   0,   0,   0,   0,   0, -15,   0,
		0,   0,   0, -17,   0,   0,   0,   0, -16,   0,   0,   0,   0, -15,   0,   0,   0,   0,   0,   0,
		-17,   0,   0,   0, -16,   0,   0,   0, -15,   0,   0,   0,   0,   0,   0,   0,   0, -17,   0,   0,
		-16,   0,   0, -15,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0, -17, -33, -16, -31, -15,   0,
		0,   0,   0,   0,   0,   0,   0,   0,   0,   0, -18, -17, -16, -15, -14,   0,   0,   0,   0,   0,
		0,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   0,   1,   1,   1,   1,   1,   1,   1,   0,   0,   0,   0,
		0,   0,  14,  15,  16,  17,  18,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  15,  31,
		16,  33,  17,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  15,   0,   0,  16,   0,   0,  17,
		0,   0,   0,   0,   0,   0,   0,   0,  15,   0,   0,   0,  16,   0,   0,   0,  17,   0,   0,   0,
		0,   0,   0,  15,   0,   0,   0,   0,  16,   0,   0,   0,   0,  17,   0,   0,   0,   0,  15,   0,
		0,   0,   0,   0,  16,   0,   0,   0,   0,   0,  17,   0,   0,  15,   0,   0,   0,   0,   0,   0,
		16,   0,   0,   0,   0,   0,   0,  17,   0,   0,   0,   0,   0,   0,   0,   0,   0};
	// END Attack- and delta-array and constants

	/* The following boards should be read as follow:

	   a1,  b1,  c1,  d1,  e1,  f1,  g1,  h1,    0,0,0,0,0,0,0,0,
	   a2,  b2,  c2,  d2,  e2,  f2,  g2,  h2,    0,0,0,0,0,0,0,0,
	   a3,  b3,  c3,  d3,  e3,  f3,  g3,  h3,    0,0,0,0,0,0,0,0,
	   a4,  b4,  c4,  d4,  e4,  f4,  g4,  h4,    0,0,0,0,0,0,0,0,
	   a5,  b5,  c5,  d5,  e5,  f5,  g5,  h5,    0,0,0,0,0,0,0,0,
	   a6,  b6,  c6,  d6,  e6,  f6,  g6,  h6,    0,0,0,0,0,0,0,0,
	   a7,  b7,  c7,  d7,  e7,  f7,  g7,  h7,    0,0,0,0,0,0,0,0,
	   a8,  b8,  c8,  d8,  e8,  f8,  g8,  h8,    0,0,0,0,0,0,0,0
	*/

	// Positioning of the knights
	public static final int[] W_KNIGHT_POS =
	{		
		-50, -40, -30, -25, -25, -30, -40, -50,		0,0,0,0,0,0,0,0,
		 -35, -25, -15, -10, -10, -15, -25, -35,	0,0,0,0,0,0,0,0,
		 -20, -10,   0,   5,   5,   0, -10, -20,	0,0,0,0,0,0,0,0,
		 -10,   0,  10,  15,  15,  10,   0, -10,	0,0,0,0,0,0,0,0,
		 -5,   5,  15,  20,  20,  15,   5,  -5,		0,0,0,0,0,0,0,0,
		  -5,   5,  15,  20,  20,  15,   5,  -5,	0,0,0,0,0,0,0,0,
		 -20, -10,   0,   5,   5,   0, -10, -20,	0,0,0,0,0,0,0,0,
		 -135, -25, -15, -10, -10, -15, -25,-135,	0,0,0,0,0,0,0,0
	};

	public static final int[] B_KNIGHT_POS =
	{		
		-135, -25, -15, -10, -10, -15, -25,-135,	0,0,0,0,0,0,0,0,
		 -20, -10,   0,   5,   5,   0, -10, -20,	0,0,0,0,0,0,0,0,
		  -5,   5,  15,  20,  20,  15,   5,  -5,	0,0,0,0,0,0,0,0,
		  -5,   5,  15,  20,  20,  15,   5,  -5,	0,0,0,0,0,0,0,0,
		 -10,   0,  10,  15,  15,  10,   0, -10,	0,0,0,0,0,0,0,0,
		 -20, -10,   0,   5,   5,   0, -10, -20,	0,0,0,0,0,0,0,0,
		 -35, -25, -15, -10, -10, -15, -25, -35,	0,0,0,0,0,0,0,0,
		 -50, -40, -30, -25, -25, -30, -40, -50,	0,0,0,0,0,0,0,0
	};

	public static final int[] KNIGHT_POS_ENDING =
	{
		-10,  -5,  -5,  -5,  -5,  -5,  -5, -10,    0,0,0,0,0,0,0,0,
		-5,   0,   0,   0,   0,   0,   0,  -5,    0,0,0,0,0,0,0,0,
		-5,   0,   5,   5,   5,   5,   0,  -5,    0,0,0,0,0,0,0,0,
		-5,   0,   5,  10,  10,   5,   0,  -5,    0,0,0,0,0,0,0,0,
		-5,   0,   5,  10,  10,   5,   0,  -5,    0,0,0,0,0,0,0,0,
		-5,   0,   5,   5,   5,   5,   0,  -5,    0,0,0,0,0,0,0,0,
		-5,   0,   0,   0,   0,   0,   0,  -5,    0,0,0,0,0,0,0,0,
		-10,  -5,  -5,  -5,  -5,  -5,  -5, -10,    0,0,0,0,0,0,0,0
	};
	// END positioning of knights

	// Positioning of the bishops
	public static final int[] W_BISHOP_POS =
	{
	   -20, -15, -15, -13, -13, -15, -15, -20,    0,0,0,0,0,0,0,0,
		-5,   0,  -5,   0,   0,  -5,   0,  -5,    0,0,0,0,0,0,0,0,
		-6,  -2,   4,   2,   2,   4,  -2,  -6,    0,0,0,0,0,0,0,0,
		-4,   0,   2,  10,  10,   2,   0,  -4,    0,0,0,0,0,0,0,0,
		-4,   0,   2,  10,  10,   2,   0,  -4,    0,0,0,0,0,0,0,0,
		-6,  -2,   4,   2,   2,   4,  -2,  -6,    0,0,0,0,0,0,0,0,
		-5,   0,  -2,   0,   0,  -2,   0,  -5,    0,0,0,0,0,0,0,0,
	    -8,  -8,  -6,  -4,  -4,  -6,  -8,  -8,    0,0,0,0,0,0,0,0
	};

	public static final int[] B_BISHOP_POS =
	{
	   -8,  -8,  -6,  -4,  -4,  -6,  -8,  -8,    0,0,0,0,0,0,0,0,
	   -5,   0,  -2,   0,   0,  -2,   0,  -5,    0,0,0,0,0,0,0,0,
	   -6,  -2,   4,   2,   2,   4,  -2,  -6,    0,0,0,0,0,0,0,0,
	   -4,   0,   2,  10,  10,   2,   0,  -4,    0,0,0,0,0,0,0,0,
	   -4,   0,   2,  10,  10,   2,   0,  -4,    0,0,0,0,0,0,0,0,
	   -6,  -2,   4,   2,   2,   4,  -2,  -6,    0,0,0,0,0,0,0,0,
	   -5,   0,  -5,   0,   0,  -5,   0,  -5,    0,0,0,0,0,0,0,0,
	  -20, -15, -15, -13, -13, -15, -15, -20,    0,0,0,0,0,0,0,0
	};
	
	public static final int[] BISHOP_POS_ENDING =
	{
		-18, -12,  -9,  -6,  -6,  -9, -12, -18,   0,0,0,0,0,0,0,0,
		-12,  -6,  -3,   0,   0,  -3,  -6, -12,   0,0,0,0,0,0,0,0,
		 -9,  -3,   0,   3,   3,   0,  -3,  -9,   0,0,0,0,0,0,0,0,
		 -6,   0,   3,   6,   6,   3,   0,  -6,   0,0,0,0,0,0,0,0,
		 -6,   0,   3,   6,   6,   3,   0,  -6,   0,0,0,0,0,0,0,0,
		 -9,  -3,   0,   3,   3,   0,  -3,  -9,   0,0,0,0,0,0,0,0,
		-12,  -6,  -3,   0,   0,  -3,  -6, -12,   0,0,0,0,0,0,0,0,
		-18, -12,  -9,  -6,  -6,  -9, -12, -18,   0,0,0,0,0,0,0,0
	};
	// END positioning of bishops

	public static final int[] W_ROOK_POS =
	{
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0
	};
	
	public static final int[] B_ROOK_POS =
	{
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0,
		-6,   -3,   0,   3,   3,   0,   -3,   -6,    0,0,0,0,0,0,0,0
	};
	
	public static final int[] ROOK_POS_ENDING =
	{
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0
	};
	
	public static final int[] W_QUEEN_POS =
	{
	  -10, -10, -10, -10, -10, -10, -10, -10,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0
	};
	
	public static final int[] B_QUEEN_POS =
	{
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
	  -10, -10, -10, -10, -10, -10, -10, -10,    0,0,0,0,0,0,0,0
	};
	
	public static final int[] QUEEN_POS_ENDING =
	{
	 -24, -16, -12,  -8,  -8, -12, -16, -24,    0,0,0,0,0,0,0,0,
	 -16,  -8,  -4,   0,   0,  -4,  -8, -16,    0,0,0,0,0,0,0,0,
	 -12,  -4,   0,   4,   4,   0,  -4, -12,    0,0,0,0,0,0,0,0,
	  -8,   0,   4,   8,   8,   4,   0,  -8,    0,0,0,0,0,0,0,0,
	  -8,   0,   4,   8,   8,   4,   0,  -8,    0,0,0,0,0,0,0,0,
	 -12,  -4,   0,   4,   4,   0,  -4, -12,    0,0,0,0,0,0,0,0,
	 -16,  -8,  -4,   0,   0,  -4,  -8, -16,    0,0,0,0,0,0,0,0,
	 -24, -16, -12,  -8,  -8, -12, -16, -24,    0,0,0,0,0,0,0,0
	};
	
	/* Positioning of the pawns */
	public static final int[] B_PAWN_POS =
	{
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,  15,  15,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,  25,  25,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,  15,  15,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0
	};

	public static final int[] W_PAWN_POS =
	{
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,  15,  15,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,  25,  25,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,  15,  15,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0,
		-15,   -5,   0,   5,   5,   0,   -5,   -15,    0,0,0,0,0,0,0,0
	};

	/* Positioning of the pawns in the endgame*/
	public static final int[] B_PAWN_POS_ENDING =
	{
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0
	};

	public static final int[] W_PAWN_POS_ENDING =
	{
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0
	};	
	


	// The following two king positions will be used in opening and middle game
	public static final int[] W_KING_POS =
	{
	 	 30,  50,   0,   0,   0,  10,  50,  30,    0,0,0,0,0,0,0,0,
		 20,  40,   0,   0,   0,   0,  40,  20,    0,0,0,0,0,0,0,0,
		-10, -20, -20, -25, -25, -20, -20, -10,    0,0,0,0,0,0,0,0,
		-15, -25, -40, -40, -40, -40, -25, -15,    0,0,0,0,0,0,0,0,
		-30, -40, -40, -40, -40, -40, -40, -30,    0,0,0,0,0,0,0,0,
		-40, -50, -50, -50, -50, -50, -50, -40,    0,0,0,0,0,0,0,0,
		-50, -50, -50, -50, -50, -50, -50, -50,    0,0,0,0,0,0,0,0,
		-50, -50, -50, -50, -50, -50, -50, -50,    0,0,0,0,0,0,0,0
	};	
	
	public static final int[] B_KING_POS =
	{
		-50, -50, -50, -50, -50, -50, -50, -50,    0,0,0,0,0,0,0,0,
		-50, -50, -50, -50, -50, -50, -50, -50,    0,0,0,0,0,0,0,0,
		-40, -50, -50, -50, -50, -50, -50, -40,    0,0,0,0,0,0,0,0,
		-30, -40, -40, -40, -40, -40, -40, -30,    0,0,0,0,0,0,0,0,
		-15, -25, -40, -40, -40, -40, -25, -15,    0,0,0,0,0,0,0,0,
		-10, -20, -20, -25, -25, -20, -20, -10,    0,0,0,0,0,0,0,0,
		 20,  40,   0,   0,   0,   0,  40,  20,    0,0,0,0,0,0,0,0,
		 30,  50,   0,   0,   0,  10,  50,  30,    0,0,0,0,0,0,0,0
		
	};		
	
	// Used to encourage the kings to move to the center in the ending
	public static final int[] KING_POS_ENDING =
	{
		-20, -15, -10, -10, -10, -10, -15, -20,    0,0,0,0,0,0,0,0,
		-15,  -5,   0,   0,   0,   0,  -5, -15,    0,0,0,0,0,0,0,0,
		-10,   0,   5,   5,   5,   5,   0, -10,    0,0,0,0,0,0,0,0,
		-10,   0,   5,  10,  10,   5,   0, -10,    0,0,0,0,0,0,0,0,
		-10,   0,   5,  10,  10,   5,   0, -10,    0,0,0,0,0,0,0,0,
		-10,   0,   5,   5,   5,   5,   0, -10,    0,0,0,0,0,0,0,0,
		-15,  -5,   0,   0,   0,   0,  -5, -15,    0,0,0,0,0,0,0,0,
		-20, -15, -10, -10, -10, -10, -10, -20,    0,0,0,0,0,0,0,0
	};	
	
	// Marks the outpost squares for knight, do not put outpost values
	// on the edges since we check for protecting pawns without checking out of board
	public static final int[] W_KNIGHT_OUTPOST =
	{
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   2,   5,  10,  10,   5,   2,   0,    0,0,0,0,0,0,0,0,
		0,   2,   5,  10,  10,   5,   2,   0,    0,0,0,0,0,0,0,0,
		0,   0,   4,   5,   5,   4,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0
	};	
	
	public static final int[] B_KNIGHT_OUTPOST =
	{
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   4,   5,   5,   4,   0,   0,    0,0,0,0,0,0,0,0,
		0,   2,   5,  10,  10,   5,   2,   0,    0,0,0,0,0,0,0,0,
		0,   2,   5,  10,  10,   5,   2,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0,
		0,   0,   0,   0,   0,   0,   0,   0,    0,0,0,0,0,0,0,0
	};	
}
