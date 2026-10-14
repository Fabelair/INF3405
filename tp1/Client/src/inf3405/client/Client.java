package inf3405.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 5050;
    private static final int LONGUEUR_MAX_MESSAGE = 200;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String adresseIP = lireAdresseIP(scanner);
        int port = lirePort(scanner);

        System.out.print("Nom d'utilisateur : ");
        String nomUtilisateur = scanner.nextLine().trim();
        System.out.print("Mot de passe : ");
        String motDePasse = scanner.nextLine().trim();

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

            Thread recepteur = new Thread(new RecepteurMessages(entree));
            recepteur.setDaemon(true);
            recepteur.start();

            while (true) {
                String message = scanner.nextLine();
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

    private static String lireAdresseIP(Scanner scanner) {
        while (true) {
            System.out.print("Adresse IP du serveur : ");
            String ip = scanner.nextLine().trim();
            if (adresseIPValide(ip)) {
                return ip;
            }
            System.out.println("Adresse IP invalide (quatre octets, ex. 127.0.0.1).");
        }
    }

    private static int lirePort(Scanner scanner) {
        while (true) {
            System.out.print("Numero de port (" + PORT_MIN + " a " + PORT_MAX + ") : ");
            String saisie = scanner.nextLine().trim();
            try {
                int port = Integer.parseInt(saisie);
                if (port >= PORT_MIN && port <= PORT_MAX) {
                    return port;
                }
                System.out.println("Port invalide (entier entre " + PORT_MIN + " et " + PORT_MAX + ").");
            } catch (NumberFormatException e) {
                System.out.println("Port invalide (\"" + saisie + "\" n'est pas un nombre entier).");
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
