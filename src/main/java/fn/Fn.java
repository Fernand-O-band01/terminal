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

            // Verificamos si existe y si se puede ejecutar
            if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
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
        // 1. Buscamos la ruta (getPath ya revisa en todas las carpetas del PATH)
        String fullPath = getPath(commandWithArgs[0]);

        if (fullPath != null) {
            try {
                // REGLA DE ORO: El programa espera que commandWithArgs[0] sea el nombre original
                // o la ruta, pero ProcessBuilder necesita la ruta para arrancar.
                // Para no romper el "Arg #0", creamos una copia o usamos la ruta directamente.

                ProcessBuilder pb = new ProcessBuilder(commandWithArgs);

                // Reemplazamos SOLO para el arranque la ubicación real del archivo
                pb.command().set(0, fullPath);

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