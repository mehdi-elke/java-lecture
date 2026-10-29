# Cadrage pédagogique — Cours Java (rentrée 2026/2027)

Document de référence : il fixe **ce que le cours doit transmettre**. Toute réécriture des supports, du TP ou de l'évaluation part d'ici.

## Contexte

- **Public** : M1 informatique, hétérogène. Des profils cybersécurité avec peu de bases en développement, et des profils avec un an d'alternance.
- **Volume** : **16h au total**, soit 4 séances de 4h. Cours, pratique et évaluation (plusieurs notes par élève) tiennent dans ce volume.
- **Version** : **Java 25 (LTS)**. On enseigne uniquement des fonctionnalités finales, jamais de preview.
- **Cours magistral** : environ **60 à 75 min par séance**. Le reste du temps va à la pratique et à l'évaluation.
- **Domaine d'illustration** : **FoodFast** (plateforme de livraison de repas). Le TP sera conçu séparément et pourra porter sur un autre domaine : les supports ne font donc pas référence à des questions de TP précises.

## Objectif global

> À la fin du module, l'étudiant sait écrire en Java moderne une petite application métier **structurée en objets et testée**. Il sait aussi **repérer et corriger les erreurs classiques de robustesse et de sécurité**.

## Acquis d'apprentissage (ce qui est évalué)

| # | L'étudiant est capable de… | Séance |
|---|---|---|
| AA1 | Écrire, compiler et exécuter un programme Java (types, conditions, boucles, méthodes), puis le construire et le tester avec Maven et JUnit | S1 |
| AA2 | Modéliser un domaine avec des classes, des `record` et des `enum`, en appliquant l'encapsulation et des invariants | S2 |
| AA3 | Utiliser l'héritage, les interfaces et le polymorphisme | S2 |
| AA4 | Choisir et utiliser la bonne collection (`List`, `Set`, `Map`) et expliquer le rôle d'`equals`/`hashCode` | S2 |
| AA5 | Transformer et agréger des données avec les lambdas et l'API Stream | S3 |
| AA6 | Gérer les erreurs avec des exceptions métier, sans masquer les erreurs ni divulguer d'information | S3 |
| AA7 | Identifier et corriger une race condition et une injection SQL | S4 |

## Décisions prises

1. **Séance 4** : on garde la concurrence ET JDBC, chacune vue rapidement (environ 30 min) et sous l'angle « le bug et la faille ».
2. **Pas de module de remise à niveau en autonomie** : les étudiants ne le feraient pas. La séance 1 reprend les bases en classe. Pour les étudiants avancés, le support détaillé de S1 propose des défis 🟣 pour qu'ils ne s'ennuient pas.
3. **Héritage** : abordé en cours (`extends`, `super`, `@Override`, classe abstraite, et « composition plutôt qu'héritage »), puis approfondi dans le support détaillé.
4. **Sécurité** : un **fil rouge léger**, avec au moins un point 🔒 par séance. En 2026, une base en sécurité est obligatoire pour tout développeur.
5. **Deux supports par séance** :
   - **Slides** (`cours/slides/`) pour la présentation : peu de texte, du code court, une idée par slide.
   - **Support détaillé** (`cours/supports/`) à partager : explications complètes, code complet et compilé, approfondissements et bonus.
6. **IA : autorisée, mais l'évaluation porte sur ce qu'elle ne fait pas à la place de l'étudiant** (décidé le 2026-10-29, voir « IA et évaluation » ci-dessous).
7. **Évaluation multi-notes** : QCM diagnostique non noté en fin de S1 ; rendu S1 avant S2 ; QCM en début de S2, S3, S4 ; vérification de 3 min en S2 ; jalons en fin de S2 et S4 ; explication de 3 min en S4. Barème et grilles : `prive/tp-miniredis/README.md` §5.
8. **Séance de 4h = 3h30 effectives** : deux pauses de 15 min.

## IA et évaluation

**Constat.** En 2026, un TP qui fonctionne ne prouve plus que l'étudiant sait coder : une IA le produit en quelques minutes. Évaluer le code rendu revient à évaluer l'accès à l'outil, et creuse l'écart entre ceux qui paient un abonnement et les autres.

**Le vrai risque pédagogique n'est pas la triche**, c'est que les débutants sautent l'étape où l'on apprend : rester bloqué, chercher, comprendre. Ils obtiennent un code qui marche sans le modèle mental qui va avec, et le découvrent en S3 ou S4, quand le projet grossit.

**Ce qu'on évalue donc** : ce que l'étudiant sait faire **devant l'enseignant, sans IA** :
- **prédire** ce que fait un code (QCM « prédisez la sortie ») ;
- **expliquer** son propre code ;
- **modifier** ce code pour une nouvelle règle ;
- **juger** si une réponse est juste, avec un test ou la référence Valkey.

Le code rendu compte peu en lui-même (rendu S1 : 5 %, fait / `mvn test` passe). Les passages de 3 min en S2 et S4 (banque de questions : `prive/tp-miniredis/verification-s2.md`) et les QCM portent l'essentiel.

**Règles annoncées en S1** (slide « Les règles du jeu ») :
1. L'IA est autorisée ; vous êtes évalués sans elle.
2. Cherchez 15 min seul avant de demander ; demandez une explication, pas du code.
3. Valkey et vos tests ont le dernier mot, pas l'IA.
4. Pas de code que vous ne savez pas expliquer dans votre dépôt.

**Pourquoi le TP s'y prête** : « fais comme Valkey » + `Compare` rend la sortie de l'IA vérifiable. Les cas limites (`EXISTS a a`, débordement d'`INCR`, `+5` refusé) sont souvent ratés par un code généré. Pour les profils cyber, relire du code qu'on n'a pas écrit et y trouver la faille est le métier.

**Piste à tester** : un atelier en S2 ou S3, « demandez `DEL` à l'IA, puis trouvez avec `Compare` ce qui ne va pas » (15 min).

**Limites assumées** : l'oral prend du temps (≈ 1h pour 20 étudiants) et reste en partie subjectif ; la grille à 3 niveaux et la banque commune le rendent défendable, pas parfait. À réévaluer après la S2, en comparant les rendus à ce que les étudiants savent expliquer.

## Niveaux

| Badge | Niveau | Où |
|---|---|---|
| 🟢 | **Socle** : indispensable, évalué | Slides et support |
| 🔵 | **Approfondissement** : pour comprendre plus finement | Surtout le support, parfois une slide |
| 🟣 | **Bonus** : étudiants avancés, Java moderne | Support uniquement |
| 🔒 | **Sécurité** : fil rouge | Slides et support |

## Contenu par séance

### S1 — Les bases du langage (AA1)
- **🟢 En cours** :
  - JDK, JVM et bytecode (bref).
  - Premier programme en *compact source file* (`void main()`, `IO.println`, JEP 512), puis la forme classe et `public static void main` nécessaire dans un projet.
  - Types primitifs et références, `String`, `var`.
  - Opérateurs utiles, `==` contre `equals` pour les `String`.
  - `if`/`else`, `switch` en expression, boucles `for`/for-each/`while`.
  - Tableaux, méthodes `static` (paramètres, retour).
  - Méthodes de `String` et `StringBuilder`.
  - Maven (structure, `pom.xml` minimal en Java 25, `mvn test`) et un premier test JUnit.
- **🔒** :
  - Débordement d'entier (`Math.addExact`).
  - Valider les entrées (bornes, formats) avant de les utiliser.
- **🔵 et 🟣 dans le support** :
  - Text blocks, `import module` (JEP 511), surcharge, portée.
  - Défis pour les avancés (algorithme de Luhn, robustesse d'un mot de passe…).
- **❌ Supprimé** : pièges d'opérateurs façon certification OCP (ordre d'évaluation, `- - x`, exemples Zoo et Meerkat).

### S2 — De la donnée à l'objet (AA2, AA3, AA4)
L'ordre est imposé, car chaque notion s'appuie sur la précédente :
1. **Classe et objet** : constructeur, `this`, encapsulation (`private`, invariants vérifiés dans le constructeur, pas de setters systématiques), `static` contre instance.
2. **`enum`** (`DishSize`, `OrderStatus` avec `switch`) et **`record`** (`Dish`, avec constructeur compact).
3. **Types riches** : `BigDecimal` (démo `0.1 + 0.2`, `compareTo` contre `equals`), `LocalDateTime`, `UUID`.
4. **Héritage** : `User` → `Customer`, `Courier` ; `extends`, `super(...)`, `@Override`, `abstract`, `Object` comme racine ; composition plutôt qu'héritage.
5. **Interfaces et polymorphisme** : `Discount` et ses implémentations ; interface ou classe abstraite.
6. **Collections** : `List`, `Set`, `Map` (des interfaces !), `ArrayList`, `HashSet`, `HashMap`, `merge`/`getOrDefault`, `List.of`, `getFirst`/`getLast`. Usage des génériques (`List<Dish>`). Contrat `equals`/`hashCode`/`toString`.
7. **`Optional`** : `findOrderById`, `orElseThrow`, et les pièges.
- **🔒** :
  - Immutabilité et copies défensives (`getItems()` non modifiable).
  - Invariants vérifiés à la construction.
  - Un `equals`/`hashCode` incorrect fait « perdre » des données.
- **🔵 et 🟣 dans le support** :
  - Écrire ses propres génériques, méthodes `default` des interfaces.
  - `LinkedList`, `TreeMap`, `LinkedHashMap` et leurs complexités.
  - Interfaces `sealed` et pattern matching dans `switch`.
  - Code avant `super()` (JEP 513).
- **❌ Supprimé** : getters et setters générés à la chaîne, `Date`/`Calendar`.

### S3 — Rendre l'application intelligente et robuste (AA5, AA6)
- **🟢 En cours** :
  - Interface fonctionnelle, lambda (de `Discount` en classe à la lambda), `Predicate`/`Function`/`Consumer`/`Supplier`/`Comparator`, références de méthodes.
  - Streams : `filter`, `map`, `sorted`, `limit`, `toList()`, `count`, `anyMatch`, `Collectors.groupingBy`, somme de `BigDecimal` avec `reduce`. Pièges : évaluation paresseuse, stream à usage unique, pas d'effets de bord.
  - Exceptions :
    - hiérarchie, checked ou unchecked ;
    - `try`/`catch`/`finally`, try-with-resources ;
    - exceptions métier (`OrderNotFoundException`) ;
    - ne jamais avaler une exception, ni renvoyer `0` ou `null` « par défaut ».
  - Journalisation avec `System.Logger` plutôt que `printStackTrace`.
- **🔒** :
  - Fuite d'information par les messages d'erreur (CWE-209).
  - Pas de données personnelles dans les logs, injection dans les logs.
- **🔵 et 🟣 dans le support** :
  - `Collectors.toMap`, `flatMap`, streams parallèles (et pourquoi les éviter).
  - Gatherers (JDK 24), variable anonyme `_`.

### S4 — Préparer l'application au monde réel (AA7)
- **🟢 Concurrence (~30 min)** :
  - Pourquoi la concurrence ; `ExecutorService` en try-with-resources avec virtual threads.
  - Race condition démontrée pour de vrai : les incréments se perdent.
  - Corrections : `AtomicInteger`, `synchronized`, `ConcurrentHashMap` et ses opérations atomiques (`merge`, `compute`, `putIfAbsent`).
  - 🔒 TOCTOU, « vérifier puis agir » : code promo à usage unique, stock.
- **🟢 JDBC (~30 min)** :
  - H2 en mémoire, `Connection` avec try-with-resources, `PreparedStatement`, `ResultSet` vers des objets.
  - 🔒 Injection SQL démontrée (concaténation contre paramètres), secrets hors du code (variables d'environnement), moindre privilège.
- **🔵 et 🟣 dans le support** :
  - Transactions.
  - Hachage de mots de passe (PBKDF2) ; l'API KDF (JEP 510) et pourquoi HKDF n'est pas fait pour hacher des mots de passe.
  - Scoped values (JEP 506).
- **❌ Supprimé** :
  - Sérialisation Java : seulement un encadré 🔒 dans le support sur le danger de la désérialisation.
  - I/O fichiers détaillées.
  - Security Manager (désactivé depuis le JDK 24).

## Ce qui reste à faire après les supports

- ~~Nouveau TP~~ : fait, Mini-Redis (`tp/miniredis/`).
- ~~Dispositif d'évaluation multi-notes~~ : fait (décision 7).
- QCM : diagnostique S1 fait (`prive/qcms/`) ; QCM de début de S2, S3, S4 à écrire.
- Section « fichiers » du support S3 (`Path`, `Files`, `ATOMIC_MOVE`, path traversal) : référencée par le TP, absente du support.
- Tuteur IA (`AGENTS.md`, Gem, GPT…).
