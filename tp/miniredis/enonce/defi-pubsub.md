# 🟣 Défi long — Pub/Sub

**Pour qui** : vous avez fini le socle 🟢 et l'approfondissement 🔵 de la séance. Le défi se poursuit d'une séance à l'autre. Il ne remplace jamais le socle : on y revient seulement quand le socle de la séance en cours est terminé.

**Objectif** : votre serveur sait **diffuser des messages**. Des clients s'abonnent à un canal (`SUBSCRIBE commandes`), d'autres y publient (`PUBLISH commandes "pizza prête"`), et chaque abonné reçoit le message aussitôt, sans rien demander.

**Évaluation** : `tools/PubSubCompare.java` et `LoadTest pubsub`, puis à l'oral : expliquez un des problèmes des paliers 3 et 4, et votre solution.

---

## Pourquoi Pub/Sub ?

C'est une fonction de Valkey peu connue, et pourtant c'est le mécanisme derrière beaucoup de besoins que vous rencontrerez :

- **Notifications en temps réel** : FoodFast prévient le client « votre commande est partie ». Le service de livraison publie, l'application mobile est abonnée.
- **Invalidation de cache** : dix serveurs gardent le menu en mémoire. Quand le menu change, un message « menu modifié » leur dit de le relire.
- **Chat, tableaux de bord en direct** : un serveur WebSocket s'abonne, et relaie chaque message aux navigateurs.

En le codant, vous verrez aussi ses **limites**. Un message publié quand personne n'écoute est perdu, définitivement. Rien n'est stocké, et rien n'est rejoué à un abonné qui se reconnecte. Quand ces limites deviennent gênantes, on passe à une file de messages durable : les Streams de Valkey, Kafka ou RabbitMQ. Vous saurez alors pourquoi.

## Ce qui se passe sur le fil

```text
client A                         serveur                         client B
SUBSCRIBE commandes      →
                         ←  ["subscribe", "commandes", 1]
                                                         ←  PUBLISH commandes "pizza prête"
                         ←  ["message", "commandes", "pizza prête"]
                                                         →  (integer) 1     (1 abonné l'a reçu)
```

Le message arrive chez A **sans que A ait envoyé de commande**. Jusqu'ici, votre serveur n'écrivait à un client qu'en réponse à ce client. Désormais, la connexion de B écrit chez A. C'est toute la difficulté du défi.

Les réponses exactes sont dans la [spécification](specification.md#défi-pubsub), et dans `tests/pubsub.expected`.

---

## Palier 1 — Le registre des abonnements (dès la séance 2)

Pas de réseau pour l'instant : un registre en pur Java, testé avec JUnit.

```java
package miniredis.pubsub;

/** Celui qui reçoit les messages d'un canal. */
public interface Subscriber {
    void push(Reply message);
}
```

```java
package miniredis.pubsub;

/** Qui écoute quel canal. Un seul registre, partagé par toutes les connexions. */
public final class PubSub {
    public void subscribe(String channel, Subscriber subscriber) { ... }
    public void unsubscribe(String channel, Subscriber subscriber) { ... }
    /** @return le nombre d'abonnés qui ont reçu le message */
    public int publish(String channel, String message) { ... }
}
```

- Le message poussé est un tableau : `new Reply.Array(List.of(new Reply.Bulk("message"), new Reply.Bulk(canal), new Reply.Bulk(texte)))`.
- S'abonner deux fois au même canal ne fait qu'un abonnement : choisissez la bonne collection.
- Un canal qui n'a plus d'abonné disparaît du registre. Sinon, après un million de canaux éphémères, la map garde un million d'ensembles vides.

**Tester sans réseau.** Dans le test, un faux abonné range ce qu'il reçoit. C'est l'intérêt de l'interface : `PubSub` ne sait pas à qui il parle.

```java
static final class Inbox implements Subscriber {
    final List<Reply> received = new ArrayList<>();
    @Override public void push(Reply message) { received.add(message); }
}

@Test
void publishReachesEverySubscriberOfTheChannelOnly() {
    PubSub pubSub = new PubSub();
    Inbox alice = new Inbox();
    Inbox bob = new Inbox();
    pubSub.subscribe("actu", alice);
    pubSub.subscribe("actu", bob);
    pubSub.subscribe("sport", bob);

    assertEquals(2, pubSub.publish("actu", "bonjour"));
    assertEquals(0, pubSub.publish("météo", "pluie"));
    assertEquals(1, alice.received.size());
}
```

## Palier 2 — Le mode abonné (séance 3)

Chaque connexion a désormais un **état** : ses canaux. Créez une classe qui le porte, une instance par connexion :

```java
public final class PubSubSession {
    public PubSubSession(PubSub pubSub, Subscriber self) { ... }
    /** @return les réponses à envoyer, ou null si la commande n'est pas pour Pub/Sub */
    public List<Reply> handle(List<String> command) { ... }
    /** À la déconnexion : quitter tous les canaux. */
    public void close() { ... }
}
```

Pourquoi une **liste** de réponses ? `SUBSCRIBE a b c` envoie **trois** confirmations, une par canal.

À écrire, et à tester avec JUnit (toujours sans réseau) :

1. `SUBSCRIBE canal [canal…]` : une confirmation `["subscribe", canal, n]` par canal. `n` est le nombre total d'abonnements de **cette connexion**.
2. `UNSUBSCRIBE [canal…]` : `["unsubscribe", canal, n]` par canal. Sans argument, la connexion quitte tous ses canaux. Si elle n'en avait aucun : `["unsubscribe", (nil), 0]`.
3. `PUBLISH canal message` : `(integer)` nombre de destinataires.
4. **Le mode abonné.** Tant qu'une connexion a au moins un abonnement, elle n'accepte que `SUBSCRIBE`, `UNSUBSCRIBE`, `PSUBSCRIBE`, `PUNSUBSCRIBE`, `PING`, `QUIT` et `RESET`. `GET plat` répond alors une erreur précise (voir la spécification), et `PING` répond `["pong", ""]` au lieu de `PONG`.

Pensez aussi à **qui appelle `close()`**. Un abonné qui se déconnecte doit quitter le registre. Sinon, `PUBLISH` continue de le compter, et d'essayer de lui écrire.

Vous ne pouvez pas encore essayer à deux clients : avant la séance 4, votre serveur n'en sert qu'un à la fois. Les tests JUnit suffisent.

## Palier 3 — Plusieurs clients, une seule socket (séance 4)

Une fois votre serveur multi-clients (séance 4, §1), branchez la session dans la boucle de `Server`. Avant le `CommandProcessor`, `session.handle(command)` a la priorité : `null` veut dire « pas pour moi ».

Puis vérifiez :

```bash
java tools/PubSubCompare.java tests/pubsub.txt
```

Ou à la main, dans deux terminaux :

```bash
java tools/Cli.java SUBSCRIBE commandes          # terminal 1 : écoute, jusqu'à Ctrl+C
java tools/Cli.java PUBLISH commandes "pizza prête"   # terminal 2
```

**Deux questions de concurrence vous attendent.**

1. **Le registre.** Plusieurs threads s'abonnent et publient en même temps. Votre `HashMap` du palier 1 n'y survit pas : séance 4, §1.6. Attention à « créer l'ensemble s'il n'existe pas, puis ajouter ». C'est un *check-then-act*, la même famille de bug que le TOCTOU. Cherchez `ConcurrentHashMap.compute`.
2. **La socket de l'abonné.** `PUBLISH` s'exécute dans le thread de **l'éditeur**, mais il écrit sur la socket de **l'abonné**. Si deux éditeurs publient en même temps vers le même abonné, leurs octets se mélangent. Il peut aussi y avoir l'abonné lui-même, qui reçoit au même moment la réponse à son `PING`. L'abonné reçoit alors un flux RESP illisible.

Mettez le problème en évidence, **puis** corrigez-le :

```bash
java tools/LoadTest.java pubsub-race    # 20 éditeurs × 500 messages vers un abonné
```

Piste : tout ce qu'on écrit à un client passe par **un seul** objet, et un seul thread à la fois peut écrire.

## Palier 4 — L'abonné lent 🔒 (séance 4)

```bash
java tools/LoadTest.java pubsub-slow
```

Un abonné s'abonne, puis ne lit plus rien. Les tampons réseau se remplissent, et le `write` de l'éditeur **bloque**. Cet éditeur est peut-être le service de commandes de FoodFast : un seul client malveillant ou planté vient de geler la publication pour tout le monde. C'est un **déni de service**.

Ce qu'on attend :

- l'éditeur ne bloque jamais : il **dépose** le message, et c'est un autre thread qui l'écrit ;
- ce qui attend d'être écrit est **borné**. Au-delà d'une limite (8 Mo par exemple), l'abonné est déconnecté. Valkey fait exactement cela : c'est son paramètre `client-output-buffer-limit pubsub`.

Pistes : une `BlockingQueue` par client, un thread virtuel qui la vide vers la socket, et un compteur des octets en attente.

**À l'oral** : pourquoi déconnecter l'abonné, plutôt que de garder ses messages en mémoire ou de les jeter en silence ?

## 🟣 Au-delà

- **`PSUBSCRIBE motif`** : s'abonner à `commande:*`. Votre `Glob` de la séance 1 resservira. Le message reçu est `["pmessage", motif, canal, message]`. Un abonné qui écoute à la fois `commande:7` et `commande:*` reçoit le message **deux fois**, et `PUBLISH` compte 2.
- **`PUBSUB CHANNELS [motif]`, `PUBSUB NUMSUB canal…`, `PUBSUB NUMPAT`** : qui écoute quoi ?
- **Piste ouverte : les notifications d'expiration.** Dans Valkey, `CONFIG SET notify-keyspace-events Ex` publie un message sur `__keyevent@0__:expired` à chaque clé expirée. Votre `Store` de la séance 2 se met à émettre des événements. Problème : votre expiration est paresseuse, puisqu'une clé n'expire que quand on la lit. Il faut donc une tâche de fond qui fait expirer les clés d'elle-même. C'est sans corrigé : à discuter avec l'enseignant.

## Pièges

- `PubSubCompare` compte les abonnés : fermez vos autres clients (un `Cli` resté abonné, par exemple) avant de le lancer.
- Le nom du canal est **sensible à la casse** : `Actu` et `actu` sont deux canaux.
- Le message est une chaîne quelconque : `"pizza\r\n+OK"` doit arriver intact. Il passe en `Bulk`, dont la longueur est annoncée, donc il ne peut pas injecter de fausse réponse. Construire la réponse en concaténant du texte casserait exactement cela.
- `UNSUBSCRIBE` sans argument, et plusieurs motifs qui correspondent au même canal : l'ordre des réponses n'est pas garanti par Valkey. Les tests fournis évitent ces cas.
