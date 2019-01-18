package ao.chess.v1.engine;

import ao.chess.v1.model.Board;
import ao.chess.v1.model.Definitions;
import ao.chess.v1.old.Evaluation;
import ao.chess.v1.old.Mediocre;

import java.io.IOException;

/**
 *  class Transposition table
 *
 *  This class holds a hashtable and entrys
 *  
 *  @author Jonatan Pettersson (mediocrechess@gmail.com)
 */
public class TranspositionTable implements Definitions
{
	public int[] hashtable; // Used for transposition table
	public long[] repZobrist; // Used for repetition detection table
	public int HASHSIZE; // The number of slots either table will have
	
	public static final int SLOTS = 6; // 3 for one 'table', 6 for two (two tables means one for depth and one for always replace)
	
	
	// Ordinary transposition table
	public TranspositionTable(int HASHSIZE, int times)
	{
		this.HASHSIZE = HASHSIZE;
		hashtable = new int[HASHSIZE*times];
	}
	
	// Repetition table
	public TranspositionTable(int HASHSIZE, boolean isRep)
	{
		hashtable = null;
		this.HASHSIZE = HASHSIZE;
		repZobrist = new long[HASHSIZE];		
	}
	
	/**
	 * Clears the repetition table
	 */
	public void repClear()
	{
		repZobrist = new long[HASHSIZE];
	}
	// END repClear()
	
	/**
	 * Clears the transposition table
	 */
	public void clear()
	{
		hashtable = new int[HASHSIZE*SLOTS];
	}
	// END clear()
	
	/**
	 * Records a position the repition table, will search through
	 * the table until it finds an empty slot
	 * 
	 * @param zobrist The key to match
	 */
	public void recordRep(long zobrist)
	{
		// TODO: Make this smoother with a better looking for empty places
		
		int hashkey = (int)(zobrist%HASHSIZE);
		
		if(repZobrist[hashkey] == 0)
		{
			repZobrist[hashkey] = zobrist;
			return;
		}
		
		int i = 1;
		int j = 0; // 'Failsafe', do not loop more than the hashsize
		
		while(j < HASHSIZE)
		{
			try
			{
			if(repZobrist[hashkey+i] == 0)
			{
				repZobrist[hashkey+i] = zobrist;
				return;
			}
			}
			catch(ArrayIndexOutOfBoundsException ex)
			{
				hashkey = 0;
				i = 0;
				
			}
			i++;
			j++;
		}
		System.out.println("Error: Repetition table is full");
			
	}
	// END recordRep()
	
	/**
	 * Removes a repetition entry
	 * 
	 * @param zobrist The key to match
	 */
	public void removeRep(long zobrist)
	{
		// TODO: Make this smoother with a better looking for empty places
		
		int hashkey = (int)(zobrist%HASHSIZE);
		
		if(repZobrist[hashkey] == zobrist)
		{
			repZobrist[hashkey] = 0;
			return;
		}
		
		int i = 1;
		int j = 0;
		
		while(j < HASHSIZE)
		{
			try
			{
			if(repZobrist[hashkey+i] == zobrist)
			{
				repZobrist[hashkey+i] = 0;
				return;
			}
			}
			catch(ArrayIndexOutOfBoundsException ex)
			{
				hashkey = 0;
				i = 0;
				
			}
			i++;
			j++;
		}
		System.out.println("Error: Repetition to be removed not found");
			
	}
	// END recordRep()
	
	/**
	 * Checks if the zobrist key exists in the repetition table
	 * will search through the whole array to see if any spot matches
	 * 
	 * TODO: Make this smoother
	 * 
	 * @param zobrist
	 * @return
	 */
	public boolean repExists(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE);
		
		if(repZobrist[hashkey] == 0) return false;
		else if(repZobrist[hashkey] == zobrist) return true;
		
		int i = 1;
		
		while(i < HASHSIZE)
		{
			try
			{
			if(repZobrist[hashkey+i] == 0) return false;
			else if(repZobrist[hashkey+i] == zobrist) return true;
			}
			catch(ArrayIndexOutOfBoundsException ex)
			{
				hashkey = 0;
				i = 0;
				
			}
			i++;
		}
		return false;
	}
	// END repExists()

	/**
	 * Records the entry if the spot is empty or new position has deeper depth
	 * or old position has wrong ancientNodeSwitch
	 * 
	 * @param zobrist
	 * @param depth
	 * @param flag
	 * @param eval
	 * @param move
	 */
	public void record(long zobrist, int depth, int flag, int eval, int move)
	{
		// Depth/new replacement scheme

		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		
		// Spot empty, add the position
		if(hashtable[hashkey] == 0)
		{
            hashtable[hashkey] = 0
				| (eval + 0x1FFFF)
				| ((Mediocre.ancientNodeSwitch + 1) << 18)
				| (flag << 20)
				| (depth << 22);
			hashtable[hashkey+1] = move;
			hashtable[hashkey+2] = (int)(zobrist >> 32);
			return;
		}

		// Spot not empty, add if new position has deeper depth or if it has an old ancientNodeSwitch
		if((hashtable[hashkey] >> 22) <= depth || (((hashtable[hashkey] >> 18) & 3) - 1) != Mediocre.ancientNodeSwitch)
		{
			hashtable[hashkey] = 0
				| (eval + 0x1FFFF)
				| ((Mediocre.ancientNodeSwitch + 1) << 18)
				| (flag << 20)
				| (depth << 22);
			hashtable[hashkey+1] = move;
			hashtable[hashkey+2] = (int)(zobrist >> 32);
			return;
		}
		
		// We could not add the position to the 'depth' part of the table,
		// so add it to the 'new' part no matter what is there
		hashtable[hashkey+3] = 0
			| (eval + 0x1FFFF)
			| ((Mediocre.ancientNodeSwitch + 1) << 18)
			| (flag << 20)
			| (depth << 22);
		hashtable[hashkey+4] = move;
		hashtable[hashkey+5] = (int)(zobrist >> 32);
		
		/*if(hashtable[hashkey+2] == (int)(zobrist >> 32) && hashtable[hashkey+1] == 0)
		{
			hashtable[hashkey+1] = move;
			return;
		}*/
	}
	// END record()
	
	public void recordEval(long zobrist, int eval)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*2;

		hashtable[hashkey] = (eval + 0x1FFFF);
		hashtable[hashkey+1] = (int)(zobrist >> 32);
	}
	// END recordEval()
	
	public int probeEval(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*2;
		if(hashtable[hashkey+1] == ((int)(zobrist >> 32)))
		{
			return (hashtable[hashkey]- 0x1FFFF);
		}
		
		
		return EVALNOTFOUND;
	}
	
	public void recordPawnEval(long zobrist, int evalWhite, int evalBlack, int passers)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*3;

		hashtable[hashkey] = 0 | (evalWhite + 0x3FFF) | ((evalBlack + 0x3FFF) << 16);
		hashtable[hashkey+1] = passers;
		hashtable[hashkey+2] = (int)(zobrist >> 32);
	}
	// END recordEval()
	
	public int probePawnEval(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*3;
		if(hashtable[hashkey+2] == ((int)(zobrist >> 32)))
		{
			Evaluation.passers = hashtable[hashkey+1];
			return (hashtable[hashkey]);
		}
		
		
		return EVALNOTFOUND;
	}
	
	/**
	 * Sets the eval for a certain position in the transposition tables
	 * used for updating the mate evaluation
	 * 
	 * @param zobrist The entry to update
	 * @param eval The new eval
	 */
	public void updateEval(long zobrist, int eval)
	{	
		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		
		// Make sure we have the right entry
		if(hashtable[hashkey+2] == (int)(zobrist >> 32))
		{
			hashtable[hashkey] = hashtable[hashkey] & 0xFFFC0000; // Clear the eval
			hashtable[hashkey] = hashtable[hashkey] | (eval + 0x1FFFF); // Set the eval
		}
		else if(hashtable[hashkey+5] == (int)(zobrist >> 32))
		{
			hashtable[hashkey] = hashtable[hashkey] & 0xFFFC0000; // Clear the eval
			hashtable[hashkey] = hashtable[hashkey] | (eval + 0x1FFFF); // Set the eval
		}
		
	}
	// END updateEval()
	
	/**
	 * Returns true if the entry at the right index is 0
	 * which means we have an entry stored
	 * 
	 * @param zobrist
	 */
	public boolean entryExists(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		if(hashtable[hashkey+2] == (int)(zobrist >> 32) && hashtable[hashkey] != 0) return true;
		else if(hashtable[hashkey+5] == (int)(zobrist >> 32) && hashtable[hashkey+3] != 0) return true;
		else return false;
	}
	//END entryExists()

	/**
	 * Returns the eval at the right index if the zobrist matches
	 * 
	 * @param zobrist
	 */
	public int getEval(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		if(hashtable[hashkey+2] == (int)(zobrist >> 32)) return ((hashtable[hashkey] & 0x3FFFF) - 0x1FFFF);
		else if(hashtable[hashkey+5] == (int)(zobrist >> 32)) return ((hashtable[hashkey+3] & 0x3FFFF) - 0x1FFFF);

		return 0;
	}
	// END getEval()
	
	/**
	 * Returns the flag at the right index if the zobrist matches
	 * 
	 * @param zobrist
	 */
	public int getFlag(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		if(hashtable[hashkey+2] == (int)(zobrist >> 32)) return ((hashtable[hashkey] >> 20) & 3);
		else if(hashtable[hashkey+5] == (int)(zobrist >> 32)) return ((hashtable[hashkey+3] >> 20) & 3);

		return 0;
	}
	// END getFlag()	
	
	/**
	 * Returns the move at the right index if the zobrist matches
	 * 
	 * @param zobrist
	 */
	public int getMove(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		if(hashtable[hashkey+2] == (int)(zobrist >> 32)) return hashtable[hashkey+1];
		else if(hashtable[hashkey+5] == (int)(zobrist >> 32)) return hashtable[hashkey+4];

		return 0;
	}
	// END getMove()
	
	/**
	 * Returns the depth at the right index if the zobrist matches
	 * 
	 * @param zobrist
	 */
	public int getDepth(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		if(hashtable[hashkey+2] == (int)(zobrist >> 32)) return (hashtable[hashkey] >> 22);
		else if(hashtable[hashkey+5] == (int)(zobrist >> 32)) return (hashtable[hashkey+3] >> 22);

		return 0;
	}
	// END getDepth()
	
	/**
	 * Returns the whole int at the right index if the zobrist matches
	 * 
	 * @param zobrist
	 */
	public int getEntry(long zobrist)
	{
		int hashkey = (int)(zobrist%HASHSIZE)*SLOTS;
		if(hashtable[hashkey+2] == (int)(zobrist >> 32)) return (hashtable[hashkey]);
		else if(hashtable[hashkey+5] == (int)(zobrist >> 32)) return (hashtable[hashkey+3]);
		return hashtable[hashkey];
	}
	// END getEntry()
	
	/**
	 *  Collects the principal variation starting from the
	 *  position on the board
	 *  
	 *  @param board The position to collect pv from
	 *  @param current_depth How deep the pv goes (avoids situations where keys point to each other infinitely)
	 *  @return collectString The moves in a string
	 */
	public int[] collectPV(Board board, int current_depth) throws IOException
	{
		int[] arrayPV = new int[128];
		int move = getMove(board.zobristKey);

		//int i = current_depth;
		int i = 20;
		int index = 0;
		while(i > 0)
		{
			if(move == 0)	break;
			arrayPV[index] = move;
			board.makeMove(move);
			move = getMove(board.zobristKey);
			i--;
			index++;
		}
		
		// Unmake the moves		
		for(i = index-1; i >= 0; i--)
		{
			board.unmakeMove(arrayPV[i]);
		}
		return arrayPV;
	}
	// END collectPV()
}
