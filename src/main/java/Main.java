import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String[] builtins = {"echo", "type", "exit", "grep"};

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();

            if (input.equals("exit")) {
                break;
            }

            if (input.startsWith("echo ")) {
                System.out.println(input.substring(5).trim());
            }
            else if (input.startsWith("type ")) {
                String cmdToSearch = input.substring(5).trim();
                handleType(cmdToSearch, builtins);
            }
            else {
                System.out.println(input + ": command not found");
            }
        }
    }

    private static void handleType(String cmd, String[] builtins) {
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