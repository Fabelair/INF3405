# INF3405 – TP1 : Application client-serveur TCP de clavardage

Deux projets Eclipse indépendants communiquant en **TCP** (aucune bibliothèque
réseau externe). Couvre l'ensemble des requis fonctionnels de la **section 4**.

## Structure

```
tp1/
├── Serveur/                    Projet Eclipse du serveur
│   └── src/inf3405/serveur/
│       ├── Serveur.java        Point d'entrée, boucle accept()
│       ├── Saisie.java         Lecture/validation IP et port
│       ├── Connexion.java      Un thread par client (auth, réception)
│       ├── Etat.java           Comptes, connectés, historique, diffusion, format
│       └── Stockage.java       utilisateurs.txt et historique.txt
└── Client/                     Projet Eclipse du client
    └── src/inf3405/client/
        ├── Client.java         Connexion, envoi, thread d'écoute
        └── Saisie.java         Lecture/validation IP, port, champs
```

## Compilation (ligne de commande)

```bash
javac -encoding UTF-8 -d Serveur/bin Serveur/src/inf3405/serveur/*.java
javac -encoding UTF-8 -d Client/bin  Client/src/inf3405/client/*.java
```

Sous Eclipse : importer chaque dossier via *File > Import > Existing Projects into Workspace*.

## Exécution

Démarrer d'abord le serveur, puis un ou plusieurs clients.

```bash
# Serveur : demande l'IP d'écoute puis le port (5000–5050)
java -cp Serveur/bin inf3405.serveur.Serveur

# Client (dans un autre terminal) : IP serveur, port, nom, mot de passe
java -cp Client/bin inf3405.client.Client
```

Dans le client : taper un message (≤ 200 caractères) puis Entrée. `/quit` pour quitter.

## Protocole (résumé)

Échanges via `DataInputStream`/`DataOutputStream` (`writeUTF`/`readUTF`) :

1. Le client envoie `nomUtilisateur` puis `motDePasse`.
2. Le serveur répond `OK`, ou `ERREUR` suivi de la raison (mot de passe invalide,
   utilisateur déjà connecté).
3. Après `OK`, le serveur envoie les 15 derniers messages, puis diffuse à tous
   les clients chaque message reçu, au format :
   `[Nom - IP:Port - AAAA-MM-JJ@HH:MM:SS] : Message`

## Persistance

Le serveur crée dans son répertoire d'exécution :

- `utilisateurs.txt` — un couple `nom<TAB>motDePasse` par ligne ;
- `historique.txt` — un message formaté par ligne.

Ces fichiers sont rechargés au redémarrage du serveur.

## Correspondance avec les requis (section 4)

| Requis | Emplacement |
|--------|-------------|
| 4.1 Serveur (validation IP/port, multi-clients, diffusion, arrêt propre) | `Serveur`, `Saisie`, `Connexion`, `Etat.diffuser` |
| 4.2 Client (validation, auth, 15 récents, ≤200 car., temps réel) | `Client`, `Saisie`, `Etat.rejoindre` |
| 4.3 Authentification (4 cas, création auto) | `Etat.authentifier` |
| 4.4 Communications et format d'affichage | `Etat.diffuser` |
| 4.5 Persistance (utilisateurs + historique) | `Stockage` |
