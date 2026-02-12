package fn;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Fn {

    // 1. Solo busca en el PATH y devuelve la ruta
    public static String getPath(String cmd) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String[] dirs = pathEnv.split(File.pathSeparator);
            for (String dir : dirs) {
                Path fullPath = Paths.get(dir).resolve(cmd + ".exe");
                if (Files.exists(fullPath) && Files.isExecutable(fullPath)) {
                    return fullPath.toString();
                }
            }
        }
        return null;
    }

    // 2. Lógica para el comando 'type'
    public static boolean handleType(String cmd, String[] builtins) {
        for (String b : builtins) {
            if (cmd.equals(b)) {
                System.out.println(cmd + " is a shell builtin");
                return true;
            }
        }
        String path = getPath(cmd);
        if (path != null) {
            System.out.println(cmd + " is " + path);
            return true;
        }
        return false;
    }

    // 3. Ejecuta el comando externo
    public static boolean execute(String[] command) {
        String commandName = command[0];
        String fullPath = getPath(commandName); // Reutilizamos getPath
        if (fullPath != null) {
            try {
                // Aquí usamos el comando tal cual para el ProcessBuilder
                new ProcessBuilder(command).inheritIO().start().waitFor();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}