<p align="center"><img src="fastlane/metadata/android/fr-FR/images/icon.png" width="96" alt="Icône de Bigly"></p>

# Bigly

**Affichez n'importe quel texte en lettres géantes sur tout l'écran.** · [English version](README.md)

Bigly est une petite application Android libre et open source. Tapez un message, appuyez sur **Afficher**, et brandissez votre téléphone : dans un concert, une salle bruyante, à l'aéroport, ou pour parler à quelqu'un de l'autre côté de la rue.

## Fonctions

- **Mode fixe** : la taille du texte s'ajuste toute seule pour remplir l'écran. Le texte sur plusieurs lignes est pris en charge, sans jamais couper un mot.
- **Mode défilant** (bandeau LED) : vitesse réglable, de gauche à droite ou de droite à gauche, en boucle continue, avec un bouton pause.
- **Effets**, un seul à la fois : clignotement, arc-en-ciel animé, tremblement, pulsation, avec une vitesse réglable.
- **Historique** des derniers messages (20 par défaut, jusqu'à 100) : rejouez-les d'un tap, supprimez-en un ou effacez tout.
- **Couleurs** : texte et fond depuis une palette ou n'importe quelle couleur personnalisée, préréglages noir sur blanc et blanc sur noir, et un avertissement quand le contraste est faible.
- Garde l'écran allumé et peut pousser la luminosité au maximum pendant l'affichage.
- Orientation automatique, avec un bouton pour verrouiller en portrait ou en paysage.
- **Widget d'écran d'accueil** : un tap affiche le dernier message en plein écran.
- Couleurs Material You sur Android 12 et plus, avec un thème de repli soigné sur Android 8 à 11. Thème clair, sombre ou système.
- Anglais et français, selon la langue du téléphone.

Touchez le message pour afficher ou masquer les contrôles (retour, effet, verrou d'orientation, pause). Le geste retour du système quitte le plein écran.

## Vie privée

Aucune permission, aucun accès réseau, aucune publicité, aucun traqueur, aucune bibliothèque propriétaire. Les réglages et l'historique restent sur l'appareil et sont exclus des sauvegardes dans le cloud.

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

Chaque push sur `main` est aussi compilé par GitHub Actions : ouvrez l'onglet **Actions**, choisissez la dernière exécution et téléchargez l'artefact `bigly-debug-apk`.

| | |
|---|---|
| Langage | Kotlin, Jetpack Compose, Material 3 |
| Android | 8.0+ (minSdk 26), cible l'API 36 |
| Identifiant | `app.bigly` (debug : `app.bigly.debug`) |

L'organisation du code est décrite dans [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Les métadonnées F-Droid se trouvent dans `fastlane/metadata/android`.

## Licence

Copyright (C) 2026 contributeurs de Bigly.

Bigly est un logiciel libre : vous pouvez le redistribuer et/ou le modifier selon les termes de la [Licence publique générale GNU](LICENSE) telle que publiée par la Free Software Foundation, soit la version 3 de la licence, soit (à votre choix) toute version ultérieure.

---

Une IA a participé, de façon éthique et encadrée, à l'écriture de cette application ; chaque décision et le résultat final restent sous responsabilité humaine.
