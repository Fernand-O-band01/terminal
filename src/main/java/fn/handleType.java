package fn;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class handleType {
    public static void execute(String cmd, String[] builtins) {
        for (String b : builtins) {
            if (cmd.equals(b)) {
                System.out.println(cmd + " is a shell builtin");
                return;
            }
        }


        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String[] dirs = pathEnv.split(File.pathSeparator);
            for (String dir : dirs) {
                Path fullPath = Paths.get(dir).resolve(cmd);
                if (Files.exists(fullPath) && Files.isExecutable(fullPath)) {
                    System.out.println(cmd + " is " + fullPath);
                    return;
                }
            }
        }

        System.out.println(cmd + ": not found");
    }
}
