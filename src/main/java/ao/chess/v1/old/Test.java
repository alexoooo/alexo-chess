package ao.chess.v1.old;

import ao.chess.v1.model.Board;

import java.io.IOException;
public class Test
{
	public static void main(String[] args) throws IOException
	{
		Board board = new Board();
		board.setupStart();
		
		Engine.search(board, 8, true, 0, 0, 0, true);
		
	}
}
