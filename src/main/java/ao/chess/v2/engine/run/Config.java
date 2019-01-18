package ao.chess.v2.engine.run;


import ao.util.io.Dirs;

import java.io.File;

/**
 * User: aostrovsky
 * Date: 18-Oct-2009
 * Time: 11:07:37 PM
 */
public class Config
{
    //--------------------------------------------------------------------
    private Config() {}


    //--------------------------------------------------------------------
    private static String workingDirectory = "";


    //--------------------------------------------------------------------
    public static String workingDirectory() {
        return workingDirectory;
    }

    public static void setWorkingDirectory(String workingDir) {
        workingDirectory = workingDir;
    }


    //--------------------------------------------------------------------
    public static File dir(String relativePath) {
        return Dirs.get(workingDirectory + relativePath);
    }
}
