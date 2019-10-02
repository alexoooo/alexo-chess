package ao.chess.v1.util;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 *
 */
public class Io
{
    //--------------------------------------------------------------------
    private static final boolean AUTO_DISPLAY = false;


    //--------------------------------------------------------------------
    private static final JTextArea textArea = new JTextArea(25, 50);
    static
    {
//        if (AUTO_DISPLAY)
//        {
            JFrame f = new JFrame();
            JScrollPane contents = new JScrollPane(textArea);
            f.getContentPane().add(contents);
            f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            f.pack();
            f.setVisible(true);
//        }
    }

    private static Deque<String> displayLines = new ConcurrentLinkedDeque<>();


    //--------------------------------------------------------------------
    private static final BufferedReader reader =
            new BufferedReader(
                    new InputStreamReader(
                            System.in));


    //--------------------------------------------------------------------
    public static void display(Object text)
    {
        displayLines.addFirst(text.toString());

        if (displayLines.size() > 1000) {
            displayLines.removeLast();
        }

        SwingUtilities.invokeLater(() ->
                textArea.setText(String.join("\n", displayLines)));
    }


    //--------------------------------------------------------------------
    public static void write(Object line)
    {
        if (AUTO_DISPLAY)
        {
            display( ">>" + line );
        }
        System.out.println( line );
    }


    //--------------------------------------------------------------------
    public static String read()
    {
        try
        {
            String line = reader.readLine();
            if (AUTO_DISPLAY)
            {
                display( "<<" + line );
            }
            return line;
        }
        catch (IOException e)
        {
            throw new Error( e );
        }
    }
}
