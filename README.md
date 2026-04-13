# Microeditor JavaFX

Projet d'architecture logicielle : micro-editeur de figures geometriques.

## Objectif

L'application permet de creer un dessin vectoriel simple a partir de figures geometriques.

Fonctionnalites principales :

- ajouter un rectangle ou un polygone depuis une toolbar
- positionner les figures sur un whiteboard
- selectionner une ou plusieurs figures
- deplacer les figures avec la souris
- grouper et degrouper des figures
- modifier les proprietes d'une figure
- supprimer des figures
- annuler et refaire une operation avec undo/redo
- sauvegarder et charger un document
- conserver la toolbar entre deux lancements

## Lancement du projet

Le projet utilise Java 21, JavaFX et Maven.

Verifier la version de Java :

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
mvn test
```

## Utilisation

- Glisser le rectangle ou le polygone depuis la toolbar gauche vers la feuille blanche.
- Cliquer sur une figure pour la selectionner.
- Faire un clic-glisse sur le fond blanc pour selectionner plusieurs figures avec un rectangle de selection.
- Utiliser `Ctrl + clic` pour selectionner plusieurs figures.
- Deplacer une figure en gardant le clic appuye.
- Relacher une figure selectionnee sur la toolbar pour l'ajouter a la toolbar.
- Relacher une figure selectionnee sur la poubelle pour la supprimer du dessin.
- Cliquer sur `Group` pour grouper plusieurs figures selectionnees.
- Cliquer sur `De-group` pour degrouper un groupe selectionne.
- Cliquer sur `Edit` pour modifier les proprietes d'une figure, puis utiliser `Appliquer`, `OK` ou `Cancel`.
- Cliquer sur la poubelle pour supprimer les figures selectionnees.
- Glisser un element de la toolbar vers la poubelle, ou faire un clic droit dessus, pour le supprimer de la toolbar.
- Utiliser les boutons undo/redo pour annuler ou refaire une action.
- Utiliser Save/Load pour sauvegarder ou charger un document.
- Fermer puis rouvrir l'application recharge automatiquement la toolbar sauvegardee a la fermeture.

## Architecture

Le projet est separe en plusieurs packages :

- `model` : donnees du dessin, figures, groupes, selection, toolbar
- `factory` : creation centralisee des figures
- `observer` : notification des changements du modele
- `command` : actions annulables et rejouables
- `persistence` : sauvegarde et chargement
- `ui` : interface JavaFX et rendu graphique

Architecture simplifiee :

```text
ui ---------------> command
 |                    |
 |                    v
 |----------------> model <----------- factory
 |                    ^
 |                    |
 v                    |
persistence ----------|

model ---> observer
```

## Design patterns utilises

### Composite

Les figures simples et les groupes partagent l'interface `Figure`.
Un `GroupFigure` peut contenir plusieurs figures, y compris d'autres groupes.

Classes principales :

- `Figure`
- `RectangleFigure`
- `PolygonFigure`
- `GroupFigure`

### Factory

La classe `FigureFactory` centralise la creation des figures.
L'interface ne cree pas directement les objets avec les constructeurs.

### Observer

Le modele notifie l'interface quand son etat change.
Cela permet de rafraichir le whiteboard apres une modification.

Classes principales :

- `ModelObserver`
- `ModelEvent`
- `ObservableModel`
- `ObservableSupport`

### Command

Chaque action importante est representee par une commande avec `execute()` et `undo()`.
Le `CommandManager` gere les piles undo et redo.

Exemples :

- `AddFigureCommand`
- `RemoveFigureCommand`
- `AddToolbarFigureCommand`
- `RemoveToolbarFigureCommand`
- `MoveFigureCommand`
- `EditFigureCommand`
- `GroupFiguresCommand`
- `UngroupFigureCommand`

### Memento

La classe `FigureState` sauvegarde l'etat d'une figure avant modification.
Elle est utilisee notamment pour annuler une edition.

## Tests

Les tests sont places dans `src/test/java`.

Tests actuels :

- undo/redo apres ajout
- undo apres suppression
- undo/redo apres deplacement
- undo apres edition
- undo apres groupement
- undo apres degroupe
- undo/redo apres ajout dans la toolbar
- undo apres suppression dans la toolbar
- sauvegarde et chargement d'un document

Commande :

```powershell
mvn test
```

## Auteur

Khadidja AHMAT HASSAN
