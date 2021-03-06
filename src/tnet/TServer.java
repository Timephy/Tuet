
package tnet;

import tlist.TListKey;
import tnet.sockets.TSocket;
import tnet.sockets.TServerSocket;
import tnet.communication.TServerCom;

import java.io.IOException;

import java.util.Scanner;

public class TServer {

    private TServerSocket socket;

    private TServerCom com;

    private boolean open = false;

    private TListKey<TSocket, Integer> clients = new TListKey<TSocket, Integer>();

    private boolean writeReadObjectsToAll = false;

    public TServer() {
        com = new TServerCom(this);
    }

    public void open(int port) throws IOException {
        close();

        System.out.println("[TServer] Trying to open on port " + port);

        try {
            socket = new TServerSocket(port);
            open = true;
            System.out.println("[TServer] Opened");
        } catch (IOException e) {
            close();
            System.out.println("[TServer] Open failed!");
            //e.printStackTrace();
            throw e;
        }

    }

    public void close() {
        open = false;
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public boolean acceptClient() {
        try {
            TSocket s = socket.accept();
            if (s != null) {
                clients.add(s);
                System.out.println("[TServer] Client (UID " + s.getUID() + ") connected");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void disconnect(TSocket client) // polite disconnect ("saying goodbye")
    {
        // TODO nice disconnect stuff (message?)
        kick(client);
        System.out.println("[TServer] Disconnected from " + client.getUID());
    }

    public void disconnect(int clientUID) // polite disconnect ("saying goodbye")
    {
        disconnect(clients.getKey(clientUID));
    }

    public void kick(TSocket client) // rude disconnect
    {
        if (client != null) {
            client.close();
            clients.remove(client);
            System.out.println("[TServer] Kicked client (UID " + client.getUID() + ")");
        }
    }

    public void kick(int clientUID) // rude disconnect
    {
        kick(clients.getKey(clientUID));
    }

    public TListKey<TSocket, Integer> getClients() {
        return clients;
    }

    public boolean isOpen() {
        return open;
    }

    public TServerCom getCom() {
        return com;
    }

    public void setWriteReadObjectsToAll(boolean b) {
        writeReadObjectsToAll = b;
    }

    public boolean getWriteReadObjectsToAll() {
        return writeReadObjectsToAll;
    }

    public void setSoTimeout(int timeout) {
        socket.setSoTimeout(timeout);
    }

    public static void main(String[] args) {
        TServer s = new TServer();
        try {
            s.open(8345);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TServerCom c = s.getCom();

        Scanner sc = new Scanner(System.in);

        boolean running = true;

        while (s.isOpen()) {
            String cmd = sc.nextLine();

            if (cmd.equals("stop")) {
                s.close();

            } else if (cmd.equals("close")) {
                s.socket.close();

            } else if (cmd.startsWith("write")) {
                try {
                    s.getCom().write(cmd.substring(6));
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

            } else if (cmd.equals("accept")) {
                System.out.println(s.acceptClient());

            } else if (cmd.equals("count")) {
                System.out.println(s.getClients().length());

            } else if (cmd.startsWith("kick")) {
                s.kick(Integer.parseInt(cmd.substring(5)));

            } else {
                System.out.println("Don't know command: " + cmd);

            }
        }
    }

}
