package inf3405.serveur;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Gere la communication avec un client (requis 4.1 a 4.4).
 *
 * Un thread est cree par client connecte. Il realise l'authentification,
 * envoie les 15 messages les plus recents, puis recoit les messages du client
 * et les transmet a l'historique pour diffusion.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final UserManager gestionnaireUtilisateurs;
    private final MessageHistory historique;

    private DataInputStream entree;
    private DataOutputStream sortie;
    private String nomUtilisateur;

    public ClientHandler(Socket socket, UserManager gestionnaireUtilisateurs, MessageHistory historique) {
        this.socket = socket;
        this.gestionnaireUtilisateurs = gestionnaireUtilisateurs;
        this.historique = historique;
    }

    @Override
    public void run() {
        try {
            entree = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            sortie = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // 1. Authentification (requis 4.3).
            String nom = entree.readUTF();
            String motDePasse = entree.readUTF();
            UserManager.Resultat resultat = gestionnaireUtilisateurs.authentifier(nom, motDePasse);

            if (resultat != UserManager.Resultat.ACCEPTE) {
                sortie.writeUTF("ERREUR");
                sortie.writeUTF(messageRefus(resultat));
                sortie.flush();
                return; // le bloc finally ferme le socket
            }

            this.nomUtilisateur = nom;
            sortie.writeUTF("OK");
            sortie.flush();

            // 2. Envoi des 15 messages les plus recents (requis 4.2).
            for (String message : historique.messagesRecents()) {
                envoyer(message);
            }

            // 3. Enregistrement pour recevoir les diffusions futures.
            historique.enregistrerClient(this);

            String ip = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            System.out.println("Nouvelle connexion : " + nom + " (" + ip + ":" + port + ")");

            // 4. Reception et diffusion des messages (requis 4.4).
            while (true) {
                String contenu = entree.readUTF();
                Message message = new Message(nom, ip, port, contenu);
                historique.diffuser(message.toString());
            }
        } catch (EOFException | SocketException e) {
            // Deconnexion normale du client (fin de flux ou socket ferme).
        } catch (IOException e) {
            System.err.println("Erreur avec un client : " + e.getMessage());
        } finally {
            if (nomUtilisateur != null) {
                gestionnaireUtilisateurs.deconnecter(nomUtilisateur);
                historique.retirerClient(this);
                System.out.println("Deconnexion : " + nomUtilisateur);
            }
            fermer();
        }
    }

    /** Envoi thread-safe d'un message vers ce client. */
    public synchronized void envoyer(String message) {
        try {
            if (sortie != null) {
                sortie.writeUTF(message);
                sortie.flush();
            }
        } catch (IOException e) {
            // Client probablement deconnecte : le retrait est gere dans run().
        }
    }

    private String messageRefus(UserManager.Resultat resultat) {
        switch (resultat) {
            case DEJA_CONNECTE:
                return "Cet utilisateur est deja connecte.";
            case MOT_DE_PASSE_INVALIDE:
                return "Mot de passe invalide.";
            default:
                return "Connexion refusee.";
        }
    }

    private void fermer() {
        try {
            socket.close();
        } catch (IOException ignore) {
            // Rien a faire : la connexion est deja fermee.
        }
    }
}
