package ao.chess.v1.util;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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


    //--------------------------------------------------------------------
    private static final BufferedReader reader =
            new BufferedReader(
                    new InputStreamReader(
                            System.in));


    //--------------------------------------------------------------------
    public static void display(Object text)
    {
        textArea.setText(text + "\n" + textArea.getText());
//        System.out.println(text);
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
