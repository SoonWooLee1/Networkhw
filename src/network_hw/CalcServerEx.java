package network_hw;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalcServerEx {
    private static final int PORT = 1234; // 서버 포트 번호
    private static final int THREAD_POOL_SIZE = 5; // 스레드 풀 크기

    public static String calculate(String expression) {
        String[] tokens = expression.split(" "); // 클라이언트에서 수신한 메시지를 공백을 기준으로 토큰화

        String ErrorMessage = null; // 오류 메시지를 저장할 변수

        if (tokens.length < 3) {
            ErrorMessage = "not enough components"; // 토큰의 개수가 3 미만이면 부족한 구성 요소로 처리
            return "Error message: " + ErrorMessage;
        }

        int operand1;
        int operand2;

        try {
            operand1 = Integer.parseInt(tokens[1]); // 피연산자 1 추출
            operand2 = Integer.parseInt(tokens[2]); // 피연산자 2 추출
        } catch (NumberFormatException e) {
            ErrorMessage = "invalid arguments"; // 숫자로 변환할 수 없는 인수로 처리
            return "Error message: " + ErrorMessage;
        }

        if (tokens.length > 3) {
            ErrorMessage = "too many arguments"; // 추가 입력값이 있는 경우 처리
            return "Error message: " + ErrorMessage;
        }

        String operator = tokens[0].toUpperCase(); // 연산자를 대문자로 변환
        int result = 0;

        switch (operator) {
            case "ADD":
                result = operand1 + operand2; // 덧셈 연산
                break;
            case "MIN":
                result = operand1 - operand2; // 뺄셈 연산
                break;
            case "MUL":
                result = operand1 * operand2; // 곱셈 연산
                break;
            case "DIV":
                if (operand2 == 0) {
                    ErrorMessage = "divided by zero"; // 0으로 나누는 경우 처리
                    return "Error message: " + ErrorMessage;
                } else {
                    double divisionResult = (double) operand1 / operand2; // 나눗셈 연산
                    return "Answer: " + divisionResult;
                }
            default:
                ErrorMessage = "unknown operator"; // 알 수 없는 연산자 처리
                return "Error message: " + ErrorMessage;
        }

        return "Answer: " + result; // 연산 결과 반환
    }

    public static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                System.out.println("클라이언트 연결됨.");

                String inputMessage;
                while ((inputMessage = reader.readLine()) != null) {
                    if (inputMessage.equalsIgnoreCase("bye")) {
                        System.out.println("클라이언트에서 연결 종료 요청.");
                        break;
                    }

                    System.out.println("수식 수신: " + inputMessage);
                    String result = calculate(inputMessage);
                    System.out.println("응답: " + result);

                    writer.write(result + "\n"); // 결과를 클라이언트에게 다시 보내기
                    writer.flush();
                }
            } catch (IOException e) {
                System.err.println("클라이언트 오류: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("클라이언트 오류: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // 스레드 풀 생성

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("클라이언트 연결 대기 중...");

            while (true) {
                Socket socket = serverSocket.accept(); // 클라이언트 연결 수락
                threadPool.execute(new ClientHandler(socket)); // 클라이언트 핸들러를 스레드 풀에서 실행
            }
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        } finally {
            threadPool.shutdown(); // 스레드 풀 종료
        }
    }
}



