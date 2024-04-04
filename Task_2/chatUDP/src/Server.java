import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static Set<InetSocketAddress> users = new HashSet<>();

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(12345)) {
            System.out.println("Servidor UDP esperando por conexão...");

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                InetSocketAddress clientAddress = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());
                users.add(clientAddress);

                Thread clientHandlerThread = new Thread(new ClientHandler(serverSocket, receivePacket, clientAddress));
                clientHandlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;
        private InetSocketAddress clientAddress;
        private String nomeUsuario;

        public ClientHandler(DatagramSocket socket, DatagramPacket packet, InetSocketAddress address) {
            this.serverSocket = socket;
            this.receivePacket = packet;
            this.clientAddress = address;
        }

        @Override
        public void run() {
            try {
                byte[] sendData;
                String mensagem;

                DatagramPacket sendPacket;

                nomeUsuario = new String(receivePacket.getData(), 0, receivePacket.getLength());

                mensagem = "Bem-vindo ao chat!";
                sendData = mensagem.getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress);
                serverSocket.send(sendPacket);

                mensagem = "[" + nomeUsuario + " entrou no chat]";
                sendData = mensagem.getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress);
                transmitirMensagem(sendPacket);

                while (true) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    String linhaEntrada = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    mensagem = "[" + nomeUsuario + "]: " + linhaEntrada;
                    sendData = mensagem.getBytes();
                    sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress);
                    transmitirMensagem(sendPacket);

                    if (linhaEntrada.equals("/exit")) {
                        break;
                    }
                }

                users.remove(clientAddress);
                mensagem = "[" + nomeUsuario + " deixou o chat]";
                sendData = mensagem.getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length);
                transmitirMensagem(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void transmitirMensagem(DatagramPacket packet) throws IOException {
            for (InetSocketAddress client : users) {
                packet.setSocketAddress(client);
                serverSocket.send(packet);
            }
        }
    }
}
