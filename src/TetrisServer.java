import java.io.*;
import java.util.*;
import java.net.*;

public class TetrisServer {
    static Vector<ClientHandler> clients = new Vector<>(2);

    public static void main(String[] args) throws IOException {
        int port = 18341;
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket;
        while (true) {
            socket = serverSocket.accept();
            //Obtain input and output streams
            DataInputStream dataIS = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOS = new DataOutputStream(socket.getOutputStream());
            System.out.println("Un cliente nuevo se ha unido" + socket.getRemoteSocketAddress().toString());
            ClientHandler client = new ClientHandler(socket, dataIS, dataOS);
            if (!clients.isEmpty()) {
                clients.get(0).opponent = client;
                client.opponent = clients.get(0);
            }
            Thread thread = new Thread(client);
            clients.add(client);
            thread.start();
        }
    }
}

class ClientHandler implements Runnable {
    ClientHandler opponent;
    DataInputStream dataIS;
    DataOutputStream dataOS;
    Socket socket;
    int score;
    boolean isPlaying;
    boolean isWaitingOponent;

    //constructor
    public ClientHandler(Socket socket, DataInputStream dataIS, DataOutputStream dataOS) {
        this.dataIS = dataIS;
        this.dataOS = dataOS;
        this.socket = socket;
        this.isWaitingOponent = true;
    }

    @Override
    public void run() {
        String received;
        if (this.opponent != null) {
            try {
                this.dataOS.writeUTF("ready");
                this.opponent.dataOS.writeUTF("ready");
                this.isPlaying = true;
                this.opponent.isPlaying = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (true) {
            try {
                received = dataIS.readUTF();
                System.out.println("Cliente: " + socket.getRemoteSocketAddress().toString() + "// Mensaje: " + received);
                //Break the string into message and client part

                if (received.equals("game over")) {
                    System.out.println("entro game over");
                    if (!opponent.isPlaying) {
                        if (!opponent.socket.isClosed()) {
                            System.out.println("Entro al verdadero game over");
                            if (this.score > opponent.score) {
                                this.dataOS.writeUTF("YOU WIN");
                                opponent.dataOS.writeUTF("YOU LOSE");

                            } else {
                                this.dataOS.writeUTF("YOU LOSE");
                                opponent.dataOS.writeUTF("YOU WIN");
                            }
                        }
                        this.isPlaying = false;
                        break;
                    }
                    this.isPlaying = false;
                    System.out.println("isPlaying false");
                } else {
                    this.score = Integer.parseInt(received);
                    opponent.dataOS.writeUTF(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            this.dataIS.close();
            this.dataOS.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



