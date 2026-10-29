# Rendu de la séance 1

**À rendre : la veille de la séance 2, à 23h59.** Vous avez un mois : terminez à la maison ce que vous n'avez pas fini en séance.

**Ce qu'on rend** : votre projet Mini-Redis, sur un **dépôt GitHub privé** partagé avec l'enseignant.

---

## 1. Ce qui doit fonctionner

Les tâches 🟢 de l'[énoncé de la séance 1](seance1.md) :

| Section de `tests/s1.txt` | Commandes |
|---|---|
| 🟢 PING et ECHO | `PING`, `ECHO` |
| 🟢 DEL et EXISTS | `DEL`, `EXISTS` |
| 🟢 STRLEN, APPEND, MGET | `STRLEN`, `APPEND`, `MGET` |
| 🟢 INCR, DECR, INCRBY, DECRBY | `INCR`, `DECR`, `INCRBY`, `DECRBY`, avec le débordement 🔒 |

Les sections 🔵 et 🟣 sont facultatives. Elles sont regardées en bonus, jamais pénalisées.

## 2. Comment c'est évalué

**Le rendu lui-même** est noté sur trois points simples :

| Critère | Comment on le vérifie |
|---|---|
| **Rendu à l'heure** | dépôt **privé**, enseignant invité, dernier commit poussé avant l'échéance |
| **Le projet compile et les tests passent** | `mvn test` affiche `BUILD SUCCESS`. Vérifiez-le en dernier, juste avant de rendre. |
| **Chaque commande a au moins un test JUnit** | dans `CommandProcessorTest`, avec un cas normal **et** un cas d'erreur |

**Ce qui compte le plus vient ensuite, en séance 2** :

- **3 minutes avec l'enseignant**, sur votre propre projet, sans IA : il choisit une de vos commandes, vous **expliquez** comment elle fonctionne, puis vous la **modifiez** selon une nouvelle règle.
- **Le jalon de fin de séance 2** : votre serveur est comparé à Valkey (`java tools/Compare.java tests/s1.txt`, sections 🟢), plus des commandes de correction qui ne sont pas dans le fichier. Ne codez pas « pour le fichier » : codez comme Valkey.

Pour vous préparer : travaillez dans `CommandProcessor.java` et `CommandProcessorTest.java` seulement, faites au moins un commit par commande avec un message clair (`INCR et DECR`, pas `update`), et **relisez votre code** avant la séance 2.

L'IA est autorisée. Mais vous êtes évalués sans elle : écrivez du code que vous savez expliquer, ligne par ligne.

## 3. Mettre votre projet sur GitHub

À faire **une seule fois**, idéalement dès la fin de la séance 1.

1. Créez un compte sur [github.com](https://github.com) si vous n'en avez pas (gratuit).
2. Dans IntelliJ, ouvrez votre projet Mini-Redis, puis menu **Git → GitHub → Share Project on GitHub**. Si le menu **Git** n'existe pas : **VCS → Share Project on GitHub**.
3. Connectez votre compte GitHub quand IntelliJ le demande.
4. Nom du dépôt : `miniredis-<nom>-<prénom>`, par exemple `miniredis-lovelace-ada`. **Cochez « Private ».** Cliquez sur **Share**.
5. Sur github.com, ouvrez votre dépôt → **Settings → Collaborators → Add people**, et invitez l'enseignant : **`mehdi-elke`**.

Ensuite, à chaque tâche terminée :

- **commit** : ⌘K (macOS) ou Ctrl+K (Windows, Linux), un message clair, puis **Commit** ;
- **push** : ⌘⇧K ou Ctrl+Maj+K, pour envoyer les commits sur GitHub.

> 🔒 Le dépôt doit rester **privé** : un dépôt public permet à tout le monde de copier votre travail, et la copie est sanctionnée pour les deux personnes.

En ligne de commande, si vous préférez :

```text
git init
git add .
git commit -m "Projet de départ"
git branch -M main
git remote add origin https://github.com/<votre-compte>/miniredis-<nom>-<prénom>.git
git push -u origin main
```

Créez d'abord le dépôt **privé et vide** sur github.com (bouton **New**), puis invitez l'enseignant comme à l'étape 5.

## 4. Avant de rendre : la checklist

- [ ] `mvn test` affiche `BUILD SUCCESS`.
- [ ] `java tools/Compare.java tests/s1.txt` : les sections 🟢 sont `✓`.
- [ ] Une commande = au moins un test JUnit.
- [ ] Tout est commité **et poussé** : rechargez la page de votre dépôt sur github.com et vérifiez que votre dernier commit y apparaît.
- [ ] L'enseignant a été invité sur le dépôt.

Le dépôt est évalué **tel qu'il est sur GitHub à l'heure limite**. Un commit fait mais pas poussé n'existe pas.

## 5. Vous êtes bloqué ?

- Relisez le support de la séance 1 : chaque énoncé indique les sections utiles.
- Demandez à Valkey : la réponse attendue est toujours celle de Valkey.
- Une question qui bloque plus d'une heure : écrivez à l'enseignant **avec** le message d'erreur complet et ce que vous avez déjà essayé.
- Un rendu incomplet vaut mieux qu'un rendu absent : poussez ce qui fonctionne.
