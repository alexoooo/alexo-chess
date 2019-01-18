package ao.chess.v2.test;

import java.util.HashMap;
import java.util.Map;

/**
 * User: alex
 * Date: 13-Sep-2009
 * Time: 5:57:15 PM
 */
public class FenCache
{
    //--------------------------------------------------------------------
    public static final FenCache GLOBAL = new FenCache();


    //--------------------------------------------------------------------
    private final Map<String, String> cache;


    //--------------------------------------------------------------------
    public FenCache()
    {
        cache = new HashMap<String, String>();
    }


    //--------------------------------------------------------------------
    public String get(String fen)
    {
        String existing = cache.get(fen);
        if (existing == null) {
//            return "";
            cache.put(fen, fen);
            existing = fen;
        }
        return existing;
    }
}
