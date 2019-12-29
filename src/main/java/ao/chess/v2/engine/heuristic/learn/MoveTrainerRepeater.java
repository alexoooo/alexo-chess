package ao.chess.v2.engine.heuristic.learn;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;


public class MoveTrainerRepeater {
    private static final Path repeaterPath = Paths.get("lookup/train/repeater.txt");
    private static final String SUN_JAVA_COMMAND = "sun.java.command";


    public static void main(String[] args) throws IOException {
//        double random = Math.random();
        Files.writeString(repeaterPath,
//                LocalTime.now() + " | " + random + "\n",
                LocalDateTime.now() + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        MoveTrainer.main(args);

        restartApplication(() -> {});
//        if (random < 0.5) {
//            restartApplication(() -> {});
//        }
    }


    /**
     * see: https://dzone.com/articles/programmatically-restart-java
     */
    public static void restartApplication(Runnable runBeforeRestart) {
        // java binary
        String java = System.getProperty("java.home") + "/bin/java";

        // vm arguments
        List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        StringBuilder vmArgsOneLine = new StringBuilder();
        for (String arg : vmArguments) {
            // if it's the agent argument : we ignore it otherwise the
            // address of the old application and the new one will be in conflict
            if (! arg.contains("-agentlib") &&
                    ! arg.contains("-javaagent")) {
                vmArgsOneLine.append(arg);
                vmArgsOneLine.append(" ");
            }
        }
        // init the command to execute, add the vm args
        final StringBuilder cmd = new StringBuilder("\"" + java + "\" " + vmArgsOneLine);

        // program main and program arguments
        String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");

        // program main is a jar
        if (mainCommand[0].endsWith(".jar")) {
            // if it's a jar, add -jar mainJar
            cmd.append("-jar ").append(new File(mainCommand[0]).getPath());
        } else {
            // else it's a .class, add the classpath and mainClass
            cmd.append("-cp \"")
                    .append(System.getProperty("java.class.path"))
                    .append("\" ").append(mainCommand[0]);
        }

        // finally add program arguments
        for (int i = 1; i < mainCommand.length; i++) {
            cmd.append(" ");
            cmd.append(mainCommand[i]);
        }
        String command = cmd.toString();

        // execute the command in a shutdown hook, to be sure that all the
        // resources have been disposed before restarting the application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        // execute some custom code before restarting
        if (runBeforeRestart!= null) {
            runBeforeRestart.run();
        }

        // exit
        System.exit(0);
    }
}
