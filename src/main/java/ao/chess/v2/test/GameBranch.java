package ao.chess.v2.test;

import java.util.HashMap;
import java.util.Map;

/**
 * User: alexo
 * Date: Feb 26, 2009
 * Time: 12:40:13 PM
 */
public class GameBranch
{
    //--------------------------------------------------------------------
    private Map<String, GameBranch> map =
            new HashMap<String, GameBranch>();
    private Map<String, String>     annot =
            new HashMap<String, String>();


    //--------------------------------------------------------------------
    public GameBranch add(String fen, String annotation)
    {
        annotate(fen, annotation);
        return add(fen);
    }
    public GameBranch add(String fen)
    {
        String cachedFen = FenCache.GLOBAL.get(fen);

//        System.out.println( fen );
        GameBranch sub = map.get( cachedFen );
        if (sub == null)
        {
            sub = new GameBranch();
            map.put(cachedFen, sub);
        }
        return sub;
    }

    public void annotate(String fen, String annotation)
    {
        String cachedFen = FenCache.GLOBAL.get(fen);
        annot.put(cachedFen, annotation);
    }
    private String annotation(String fen)
    {
        return annot.containsKey(fen)
               ? "    annot: " + annot.get(fen)
               : "";
    }


    //--------------------------------------------------------------------
    public GameBranch minus(GameBranch missing)
    {
        GameBranch delta = new GameBranch();
        delta.annot.putAll( annot );
        Map<String, GameBranch> a =
                new HashMap<String, GameBranch>( map );

        a.keySet().removeAll(
                missing.map.keySet());
        if (! a.isEmpty()) {
            delta.map.putAll( a );
        } else {
            for (String k : map.keySet()) {
                GameBranch subDelta =
                        map.get(k).minus(
                                missing.map.get(k));

                if (! subDelta.map.isEmpty()) {
//                    delta.annot.putAll( subDelta.annot );

                    GameBranch sub = delta.add(k);
                    sub.map  .putAll( subDelta.map );
                    sub.annot.putAll( subDelta.annot );
                }
            }
        }

        return delta;
    }

    @Override public boolean equals(Object obj)
    {
        assert obj instanceof GameBranch;

        GameBranch other = (GameBranch) obj;
        if (! map.keySet().equals( other.map.keySet() )) {
            return false;
        }

        for (String k : map.keySet()) {
            if (! map.get(k).equals( other.map.get(k) )) {
                return false;
            }
        }
        return true;
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return toString(0);
    }

    public String toString(int ply)
    {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, GameBranch> e : map.entrySet()) {
            str.append(pad(ply))
               .append(e.getKey())
               .append(annotation(e.getKey()))
               .append("\n")
               .append(e.getValue().toString(ply + 1));
        }
        return str.toString();
    }
    private String pad(int ply) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < ply; i++) {
            str.append("\t");
        }
        return str.toString();
    }
}
