# Séance 1 — Codes, URL et console

**Objectif** : à la fin de la séance, la console ShortLink crée des liens, les ouvre, compte les clics, et refuse les entrées dangereuses.

**Vous travaillez dans** : `Codes.java`, `Urls.java`, `Shell.java`, et les fichiers de test.

**Cours utile** : support de la séance 1, §2.6 (débordement), §3.2 (`switch`), §3.3 (boucles), §3.5 (méthodes `static`), §3.8 (`List` et `Map`), §4.1 (méthodes de `String`), §4.2 (`StringBuilder`), §4.3 (valider les entrées), §5 (Maven et JUnit).

| § | Exercice | Niveau | Durée |
|---|---|---|---|
| 0 | Prise en main | 🟢 | 25 min |
| 1 | Un code est-il valide ? | 🟢 | 15 min |
| 2 | Une URL est-elle acceptable ? | 🟢 🔒 | 30 min |
| 3 | Fabriquer un code | 🟢 | 30 min |
| 4 | Les commandes de la console | 🟢 | 45 min |
| 5 | Le domaine d'une URL | 🔵 🔒 | 30 min |
| 6 | Relire un code | 🔵 🔒 | 20 min |
| 7 | Alias et codes imprévisibles | 🟣 🔒 | |

Les durées sont celles d'un étudiant à l'aise : le socle 🟢 fait plus que les 2 h de TP. **En séance, visez la fin du §3.** Le §4, et ce que vous ne finissez pas, se termine à la maison, pour le [rendu](rendu-seance1.md).

---

## 0. 🟢 Prise en main (25 min)

1. Récupérez et ouvrez le projet (voir la [présentation](00-presentation.md)).
2. Lancez `mvn test` : `BUILD SUCCESS`, 41 tests, 40 « skipped ». Les lignes `WARNING: ... sun.misc.Unsafe ...` sont normales avec Java 25 : seul `BUILD FAILURE` est une erreur.
3. Mettez le projet sous Git tout de suite : dans IntelliJ, **VCS → Enable Version Control Integration → Git**, puis un premier commit « Projet de départ » (⌘K ou Ctrl+K).
4. Ouvrez `PremierTest.java` et lancez-le avec le ▶ vert à côté de `class PremierTest` : il passe. C'est à cela que ressemble un test JUnit.
5. Lancez la console : ▶ à côté de `main` dans `Main.java`. Tapez `help`, puis `add https://foodfast.fr`. **Le programme plante et s'arrête**, avec en rouge `UnsupportedOperationException: §2 : à écrire`. C'est normal : `add` a besoin de méthodes que vous n'avez pas encore écrites. La console ne marchera qu'à la fin du §3.
6. Lisez `Shell.java` en entier. Repérez :
   - le `switch` qui choisit la méthode selon le premier mot ;
   - `add(...)`, et ses quatre étapes commentées : c'est le modèle de toutes les commandes ;
   - les deux `Map` qui gardent les liens et les clics.

## 1. 🟢 Un code est-il valide ? (15 min)

**Fichier de test** : `CodesIsValidTest`. **À écrire** : `Codes.isValid(String code)`.

Un code valide a **entre 4 et 12 caractères**, et chaque caractère est dans `Codes.ALPHABET` (chiffres et lettres sans accent). `null` n'est pas valide.

**Pas à pas**

1. Commencez par les cas qui refusent tout de suite : `null`, trop court, trop long. Utilisez les constantes `MIN_LENGTH` et `MAX_LENGTH` plutôt que 4 et 12.
2. Puis parcourez le code caractère par caractère :
   ```java
   for (int i = 0; i < code.length(); i++) {
       char c = code.charAt(i);
       // c est-il dans ALPHABET ?
   }
   ```
3. Pour savoir si `c` est dans l'alphabet : `ALPHABET.indexOf(c)` rend sa position, ou **-1** s'il n'y est pas.

**Piège** : testez `code == null` **en premier**. `code.length()` sur `null` lance une `NullPointerException`.

**Pourquoi** 🔒 : le code vient de l'utilisateur, et il servira plus tard de chemin web (`/4c92`) et de clé en base de données. Accepter seulement des caractères connus ferme d'avance la porte à `../`, aux espaces et aux caractères spéciaux. On valide **par liste blanche** (ce qui est permis), jamais par liste noire (ce qui est interdit) : on oublie toujours un cas dans une liste noire.

## 2. 🟢 🔒 Une URL est-elle acceptable ? (30 min)

**Fichier de test** : `UrlsIsValidTest`. **À écrire** : `Urls.isValid(String url)`.

Les quatre règles sont dans la Javadoc de la méthode :

1. pas `null`, entre 1 et 2 000 caractères ;
2. commence par `http://` ou `https://`, en majuscules ou en minuscules ;
3. au moins un caractère après `://`, et ce n'est ni `/`, ni `?`, ni `#` (il faut un nom de site) ;
4. aucun espace, aucun caractère de contrôle.

**Pas à pas**

1. Règle 1 : comme au §1.
2. Règle 2 : `url.toLowerCase().startsWith("https://")` ignore la casse. Gardez dans une variable la **longueur** du préfixe trouvé (7 pour `http://`, 8 pour `https://`), ou 0 si aucun : elle sert à la règle 3.
3. Règle 3 : le caractère juste après le préfixe est `url.charAt(longueur)`. Vérifiez d'abord qu'il existe : `url.length() > longueur`.
4. Règle 4 : une boucle sur les caractères, avec `Character.isWhitespace(c)` et `Character.isISOControl(c)`.

**Pourquoi** 🔒 : un raccourcisseur qui accepte n'importe quoi devient une arme. Le test refuse `javascript:alert(document.cookie)` : un lien court vers ce texte exécuterait du code dans le navigateur de la victime, qui fait confiance au lien. Le retour à la ligne refusé par la règle 4 servira en séance 3 (journaux falsifiés).

## 3. 🟢 Fabriquer un code (30 min)

**Fichier de test** : `CodesEncodeTest`. **À écrire** : `Codes.encode(long number)`.

Chaque lien reçoit un numéro : 1 000 000, puis 1 000 001… Le code est ce numéro écrit **en base 62**, avec les 62 symboles de `ALPHABET`. C'est exactement comme écrire un nombre en base 10, avec 62 chiffres au lieu de 10.

**Comment on écrit un nombre dans une base** (ici en base 10, pour 472) :

| Étape | Calcul | Reste = chiffre |
|---|---|---|
| 1 | 472 % 10 = 2, puis 472 / 10 = 47 | 2 |
| 2 | 47 % 10 = 7, puis 47 / 10 = 4 | 7 |
| 3 | 4 % 10 = 4, puis 4 / 10 = 0 : on s'arrête | 4 |

On obtient les chiffres **à l'envers** : 2, 7, 4. Il faut retourner pour lire 472. En base 62, c'est pareil avec `% 62` et `/ 62`. Le chiffre de valeur 12 est `ALPHABET.charAt(12)`, soit `c`.

**Pas à pas**

1. Un nombre négatif : `throw new IllegalArgumentException("nombre négatif")`.
2. Le cas `0` à part : la boucle ne tournerait pas, et rendrait `""` au lieu de `"0"`.
3. La boucle :
   ```java
   StringBuilder code = new StringBuilder();
   while (number > 0) {
       // ajouter le symbole de valeur number % 62, puis diviser number par 62
   }
   return code.reverse().toString();
   ```
4. `number % 62` est un `long` : `charAt` attend un `int`. Convertissez : `(int) (number % 62)`.

Vérifiez à la main avec le test `theFirstLinkOfTheShell` : 1 000 000 donne `4c92`.

Maintenant, relancez la console : `add https://foodfast.fr` répond `Lien créé : 4c92`.

## 4. 🟢 Les commandes de la console (45 min)

**Fichier de test** : `ShellTest`. **À écrire** : quatre commandes dans `Shell.java`.

| Commande | Réponse | Erreurs |
|---|---|---|
| `open <code>` | l'URL longue, et un clic de plus | `Erreur : code invalide`, `Erreur : code inconnu` |
| `clicks <code>` | le nombre de clics : `2` | les mêmes |
| `delete <code>` | `Lien supprimé : 4c92` | les mêmes |
| `list` | une ligne par lien, dans l'ordre de création : `4c92 -> https://foodfast.fr (2 clics)`. Pas de lien : `(aucun lien)` | |

Avec un mauvais nombre d'arguments : `Erreur : usage : open <code>`, `Erreur : usage : list` (`list tout`)… avec `usage(...)`, déjà écrite.

**Pas à pas**

1. Commencez par `open`. Ajoutez une ligne au `switch` : `case "open" -> open(args);`
2. Écrivez la méthode `private String open(List<String> args)` en suivant les **quatre étapes** de `add` :
   1. le bon nombre d'arguments ;
   2. un code valide (`Codes.isValid`), puis un code connu (`links.containsKey(code)`) ;
   3. le travail : un clic de plus dans `clicks` ;
   4. la réponse : `links.get(code)`.
3. Faites passer les tests de `open`, puis faites `clicks` et `delete`.
   - `clicks` doit rendre une `String`, mais `clicks.get(code)` est un `Integer` : `return clicks.get(code);` ne compile pas. Convertissez avec `String.valueOf(...)`.
   - Le champ `clicks` et la méthode `clicks(...)` ont le même nom : Java les distingue, `clicks.get(...)` désigne la `Map`.
   - Vous allez écrire trois fois la même vérification du code : déplacez-la dans une méthode à vous, par exemple `private String checkCode(String code)`, qui rend le message d'erreur ou `null` si tout va bien.
4. `list` : parcourez `links` avec
   ```java
   for (Map.Entry<String, String> link : links.entrySet()) {
       String code = link.getKey();
       String url = link.getValue();
   }
   ```
   et construisez le texte avec un `StringBuilder`. Les lignes sont séparées par `'\n'`, sans `'\n'` après la dernière : ajoutez le `'\n'` **avant** chaque ligne, sauf la première.
   ```java
   if (!text.isEmpty()) {   // text est votre StringBuilder : déjà une ligne ?
       text.append('\n');
   }
   ```

**Pièges**
- `0 clic` et `1 clic`, mais `2 clics` : regardez bien le test.
- `delete` supprime le lien **et** ses clics.
- Un code supprimé n'est jamais redonné : le test `deleteDoesNotReuseCodes` le vérifie, et `add` le fait déjà. Pourquoi est-ce important ? Quelqu'un a peut-être imprimé l'ancien lien sur une affiche.

Essayez ensuite toutes vos commandes dans la console.

## 5. 🔵 🔒 Le domaine d'une URL (30 min)

**Fichier de test** : `DomainTest`. **À écrire** : `Urls.domain(String url)`, puis la commande `domains`.

`https://www.FoodFast.fr:8443/menu?x=1` a pour domaine `foodfast.fr` : en minuscules, sans `www.`, sans port (`:8443`), sans chemin.

**Pas à pas** : enlevez le préfixe `https://`, coupez au premier `/`, puis au premier `?`, puis au premier `#` (trois coupes successives, une petite méthode à vous évite de l'écrire trois fois), gardez ce qui suit le dernier `@` (voir le piège ci-dessous), coupez au `:`, passez en minuscules, enlevez `www.` au début. Dans cet ordre : `https://ada:secret@foodfast.fr` contient deux `:`. Les méthodes utiles : `indexOf`, `substring`, `toLowerCase`, `startsWith`.

🔒 **Le piège de l'arobase.** Dans `https://foodfast.fr@evil.example/connexion`, ce qui précède `@` est un **identifiant de connexion**, pas le site. Le navigateur va sur `evil.example`. C'est une technique d'hameçonnage réelle : le lecteur voit `foodfast.fr` et fait confiance au lien. Le domaine est ce qui suit le **dernier** `@` (`lastIndexOf`).

Puis la commande `domains` : le nombre de liens par domaine, par ordre alphabétique, une ligne par domaine.

```
example.com 1
foodfast.fr 2
```

Pas de lien : `(aucun lien)`. Les URL sont dans `links.values()`. Comptez dans une `Map<String, Integer>`. Pour l'ordre alphabétique, cherchez ce que fait une `TreeMap` (même usage qu'une `HashMap`, les clés sont triées).

## 6. 🔵 🔒 Relire un code (20 min)

**Fichier de test** : `CodesDecodeTest`. **À écrire** : `Codes.decode(String code)`, l'inverse de `encode`.

Pour relire `472` en base 10 : 0 × 10 + 4 = 4, puis 4 × 10 + 7 = 47, puis 47 × 10 + 2 = 472. En base 62, multipliez par 62, et la valeur d'un symbole est `ALPHABET.indexOf(c)`.

🔒 **Le débordement.** Un code de 12 caractères peut dépasser le plus grand `long`. En Java, le calcul ne s'arrête pas : il **déborde** et donne un nombre négatif, sans aucune erreur (support S1, §2.6). Un code valide qui désigne silencieusement le lien n° -8 000 000 000 est un bug sérieux. Cherchez les méthodes de `Math` qui lèvent une exception au lieu de déborder (support §2.6), et transformez cette exception en `IllegalArgumentException` (`try`/`catch`, support §4.6) :

```java
try {
    // le calcul qui peut déborder
} catch (ArithmeticException e) {
    throw new IllegalArgumentException("code trop grand");
}
```

## 7. 🟣 🔒 Alias et codes imprévisibles (bonus)

**Fichier de test** : `BonusTest`.

1. **Les alias.** `add https://foodfast.fr/promo promo` crée le lien avec le code `promo`, s'il est valide et libre. L'URL se vérifie d'abord (`Erreur : URL invalide`), puis l'alias (`Erreur : code invalide`, `Erreur : code déjà pris`). Attention : un alias peut prendre un code que la suite (`4c92`, `4c93`…) donnera plus tard. Que doit faire `add` sans alias ? Le test `anAliasCannotStealAFutureSequentialCode` vous le dit.
2. **Les codes imprévisibles.** Nos codes se suivent : avec `4c92`, on devine `4c93`, `4c94`… et on peut visiter **tous** les liens créés, y compris ceux qui n'étaient destinés qu'à une seule personne. Des services réels ont exposé des documents privés de cette façon. Écrivez `Codes.random(int length)` : un code de `length` symboles tirés au hasard. Utilisez `SecureRandom`, pas `Random` : la suite de `Random` se devine à partir de quelques valeurs observées (support S1, défi 6).

Avec 8 symboles, il y a 62⁸, soit environ 218 000 milliards de codes possibles. Combien d'essais faut-il pour tomber sur un des 1 000 liens existants ?

3. **La règle cassée.** Le §4 dit : « un code supprimé n'est jamais redonné ». Avec les alias, ce n'est plus vrai. Trouvez une suite de commandes qui redonne un code supprimé (indice : un alias choisi parmi les codes que la suite donnera), écrivez le test qui le montre, puis corrigez `add`.

---

## Avant de partir

- [ ] `mvn test` passe.
- [ ] Les tests des §1 à §4 sont activés (plus de `@Disabled`) et passent.
- [ ] Au moins un test à vous dans chaque fichier de test activé.
- [ ] Tout est commité.

Ce qui est attendu pour le rendu, et comment mettre votre projet sur GitHub : [rendu de la séance 1](rendu-seance1.md).
