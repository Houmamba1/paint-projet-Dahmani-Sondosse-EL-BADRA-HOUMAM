# Validation exécutée — Paint International

Date : 26 octobre 2026. Environnement : Linux, OpenJDK 17.0.20, affichage X11 virtuel Xvfb de 1440 × 1000 pixels. L'environnement initial comportait Java en version headless ; les outils de compilation et bibliothèques graphiques ont été préparés uniquement pour les tests. Ils ne sont pas des dépendances du projet et ne sont pas inclus dans l'archive.

## Résultat réel

- Compilation des six classes avec `--release 17 -encoding UTF-8` : succès.
- Construction du JAR avec Main-Class `paint.Main` et ressources à la racine : succès.
- Exécution de ModelTest en mode headless contre le JAR : **410 assertions réussies**.
- Exécution de GuiTest sur un affichage graphique contre le JAR : **33 assertions réussies**.
- Démarrage séparé par `java -jar dist/PaintInternational.jar` : le processus reste actif pendant les quatre secondes d'observation, sans erreur de démarrage ; il est ensuite arrêté par le banc de validation.
- Captures FR/EN/AR issues de la fenêtre réelle : réalisées ; vues française et arabe inspectées visuellement.

Les nombres d'assertions incluent les contrôles des chaînes de traduction : ils ne représentent pas 443 scénarios indépendants.

## Couverture du modèle

| Fonction | Vérification exécutée |
| --- | --- |
| Huit outils | Événements press/drag/release ; changement effectif de pixels |
| Formes vides/remplies | Intérieur blanc ou coloré selon l'outil |
| Cercle | Axes égaux sur la forme produite |
| Rectangle inversé | Glissement vers le haut et la gauche |
| Crayon ponctuel | Clic sans déplacement |
| Gomme | Pixels repeints en blanc |
| Aperçu | Visible lors de paint ; image persistante encore inchangée |
| Épaisseur | Pixels à distance de l'axe du trait |
| Abandon d'un geste | Aucun état ni pixel validé après cancelGesture |
| Historique | Undo/redo, retour à l'état sauvegardé, branche redo supprimée |
| Limite d'historique | Au plus 19 annulations sur petit document après 30 gestes |
| Effacement | Image blanche, puis annulation vers l'état précédent |
| PNG | Écriture/relecture et remplacement ; comparaison d'un pixel |
| Extensions | Ajout .png et conservation de .PNG |
| Entrées invalides | Image illisible, destination inexistante, tailles nulles/négatives/excessives |
| Transparence | Composition sur fond blanc |
| Traductions | Clés et valeurs non vides dans les trois bundles ; orientation |

## Couverture graphique

| Fonction | Vérification exécutée |
| --- | --- |
| Fenêtre | Affichage de PaintFrame et absence d'exception non gérée |
| Souris réelle | Robot dessine un geste ; le document devient modifié |
| Clavier réel | Ctrl+Z et Ctrl+Y déclenchent les actions attendues |
| Choix des outils | Activation des huit JRadioButton |
| Redimensionnement | Passage à 900 × 650 puis 1200 × 820 ; dimensions du document conservées |
| Langues | Français → anglais → arabe → français ; textes et RTL, pixels conservés |
| Couleur | Modification d'un slider RGB et validation du dialogue |
| Enregistrer | Choix d'un chemin PNG ; fichier créé et indicateur modifié supprimé |
| Enregistrer sous | Annulation du sélecteur sans perte du fichier |
| Exporter | Copie PNG créée ; indicateur de modifications conservé |
| Effacer | Refus puis acceptation de confirmation ; annulation de l'effacement |
| Enregistrer existant | Sauvegarde sans sélection de chemin |
| Ouvrir | Lecture du PNG et retour à un état non modifié |
| Nouveau | Dialogue dimensions 320 × 240 et nouveau document |
| Ouvrir après Nouveau | Rétablissement des dimensions du fichier |
| Lecture en erreur | Dialogue d'erreur ; dessin précédent conservé |
| Quitter | Annulation puis abandon des modifications |
| Croix de fermeture | WindowEvent reçu et fermeture annulée |

Les tests de dialogues utilisent les composants Swing réellement créés, `doClick`, `approveSelection` et `cancelSelection`. Les interactions de dessin et Ctrl+Z/Y sont envoyées par Robot. Il ne s'agit pas d'une automatisation intégralement fondée sur des clics de coordonnées.

## Vérifications non exécutées et limites

- Les scripts `build.bat`, `run.bat`, `test.bat` sont fournis mais n'ont pas été exécutés sur Windows.
- Pas de test sur macOS, d'association système du double-clic JAR ni de configuration d'un IDE.
- Les formats JPEG/GIF/BMP passent par les lecteurs ImageIO ; le test automatisé de fichier exécuté ici porte sur PNG, un fichier invalide et la conversion de transparence.
- Pas de simulation de disque plein, panne électrique, permissions de fichiers propres à Windows ou fermeture de la JVM pendant un encodage.
- Pas de test d'endurance de plusieurs heures ni de mesure complète de mémoire maximale.
- L'aperçu et les pixels de chaque outil sont testés par les événements Swing ; la souris Robot a été utilisée pour le crayon.
- Le choix d'écrasement existe dans le code ; tous les chemins de réponse à cette confirmation ne sont pas automatisés dans ce banc.

## Contrôle rapide conseillé sur le poste de remise

1. Exécuter `java -version`, puis `run.bat` sur Windows.
2. Dessiner avec chacun des outils et déplacer/masquer la fenêtre.
3. Enregistrer dans un dossier personnel dont le nom contient des espaces et des accents.
4. Ouvrir un JPEG et l'enregistrer sous un nouveau PNG.
5. Refaire le changement des trois langues et vérifier le rendu arabe de la police système.
6. Tester refus/acceptation d'un écrasement et les trois choix de fermeture.

Ces contrôles locaux complètent les tests réellement exécutés ; ils ne sont pas présentés comme déjà faits.
