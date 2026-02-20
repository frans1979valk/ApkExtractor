# APK Extractor

Een eenvoudige, vriendelijke, advertentievrije APK-extractor en back-up-app voor Android.
Geen abonnementen, geen trackers, geen internettoestemming.

## Openen en bouwen

1. Open Android Studio (Iguana 2024.1+ aanbevolen)
2. Kies **File > Open** en selecteer de map `ApkExtractor`
3. Android Studio synchroniseert Gradle automatisch
4. Als er gevraagd wordt om de Gradle wrapper, klik op **OK** om `gradle-wrapper.jar` te laten genereren
5. Sluit een apparaat aan of start een emulator (API 26+)
6. Klik op **Run** (groene afspeelknop)

### Bouwen via de terminal (optioneel)

Als je via de terminal wilt bouwen en de wrapper jar ontbreekt:

```bash
# Genereer de wrapper jar (Gradle moet geïnstalleerd zijn)
gradle wrapper --gradle-version 8.7

# Bouw het project
./gradlew assembleDebug
```

## Functies

- **Geïnstalleerde apps-lijst**: Blader door alle geïnstalleerde apps met zoeken en sorteren
- **App-details**: Bekijk versie, pakketnaam, updatedatum en APK-grootte
- **APK exporteren**: Sla een APK op naar een zelfgekozen locatie via Android's bestandskiezer (SAF)
- **APK delen**: Deel een APK via het systeemdeelmenu (Quick Share, berichten-apps, etc.)
- **Veilig verwijderen**: Verwijder zelf geïnstalleerde apps via het systeemverwijderprogramma (systeem-apps zijn beschermd)
- **Instellingen**: Schakel zichtbaarheid van systeem-apps in/uit, wijzig sorteervolgorde
- **Over/Help**: Privacyverklaring en gebruikershandleiding

## Hoe exporteren werkt (SAF)

Exporteren maakt gebruik van Android's **Storage Access Framework** (ACTION_CREATE_DOCUMENT):

1. Tik op een app in de lijst om de details te openen
2. Tik op **APK exporteren**
3. De systeembestandskiezer opent met een voorgestelde bestandsnaam
4. Kies waar je de APK wilt opslaan
5. De app kopieert de APK als stream van de installatielocatie naar de gekozen bestemming

Deze aanpak is volledig Scoped Storage-compatibel en vereist geen opslagtoestemmingen.

## Hoe delen werkt

Delen kopieert de APK naar een tijdelijke cachemap en deelt deze vervolgens via `FileProvider` met `ACTION_SEND`:

- Opent het standaard Android-deelmenu
- Werkt met Quick Share / Nearby Share, berichten-apps, bestandsoverdracht-apps, etc.
- Geen Bluetooth- of Wi-Fi-toestemmingen nodig — het systeem regelt het transport
- Gecachete bestanden worden automatisch na 24 uur opgeruimd

## Hoe verwijderen werkt

- **Gebruikersapps**: Door op "Verwijderen" te tikken wordt het standaard systeemverwijderprogramma geopend (ACTION_DELETE). Geen stille verwijderingen.
- **Systeem-apps**: De verwijderknop is verborgen. Een "Systeem-app"-badge wordt getoond. Door erop te tikken wordt uitgelegd waarom verwijderen niet mogelijk is, met de optie om de systeem App-info te openen.

## Beperkingen van systeem-apps

- Systeem-apps (vooraf geïnstalleerd door de fabrikant) worden getoond met een "Systeem-app"-badge
- Sommige systeem-APK's zijn mogelijk niet leesbaar op bepaalde apparaten door OS-beperkingen
- Systeem-apps kunnen niet worden verwijderd via deze of enige andere app van derden
- Exporteren en delen zou voor de meeste systeem-apps moeten werken, maar resultaten variëren per apparaat

## Privacy en compliance

- **Geen internettoestemming** — de app kan geen verbinding maken met het netwerk
- **Geen analytics of trackers** — er wordt niets verzameld of verzonden
- **Geen advertenties of betalingen** — volledig gratis
- **Geen DRM-omzeiling** — exporteert alleen APK's van reeds geïnstalleerde apps
- **Play Store-compatibel** — gebruikt QUERY_ALL_PACKAGES met legitieme onderbouwing

## Technische stack

- Kotlin
- Jetpack Compose (Material 3)
- MVVM (ViewModel + Repository)
- Coroutines
- DataStore Preferences
- Navigation Compose
- minSdk 26, targetSdk 35

## Ondersteunde talen

- Engels (en)
- Nederlands (nl)
- Duits (de)
- Hindi (hi)
- Spaans (es)
- Frans (fr)

De taal volgt automatisch de systeemtaal van het apparaat.
