package inf3405.serveur;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

    public static void main(String[] args) {
        Saisie saisie = new Saisie();
        String adresseIP = saisie.lireAdresseIP();
        int port = saisie.lirePort();

        Etat etat = new Etat();

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
                new Thread(new Connexion(socket, etat)).start();
            }
        } catch (IOException e) {
            System.err.println("Impossible de demarrer le serveur : " + e.getMessage());
        }
    }
}
