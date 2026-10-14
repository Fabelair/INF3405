package inf3405.serveur;

import java.util.Scanner;

public class Saisie {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 5050;

    private final Scanner scanner = new Scanner(System.in);

    public String lireAdresseIP() {
        while (true) {
            System.out.print("Entrez l'adresse IP d'ecoute du serveur : ");
            String ip = scanner.nextLine().trim();
            if (adresseIPValide(ip)) {
                return ip;
            }
            System.out.println("Adresse IP invalide. Elle doit etre composee de quatre octets (ex. 127.0.0.1).");
        }
    }

    public int lirePort() {
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

    private static boolean adresseIPValide(String ip) {
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
