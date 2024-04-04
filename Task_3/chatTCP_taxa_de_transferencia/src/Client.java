import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Insira seu nickname: ");
            String userName = stdIn.readLine();

            out.println(userName);

            
            int[] dataSizes = {1024, 2048, 4096, 8192, 16384, 32768, 65536};

            for (int dataSize : dataSizes) {
                long startTime = System.nanoTime();

                
                byte[] data = new byte[dataSize];
                out.println(dataSize);
                out.flush();
                socket.getOutputStream().write(data);
                socket.getOutputStream().flush();

                long endTime = System.nanoTime();

                double transferRate = calculateTransferRate(dataSize, startTime, endTime);
                System.out.println("Tamanho dos dados: " + dataSize + " bytes - Taxa de transferência: " + transferRate + " bytes/segundo");
            }

            Thread receiveThread = new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readLine()) != null) {
                        System.out.println(receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equals("/exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double calculateTransferRate(int dataSize, long startTime, long endTime) {
        double elapsedTimeSeconds = (endTime - startTime) / 1_000_000_000.0;
        return dataSize / elapsedTimeSeconds;
    }
}