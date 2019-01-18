package ao.chess.v1.ai;

/**
 * in [0, 1]
 */
public class Reward
{
    //--------------------------------------------------------------------
    public static final Reward LOWEST = new Reward(-1, false);


    //--------------------------------------------------------------------
    private final double val;


    //--------------------------------------------------------------------
    public Reward()
    {
        this(0);
    }
    public Reward(double value)
    {
        this(value, true);
    }
    private Reward(double value, boolean check)
    {
        if (check)
        {
            assertZeroToOne(value);
        }
        val = value;
    }


    //--------------------------------------------------------------------
    public Reward plus(Reward addend)
    {
        return new Reward(val + addend.val, false);
    }

    public Reward compliment()
    {
        assertZeroToOne(val);
        return new Reward(1.0 - val);
    }

    public Reward square()
    {
        return new Reward(val * val);
    }


    //--------------------------------------------------------------------
    public boolean greaterThan(Reward reward)
    {
        return val > reward.val;
    }

    public double averagedOver(int visits)
    {
        return val / visits;
    }

    public Reward normalize(int visits)
    {
        return new Reward(averagedOver(visits), true);
    }

    public Reward averageOverTwo()
    {
        return new Reward(averagedOver(2));
    }


    //--------------------------------------------------------------------
    private static void assertZeroToOne(double value)
    {
        assert 0 <= value && value <= 1.0;
    }

    public double value()
    {
        return val;
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return String.valueOf( val );
    }
}