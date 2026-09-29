package inf3405.serveur;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Historique des conversations et diffusion des messages (requis 4.4 et 4.5).
 *
 * Conserve tous les messages (formates), les persiste sur disque et les diffuse
 * aux clients connectes. Toutes les operations sont synchronisees.
 */
public class MessageHistory {

    private static final String FICHIER_HISTORIQUE = "historique.txt";
    private static final int NB_MESSAGES_RECENTS = 15;

    private final List<String> historique = new ArrayList<>();
    private final List<ClientHandler> clients = new ArrayList<>();

    public MessageHistory() {
        charger();
    }

    /** Enregistre un client pour qu'il recoive les diffusions. */
    public synchronized void enregistrerClient(ClientHandler client) {
        clients.add(client);
    }

    /** Retire un client (lors de sa deconnexion). */
    public synchronized void retirerClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Ajoute un message a l'historique, le persiste, l'affiche cote serveur en
     * temps reel et le diffuse a tous les clients connectes (requis 4.1 et 4.4).
     */
    public synchronized void diffuser(String messageFormate) {
        historique.add(messageFormate);
        ajouterAuFichier(messageFormate);
        System.out.println(messageFormate); // affichage temps reel cote serveur
        for (ClientHandler client : clients) {
            client.envoyer(messageFormate);
        }
    }

    /** Retourne les 15 messages les plus recents, ou moins s'il y en a moins (requis 4.2). */
    public synchronized List<String> messagesRecents() {
        int debut = Math.max(0, historique.size() - NB_MESSAGES_RECENTS);
        return new ArrayList<>(historique.subList(debut, historique.size()));
    }

    /** Charge l'historique complet depuis le disque au demarrage. */
    private void charger() {
        File fichier = new File(FICHIER_HISTORIQUE);
        if (!fichier.exists()) {
            return;
        }
        try (BufferedReader lecteur = new BufferedReader(
                new InputStreamReader(new FileInputStream(fichier), StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = lecteur.readLine()) != null) {
                historique.add(ligne);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'historique : " + e.getMessage());
        }
    }

    /** Ajoute une ligne a la fin du fichier d'historique (mode append). */
    private void ajouterAuFichier(String ligne) {
        try (BufferedWriter ecrivain = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FICHIER_HISTORIQUE, true), StandardCharsets.UTF_8))) {
            ecrivain.write(ligne);
            ecrivain.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ecriture de l'historique : " + e.getMessage());
        }
    }
}
