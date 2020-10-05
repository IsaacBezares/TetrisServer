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
            System.out.println("New client request received: ");
            //Obtain input and output streams
            DataInputStream dataIS = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOS = new DataOutputStream(socket.getOutputStream());
            System.out.println("Creating a new handler for this client...");
            System.out.println("Un cliente nuevo se ha unido" + socket);
            ClientHandler client = new ClientHandler(socket, gameCode, dataIS, dataOS);
            Thread thread = new Thread(client);
            System.out.println("Adding this client to active client list...");
            clients.add(client);
            thread.start();
        }
    }
}

class ClientHandler implements Runnable {
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
        while (true) {
            if (!socket.isClosed()) {
                try {
                    received = dataIS.readUTF();
                    System.out.println(received);
                    //Break the string into message and client part
                    StringTokenizer stringToken = new StringTokenizer(received, "/");
                    String messageToSend = stringToken.nextToken();
                    String client = stringToken.nextToken();

                    if (messageToSend.equals("game over")) {
                        for (ClientHandler toSearch : TetrisServer.clients) {
                            if (toSearch.partida.equals(this.partida) &&
                                    !toSearch.socket.getRemoteSocketAddress().toString().equals(this.socket.getRemoteSocketAddress().toString())) { //partida.equals(client)
                                if (!toSearch.isPlaying) {
                                    if (this.score > toSearch.score) {
                                        this.dataOS.writeUTF("YOU WIN");
                                        toSearch.dataOS.writeUTF("YOU LOOSE");
                                    } else {
                                        this.dataOS.writeUTF("YOU LOOSE");
                                        toSearch.dataOS.writeUTF("YOU WIN");
                                    }
                                    this.socket.close();
                                    toSearch.socket.close();
                                }
                            }
                        }
                        this.isPlaying = false;
                        break;
                    } else {
                        score = Integer.parseInt(messageToSend);
                        //Busca al cliente en la lista de clientes conectados
                        for (ClientHandler toSearch : TetrisServer.clients) {
                            System.out.println("Cliente iterado: " + toSearch.partida + " IP: " + toSearch.socket.getRemoteSocketAddress().toString());
                            //Lo encuentra por nombre
                            if (toSearch.partida.equals(this.partida) &&
                                    !toSearch.socket.getRemoteSocketAddress().toString().equals(this.socket.getRemoteSocketAddress().toString())) {
                                //Si lo encuentra, la partida empieza, ambos empiezan a jugar, dejan de estar
                                //esperando oponente y les manda una respuesta ready para que empiecen sus juegos
                                System.out.println("Paso");
                                if (toSearch.isWaitingOponent) {
                                    System.out.println("Paso");
                                    isWaitingOponent = false;
                                    toSearch.isWaitingOponent = false;
                                    isPlaying = true;
                                    toSearch.isPlaying = true;
                                    System.out.println("Paso");
                                    this.dataOS.writeUTF("ready");
                                    toSearch.dataOS.writeUTF("ready");
                                    System.out.println("Paso");
                                } else {
                                    toSearch.dataOS.writeUTF(messageToSend);
                                }
                                //this.partida + " : " + messageToSend
                                break;
                            }
                        }
                        //En el caso de que sea el primero en conectar no hace nada
                        //no deberia haber errores
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
}



