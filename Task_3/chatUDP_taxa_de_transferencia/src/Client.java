import java.io.*;
import java.net.*;

public class Client {
    private static final int SERVER_PORT = 12345;
    private static final int NUM_ITERATIONS = 5;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket();
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

            System.out.print("Insira seu nickname: ");
            String userName = stdIn.readLine();

            byte[] sendData = userName.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);

            int[] dataSizes = {1024, 2048, 4096, 8192, 16384, 32768, 65536};

            for (int dataSize : dataSizes) {
                long totalTime = 0;
                for (int i = 0; i < NUM_ITERATIONS; i++) {
                    long startTime = System.nanoTime();

                    byte[] data = new byte[dataSize];
                    DatagramPacket dataPacket = new DatagramPacket(data, data.length, serverAddress, SERVER_PORT);
                    socket.send(dataPacket);

                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);

                    long endTime = System.nanoTime();
                    totalTime += endTime - startTime;
                }

                long averageTime = totalTime / NUM_ITERATIONS;
                double transferRate = calculateTransferRate(dataSize, averageTime);
                System.out.println("Tamanho dos dados: " + dataSize + " bytes - Taxa de transferência: " + transferRate + " bytes/segundo");
            }

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                byte[] sendDataMsg = userInput.getBytes();
                DatagramPacket sendPacketMsg = new DatagramPacket(sendDataMsg, sendDataMsg.length, serverAddress, SERVER_PORT);
                socket.send(sendPacketMsg);
                if (userInput.equals("/exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double calculateTransferRate(int dataSize, long elapsedTime) {
        double elapsedSeconds = elapsedTime / 1_000_000_000.0;
        return dataSize / elapsedSeconds;
    }
}
