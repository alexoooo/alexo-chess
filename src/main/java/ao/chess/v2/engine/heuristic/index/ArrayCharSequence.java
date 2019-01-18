package ao.chess.v2.engine.heuristic.index;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 2:27:57 PM
 */
public class ArrayCharSequence implements CharSequence
{
    //--------------------------------------------------------------------
    private char[] DELEGET;


    //--------------------------------------------------------------------
    public ArrayCharSequence(char[] deleget) {
        DELEGET = deleget;
    }


    //--------------------------------------------------------------------
    @Override
    public int length() {
        return DELEGET.length;
    }

    @Override
    public char charAt(int index) {
        return DELEGET[ index ];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return new String(DELEGET);
    }
}
