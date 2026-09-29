package inf3405.serveur;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Point d'entree du serveur de clavardage (requis 4.1).
 *
 * Au demarrage, demande l'adresse IP d'ecoute et le port (5000-5050), valide
 * les saisies, puis accepte simultanement plusieurs connexions clientes en
 * creant un thread {@link ClientHandler} par client.
 */
public class Server {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 5050;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String adresseIP = lireAdresseIP(scanner);
        int port = lirePort(scanner);

        // Structures partagees entre tous les clients (thread-safe).
        UserManager gestionnaireUtilisateurs = new UserManager();
        MessageHistory historique = new MessageHistory();

        try (ServerSocket serveur = new ServerSocket(port, 50, InetAddress.getByName(adresseIP))) {
            System.out.println("Serveur demarre sur " + adresseIP + ":" + port);
            System.out.println("En attente de connexions...");

            // Fermeture propre des ressources lors de l'arret (requis 4.1).
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nArret du serveur...");
                try {
                    serveur.close();
                } catch (IOException ignore) {
                    // Le serveur est deja ferme.
                }
            }));

            // Boucle d'acceptation : un thread par client connecte.
            while (true) {
                Socket socket = serveur.accept();
                ClientHandler handler = new ClientHandler(socket, gestionnaireUtilisateurs, historique);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Impossible de demarrer le serveur : " + e.getMessage());
        }
    }

    /** Demande et valide l'adresse IP (quatre octets) jusqu'a obtenir une saisie valide. */
    private static String lireAdresseIP(Scanner scanner) {
        while (true) {
            System.out.print("Entrez l'adresse IP d'ecoute du serveur : ");
            String ip = scanner.nextLine().trim();
            if (adresseIPValide(ip)) {
                return ip;
            }
            System.out.println("Adresse IP invalide. Elle doit etre composee de quatre octets (ex. 127.0.0.1).");
        }
    }

    /** Demande et valide le port (plage 5000-5050) jusqu'a obtenir une saisie valide. */
    private static int lirePort(Scanner scanner) {
        while (true) {
            System.out.print("Entrez le numero de port (" + PORT_MIN + " a " + PORT_MAX + ") : ");
            String saisie = scanner.nextLine().trim();
            try {
                int port = Integer.parseInt(saisie);
                if (port >= PORT_MIN && port <= PORT_MAX) {
                    return port;
                }
            } catch (NumberFormatException ignore) {
                // Retombe sur le message d'erreur ci-dessous.
            }
            System.out.println("Port invalide. Entier compris entre " + PORT_MIN + " et " + PORT_MAX + ".");
        }
    }

    /**
     * Valide une adresse IPv4 : exactement quatre octets, chacun compris entre 0 et 255.
     */
    public static boolean adresseIPValide(String ip) {
        if (ip == null) {
            return false;
        }
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            try {
                int valeur = Integer.parseInt(octet);
                if (valeur < 0 || valeur > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
