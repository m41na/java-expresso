package works.hop.expresso.web;

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {

        Socket client = new Socket("127.0.0.1", 8888);
        try(BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader bis = new BufferedReader(new InputStreamReader(System.in))){
            String input;
            while((input = bis.readLine()) != null){
                if(input.equals(":quit")){
                    break;
                }
                System.out.printf("client request: %s%n", input);
                //send request to server
                out.write(input + "\n");
                out.flush();
                System.out.println("client request submitted");

                String response = in.readLine();
                System.out.printf("response received: %s%n", response);
            }
        }
    }
}
