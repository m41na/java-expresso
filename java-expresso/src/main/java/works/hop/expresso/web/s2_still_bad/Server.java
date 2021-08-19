package works.hop.expresso.web.s2_still_bad;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        while (true) {
            System.out.println("Awaiting connection request...");
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> {
                System.out.println("Handling client connection");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Ready to accept requests");
                    String line;
                    while ((line = in.readLine()) != null) {
                        line = line.chars().map(ch -> Character.isLetter(ch) ? (ch ^ ' ') : ch).mapToObj(ch -> ((char) ch + "")).collect(Collectors.joining());
                        //echo back input
                        System.out.printf("response ready: %s%n", line);
                        out.write(line + "\n");
                        out.flush();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).start();
        }
    }
}
