package inf3405.serveur;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Connexion implements Runnable {

    private final Socket socket;
    private final Etat etat;

    private DataInputStream entree;
    private DataOutputStream sortie;
    private String nomUtilisateur;

    public Connexion(Socket socket, Etat etat) {
        this.socket = socket;
        this.etat = etat;
    }

    @Override
    public void run() {
        try {
            entree = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            sortie = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            String nom = entree.readUTF();
            String motDePasse = entree.readUTF();
            Etat.Resultat resultat = etat.authentifier(nom, motDePasse);

            if (resultat != Etat.Resultat.ACCEPTE) {
                sortie.writeUTF("ERREUR");
                sortie.writeUTF(messageRefus(resultat));
                sortie.flush();
                return;
            }

            this.nomUtilisateur = nom;
            sortie.writeUTF("OK");
            sortie.flush();

            etat.rejoindre(this);

            String ip = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            System.out.println("Nouvelle connexion : " + nom + " (" + ip + ":" + port + ")");

            while (true) {
                String contenu = entree.readUTF();
                etat.diffuser(nom, ip, port, contenu);
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Connexion fermee par le client.");
        } catch (IOException e) {
            System.err.println("Erreur avec un client : " + e.getMessage());
        } finally {
            if (nomUtilisateur != null) {
                etat.quitter(this, nomUtilisateur);
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

    private String messageRefus(Etat.Resultat resultat) {
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
