# Little Chess 🐴♟️

A gentle Android app for a 1-year-old: big pictures, a friendly voice and happy chimes,
in **English, বাংলা (Bengali) and हिन्दी (Hindi)**.
No ads, no internet, no in-app purchases, no permissions.

## What's inside

| Tile | What it does |
| --- | --- |
| **Colors** | Red, orange, yellow, green, blue, purple, pink, brown, black, white, each with a matching thing ("Red! A red apple.") |
| **Shapes** | Circle, square, triangle, star, heart, rectangle, oval, diamond, each with a smiley face |
| **Animals** | 18 animals with their sounds ("Cow! Moo!" / "গরু! হাম্বা!" / "गाय! मूँ!") |
| **Things** | Everyday objects, food, vehicles and nature words |
| **Numbers** | Counting 1 to 5 out loud ("One... two... three... Three ducks!") |
| **Chess pieces** | King, queen, rook, bishop, knight and pawn, in white and black |
| **Chess board** | A real board in the starting position. Touching a piece says its colour and name ("Black knight!"). Pieces can be dragged anywhere; the green button puts them all back. |
| **Chess moves** | "Catch the star": one piece on the board with green dots where it can go. Tap a dot to move it, and land on the star for a party. The arrow switches piece (rook, bishop, queen, king, knight, pawn), and each one explains its move ("The knight jumps like an L!"). |
| **Surprise!** | A shuffled mix of everything |

On a card, **tap the picture** to hear it again (with sparkles), **tap anywhere else or swipe** to go to the next one.
The house button goes back to the menu.

### For grown-ups

* **Language:** tap English / বাংলা / हिन्दी on the menu. Words on screen and the voice both switch.
  Bengali and Hindi speech need the phone's Google text-to-speech voice for that language; if it is
  missing the app offers to open the download screen.

* **Name:** long-press the title on the menu screen to enter your child's name, and the app will greet her by name.
* **Exiting:** the back button needs to be pressed twice on the menu, so little hands don't close the app by accident.
  For full lock-down, use Android's *App pinning* (Settings → Security → App pinning).
* **Voice:** uses the phone's built-in text-to-speech. If it is silent, install or enable
  *Speech Services by Google* and an English voice in Settings → Accessibility → Text-to-speech.

## Getting the APK

Every push builds the app with GitHub Actions (`.github/workflows/build-apk.yml`).
Open the repository's **Releases** page on your phone, download `LittleChess.apk` from the latest
"Little Chess build", and open it to install. Android will ask you to allow installs from your browser the first time.

## Building locally

Needs the Android SDK (API 34) and JDK 17:

```sh
./gradlew assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

The app is plain Java with no libraries. All pictures are drawn in code (chess pieces and shapes as
vector paths, everything else as emoji), and the chimes are synthesised at startup.
