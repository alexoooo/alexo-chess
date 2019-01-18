package ao.chess.v2.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: alex
 * Date: 26-Sep-2009
 * Time: 11:17:57 AM
 */
public class Pool
{
    //--------------------------------------------------------------------
    private Pool() {}


    //--------------------------------------------------------------------
    public static final int             CORES = Math.min(Math.max(
            Runtime.getRuntime().availableProcessors() - 1, 1), 8);

    public static final ExecutorService EXEC  =
            Executors.newFixedThreadPool( CORES );
}
