package fn;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fn {

    private static Path currentPath = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

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

    public static String[] parseArguments(String input) {
        java.util.List<String> args = new java.util.ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;

        // Este flag sirve para detectar cadenas vacías explícitas como "" o ''
        // Si abrimos comillas, ya cuenta como que "tocamos" el argumento.
        boolean touchedArg = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inSingleQuotes) {
                if (c == '\'') {
                    inSingleQuotes = false; // Cerramos comilla simple
                } else {
                    currentArg.append(c);   // Todo lo de adentro se guarda literal
                }
            } else if (inDoubleQuotes) {
                if (c == '\"') {
                    inDoubleQuotes = false; // Cerramos comilla doble
                } else if (c == '\\') {
                    // (Opcional) Aquí iría la lógica de escapes dentro de dobles comillas
                    // Para este test en específico, a veces hay que manejar \" o \\
                    // Si el test te falla con backslash, avísame para agregar eso.
                    if (i + 1 < input.length()) {
                        char nextChar = input.charAt(i + 1);
                        // Escapar solo caracteres especiales si es necesario
                        currentArg.append(nextChar);
                        i++;
                    } else {
                        currentArg.append(c);
                    }
                } else {
                    currentArg.append(c);
                }
            } else {
                // NO estamos entre comillas
                if (c == '\'') {
                    inSingleQuotes = true;
                    touchedArg = true; // Marcamos que este argumento existe (aunque esté vacío)
                } else if (c == '\"') {
                    inDoubleQuotes = true;
                    touchedArg = true;
                } else if (c == ' ') {
                    // El espacio FUERA de comillas es lo único que separa argumentos
                    if (currentArg.length() > 0 || touchedArg) {
                        args.add(currentArg.toString());
                        currentArg.setLength(0); // Limpiamos para el siguiente
                        touchedArg = false;
                    }
                } else {
                    // Carácter normal (ej: la palabra script o shell)
                    currentArg.append(c);
                    touchedArg = true;
                }
            }
        }

        // Agregar el último argumento si quedó algo pendiente
        if (currentArg.length() > 0 || touchedArg) {
            args.add(currentArg.toString());
        }

        return args.toArray(new String[0]);
    }

    public static boolean echo(String[] command) {
        StringBuilder sb = new StringBuilder();
        // Empezamos en 1 porque el 0 es "echo"
        for (int i = 1; i < command.length; i++) {
            sb.append(command[i]);

            // Solo agregamos espacio si NO es el último argumento
            if (i < command.length - 1) {
                sb.append(" ");
            }
        }
        System.out.println(sb.toString());
        return true;
    }

    public static boolean ChangeDirectory(String[] command){
        if (command.length < 2) return false;

        String target = command[1];
        Path newPath;

        if (target.equals("~")) {
            // Ir al HOME del usuario
            newPath = Paths.get(System.getenv("HOME"));
        } else {
            // Resolver la ruta (funciona para rutas relativas como 'docs' o absolutas como '/tmp')
            newPath = currentPath.resolve(target).normalize();
        }

        // Verificar si la carpeta existe
        if (Files.exists(newPath) && Files.isDirectory(newPath)) {
            currentPath = newPath;
            // Actualizamos la propiedad de Java para que pwd también se entere
            System.setProperty("user.dir", currentPath.toString());
        } else {
            System.out.println("cd: " + target + ": No such file or directory");
        }
        return false;
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
                pb.directory(new java.io.File(System.getProperty("user.dir")));
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