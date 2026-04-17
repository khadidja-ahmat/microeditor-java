# Micro-editeur de figures geometriques

Projet d'architecture logicielle : realisation d'un micro-editeur de dessin vectoriel permettant de creer, modifier, combiner, sauvegarder et recharger des figures geometriques.

L'application est developpee en Java 21 avec JavaFX et Maven.

## Fonctionnalites

- Ajouter des figures depuis une toolbar vers le whiteboard par glisser-deposer.
- Supprimer un element de la toolbar via la poubelle.
- Creer des rectangles et des polygones reguliers.
- Selectionner une ou plusieurs figures avec un rectangle de selection ou avec `Ctrl + clic`.
- Deplacer des figures, groupes et sous-groupes.
- Grouper plusieurs objets.
- Creer des sous-groupes et groupes imbriques.
- Degrouper un groupe en reinserant ses enfants dans son groupe parent.
- Modifier les proprietes d'une figure ou d'un groupe via une boite de dialogue.
- Ajouter un objet ou un groupe personnalise dans la toolbar.
- Supprimer des objets du dessin via la poubelle.
- Annuler et refaire les operations avec undo/redo.
- Sauvegarder et charger un document.
- Sauvegarder automatiquement l'etat de la toolbar entre deux lancements.

## Figures disponibles

### Rectangle

Proprietes gerees :

- largeur
- hauteur
- position
- rotation
- centre de rotation
- translation
- couleur
- arrondi des bords

### Polygone regulier

Proprietes gerees :

- nombre de cotes
- longueur/rayon utilise pour les sommets
- position
- rotation
- centre de rotation
- translation
- couleur

## Lancement

Verifier Java et Maven :

```powershell
java -version
mvn -version
```

Compiler le projet :

```powershell
mvn clean compile
```

Lancer l'application :

```powershell
mvn javafx:run
```

Lancer les tests :

```powershell
mvn clean test
```

## Utilisation

### Ajouter une figure au dessin

1. Cliquer sur une figure dans la toolbar.
2. La glisser vers le whiteboard.
3. Relacher au-dessus de la zone blanche.

Une nouvelle instance de la figure est ajoutee au document.

### Supprimer un element de la toolbar

1. Glisser un element de la toolbar.
2. Le deposer sur la poubelle.

L'element est retire de la toolbar.

### Selectionner des objets

Deux modes de selection sont disponibles :

- rectangle de selection sur le whiteboard ;
- `Ctrl + clic` pour selectionner plusieurs objets successivement.

### Grouper des objets

1. Selectionner au moins deux objets.
2. Faire clic droit.
3. Choisir `Group`.

Les objets selectionnes deviennent les enfants d'un nouveau groupe.

### Creer un sous-groupe

Il est possible de regrouper des groupes avec d'autres objets.

Exemple :

```text
Document
└── Grand groupe
    ├── Sous-groupe
    │   ├── Rectangle 1
    │   └── Rectangle 2
    └── Polygone
```

### Degrouper un groupe

1. Selectionner un groupe.
2. Faire clic droit.
3. Choisir `De-group`.

Les enfants du groupe sont reinseres dans le groupe parent sans changer de position.

Exemple :

```text
Avant :
Document
└── Groupe A
    ├── Rectangle 1
    └── Groupe B
        ├── Rectangle 2
        └── Polygone

Apres De-group sur Groupe B :
Document
└── Groupe A
    ├── Rectangle 1
    ├── Rectangle 2
    └── Polygone
```

### Deplacer un grand groupe

Pour deplacer un groupe contenant des sous-groupes :

1. Cliquer dans le blanc pour vider la selection.
2. Faire un rectangle de selection autour du grand groupe.
3. Glisser depuis une figure du groupe pour deplacer l'ensemble.

Pour manipuler un sous-groupe :

1. Cliquer dans le blanc pour vider la selection.
2. Cliquer directement sur une figure du sous-groupe.
3. Deplacer le sous-groupe ou utiliser `De-group`.

### Modifier les proprietes

1. Selectionner une figure ou un groupe.
2. Faire clic droit.
3. Choisir `Edit`.
4. Modifier les champs voulus.

Boutons disponibles :

- `Appliquer` : applique les modifications sans fermer la boite.
- `OK` : applique les modifications et ferme la boite.
- `Cancel` : annule les modifications effectuees depuis l'ouverture de la boite.

### Ajouter un objet dans la toolbar

1. Selectionner une figure ou un groupe sur le whiteboard.
2. Glisser la selection vers la toolbar.
3. Relacher sur la toolbar.

Un nouvel element reutilisable est ajoute a la toolbar.

### Sauvegarder et charger

- `Save` sauvegarde le document courant.
- `Load` remplace le document courant par un document sauvegarde.

La toolbar est sauvegardee automatiquement a la fermeture de l'application et rechargee au lancement suivant.

## Architecture

Le projet separe les donnees de la scene, les commandes metier, la persistance et l'interface graphique.

Packages principaux :

- `model` : figures, groupes, document, selection et toolbar.
- `ui` : interface JavaFX, whiteboard, toolbar et dialogues.
- `command` : actions annulables et rejouables.
- `persistence` : sauvegarde et chargement.
- `factory` : creation centralisee des figures.
- `observer` : notification des changements du modele.

## Patrons de conception

### Composite

Les figures simples et les groupes partagent l'interface `Figure`.

Un `GroupFigure` peut contenir plusieurs `Figure`, y compris d'autres `GroupFigure`. Cela permet de representer des groupes, sous-groupes et sous-sous-groupes.

Classes concernees :

- `Figure`
- `RectangleFigure`
- `PolygonFigure`
- `GroupFigure`

### Command

Chaque action importante est encapsulee dans une commande avec `execute()` et `undo()`.

Le `CommandManager` gere les piles undo et redo.

Commandes principales :

- `AddFigureCommand`
- `RemoveFigureCommand`
- `MoveFigureCommand`
- `EditFigureCommand`
- `GroupFiguresCommand`
- `UngroupFigureCommand`
- `AddToolbarFigureCommand`
- `RemoveToolbarFigureCommand`

### Memento

`FigureState` conserve l'etat d'une figure avant modification.

Ce patron est utilise pour annuler correctement les editions de proprietes.

### Factory

`FigureFactory` centralise la creation des figures initiales.

L'interface ne depend pas directement des constructeurs concrets.

### Observer

Le modele notifie l'interface lorsqu'il change.

Cela permet de rafraichir le whiteboard ou la toolbar apres une modification.

Classes concernees :

- `ObservableModel`
- `ObservableSupport`
- `ModelObserver`
- `ModelEvent`

## Undo / Redo

Le mecanisme d'undo/redo couvre les operations principales :

- ajout d'une figure au document ;
- suppression d'une figure ;
- deplacement ;
- edition de proprietes ;
- groupage ;
- degroupage ;
- ajout d'un element dans la toolbar ;
- suppression d'un element de la toolbar.

Les actions composees, comme le deplacement de plusieurs figures, sont regroupees dans une seule action utilisateur avec `CompositeCommand`.

## Persistance

La sauvegarde utilise la serialisation Java.

Deux types d'etat sont geres :

- le document courant, sauvegarde et charge via `Save` / `Load` ;
- la toolbar, sauvegardee automatiquement a la fermeture puis rechargee au demarrage.

## Tests

Les tests sont places dans `src/test/java`.

Ils verifient notamment :

- undo/redo apres ajout ;
- undo apres suppression ;
- undo/redo apres deplacement ;
- undo apres edition ;
- undo apres groupage ;
- undo apres degroupage ;
- degroupage d'un sous-groupe dans son groupe parent ;
- ajout et suppression dans la toolbar ;
- sauvegarde et chargement.

Commande recommandee :

```powershell
mvn clean test
```

## Demonstration 

Scenario simple pour presenter le projet :

1. Glisser un rectangle depuis la toolbar vers le whiteboard.
2. Glisser un polygone vers le whiteboard.
3. Modifier leurs proprietes avec `Edit`.
4. Selectionner les deux objets puis cliquer sur `Group`.
5. Creer un deuxieme groupe, puis regrouper les deux groupes pour obtenir un groupe imbrique.
6. Deplacer le grand groupe.
7. Selectionner un sous-groupe puis utiliser `De-group`.
8. Utiliser `Undo` puis `Redo`.
9. Ajouter un groupe personnalise dans la toolbar.
10. Sauvegarder le document, le recharger, puis relancer l'application pour montrer que la toolbar est conservee.

## Auteurs

- Khadidja AHMAT HASSAN
- Djenaba Barry
