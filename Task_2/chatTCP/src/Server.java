import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static Set<PrintWriter> users = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket servidorSocket = new ServerSocket(12345)) {
            while (true) {
                Socket socketCliente = servidorSocket.accept();
                System.out.println("Novo user: " + socketCliente);

                PrintWriter out = new PrintWriter(socketCliente.getOutputStream(), true);
                users.add(out);

                Thread threadCliente = new Thread(new ClientHandler(socketCliente, out));
                threadCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socketCliente;
        private PrintWriter out;
        private BufferedReader in;
        private String nomeUsuario;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.socketCliente = socket;
            this.out = writer;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

                out.println("Bem-vindo ao chat!");
                nomeUsuario = in.readLine();
                transmitirMensagem("[" + nomeUsuario + " entrou no chat]");

                String linhaEntrada;
                while ((linhaEntrada = in.readLine()) != null) {
                    transmitirMensagem("[" + nomeUsuario + "]: " + linhaEntrada);
                    if (linhaEntrada.equals("/exit")) {
                        break;
                    }
                }

                users.remove(out);
                transmitirMensagem("[" + nomeUsuario + " deixou o chat]");
                socketCliente.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void transmitirMensagem(String mensagem) {
        for (PrintWriter cliente : users) {
            cliente.println(mensagem);
        }
    }
}
