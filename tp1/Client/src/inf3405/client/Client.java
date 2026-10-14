package inf3405.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private static final int LONGUEUR_MAX_MESSAGE = 200;

    public static void main(String[] args) {
        Saisie saisie = new Saisie();
        String adresseIP = saisie.lireAdresseIP();
        int port = saisie.lirePort();
        String nomUtilisateur = saisie.lireChamp("Nom d'utilisateur : ");
        String motDePasse = saisie.lireChamp("Mot de passe : ");

        try (Socket socket = new Socket(adresseIP, port)) {
            DataInputStream entree = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream sortie = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            sortie.writeUTF(nomUtilisateur);
            sortie.writeUTF(motDePasse);
            sortie.flush();

            String reponse = entree.readUTF();
            if (!reponse.equals("OK")) {
                String raison = entree.readUTF();
                System.out.println("Connexion refusee : " + raison);
                return;
            }

            System.out.println("Connexion reussie. Vous etes dans la salle de clavardage.");
            System.out.println("Tapez vos messages (max " + LONGUEUR_MAX_MESSAGE
                    + " caracteres). Tapez /quit pour quitter.\n");

            Thread recepteur = new Thread(() -> ecouter(entree));
            recepteur.setDaemon(true);
            recepteur.start();

            while (true) {
                String message = saisie.lireLigne();
                if (message.equals("/quit")) {
                    break;
                }
                if (message.isEmpty()) {
                    continue;
                }
                if (message.length() > LONGUEUR_MAX_MESSAGE) {
                    System.out.println("Message trop long (max " + LONGUEUR_MAX_MESSAGE
                            + " caracteres). Non envoye.");
                    continue;
                }
                sortie.writeUTF(message);
                sortie.flush();
            }
        } catch (UnknownHostException e) {
            System.err.println("Hote introuvable : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
        System.out.println("Deconnecte.");
    }

    private static void ecouter(DataInputStream entree) {
        try {
            while (true) {
                System.out.println(entree.readUTF());
            }
        } catch (IOException e) {
            System.out.println("Connexion au serveur perdue.");
        }
    }
}
