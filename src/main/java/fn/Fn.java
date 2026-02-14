package fn;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Fn {

    // 1. Solo busca en el PATH y devuelve la ruta
    public static String getPath(String command) {
        String pathEnv = System.getenv("PATH"); // Esto lee /tmp/fox y el resto
        if (pathEnv == null) return null;

        String[] directories = pathEnv.split(File.pathSeparator);
        for (String directory : directories) {
            // Creamos la ruta completa al archivo
            Path fullPath = Paths.get(directory).resolve(command);
            File file = new File(directory, command);

            // Verificamos si existe y si se puede ejecutar
            if (Files.exists(fullPath) && Files.isRegularFile(fullPath) && file.canExecute()) {
                return fullPath.toString();
            }

            // También probamos con .exe por si estás en Windows localmente
            Path fullPathExe = Paths.get(directory).resolve(command + ".exe");
            if (Files.exists(fullPathExe) && Files.isRegularFile(fullPathExe)) {
                return fullPathExe.toString();
            }
        }
        return null;
    }

    public static void currentDirectory() {
        System.out.println(System.getProperty("user.dir"));
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
    public static boolean execute(String[] commandWithArgs) {
        String fullPath = getPath(commandWithArgs[0]);

        if (fullPath != null) {
            try {

                ProcessBuilder pb = new ProcessBuilder(commandWithArgs);
                pb.inheritIO();
                Process process = pb.start();
                process.waitFor();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }



}