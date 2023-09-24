package net.LEM;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String[] kArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            kArgs[i - 1] = args[i];
        }

        if ((args[0] == "-c") || (args[0] == "--compile")){
            Compile.main(kArgs);
        } else if ((args[0] == "-i") || (args[0] == "--install")) {
            Install.main(kArgs);
        }
    }


}