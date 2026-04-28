# TP 1 : Les bases de Java

## Exercice 1: Hello World
- Compiler et exécuter un programme affichant “Hello World!” en ligne de commande.
- Afficher les arguments passés en ligne de commande.

## Exercice 2: Jour de la semaine
- Afficher le jour de la semaine en fonction d'un nombre saisi.
- Utiliser un `switch case` pour afficher le jour correspondant au nombre saisi.
- Si l'utilisateur entre un nombre en dehors de la plage 1-7, afficher un message indiquant que le nombre est invalide.

## Exercice 3: Boucles (SumAllIntegers.java et SumAllEvenIntegers.java)
- Compléter la classe `SumAllIntegers` pour effectuer la somme de tous les entiers de 1 jusqu'à ce nombre (uniquement pour les entiers positifs).
- Compléter la classe `SumAllEvenIntegers` pour effectuer la somme de tous les entiers pairs de 1 jusqu'à ce nombre (uniquement pour les entiers positifs).

## Exercice 4: Inverser une chaîne
- Compléter le programme `ReverseString` qui prend une chaîne de caractères en entrée et l'affiche à l'envers.

## Exercice 5: FizzBuzz
- Pour les multiples de trois, afficher “Fizz” au lieu du nombre et pour les multiples de cinq, afficher “Buzz”.
- Pour les nombres qui sont des multiples de trois et cinq, afficher “FizzBuzz”.
- Compléter la classe de test `FizzBuzzTest.java`

## Exercice 6: Année bissextile
- Toutes les années divisibles par 400 SONT des années bissextiles (par exemple, 2000 était une année bissextile).
- Toutes les années divisibles par 100 mais pas par 400 NE SONT PAS des années bissextiles (par exemple, 1700, 1800, et 1900 n'étaient PAS des années bissextiles, tout comme 2100 ne le sera pas).
- Toutes les années divisibles par 4 mais pas par 100 SONT des années bissextiles (par exemple, 2008, 2012, 2016).
- Toutes les années non divisibles par 4 NE SONT PAS des années bissextiles (par exemple, 2017, 2018, 2019).
- Écrire des tests et implémenter `LeapYear` en respectant ces 4 règles.

## Exercice 7 : Conception d'une Classe Simple

Vous allez concevoir une classe représentant un `Book` dans une bibliothèque.

1.  Créez une classe `Book`, dans un nouveau package (avec un nom cohérent), avec les attributs suivants:
    *   `title`: le titre du livre
    *   `author`: l'auteur du livre
    *   `yearPublished` : l'année de publication (ne peut pas être null)
    *   `price` : le prix du livre (ne peut pas être null)
2.  Implémentez un constructeur qui prend en paramètres tous les attributs.
3.  Ajoutez une méthode `equals` qui compare deux objets Book sur la base du titre et de l'auteur (écrivez les tests associés)
4.  Ajoutez une méthode `toString` qui retourne une description complète du livre (titre, auteur, année, prix), (écrivez les tests associés)
5.  Créez une méthode de réduction (`applyDiscount`) qui prend un pourcentage de réduction en paramètre et applique cette réduction au prix du livre, (écrivez les tests associés)
6.  Créez une méthode qui compare l'année de publication de deux livres et retourne le plus récent, (écrivez les tests associés)
