package inf3405.serveur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Etat {

    public enum Resultat {
        ACCEPTE,
        MOT_DE_PASSE_INVALIDE,
        DEJA_CONNECTE
    }

    private static final int NB_MESSAGES_RECENTS = 15;
    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");

    private final Stockage stockage = new Stockage();
    private final Map<String, String> comptes = stockage.chargerComptes();
    private final Set<String> connectes = new HashSet<>();
    private final List<String> historique = stockage.chargerHistorique();
    private final List<Connexion> clients = new ArrayList<>();

    public synchronized Resultat authentifier(String nom, String motDePasse) {
        if (connectes.contains(nom)) {
            return Resultat.DEJA_CONNECTE;
        }
        if (comptes.containsKey(nom)) {
            if (!comptes.get(nom).equals(motDePasse)) {
                return Resultat.MOT_DE_PASSE_INVALIDE;
            }
        } else {
            comptes.put(nom, motDePasse);
            stockage.sauvegarderComptes(comptes);
        }
        connectes.add(nom);
        return Resultat.ACCEPTE;
    }

    public synchronized void rejoindre(Connexion client) {
        int debut = Math.max(0, historique.size() - NB_MESSAGES_RECENTS);
        for (String message : historique.subList(debut, historique.size())) {
            client.envoyer(message);
        }
        clients.add(client);
    }

    public synchronized void quitter(Connexion client, String nom) {
        clients.remove(client);
        connectes.remove(nom);
    }

    public synchronized void diffuser(String nom, String ip, int port, String contenu) {
        String horodatage = LocalDateTime.now().format(FORMAT_DATE);
        String message = "[" + nom + " - " + ip + ":" + port + " - " + horodatage + "] : " + contenu;
        historique.add(message);
        stockage.ajouterAHistorique(message);
        System.out.println(message);
        for (Connexion client : clients) {
            client.envoyer(message);
        }
    }
}
