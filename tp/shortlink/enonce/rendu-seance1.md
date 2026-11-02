# Rendu de la séance 1

**À rendre : la veille de la séance 2, à 23h59.** Vous avez un mois : terminez à la maison ce que vous n'avez pas fini en séance.

**Ce qu'on rend** : votre projet ShortLink, sur un **dépôt GitHub privé** partagé avec l'enseignant.

---

## 1. Ce qui doit fonctionner

Les exercices 🟢 de l'[énoncé de la séance 1](seance1.md) :

| Exercice | Fichier de test | À écrire |
|---|---|---|
| §1 | `CodesIsValidTest` | `Codes.isValid` |
| §2 🔒 | `UrlsIsValidTest` | `Urls.isValid` |
| §3 | `CodesEncodeTest` | `Codes.encode` |
| §4 | `ShellTest` | `open`, `clicks`, `delete`, `list` dans `Shell` |

Les exercices 🔵 et 🟣 (§5 à §7) sont facultatifs. Ils sont regardés en bonus, jamais pénalisés.

## 2. Comment c'est évalué

**Le rendu lui-même** est noté sur trois points simples :

| Critère | Comment on le vérifie |
|---|---|
| **Rendu à l'heure** | dépôt **privé**, enseignant invité, dernier commit poussé avant l'échéance |
| **Le projet compile et les tests passent** | `mvn test` affiche `BUILD SUCCESS`, et il ne reste **aucun `@Disabled`** dans les fichiers de test des §1 à §4. Vérifiez-le en dernier, juste avant de rendre. |
| **Des tests à vous** | au moins un test écrit par vous dans chacun de ces quatre fichiers, là où c'est marqué |

**Ce qui compte le plus vient ensuite, en séance 2** :

- **3 minutes avec l'enseignant**, sur votre propre projet, sans IA : il choisit une de vos méthodes, vous **expliquez** comment elle fonctionne, puis vous la **modifiez** selon une nouvelle règle.
- **Le jalon de fin de séance 2** : votre code passe les tests fournis, plus des tests de correction que vous n'avez pas. Ne codez pas « pour les tests » : codez ce que dit la Javadoc et l'énoncé.

Pour vous préparer : faites au moins un commit par exercice avec un message clair (`§3 : encode`, pas `update`), et **relisez votre code** avant la séance 2.

L'IA est autorisée. Mais vous êtes évalués sans elle : écrivez du code que vous savez expliquer, ligne par ligne.

## 3. Mettre votre projet sur GitHub

À faire **une seule fois**, idéalement dès la fin de la séance 1.

1. Créez un compte sur [github.com](https://github.com) si vous n'en avez pas (gratuit).
2. Dans IntelliJ, ouvrez votre projet ShortLink, puis menu **Git → GitHub → Share Project on GitHub**. Si le menu **Git** n'existe pas : **VCS → Share Project on GitHub**.
3. Connectez votre compte GitHub quand IntelliJ le demande.
4. Nom du dépôt : `shortlink-<nom>-<prénom>`, par exemple `shortlink-lovelace-ada`. **Cochez « Private ».** Cliquez sur **Share**.
5. Sur github.com, ouvrez votre dépôt → **Settings → Collaborators → Add people**, et invitez l'enseignant : **`mehdi-elke`**.

Ensuite, à chaque exercice terminé :

- **commit** : ⌘K (macOS) ou Ctrl+K (Windows, Linux), un message clair, puis **Commit** ;
- **push** : ⌘⇧K ou Ctrl+Maj+K, pour envoyer les commits sur GitHub.

> 🔒 Le dépôt doit rester **privé** : un dépôt public permet à tout le monde de copier votre travail, et la copie est sanctionnée pour les deux personnes.

En ligne de commande, si vous préférez :

```text
git init                              # inutile si vous l'avez fait au §0 de la séance 1
git add .
git commit -m "Projet de départ"      # idem
git branch -M main
git remote add origin https://github.com/<votre-compte>/shortlink-<nom>-<prénom>.git
git push -u origin main
```

Créez d'abord le dépôt **privé et vide** sur github.com (bouton **New**), puis invitez l'enseignant comme à l'étape 5.

## 4. Avant de rendre : la checklist

- [ ] `mvn test` affiche `BUILD SUCCESS`.
- [ ] Plus de `@Disabled` dans `CodesIsValidTest`, `UrlsIsValidTest`, `CodesEncodeTest` et `ShellTest`.
- [ ] Un test à vous dans chacun de ces fichiers.
- [ ] Tout est commité **et poussé** : rechargez la page de votre dépôt sur github.com et vérifiez que votre dernier commit y apparaît.
- [ ] L'enseignant a été invité sur le dépôt.

Le dépôt est évalué **tel qu'il est sur GitHub à l'heure limite**. Un commit fait mais pas poussé n'existe pas.

## 5. Vous êtes bloqué ?

- Relisez l'exercice : chaque exercice indique les sections utiles du support de la séance 1.
- Lisez le test qui échoue : son nom et la valeur attendue disent ce qui manque.
- Une question qui bloque plus d'une heure : écrivez à l'enseignant **avec** le message d'erreur complet et ce que vous avez déjà essayé.
- Un rendu incomplet vaut mieux qu'un rendu absent : poussez ce qui fonctionne.
