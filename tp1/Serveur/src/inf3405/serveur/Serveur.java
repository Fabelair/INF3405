package inf3405.serveur;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Serveur {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 5050;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String adresseIP = lireAdresseIP(scanner);
        int port = lirePort(scanner);

        GestionnaireUtilisateurs gestionnaireUtilisateurs = new GestionnaireUtilisateurs();
        HistoriqueMessages historique = new HistoriqueMessages();

        try (ServerSocket serveur = new ServerSocket(port, 50, InetAddress.getByName(adresseIP))) {
            System.out.println("Serveur demarre sur " + adresseIP + ":" + port);
            System.out.println("En attente de connexions...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nArret du serveur...");
                try {
                    serveur.close();
                } catch (IOException e) {
                    System.err.println("Erreur lors de la fermeture du serveur : " + e.getMessage());
                }
            }));

            while (true) {
                Socket socket = serveur.accept();
                GestionnaireClient gestionnaire = new GestionnaireClient(socket, gestionnaireUtilisateurs, historique);
                new Thread(gestionnaire).start();
            }
        } catch (IOException e) {
            System.err.println("Impossible de demarrer le serveur : " + e.getMessage());
        }
    }

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

    private static int lirePort(Scanner scanner) {
        while (true) {
            System.out.print("Entrez le numero de port (" + PORT_MIN + " a " + PORT_MAX + ") : ");
            String saisie = scanner.nextLine().trim();
            try {
                int port = Integer.parseInt(saisie);
                if (port >= PORT_MIN && port <= PORT_MAX) {
                    return port;
                }
                System.out.println("Port invalide. Entier compris entre " + PORT_MIN + " et " + PORT_MAX + ".");
            } catch (NumberFormatException e) {
                System.out.println("Port invalide. \"" + saisie + "\" n'est pas un nombre entier.");
            }
        }
    }

    public static boolean adresseIPValide(String ip) {
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
