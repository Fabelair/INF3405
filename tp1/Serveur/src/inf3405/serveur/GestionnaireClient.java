package inf3405.serveur;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class GestionnaireClient implements Runnable {

    private final Socket socket;
    private final GestionnaireUtilisateurs gestionnaireUtilisateurs;
    private final HistoriqueMessages historique;

    private DataInputStream entree;
    private DataOutputStream sortie;
    private String nomUtilisateur;

    public GestionnaireClient(Socket socket, GestionnaireUtilisateurs gestionnaireUtilisateurs, HistoriqueMessages historique) {
        this.socket = socket;
        this.gestionnaireUtilisateurs = gestionnaireUtilisateurs;
        this.historique = historique;
    }

    @Override
    public void run() {
        try {
            entree = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            sortie = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            String nom = entree.readUTF();
            String motDePasse = entree.readUTF();
            GestionnaireUtilisateurs.Resultat resultat = gestionnaireUtilisateurs.authentifier(nom, motDePasse);

            if (resultat != GestionnaireUtilisateurs.Resultat.ACCEPTE) {
                sortie.writeUTF("ERREUR");
                sortie.writeUTF(messageRefus(resultat));
                sortie.flush();
                return;
            }

            this.nomUtilisateur = nom;
            sortie.writeUTF("OK");
            sortie.flush();

            for (String message : historique.messagesRecents()) {
                envoyer(message);
            }

            historique.enregistrerClient(this);

            String ip = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            System.out.println("Nouvelle connexion : " + nom + " (" + ip + ":" + port + ")");

            while (true) {
                String contenu = entree.readUTF();
                Message message = new Message(nom, ip, port, contenu);
                historique.diffuser(message.toString());
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Connexion fermee par le client.");
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

    public synchronized void envoyer(String message) {
        try {
            sortie.writeUTF(message);
            sortie.flush();
        } catch (IOException e) {
            System.err.println("Impossible d'envoyer le message a " + nomUtilisateur + " : " + e.getMessage());
        }
    }

    private String messageRefus(GestionnaireUtilisateurs.Resultat resultat) {
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
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
        }
    }
}
