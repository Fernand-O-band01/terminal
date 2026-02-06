import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner scanner = new Scanner(System.in);
        while (true){

            System.out.print("$ ");
            String cmnd = scanner.nextLine();

            if(cmnd.equals("exit")){
                break;
            }

            System.out.println(cmnd + ": command not found");

        }

    }
}
