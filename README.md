# Little Chess 🐴♟️

A gentle Android app for a 1-year-old: big pictures, a friendly voice and happy chimes,
in **English, বাংলা (Bengali) and हिन्दी (Hindi)**.
No ads, no internet, no in-app purchases, no permissions.

## What's inside

বাংলা comes first: the app starts in Bengali, every card shows the Bengali word under the
English (or the English under the Bengali), and there is a full Bengali alphabet and Bengali rhymes.

| Tile | What it does |
| --- | --- |
| **Animals / প্রাণী** | 100 animals, each with its sound or a fun fact ("গরু! হাম্বা!"), starting with the Royal Bengal tiger |
| **Birds / পাখি** | 100 birds, Bengal's own first: দোয়েল, মাছরাঙা, শালিক, বুলবুলি, কোকিল, টুনটুনি, বাবুই... Birds without an emoji are drawn by the app |
| **Bangla letters / বর্ণমালা** | All 50 letters, অ to ঁ, each with a picture word ("অ-তে অজগর"), always read in Bengali |
| **Music / গান-বাজনা** | 14 instruments to play: piano, tabla, guitar, flute (বাঁশি), harmonium, sitar, ektara, dhak, conch (শাঁখ), violin, xylophone, bells, drums and trumpet. ▶ plays a song or rhythm (Teentaal, Kaharwa, Dadra on the tabla); ☝ "learn" mode shows a star on the next note so the child can play Twinkle Twinkle, Sa Re Ga Ma, Jingle Bells and more |
| **Find it! / খুঁজে দেখো** | A game: three pictures and "বিড়াল কোথায়?". Topics change every round; stars for right answers |
| **Colors, Shapes, Numbers** | 12 colours, 8 shapes, counting 1 to 10 (in ১ ২ ৩ in Bengali) |
| **Food / খাবার** | 100 foods: fruits, vegetables, ভাত, ডাল, মুড়ি, বিরিয়ানি, sweets and drinks |
| **Things / জিনিস** | 100 everyday things: toys, home, clothes, school, nature, places |
| **Vehicles, My body** | 42 vehicles (Kolkata tram, yellow taxi, auto...) and 26 body parts and actions |
| **ABC** | A for Apple to Z for Zebra, read in English |
| **Rhymes / ছড়া** | 11 Bengali ছড়া (আয় আয় চাঁদ মামা, হাট্টিমা টিম টিম...), plus English and Hindi rhymes, each read in its own language |
| **Chess pieces, board, moves** | The original chess corner |
| **Surprise!** | A shuffled mix of everything |

On a card, **tap the picture** to hear it again (with sparkles), **tap anywhere else or swipe** to go to the next one.
The orange button shuffles the deck, the house button goes back to the menu.

The word lists live in `app/src/main/assets/*.txt` (one card per line), so more words are easy to add.

### For grown-ups

* **Language:** tap বাংলা / English / हिन्दी on the menu (Bengali is the default). Words on screen and the voice both switch.
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

The app is plain Java with no libraries. All pictures are drawn in code (chess pieces, shapes and birds as
vector paths, everything else as emoji), and every instrument sound is synthesised on the phone
(plucked strings, tabla, bells and so on), so the APK stays small and works offline.
