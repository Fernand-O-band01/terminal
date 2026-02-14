import fn.Fn;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        String[] builtins = {"echo", "type", "exit", "grep", "pwd", "cd"};

        while (true) {

            System.out.print("$ ");
            String input = scanner.nextLine();
            String[] commandWithArgs = input.trim().split("\\s+");

            if (input.equals("exit")) {
                break;
            }



            if (input.startsWith("echo ")) {
                System.out.println(input.substring(5).trim());
            }
            else if (input.startsWith("type ")) {
                String cmdToSearch = input.substring(5).trim();
                if(Fn.handleType(cmdToSearch, builtins)){
                }
                else{
                    System.out.println(cmdToSearch + ": not found");
                }
            }else if(input.equals("pwd")){
                Fn.currentDirectory();
            }else if (input.startsWith("cd ")) {
                if(Fn.ChangeDirectory(commandWithArgs)){}
            }
            else {

                if (commandWithArgs.length > 0) {
                    // Ejecutamos UNA SOLA VEZ y guardamos el resultado en 'success'
                    boolean success = Fn.execute(commandWithArgs);

                    // Ahora evaluamos la variable, NO volvemos a llamar a la función
                    if (!success) {
                        System.out.println(commandWithArgs[0] + ": command not found");
                    }
                }
            }

        }
    }

}