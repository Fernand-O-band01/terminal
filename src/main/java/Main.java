import fn.Fn;

import java.util.Scanner;

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
                Fn.handleType(cmdToSearch, builtins);
            }
            else{
                String[] commandWithArgs = input.trim().split("\\s+");

                if (commandWithArgs.length > 0) {
                    // Ejecutamos una sola vez y verificamos el resultado inmediatamente
                    boolean success = Fn.execute(commandWithArgs);

                    if (!Fn.execute(commandWithArgs)) {
                        System.out.println(commandWithArgs[0] + ": command not found");
                    }
                }

            }

        }
    }

}