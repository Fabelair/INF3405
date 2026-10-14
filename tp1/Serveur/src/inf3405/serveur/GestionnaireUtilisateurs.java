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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GestionnaireUtilisateurs {

    private static final String FICHIER_UTILISATEURS = "utilisateurs.txt";
    private static final String SEPARATEUR = "\t";

    private final Map<String, String> utilisateurs = new HashMap<>();
    private final Set<String> connectes = new HashSet<>();

    public enum Resultat {
        ACCEPTE,
        MOT_DE_PASSE_INVALIDE,
        DEJA_CONNECTE
    }

    public GestionnaireUtilisateurs() {
        charger();
    }

    public synchronized Resultat authentifier(String nom, String motDePasse) {
        if (connectes.contains(nom)) {
            return Resultat.DEJA_CONNECTE;
        }
        if (utilisateurs.containsKey(nom)) {
            if (!utilisateurs.get(nom).equals(motDePasse)) {
                return Resultat.MOT_DE_PASSE_INVALIDE;
            }
        } else {
            utilisateurs.put(nom, motDePasse);
            sauvegarder();
        }
        connectes.add(nom);
        return Resultat.ACCEPTE;
    }

    public synchronized void deconnecter(String nom) {
        connectes.remove(nom);
    }

    private void charger() {
        File fichier = new File(FICHIER_UTILISATEURS);
        if (!fichier.exists()) {
            return;
        }
        try (BufferedReader lecteur = new BufferedReader(
                new InputStreamReader(new FileInputStream(fichier), StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = lecteur.readLine()) != null) {
                int index = ligne.indexOf(SEPARATEUR);
                if (index > 0) {
                    utilisateurs.put(ligne.substring(0, index), ligne.substring(index + 1));
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    private void sauvegarder() {
        try (BufferedWriter ecrivain = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FICHIER_UTILISATEURS), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, String> entree : utilisateurs.entrySet()) {
                ecrivain.write(entree.getKey() + SEPARATEUR + entree.getValue());
                ecrivain.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des utilisateurs : " + e.getMessage());
        }
    }
}
