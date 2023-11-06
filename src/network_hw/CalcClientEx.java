package network_hw;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class CalcClientEx {
    private static final String CONFIG_FILE = "server_info.dat"; // 서버 설정 파일 이름
    private static final String DEFAULT_SERVER_IP = "localhost"; // 기본 서버 IP
    private static final int DEFAULT_PORT = 1234; // 기본 서버 포트 번호

    public static void main(String[] args) {
        String serverIp = DEFAULT_SERVER_IP;
        int serverPort = DEFAULT_PORT;

        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (BufferedReader configReader = new BufferedReader(new FileReader(configFile))) {
                String line = configReader.readLine();
                if (line != null && !line.isEmpty()) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        serverIp = parts[0].trim(); // 서버 IP 설정
                        serverPort = Integer.parseInt(parts[1].trim()); // 서버 포트 번호 설정
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading configuration file: " + e.getMessage());
            }
        }

        try (Socket socket = new Socket(serverIp, serverPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the server at " + serverIp + ":" + serverPort);

            while (true) {
                System.out.print("Enter a calculation command (e.g., ADD 2 3) or 'bye' to exit: ");
                String expression = scanner.nextLine();

                out.write(expression + "\n"); // 서버로 메시지 전송
                out.flush();

                if (expression.equalsIgnoreCase("bye")) {
                    System.out.println("Connection closed.");
                    break;
                }

                String result = in.readLine(); // 서버 응답 수신
                System.out.println("Server response: " + result);
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
