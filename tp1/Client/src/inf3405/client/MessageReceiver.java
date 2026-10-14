package inf3405.client;

import java.io.DataInputStream;
import java.io.IOException;

public class MessageReceiver implements Runnable {

    private final DataInputStream entree;

    public MessageReceiver(DataInputStream entree) {
        this.entree = entree;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = entree.readUTF();
                System.out.println(message);
            }
        } catch (IOException e) {
            System.out.println("Connexion au serveur perdue.");
        }
    }
}
