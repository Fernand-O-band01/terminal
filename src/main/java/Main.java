import fn.handleType;

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
                handleType.execute(cmdToSearch, builtins);
            }
            else {
                System.out.println(input + ": command not found");
            }
        }
    }

}