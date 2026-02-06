import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner scanner = new Scanner(System.in);

        String[] commands = {"echo" , "type", "exit"};

        while (true) {


            System.out.print("$ ");
            String cmnd = scanner.nextLine();
            if (cmnd.equals("exit")) {
                break;
            }

            for (String command : commands){

                if(cmnd.startsWith(commands[0])){
                    System.out.println(cmnd.substring(5).trim());
                    break;
                }

                if (cmnd.startsWith("type ")) {

                    String commandToSearch = cmnd.substring(5).trim();
                    boolean exits = false;

                    for (String c : commands) {
                        if (commandToSearch.equals(c)) {
                            exits = true;
                            break;
                        }
                    }

                    if (exits) {
                        System.out.println(commandToSearch + " is a shell builtin");
                        break;
                    } else {
                        System.out.println(commandToSearch + ": not found");
                        break;
                    }


                }
            }
            

        }

    }

}



