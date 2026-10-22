<p align="center"><img src="fastlane/metadata/android/fr-FR/images/icon.png" width="96" alt="Icône de Bigly"></p>

# Bigly

**Affichez n'importe quel texte en lettres géantes sur tout l'écran.** · [English version](README.md)

<p align="center">
  <a href="https://github.com/nomemleak/Bigly/releases/latest"><img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="Disponible sur GitHub" height="80"></a>
  <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/nomemleak/Bigly"><img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Disponible sur Obtainium" height="80"></a>
  <a href="https://f-droid.org/packages/io.github.nomemleak.bigly/"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Disponible sur F-Droid" height="80"></a>
</p>

<p align="center"><sub>Le lien F-Droid fonctionnera une fois Bigly accepté dans F-Droid.</sub></p>

Bigly est une petite application Android libre et open source. Tapez un message, appuyez sur **Afficher**, et brandissez votre téléphone : dans un concert, une salle bruyante, à l'aéroport, ou pour parler à quelqu'un de l'autre côté de la rue.

## Fonctions

- **Aperçu en direct** : la grande zone de saisie montre le message dans ses couleurs et sa police finales. Thèmes de couleurs, mode d'affichage et effets sont juste en dessous, sans passer par les réglages.
- **Mode fixe** : la taille du texte s'ajuste toute seule pour remplir l'écran. Le texte sur plusieurs lignes est pris en charge, sans jamais couper un mot.
- **Mode défilant** (bandeau LED) : vitesse réglable, de gauche à droite ou de droite à gauche, en boucle continue, avec un bouton pause.
- **Effets**, un seul à la fois : clignotement, arc-en-ciel animé, tremblement, pulsation, avec une vitesse réglable.
- **Historique** des derniers messages (20 par défaut, jusqu'à 100) : rejouez-les d'un tap avec les couleurs, l'effet et le mode qu'ils avaient ; supprimez-en un ou effacez tout.
- **Couleurs** : texte et fond depuis une palette ou n'importe quelle couleur personnalisée, et un avertissement quand le contraste est faible.
- Garde l'écran allumé et peut pousser la luminosité au maximum pendant l'affichage.
- Orientation automatique, avec un bouton pour verrouiller en portrait ou en paysage.
- **Widget d'écran d'accueil** : un tap affiche le dernier message en plein écran.
- **Raccourcis** (appui long sur l'icône) : Nouveau message, Dernier message, Historique.
- **Partager vers Bigly** : partagez du texte depuis n'importe quelle app ; il pré-remplit le champ de saisie.
- **Emojis** en très grand dans tous les modes et effets ; le calcul automatique de taille les mesure aussi.
- **Export en image** : un PNG du message tel qu'affiché, partagé via le menu Partager d'Android.
- **13 préréglages de couleurs** (jaune sur noir, vert sur noir, blanc sur bleu…), tous avec un contraste d'au moins 4,5:1, disponibles dans les réglages et sur l'écran d'affichage.
- Couleurs Material You sur Android 12 et plus, avec un thème de repli soigné sur Android 8 à 11. Thème clair, sombre ou système.
- Anglais et français, selon la langue du téléphone.

Touchez le message pour afficher ou masquer les contrôles (retour, effet, couleurs, verrou d'orientation, export, pause). Le geste retour du système quitte le plein écran.

## Vie privée

Aucune permission (les images exportées sont partagées depuis le cache privé de l'app via un FileProvider, sans permission de stockage), aucun accès réseau, aucune publicité, aucun traqueur, aucune bibliothèque propriétaire. Les réglages et l'historique restent sur l'appareil et sont exclus des sauvegardes dans le cloud.

## Accessibilité

- Les animations suivent le réglage système « supprimer les animations » : les effets restent figés quand il est activé.
- Le clignotement ne dépasse jamais 2,5 flashs par seconde.
- Tous les contrôles ont une description ; les réglages préviennent quand le contraste entre le texte et le fond est insuffisant.

## Compilation

Prérequis : JDK 17 ou plus récent, et le SDK Android (avec `ANDROID_HOME` défini, ou un fichier `local.properties` contenant `sdk.dir=...`). Android Studio fournit les deux.

```sh
./gradlew testDebugUnitTest   # tests unitaires
./gradlew assembleDebug       # APK debug
```

L'APK est produit dans `app/build/outputs/apk/debug/app-debug.apk`. Installez-le avec :

```sh
adb install app/build/outputs/apk/debug/app-debug.apk
```

Chaque push sur `main` est aussi compilé par GitHub Actions. Le dernier APK debug est joint à la [pré-release `debug-latest`](../../releases/tag/debug-latest), ainsi qu'à chaque exécution de l'onglet **Actions** (artefact `bigly-debug-apk`).

| | |
|---|---|
| Langage | Kotlin, Jetpack Compose, Material 3 |
| Android | 8.0+ (minSdk 26), cible l'API 36 |
| Identifiant | `io.github.nomemleak.bigly` (debug : `io.github.nomemleak.bigly.debug`) |

L'organisation du code est décrite dans [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Les métadonnées F-Droid se trouvent dans `fastlane/metadata/android`.

## Vérification des développeurs par Google

Google prévoit d'exiger, à partir de 2026/2027, que les développeurs de toute application sur les appareils Android certifiés transmettent leur identité à Google. Bigly ne le fera pas. Au premier lancement de chaque nouvelle version, un court message de [FreeDroidWarn](https://github.com/woheller69/FreeDroidWarn) (Apache 2.0) l'explique et renvoie vers [Keep Android Open](https://keepandroidopen.org/). Ouvrir ces liens reste votre choix : Bigly lui-même n'a toujours aucun accès réseau.

## Licence

Copyright (C) 2026 contributeurs de Bigly.

Bigly est un logiciel libre : vous pouvez le redistribuer et/ou le modifier selon les termes de la [Licence publique générale GNU](LICENSE) telle que publiée par la Free Software Foundation, soit la version 3 de la licence, soit (à votre choix) toute version ultérieure.

---

Une IA a participé, de façon éthique et encadrée, à l'écriture de cette application ; chaque décision et le résultat final restent sous responsabilité humaine.
