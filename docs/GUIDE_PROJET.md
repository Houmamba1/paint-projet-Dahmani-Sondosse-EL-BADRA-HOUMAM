# Paint International — Analyse, conception et guide de soutenance

## 1. Lecture des documents et contraintes exactes

Le cours officiel fourni, « Java II - 1-GUI-V2_4(1).pdf », est un support de **318 pages PDF**, dont la dernière diapositive porte le numéro **319**. Il présente un chapitre général « Les interfaces graphiques », subdivisé en thèmes. Il ne s'agit pas de dix chapitres numérotés distincts. Les numéros cités dans ce guide sont les positions PDF ; à partir de la page PDF 205, le numéro imprimé est généralement supérieur d'une unité.

L'image de l'énoncé retrouvée dans les pièces précédentes indique les mêmes exigences que la dernière diapositive : **créer une application de dessin internationalisée similaire à Paint**, à rendre en **exécutable et code source**, en **groupe de 3 à 4 personnes**. Elle ne fixe pas une liste détaillée d'outils, une version de Java ou un nombre exact de langues.

La liste détaillée du message de demande constitue le cahier des charges supplémentaire utilisé pour l'implémentation. Il faut distinguer les exigences du professeur et les choix pratiques faits pour y répondre.

| Niveau | Exigences | Réponse du projet |
| --- | --- | --- |
| Obligatoire dans l'énoncé | Dessin similaire à Paint | Application de bureau Swing, outils et fichiers images |
| Obligatoire dans l'énoncé | Internationalisation | FR/EN/AR via ResourceBundle et properties |
| Obligatoire dans l'énoncé | Source + exécutable | Sources complètes et JAR exécutable |
| Obligatoire dans l'énoncé | Groupe de 3 à 4 | Répartition indicative proposée en fin de guide |
| Nécessaire au périmètre demandé | Crayon, gomme, formes, couleurs et épaisseur | Huit outils, aperçu et réglages |
| Nécessaire au périmètre demandé | Nouveau/Ouvrir/Enregistrer/Enregistrer sous/Exporter/Quitter | JFileChooser, ImageIO et confirmations |
| Nécessaire au périmètre demandé | Annuler/rétablir/effacer/raccourcis | Historique borné, actions et accélérateurs |
| Améliorations retenues | Arabe RTL, dimensions personnalisées, écriture temporaire, lecture hors EDT | Implémentées et expliquées |
| Améliorations non retenues | Zoom, calques, sélection, texte sur le dessin, DnD et CCP d'images | Hors périmètre, aucune commande fictive |

## 2. Analyse du cours, thème par thème

L'annexe `INVENTAIRE_COURS.md` relève les classes et méthodes présentes dans le texte de **chaque page**, y compris les exemples. Ce relevé automatique est lexical : il contient aussi des noms d'exemples et des appels qui ne sont pas des méthodes Swing. Les schémas uniquement graphiques ne peuvent pas être entièrement décrits par ce relevé. La synthèse suivante précise ce qui est réellement utile au projet.

### 2.1 Introduction et hiérarchie — pages PDF 1 à 25

Le cours situe AWT, Swing et JFC, puis distingue conteneurs, composants et layouts. AWT fournit notamment les couleurs, coordonnées et événements ; Swing fournit les composants. JFrame est une fenêtre de haut niveau ; JPanel regroupe des composants ou sert de surface personnalisée. JRootPane contient notamment le contentPane, le layeredPane et le glassPane ; la barre de menus s'installe par `setJMenuBar`.

Conteneurs présentés : JFrame, JWindow, JDialog, JPanel, JScrollPane, JToolBar, JSplitPane, JTabbedPane, JInternalFrame et JLayeredPane. Les composants usuels héritent de JComponent, contrairement aux fenêtres de haut niveau.

Méthodes principales : `setContentPane`, `getContentPane`, `setLayout`, `add`, `setSize`, `setBounds`, `setLocation`, `pack`, `setTitle`, `setVisible`, `setDefaultCloseOperation`, `setResizable`, `setBackground`, `setForeground`, `setFont`, `setBorder`, `setOpaque`, `setPreferredSize`, `setMinimumSize`, `setMaximumSize`, `setAlignmentX/Y`.

Application : PaintFrame hérite de JFrame, contient un JPanel racine et une zone de dessin spécialisée. La fermeture utilise DO_NOTHING_ON_CLOSE pour pouvoir demander confirmation.

### 2.2 Composants — pages PDF 26 à 82

Le support traite JButton, JLabel, JTextField, JTextArea, JCheckBox, JRadioButton et ButtonGroup, JProgressBar, JComboBox, JList, JTabbedPane, JMenuBar/JMenu/JMenuItem, JCheckBoxMenuItem, JRadioButtonMenuItem, JSeparator, JPopupMenu, JToolBar, JTable et JTree. Il cite également JPasswordField, JFormattedTextField, JEditorPane, JTextPane et JSlider dans le panorama.

Méthodes usuelles : `setText/getText`, `setIcon`, `setToolTipText`, `isSelected/setSelected`, `addActionListener`, `addItemListener`, `getSelectedItem/getSelectedIndex`, `setSelectionMode`, `addTab`, `addSeparator`, `setMnemonic`, `setAccelerator`, `KeyStroke.getKeyStroke`, `setFloatable`, `setRollover`, `setEditable`, `setStringPainted` et les méthodes des modèles de liste/arbre/table décrites dans l'annexe.

Application : boutons de commandes, boutons radio exclusifs pour les outils/langues, JComboBox pour l'épaisseur, JLabel pour l'état et JTextField pour les dimensions. Les tables, arbres, onglets et composants de texte avancés n'apportent rien à ce périmètre.

### 2.3 Layouts — pages PDF 83 à 99

- **BorderLayout** : cinq zones, centre extensible. Utilisé dans la fenêtre et la colonne d'outils.
- **FlowLayout** : place les composants en ligne. Utilisé pour accueillir le document à sa taille naturelle dans l'espace de travail.
- **GridLayout** : cellules de même taille. Utilisé pour les outils et formulaires simples.
- **BoxLayout** : une ligne ou colonne ; étudié mais non nécessaire ici.
- **CardLayout** : plusieurs cartes dont une visible, avec next/previous/first/last/show ; non nécessaire.
- **GridBagLayout** : contraintes gridx/gridy, gridwidth/gridheight, weightx/weighty, fill, anchor, insets, ipadx/ipady ; non nécessaire ici.
- **Positionnement absolu** : setLayout(null), setBounds/setSize/setLocation ; présenté mais évité pour les composants.

Les coordonnées du dessin ne constituent pas un placement absolu des composants : elles décrivent les pixels de l'image. Les layouts organisent l'interface ; Graphics2D trace à l'intérieur du document.

### 2.4 Dialogues — pages PDF 100 à 120

JOptionPane fournit `showMessageDialog`, `showConfirmDialog`, `showOptionDialog` et `showInputDialog`. Le cours détaille les parents, titres, icônes, réponses et types de messages. JDialog permet une fenêtre modale personnalisée, avec `setModal`, `pack`, `setVisible` et `dispose`. JColorChooser expose `showDialog` et renvoie une couleur ou null. JFileChooser fournit `showOpenDialog`, `showSaveDialog`, `getSelectedFile` et le résultat APPROVE_OPTION.

Application : JFileChooser pour ouvrir/enregistrer ; JOptionPane pour avertissements, formulaires et confirmations ; JDialog pour patienter pendant les E/S. Le choix de couleur utilise un petit formulaire RGB avec trois JSlider et un aperçu, plutôt que les nombreux panneaux de JColorChooser. Ce choix facilite une traduction arabe complète et conserve une interface simple. JSlider est cité dans le cours, mais son ChangeListener est une petite extension détaillée plus bas.

### 2.5 Événements — pages PDF 121 à 203

Le modèle repose sur **source → objet événement → écouteur enregistré**. Les écouteurs doivent terminer rapidement. Le cours présente EventObject/AWTEvent, ActionEvent, AdjustmentEvent, ComponentEvent, ContainerEvent, FocusEvent, InputEvent, ItemEvent, KeyEvent, MouseEvent, MouseWheelEvent, TextEvent et WindowEvent.

Interfaces : ActionListener, AdjustmentListener, ComponentListener, ContainerListener, FocusListener, ItemListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, TextListener, WindowListener et WindowFocusListener. Il explique classes autonomes, classes internes, classes anonymes, adaptateurs et lambdas pour les interfaces fonctionnelles.

Points essentiels : `addXXXListener/removeXXXListener`, `getSource`, `getActionCommand`, `getModifiers`, `getWhen`, `getKeyCode`, `getKeyChar`, `getX/getY/getPoint`, `getButton`, `isPopupTrigger` et `getWheelRotation`. Les événements souris ont cinq méthodes MouseListener et deux méthodes MouseMotionListener. WindowAdapter évite d'écrire toutes les méthodes de WindowListener.

Application : DrawingPanel implémente directement les deux interfaces souris. Les boutons reçoivent des ActionListener via des lambdas ; AbstractAction centralise les commandes communes aux menus et à la barre d'outils. WindowAdapter intercepte la fermeture.

### 2.6 Dessin 2D — pages PDF 204 à 274

Le cours présente Graphics et Graphics2D, `paintComponent`, `repaint`, `drawString`, `drawLine`, `drawRect/fillRect`, `drawOval/fillOval`, polygones et arcs ; puis l'API Shape et `draw/fill`.

Géométrie : Point2D, Line2D, RectangularShape, Rectangle2D, RoundRectangle2D, Ellipse2D, Arc2D, QuadCurve2D, CubicCurve2D et GeneralPath, avec moveTo/lineTo/quadTo/curveTo/closePath. Les contours utilisent Stroke/BasicStroke (largeur, extrémités, jointures et tirets). Les peintures peuvent être Color, GradientPaint ou TexturePaint.

Le texte couvre Font, familles logiques/physiques, GraphicsEnvironment, FontMetrics, TextAttribute, TextLayout, AttributedCharacterIterator et LineBreakMeasurer. Les images couvrent Image/BufferedImage, ImageIO.read/write, drawImage et createGraphics, ainsi que les filtres ConvolveOp, AffineTransformOp, LookupOp et RescaleOp.

Le support traite AffineTransform et translate/rotate/scale/shear, les zones de découpage setClip/clip, AlphaComposite/setComposite, les RenderingHints d'anticrénelage et d'interpolation, les opérations booléennes Area et la détection hit/contains.

Application : uniquement le sous-ensemble nécessaire : BufferedImage, Graphics2D, Color, BasicStroke, Line2D, Rectangle2D, Ellipse2D, draw/fill, drawImage, createGraphics, dispose et anticrénelage. Le cours présente aussi du dessin immédiat avec getGraphics ; le projet préfère l'image persistante et paintComponent, afin que le dessin survive au masquage et au redimensionnement.

### 2.7 Transfert de données — pages PDF 275 à 313

Thèmes : drag and drop, couper/copier/coller, TransferHandler, TransferSupport, Transferable, DataFlavor, DropMode et ActionMap/InputMap. Méthodes principales : setDragEnabled, setTransferHandler, getSourceActions, createTransferable, exportDone, canImport, importData, getDropLocation, getDropAction, setDropAction, setShowDropLocation et getTransferData. Le cours distingue le support textuel intégré et le traitement explicite pour les composants non textuels.

Ces fonctionnalités sont étudiées, mais le cahier des charges ne demande pas de sélectionner une région d'image ou de coller une image. Elles ne sont donc pas implémentées. InputMap/ActionMap est repris pour Échap, sans introduire de système de presse-papiers.

### 2.8 Internationalisation — pages PDF 314 à 317

Le cours définit i18n/localisation et montre Locale, Locale.getDefault/setDefault, ResourceBundle.getBundle et getString, associés à plusieurs fichiers properties. Le projet applique directement ce mécanisme. La reconstruction dynamique des libellés et l'orientation arabe complètent l'exemple console du cours.

## 3. Architecture : six classes et responsabilités

| Classe | Responsabilité | Attributs principaux | Méthodes importantes | Événements |
| --- | --- | --- | --- | --- |
| Main | Démarrer Swing et appliquer une présentation lisible | Aucun état métier | main, configureUi | Planification sur EDT |
| PaintFrame | Assembler l'interface et coordonner les commandes | drawing, language, actions, currentFile, status, busy | createActions, rebuildInterface, refreshState, newDocument, openDocument, save, canDiscard, performIo | Actions, fermeture, choix de langue/couleur/épaisseur |
| DrawingPanel | Conserver les pixels, afficher, dessiner et gérer l'historique | history/index, savedId/nextId, tool, color, strokeWidth, start/end, working | paintComponent, renderShape, drawSegment, commit, undo/redo, loadDocument, snapshot | MouseListener et MouseMotionListener ; Échap |
| DrawingTool | Représenter les huit outils sans chaînes ambiguës | key | key, values | Aucun |
| FileManager | Lire une image et écrire un PNG avec traitement des erreurs | Aucun état partagé | read, pngFile, writePng | Aucun : appelée par les commandes |
| LanguageManager | Charger les traductions et l'orientation | locale, messages | setLanguage, text, orientation | Appelée lors du choix de langue |

L'historique reste dans DrawingPanel pour éviter une architecture disproportionnée. Sa classe interne State est un simple couple image/identifiant, et non une septième classe publique. Les méthodes de pixels ne connaissent ni JFileChooser ni la fenêtre ; FileManager ne connaît ni menus ni langue. C'est une séparation simple entre surface de dessin, coordination graphique, stockage et traductions.

## 4. Fenêtre, composants et layouts en pratique

`JFrame` représente la fenêtre que l'utilisateur déplace, réduit ou ferme. `JPanel` est une partie de cette fenêtre : panneau racine, panneau d'outils, formulaire ou zone de dessin. Les deux sont utilisés ensemble.

Le panneau racine utilise BorderLayout : barre d'outils au NORTH, zone d'état au SOUTH, outils au LINE_START et JScrollPane au CENTER. LINE_START désigne le côté de début de lecture : gauche en français, droite en arabe. Le panneau central contient le document avec ses dimensions en pixels ; les barres de défilement donnent accès aux parties non visibles.

Les boutons radio sont regroupés dans un ButtonGroup : choisir un outil désélectionne le précédent. Les boutons de commandes et JMenuItem partagent des Action : changer le libellé ou activer/désactiver Annuler actualise les deux représentations. Une JComboBox contient les épaisseurs proposées. Les composants gardent des dimensions calculées par les layouts.

## 5. Cycle de dessin : presse, glissement, relâchement

1. `mousePressed` accepte seulement le bouton gauche à l'intérieur de l'image. Il mémorise le point de départ.
2. Pour crayon/gomme, une copie de l'image devient le tampon de travail. Chaque déplacement relie le point précédent au point courant. Une extrémité arrondie donne un trait continu ; un simple clic produit un disque.
3. Pour les formes, les coordonnées changent mais l'image enregistrée dans l'historique reste intacte. `paintComponent` affiche l'image puis la forme temporaire.
4. `mouseReleased` dessine le dernier segment ou la forme définitive dans une nouvelle image, puis enregistre **un seul état pour tout le geste**.
5. `repaint` demande à Swing de réafficher ; ce n'est pas un dessin immédiat dans le callback souris.
6. Échap abandonne le tampon/aperçu. L'image historique ne change pas.

Pour un rectangle ou une ellipse : `x = min(xDépart, xFin)`, `y = min(yDépart, yFin)`, `largeur = abs(xFin - xDépart)`, `hauteur = abs(yFin - yDépart)`. Le geste fonctionne donc aussi vers le haut ou la gauche.

Pour un cercle, largeur et hauteur sont égales au maximum des deux déplacements absolus. La direction du geste détermine de quel côté du point initial le carré englobant s'étend.

`Graphics` est le contexte graphique général fourni par Swing. `Graphics2D` ajoute les formes, les styles de traits et les indications de qualité. `paintComponent` appelle d'abord `super.paintComponent`. Chaque contexte créé par `create` ou `createGraphics` est libéré par `dispose`. Le code ne libère pas le Graphics original reçu de Swing.

## 6. Persistance et historique

BufferedImage contient les pixels du document, contrairement à un tracé éphémère sur l'écran. Déplacer une fenêtre au-dessus du dessin puis la retirer ne détruit donc pas l'image.

La liste history contient des images qui ne sont plus modifiées une fois validées. index indique l'état courant. Annuler décrémente cet index ; rétablir l'incrémente. Dessiner après une annulation supprime la branche future. Effacer ajoute une image blanche, ce qui permet d'annuler cet effacement.

Chaque état porte un identifiant unique. `savedId` mémorise celui qui a été enregistré ; `isModified` compare l'identifiant courant à savedId. Cela traite correctement l'annulation jusqu'à l'état sauvegardé, même si la liste est tronquée. Le simple changement d'outil, de langue ou de couleur ne modifie pas les pixels et ne rend pas le document modifié.

L'historique est borné par deux limites : 20 états au maximum et environ 64 Mio de tableaux de pixels. Ce choix évite qu'une longue session multiplie sans borne les copies d'images. Les états anciens sont supprimés en premier. Les dimensions sont limitées à quatre millions de pixels.

## 7. Fichiers, confirmations et erreurs

JFileChooser sélectionne un chemin : il ne lit ni n'enregistre les pixels. FileManager réalise cette deuxième étape avec ImageIO. Le filtre du sélecteur améliore la navigation mais ne garantit pas la validité d'un fichier : la lecture vérifie qu'un décodeur existe et que ses dimensions respectent la limite avant de charger les pixels.

Pour enregistrer, l'application écrit d'abord un fichier temporaire dans le même dossier. Elle ne remplace le fichier final qu'après succès de l'encodage PNG. Le déplacement atomique est essayé puis remplacé par un déplacement standard si le système ne le propose pas. Ce mécanisme limite les risques de fichier partiellement écrit ; il n'est pas une garantie absolue face à une panne matérielle.

Les IOException et erreurs de dimensions sont converties en messages traduits. Les réponses fermer/cancel du sélecteur ou d'un dialogue ne sont jamais interprétées comme un succès. « Enregistrer » depuis la confirmation de fermeture doit réussir pour permettre la fermeture. Un échec de lecture conserve l'ancien dessin.

Le travail d'E/S s'exécute dans SwingWorker : doInBackground lit/écrit hors EDT ; done ferme le dialogue d'attente. Un dialogue modal empêche d'éditer l'image durant ce travail, tout en laissant Swing repeindre l'interface. La confirmation et la mise à jour du modèle se font sur EDT.

## 8. Internationalisation détaillée

Exemple conceptuel :

```java
Locale locale = new Locale("fr");
ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
String titre = bundle.getString("save");
```

Avec la clé `save`, le bundle français renvoie « Enregistrer », l'anglais « Save » et l'arabe « حفظ ». Les fichiers possèdent les mêmes clés. `messages.properties` est le repli français. Les fichiers utilisent des échappements `\uXXXX` pour un chargement portable et sans ambiguïté d'encodage.

Lors d'un changement de langue :

1. setLanguage crée la Locale, recharge le bundle et actualise les libellés Swing standards pertinents dans UIManager.
2. rebuildInterface met à jour les noms des Action, recrée menus/barres/outils et réinstalle la **même** zone DrawingPanel.
3. La couleur, l'outil, les pixels, l'épaisseur, le chemin et l'historique restent dans leurs objets existants.
4. ComponentOrientation choisit LEFT_TO_RIGHT ou RIGHT_TO_LEFT selon la Locale ; les outils passent du côté adapté.
5. Le repère du document reste en haut à gauche. Changer de langue ne reflète pas l'image.

Les noms de langues « Français », « English », « العربية » sont volontairement affichés dans leur propre langue pour être reconnaissables. Les noms de fichiers et certaines métadonnées du système ne sont pas des textes de traduction de l'application. Le rendu des caractères arabes utilise les polices disponibles dans le JRE et le système ; les captures de validation montrent le rendu sur l'environnement Linux de test.

Pour une langue supplémentaire, créer un properties avec les mêmes clés. La sélection de langue est actuellement limitée volontairement aux trois entrées du menu ; pour en ajouter une quatrième, ajouter aussi son code et son nom dans rebuildInterface. Il faut alors reconstruire le JAR, puisque cette liste est dans le code. Modifier seulement les textes d'une langue existante ne nécessite aucune modification des classes Java, mais demande de remettre les ressources à jour dans le JAR.

## 9. Éléments au-delà des exemples du cours

| Élément | Statut par rapport au PDF | Justification |
| --- | --- | --- |
| SwingUtilities.invokeLater / EDT | Déjà utilisé dans les exemples de JToolBar et de transfert | Création de Swing sur son thread graphique |
| Graphics2D, BufferedImage, BasicStroke, ImageIO | Explicitement étudiés | Moteur de dessin et fichiers |
| ResourceBundle/Locale/properties | Explicitement étudiés | Internationalisation |
| Action, ActionMap/InputMap | Présents dans la partie transfert | Réutilisation pour commandes et Échap |
| AbstractAction | Formalisation pratique supplémentaire | Une commande partagée par menu et bouton |
| JSlider et ChangeListener | JSlider cité ; son utilisation détaillée dépasse les exemples | Sélecteur RGB simple et traduisible |
| ComponentOrientation/applyComponentOrientation | Complément à l'i18n, non développé dans les pages étudiées | Arabe de droite à gauche |
| UIManager et changement de police | UIManager déjà utilisé ; configuration étendue | Texte lisible, traduction des dialogues standards |
| SwingWorker, Callable | Non enseignés dans ce chapitre | Ne pas faire d'E/S longue sur EDT |
| ImageReader/ImageInputStream | Extension de la lecture ImageIO montrée | Contrôler les dimensions avant décodage |
| Files/Path et déplacement atomique | Non développés dans ce chapitre GUI | Remplacement de fichiers après encodage réussi |
| Historique d'images et identifiants d'états | Algorithme du projet, absent des exemples | Annuler/rétablir et suivi fiable des modifications |
| Tests Java, Robot, FutureTask | Outillage de validation supplémentaire | Tests reproductibles, absents du JAR livré |

Aucune de ces extensions n'exige une bibliothèque externe. Les lambdas, enums, collections et interfaces fonctionnelles sont du Java standard ; certaines dépassent les seules notions graphiques du PDF et doivent être expliquées à l'oral. Une lambda utilisée comme ActionListener représente simplement la méthode actionPerformed ; elle ne remplace pas une interface à plusieurs méthodes comme MouseListener.

## 10. Démonstration conseillée, en cinq minutes

1. Lancer le JAR et expliquer JFrame, le panneau racine et les trois layouts utilisés.
2. Tracer au crayon, changer couleur/épaisseur, puis faire un rectangle rempli et un cercle.
3. Maintenir une forme sans relâcher pour montrer l'aperçu ; utiliser Échap pour l'abandonner.
4. Effacer et annuler l'effacement. Montrer Ctrl+Z puis Ctrl+Y.
5. Enregistrer en PNG, modifier, puis annuler jusqu'à l'état sauvegardé : l'astérisque disparaît.
6. Passer en anglais puis en arabe. Montrer les nouveaux libellés, l'orientation et la conservation des pixels.
7. Ouvrir le PNG, redimensionner la fenêtre et montrer le défilement.
8. Modifier et fermer : expliquer les trois choix de la confirmation.

## 11. Questions possibles du professeur

**Pourquoi pas tout dans Main ?** Main ne démarre que l'application. Les autres classes isolent interface, pixels, fichiers et traduction ; cela rend le code plus facile à tester et à expliquer.

**Pourquoi BufferedImage plutôt que getGraphics dans mouseDragged ?** L'écran n'est pas une mémoire persistante. Le panneau doit pouvoir se redessiner après masquage ; l'image conserve les données nécessaires.

**Différence entre repaint et paintComponent ?** repaint demande un futur rafraîchissement ; Swing appelle ensuite paintComponent avec son contexte graphique. On n'appelle normalement pas paintComponent soi-même depuis l'application.

**Quelle différence entre draw et fill ?** draw trace le contour avec BasicStroke ; fill peint l'intérieur de la forme.

**La gomme supprime-t-elle des objets ?** Non : c'est un éditeur de pixels. Elle peint en blanc, sans sélection d'objets ni transparence.

**Pourquoi le menu et le bouton Annuler se désactivent ensemble ?** Ils partagent la même Action dont la propriété enabled suit la présence d'un état précédent.

**Peut-on changer de langue sans perdre le travail ?** Oui : seule l'interface est reconstruite. Le modèle de dessin et l'historique restent les mêmes.

**Pourquoi l'historique peut-il contenir moins de 20 états ?** La limite de mémoire est appliquée aussi. Une grande image consomme davantage par état.

**Que contient le JAR ?** Les classes compilées, le manifeste avec Main-Class et les properties. Il ne contient ni un JRE, ni les sources, ni les tests.

## 12. Répartition indicative du groupe

À trois : membre 1 interface et événements de commandes ; membre 2 dessin et historique ; membre 3 fichiers, traductions et intégration. À quatre, séparer fichiers/tests et internationalisation/documentation. Tous les membres doivent savoir suivre un geste souris jusqu'à sa validation, expliquer le chargement d'une traduction et reproduire la compilation. Remplacer cette répartition indicative par les contributions réellement réalisées avant la soutenance.
