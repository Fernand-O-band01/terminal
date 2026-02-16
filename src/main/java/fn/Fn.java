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
        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 1. MANEJO DE LA BARRA INVERTIDA (\)
            if (c == '\\') {
                // Caso A: Dentro de comillas simples
                if (inSingleQuotes) {
                    // En '...', la barra NO es especial. Se guarda tal cual.
                    currentArg.append(c);
                }
                // Caso B: Dentro de comillas dobles
                else if (inDoubleQuotes) {
                    // En "...", la barra solo escapa caracteres especiales como \ $ " o newline
                    if (i + 1 < input.length()) {
                        char nextChar = input.charAt(i + 1);
                        if (nextChar == '\\' || nextChar == '"' || nextChar == '$' || nextChar == '\n') {
                            currentArg.append(nextChar);
                            i++; // Saltamos el carácter escapado
                        } else {
                            // Si no es especial, la barra se queda (ej: "\a" -> "\a")
                            currentArg.append(c);
                        }
                    } else {
                        currentArg.append(c);
                    }
                }
                // Caso C: FUERA de comillas (TU ERROR ACTUAL)
                else {
                    // Aquí la barra escapa CUALQUIER COSA (incluyendo el espacio)
                    if (i + 1 < input.length()) {
                        currentArg.append(input.charAt(i + 1));
                        i++; // Importante: saltar el carácter que acabamos de agregar
                    }
                }
            }
            // 2. MANEJO DE COMILLAS
            else if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            }
            else if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            }
            // 3. MANEJO DE ESPACIOS (Separadores)
            else if (c == ' ' && !inSingleQuotes && !inDoubleQuotes) {
                if (currentArg.length() > 0) {
                    args.add(currentArg.toString());
                    currentArg.setLength(0); // Reseteamos el buffer
                }
            }
            // 4. CARACTERES NORMALES
            else {
                currentArg.append(c);
            }
        }

        // Agregar el último argumento si quedó pendiente
        if (currentArg.length() > 0) {
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
        // 1. Separamos el comando de la redirección
        List<String> cleanCommand = new ArrayList<>();
        File outputFile = null;
        boolean redirecting = false;

        for (int i = 0; i < commandWithArgs.length; i++) {
            // Detectamos el operador de redirección
            // (En Shells reales también existe "1>", aquí asumimos ">")
            if (commandWithArgs[i].equals(">") || commandWithArgs[i].equals("1>")) {
                if (i + 1 < commandWithArgs.length) {
                    outputFile = new File(commandWithArgs[i + 1]);
                    redirecting = true;
                    i++; // Saltamos el nombre del archivo para no agregarlo al comando
                }
            } else {
                cleanCommand.add(commandWithArgs[i]);
            }
        }

        // Convertimos la lista limpia de vuelta a Array
        String[] finalCommand = cleanCommand.toArray(new String[0]);

        // Buscamos el ejecutable (usando el nombre limpio, ej: "ls")
        String fullPath = getPath(finalCommand[0]);

        if (fullPath != null) {
            try {
                ProcessBuilder pb = new ProcessBuilder(finalCommand);
                pb.directory(new File(System.getProperty("user.dir"))); // Mantenemos tu lógica de CD

                if (redirecting && outputFile != null) {
                    // CASO REDIRECCIÓN:
                    // 1. stdout va al archivo
                    pb.redirectOutput(outputFile);
                    // 2. stderr se queda en la consola (importante para errores)
                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    // 3. stdin (entrada) no cambia
                    pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                } else {
                    // CASO NORMAL: Todo a la consola
                    pb.inheritIO();
                }

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