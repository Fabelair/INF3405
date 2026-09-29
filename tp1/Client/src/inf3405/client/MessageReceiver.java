package inf3405.client;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Ecoute en continu les messages diffuses par le serveur et les affiche
 * (requis 4.2). Execute dans un thread distinct pour permettre la reception
 * en temps reel pendant que l'utilisateur saisit ses propres messages.
 */
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
            // Fin de flux : le serveur a ferme la connexion ou est arrete.
            System.out.println("Connexion au serveur perdue.");
        }
    }
}
