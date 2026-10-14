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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stockage {

    private static final String FICHIER_COMPTES = "utilisateurs.txt";
    private static final String FICHIER_HISTORIQUE = "historique.txt";
    private static final String SEPARATEUR = "\t";

    public Map<String, String> chargerComptes() {
        Map<String, String> comptes = new HashMap<>();
        for (String ligne : lireLignes(FICHIER_COMPTES)) {
            int index = ligne.indexOf(SEPARATEUR);
            if (index > 0) {
                comptes.put(ligne.substring(0, index), ligne.substring(index + 1));
            }
        }
        return comptes;
    }

    public void sauvegarderComptes(Map<String, String> comptes) {
        List<String> lignes = new ArrayList<>();
        for (Map.Entry<String, String> compte : comptes.entrySet()) {
            lignes.add(compte.getKey() + SEPARATEUR + compte.getValue());
        }
        ecrireLignes(FICHIER_COMPTES, lignes, false);
    }

    public List<String> chargerHistorique() {
        return lireLignes(FICHIER_HISTORIQUE);
    }

    public void ajouterAHistorique(String message) {
        ecrireLignes(FICHIER_HISTORIQUE, List.of(message), true);
    }

    private List<String> lireLignes(String nomFichier) {
        List<String> lignes = new ArrayList<>();
        File fichier = new File(nomFichier);
        if (!fichier.exists()) {
            return lignes;
        }
        try (BufferedReader lecteur = new BufferedReader(
                new InputStreamReader(new FileInputStream(fichier), StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = lecteur.readLine()) != null) {
                lignes.add(ligne);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture de " + nomFichier + " : " + e.getMessage());
        }
        return lignes;
    }

    private void ecrireLignes(String nomFichier, List<String> lignes, boolean ajout) {
        try (BufferedWriter ecrivain = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(nomFichier, ajout), StandardCharsets.UTF_8))) {
            for (String ligne : lignes) {
                ecrivain.write(ligne);
                ecrivain.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ecriture de " + nomFichier + " : " + e.getMessage());
        }
    }
}
