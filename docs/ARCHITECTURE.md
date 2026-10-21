# Bigly — Architecture

Application Android native, mono-module, sans dépendance hors AndroidX/Kotlin (compatible F-Droid).

## Choix techniques

| Sujet | Choix | Pourquoi |
|---|---|---|
| Langage / UI | Kotlin, Jetpack Compose, Material 3 | demandé |
| SDK | minSdk 26, compileSdk/targetSdk 36 | Android 8+, dernière API stable |
| Navigation | 2 activités : `MainActivity` (accueil + réglages) et `DisplayActivity` (plein écran) | la fenêtre plein écran a ses propres réglages (luminosité, écran allumé, orientation, mode immersif) et le widget l'ouvre directement |
| Réglages | DataStore Preferences | standard, asynchrone, local |
| Historique | fichier texte privé (`files/history.txt`), écriture atomique, `StateFlow` | logique 100 % Kotlin, testable sans Android |
| Taille de police | recherche dichotomique sur la taille, mesure via `TextMeasurer` | le texte multiligne remplit l'écran sans couper un mot |
| Effets | une boucle d'images (`withFrameNanos`) lue dans `graphicsLayer` / `drawWithContent` | pas de recomposition à chaque image |
| Arc-en-ciel | dégradé posé comme `brush` du texte | les emojis gardent leurs couleurs |
| Emojis | coupures de ligne permises entre emojis et idéogrammes (`Emoji`, `FontSizeFitter`) | taille maximale sans couper un mot |
| Raccourcis | `ShortcutManagerCompat`, publiés au lancement | fonctionnent aussi pour le build `.debug`, libellés traduits |
| Partage entrant | filtre `ACTION_SEND text/plain` sur `MainActivity`, nettoyage dans `SharedText` | pré-remplit la saisie |
| Export | `MessageImage` dessine le message dans un bitmap, `FileProvider` sur le cache | aucune permission de stockage |
| Widget | `AppWidgetProvider` classique (RemoteViews) | aucune dépendance supplémentaire |
| Réduire les animations | `ValueAnimator.areAnimatorsEnabled()` | si désactivé : effets figés |
| Rotation | `configChanges` sur `DisplayActivity` + `rememberSaveable` | pas de recréation, l'état survit |
| Données | `allowBackup=false`, aucune permission, aucun réseau | stockage local uniquement |

## Arborescence

```
bigly/
├── LICENSE                          GPLv3
├── README.md / README.fr.md
├── build.gradle.kts, settings.gradle.kts, gradle.properties
├── gradle/libs.versions.toml, gradle/wrapper/*, gradlew, gradlew.bat
├── .github/workflows/build.yml      tests + APK debug à chaque push
├── docs/ARCHITECTURE.md
├── fastlane/metadata/android/{en-US,fr-FR}/
│   ├── title.txt, short_description.txt, full_description.txt
│   ├── changelogs/1.txt
│   └── images/icon.png, images/phoneScreenshots/
└── app/
    ├── build.gradle.kts, proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/app/bigly/
        │   │   ├── BiglyApp.kt                 Application + conteneur de dépendances
        │   │   ├── MainActivity.kt             accueil ⇄ historique ⇄ réglages, partage entrant
        │   │   ├── Shortcuts.kt                raccourcis d'appui long
        │   │   ├── DisplayActivity.kt          plein écran, fenêtre, orientation
        │   │   ├── data/
        │   │   │   ├── AppSettings.kt          modèle + énumérations (mode, effet, sens, thème)
        │   │   │   ├── SettingsRepository.kt   DataStore
        │   │   │   ├── HistoryEntry.kt
        │   │   │   ├── HistoryCodec.kt         (dé)sérialisation texte
        │   │   │   ├── HistoryList.kt          règles : dédoublonnage, ordre, limite
        │   │   │   └── HistoryRepository.kt    fichier + Flow
        │   │   ├── text/                       FontSizeFitter, Emoji, SharedText (pur Kotlin)
        │   │   ├── data/ColorPresets.kt        13 préréglages de couleurs
        │   │   ├── util/                       ColorUtils (contraste), ImageExport, Motion
        │   │   ├── ui/theme/                   Theme.kt, Color.kt, Type.kt
        │   │   ├── ui/home/                    HomeScreen (saisie, récents), HistoryScreen
        │   │   ├── ui/settings/                SettingsScreen.kt, ColorPickerDialog.kt
        │   │   ├── ui/display/                 DisplayScreen, FitText, MarqueeText, TextFitting,
        │   │   │                               TextEffects, DisplayControls, MessageImage (export)
        │   │   └── widget/BiglyWidgetProvider.kt
        │   └── res/
        │       ├── values/ et values-fr/strings.xml
        │       ├── values/themes.xml, colors.xml
        │       ├── drawable/ic_launcher_foreground.xml, ic_launcher_monochrome.xml, icônes des contrôles
        │       ├── mipmap-anydpi/ic_launcher.xml, ic_launcher_round.xml
        │       ├── layout/widget_bigly.xml
        │       └── xml/widget_info.xml, locales_config.xml
        └── test/java/app/bigly/
            ├── FontSizeFitterTest.kt, EmojiFontSizeTest.kt, SharedTextTest.kt
            ├── ColorPresetTest.kt
            ├── HistoryCodecTest.kt
            ├── HistoryListTest.kt
            ├── HistoryRepositoryTest.kt
            └── ColorUtilsTest.kt
```

## Flux principaux

1. **Accueil** : l'utilisateur tape un texte, choisit Fixe/Défilant, appuie sur « Afficher » → le texte est ajouté en tête de l'historique → `DisplayActivity` s'ouvre avec le texte.
2. **Affichage** : lit les réglages (couleurs, effet, vitesse, sens, luminosité, écran allumé), passe en mode immersif. Un tap affiche/masque les contrôles (retour, effet, verrou d'orientation, pause du défilement). Retour = fermeture.
3. **Widget** : un tap ouvre `DisplayActivity` avec l'action « dernier message » ; le widget se met à jour quand l'historique change.
