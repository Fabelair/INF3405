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

/**
 * Base de donnees des utilisateurs et gestion de l'authentification (requis 4.3 et 4.5).
 *
 * Conserve les correspondances nom / mot de passe, cree automatiquement les
 * nouveaux comptes et persiste les utilisateurs sur disque. Toutes les methodes
 * publiques sont synchronisees car plusieurs threads clients y accedent.
 */
public class UserManager {

    private static final String FICHIER_UTILISATEURS = "utilisateurs.txt";
    private static final String SEPARATEUR = "\t";

    private final Map<String, String> utilisateurs = new HashMap<>(); // nom -> mot de passe
    private final Set<String> connectes = new HashSet<>();

    /** Resultat possible d'une tentative d'authentification. */
    public enum Resultat {
        ACCEPTE,
        MOT_DE_PASSE_INVALIDE,
        DEJA_CONNECTE
    }

    public UserManager() {
        charger();
    }

    /**
     * Tente d'authentifier un utilisateur (requis 4.3) :
     * <ul>
     *   <li>utilisateur inexistant : creation automatique du compte;</li>
     *   <li>utilisateur existant, bon mot de passe, non connecte : accepte;</li>
     *   <li>deja connecte : refuse;</li>
     *   <li>mauvais mot de passe : refuse.</li>
     * </ul>
     */
    public synchronized Resultat authentifier(String nom, String motDePasse) {
        if (connectes.contains(nom)) {
            return Resultat.DEJA_CONNECTE;
        }
        if (utilisateurs.containsKey(nom)) {
            if (!utilisateurs.get(nom).equals(motDePasse)) {
                return Resultat.MOT_DE_PASSE_INVALIDE;
            }
        } else {
            // Nouvel utilisateur : creation automatique et persistance.
            utilisateurs.put(nom, motDePasse);
            sauvegarder();
        }
        connectes.add(nom);
        return Resultat.ACCEPTE;
    }

    /** Marque un utilisateur comme deconnecte pour autoriser une reconnexion. */
    public synchronized void deconnecter(String nom) {
        connectes.remove(nom);
    }

    /** Charge la base d'utilisateurs depuis le disque au demarrage. */
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

    /** Reecrit l'ensemble de la base d'utilisateurs sur disque. */
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
