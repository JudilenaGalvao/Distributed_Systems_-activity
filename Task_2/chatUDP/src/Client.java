import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket();
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
            int serverPort = 12345;

            System.out.print("Insira seu nickname: ");
            String userName = stdIn.readLine();

            byte[] sendData = userName.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);

            Thread receiveThread = new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    while (true) {
                        socket.receive(receivePacket);
                        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println(receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                byte[] sendDataMsg = userInput.getBytes();
                DatagramPacket sendPacketMsg = new DatagramPacket(sendDataMsg, sendDataMsg.length, serverAddress, serverPort);
                socket.send(sendPacketMsg);
                if (userInput.equals("/exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
