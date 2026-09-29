package inf3405.serveur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represente un message de clavardage (requis 4.4).
 *
 * L'horodatage est fixe au moment de la creation. La representation textuelle
 * respecte le format impose :
 * {@code [Nom d'utilisateur - Adresse IP:Port - AAAA-MM-JJ@HH:MM:SS] : Message}
 */
public class Message {

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");

    private final String nomUtilisateur;
    private final String adresseIP;
    private final int port;
    private final String horodatage;
    private final String contenu;

    public Message(String nomUtilisateur, String adresseIP, int port, String contenu) {
        this.nomUtilisateur = nomUtilisateur;
        this.adresseIP = adresseIP;
        this.port = port;
        this.horodatage = LocalDateTime.now().format(FORMAT_DATE);
        this.contenu = contenu;
    }

    @Override
    public String toString() {
        return "[" + nomUtilisateur + " - " + adresseIP + ":" + port + " - " + horodatage + "] : " + contenu;
    }
}
