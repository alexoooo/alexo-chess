package ao.chess.v1.ai;

import ao.chess.v1.model.Board;

/**
 * 
 */
public class RandomBot implements Bot
{
    //--------------------------------------------------------------------
    public int act(Board board)
    {
        int legalMoves[] = new int[128];
        int moveCount    = board.generateMoves(false, legalMoves, 0);
        return random(legalMoves, moveCount);
    }


    //--------------------------------------------------------------------
    public static int random(int fromMoves[])
    {
        return random(fromMoves, fromMoves.length);
    }
    public static int random(int moves[], int moveCount)
    {
        int    bestMove       = 0;
        double bestMoveWeight = 0;

        for (int i = 0; i < moveCount; i++)
        {
            int move = moves[ i ];
            if (move != 0)
            {
                double weight = Math.random();
                if (bestMoveWeight < weight)
                {
                    bestMove       = move;
                    bestMoveWeight = weight;
                }
            }
        }

        return bestMove;
    }
}
