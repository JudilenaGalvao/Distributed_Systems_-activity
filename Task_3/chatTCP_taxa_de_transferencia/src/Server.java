import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static Set<PrintWriter> users = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo user: " + clientSocket);

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                users.add(out);

                Thread clientThread = new Thread(new ClientHandler(clientSocket, out));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final PrintWriter out;
        private BufferedReader in;
        private String userName;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.clientSocket = socket;
            this.out = writer;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("Bem-vindo ao chat!");
                userName = in.readLine();
                transmitMessage("[" + userName + " entrou no chat]");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    transmitMessage("[" + userName + "]: " + inputLine);
                    if (inputLine.equals("/exit")) {
                        break;
                    }
                }

                users.remove(out);
                transmitMessage("[" + userName + " deixou o chat]");
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void transmitMessage(String message) {
            for (PrintWriter user : users) {
                user.println(message);
            }
        }
    }
}