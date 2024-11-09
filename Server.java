import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345; 

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        System.out.println("Server starting...");
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Listening on port " + PORT);

            while (true) { 
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("Server socket closed.");
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());) {

            int numberOfStudents = dis.readInt();
            System.out.println("Number of students: " + numberOfStudents);

            List<Float> totalMarksList = new ArrayList<>();
            for (int i = 0; i < numberOfStudents; i++) {
                int maths = dis.readInt();
                int physics = dis.readInt();
                int chemistry = dis.readInt();

                if (maths < 0 || physics < 0 || chemistry < 0 || maths > 100 || physics > 100 || chemistry > 100) {
                    throw new IllegalArgumentException(
                            "Invalid marks for student " + (i + 1) + ". Each subject should be between 0 and 100.");
                }

                float total = maths + (physics / 2.0f) + (chemistry / 2.0f);
                if (total > 200.0f) {
                    throw new IllegalArgumentException("Total marks for student " + (i + 1) + " exceed 200.");
                }

                totalMarksList.add(total);
            }
            System.out.println("Total marks received: " + totalMarksList);

            int numberOfSeats = dis.readInt();
            System.out.println("Number of seats: " + numberOfSeats);

            float cutoffMark = calculateCutoffMark(totalMarksList, numberOfSeats);
            System.out.println("Calculated cutoff mark: " + cutoffMark);

            dos.writeFloat(cutoffMark);
            dos.flush();
            System.out.println("Cutoff mark sent to client.");

        } catch (IOException e) {
            System.err.println("I/O Error handling client: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Data Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client connection closed.\n");
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private float calculateCutoffMark(List<Float> totalMarks, int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive.");
        }
        if (totalMarks.size() < seats) {
            throw new IllegalArgumentException("Number of seats exceeds number of students.");
        }

        Collections.sort(totalMarks, Collections.reverseOrder());

        float cutoff = totalMarks.get(seats - 1);

        return cutoff;
    }
}
