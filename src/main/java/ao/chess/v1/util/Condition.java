package ao.chess.v1.util;


//--------------------------------------------------------------------
// This code (c) 1998 Allen I. Holub. All rights reserved.
//--------------------------------------------------------------------
// This code may not be distributed by yourself except in binary form,
// incorporated into a Java .class file. You may use this code freely
// for personal purposes, but you may not incorporate it into any
// commercial product without express permission of Allen I. Holub in writing.
//--------------------------------------------------------------------
/**
 * Note that in order to comply with the above license, this
 *  source code cannot be distributed.  I only ever distribute
 *  a compiled JAR of this library.
 */

/**
 *  This class implements a simple "condition variable." The notion
 *  is that a thread waits for some condition to become true.
 *  If the condition is false, then no wait occurs.
 *
 *  Be very careful of nested-monitor-lockout here:
 * <PRE>
 *   class lockout
 *   {  Condition godot = new Condition(false);
 *
 *      synchronized void f()
 *      {
 *          some_code();
 *          godot.waitForTrue();
 *      }
 *
 *      synchronized void set() // Deadlock if another thread is in f()
 *      {   godot.setTrue();
 *      }
 *   }
 * </PRE>
 *  You enter f(), locking the monitor, then block waiting for the
 *  condition to become true. Note that you have not released the
 *  monitor for the "lockout" object. [The only way to set godot true
 *  is to call set(), but you'll block on entry to set() because
 *  the original caller to f() has the monitor containing "lockout"
 *  object.]
 *  <p>Solve the problem by releasing the monitor before waiting:
 * <PRE>
 *   class okay
 *   {  Condition godot = new Condition(false);
 *
 *      void f()
 *      {   synchronized( this )
 *          {   some_code();
 *          }
 *          godot.waitForTrue();  // Move the wait outside the monitor
 *      }
 *
 *      synchronized void set()
 *      {   godot.setTrue();
 *      }
 *   }
 * </PRE>
 * or by not synchronizing the `set()` method:
 * <PRE>
 *   class okay
 *   {  Condition godot = new Condition(false);
 *
 *      synchronized void f()
 *      {   some_code();
 *          godot.waitForTrue();
 *      }
 *
 *      void set()              // Remove the synchronized statement
 *      {   godot.setTrue();
 *      }
 *  }
 * </PRE>
 * The normal wait()/notify() mechanism doesn't have this problem since
 * wait() releases the monitor, but you can't always use wait()/notify().
 */


public class Condition
{
    //--------------------------------------------------------------------
    private volatile boolean condition;


    //--------------------------------------------------------------------
    /** Create a new condition variable in a known state.
     * @param isTrue ...
     */
    public Condition( boolean isTrue )
    {
        condition = isTrue;
    }


    //--------------------------------------------------------------------
    /** See if the condition variable is true (without releasing).
     * @return ...
     */
    public synchronized boolean isTrue()
    {
        return condition;
    }


    //--------------------------------------------------------------------
    /** Set the condition to true. Waiting threads are notityed.
     */
    public synchronized void setTrue()
    {
        condition = true;
        notifyAll();
    }


    /** Set the condition to false. Waiting threads are not affected.
     */
    public synchronized void setFalse()
    {
        condition = false;
//        notifyAll();
    }


    //--------------------------------------------------------------------
    /** Release all waiting threads without setting the condition true
     */
    public synchronized void releaseAll()
    {
        notifyAll();
    }


    /** Release one waiting thread without setting the condition true
     */
    public synchronized void releaseOne()
    {
        notify();
    }


    //--------------------------------------------------------------------
    /** Wait for the condition to become true.
     *  @param timeout Timeout in milliseconds
     *  @throws InterruptedException ...
     */
    public synchronized void waitForTrue(
            long timeout) throws InterruptedException
    {
        if( !condition )
        {
            wait( timeout );
        }
    }


    public synchronized void ignorantWaitForTrue()
    {
        try
        {
            waitForTrue();
        }
        catch (InterruptedException interrupted)
        {
            interrupted.printStackTrace();
        }
    }
    /** Wait (potentially forever) for the condition to become true.
     *  @throws InterruptedException ...
     */
    public synchronized void waitForTrue() throws InterruptedException
    {
        waitForTrue(0);
    }


    //--------------------------------------------------------------------
    /** Wait for the condition to become false.
     *  @param timeout Timeout in milliseconds
     *  @throws InterruptedException ...
     */
    public synchronized void waitForFalse(
            long timeout) throws InterruptedException
    {
        if( condition )
        {
            wait( timeout );
        }
    }


    /** Wait (potentially forever) for the condition to become true.
     *  @throws InterruptedException ...
     */
    public synchronized void waitForFalse() throws InterruptedException
    {
        waitForFalse(0);
    }
}


