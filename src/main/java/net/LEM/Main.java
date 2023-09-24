package net.LEM;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String[] kArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            kArgs[i - 1] = args[i];
        }

        if (args.length == 0) {
            // Handle case when no arguments are provided.
            System.out.println("Usage: java Main [-c|--compile|-i|--install] <filename>");
        } else if ((args[0].equals("-c") || args[0].equals("--compile"))) {
            Compile.main(kArgs);
        } else if ((args[0].equals("-i") || args[0].equals("--install"))) {
            if (kArgs.length > 0 && kArgs[0].endsWith(".lebmod")) {
                oldInstall.main(kArgs);
            } else {
                Install.main(kArgs);
            }
        } else {
            // Handle invalid command.
            System.out.println("Invalid command: " + args[0]);
            System.out.println("Usage: java -jar ToolBox [-c|--compile|-i|--install] <filename>");
        }
    }
}