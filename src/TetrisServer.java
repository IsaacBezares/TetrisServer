import java.io.*;
import java.util.*;
import java.net.*;

public class TetrisServer {
    static Vector<ClientHandler> clients = new Vector<>();

    public static void main(String[] args) throws IOException {
        char[] valueData = new char[4];
        String gameCode = new String();

        for (int j = 0; j < valueData.length; j++) {
            int codigoAscii = (int) Math.floor(Math.random() * (122 - 97) + 97);
            valueData[j] = (char) codigoAscii;
            gameCode = gameCode + valueData[j];
        }
        System.out.println("Codigo de acceso a partida " + gameCode);
        int port = 18341;
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket;
        while (true) {
            socket = serverSocket.accept();
            //Obtain input and output streams
            DataInputStream dataIS = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOS = new DataOutputStream(socket.getOutputStream());
            System.out.println("Un cliente nuevo se ha unido" + socket);
            ClientHandler client = new ClientHandler(socket, gameCode, dataIS, dataOS);
            for (ClientHandler toSearch : TetrisServer.clients) {
                if (toSearch.partida.equals(client.partida)) {
                    toSearch.opponent = client;
                    client.opponent = toSearch.opponent;
                }
            }
            Thread thread = new Thread(client);
            clients.add(client);
            thread.start();
        }
    }
}

class ClientHandler implements Runnable {
    ClientHandler opponent;
    String partida;
    DataInputStream dataIS;
    DataOutputStream dataOS;
    Socket socket;
    int score;
    boolean isPlaying;
    boolean isWaitingOponent;

    //constructor
    public ClientHandler(Socket socket, String partida, DataInputStream dataIS, DataOutputStream dataOS) {
        this.dataIS = dataIS;
        this.dataOS = dataOS;
        this.socket = socket;
        this.partida = partida;
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
            if (!socket.isClosed()) {
                try {
                    received = dataIS.readUTF();
                    System.out.println(received);
                    //Break the string into message and client part
                    StringTokenizer stringToken = new StringTokenizer(received, "/");
                    String messageToSend = stringToken.nextToken();

                    if (messageToSend.equals("game over")) {

                        if (!opponent.isPlaying) {
                            if (this.score > opponent.score) {
                                this.dataOS.writeUTF("YOU WIN");
                                opponent.dataOS.writeUTF("YOU LOSE");

                            } else {
                                this.dataOS.writeUTF("YOU LOSE");
                                opponent.dataOS.writeUTF("YOU WIN");
                            }
                            this.dataIS.close();
                            this.dataOS.close();
                            this.opponent.dataIS.close();
                            this.opponent.dataOS.close();
                            this.socket.close();
                            opponent.socket.close();

                        }
                        this.isPlaying = false;
                        break;
                    } else {
                        score = Integer.parseInt(messageToSend);
                        opponent.dataOS.writeUTF(messageToSend);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            this.dataIS.close();
            this.dataOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



