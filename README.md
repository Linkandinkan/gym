# Hemmagym – Android-app

Allt (HTML, JavaScript, typsnitt, Chart.js, ikon) ligger inbyggt i appen. Den behöver ingen internetanslutning
och inga behörigheter. Du bygger APK-filen själv på ett av sätten nedan.

## Alternativ 1: Bygg automatiskt på GitHub (inget behöver installeras)

1. Skapa ett gratis konto på github.com och klicka **New repository** (valfritt namn, gärna *private*).
2. Packa upp zip-filen på datorn. Klicka **uploading an existing file** i det nya repot och dra in ALLA filer och mappar.
   - Mappen `.github` är dold i vissa filhanterare. Om den inte följer med: klicka **Add file → Create new file**,
     skriv `.github/workflows/build.yml` som filnamn och klistra in innehållet från filen med samma namn.
3. Klicka **Commit changes**.
4. Gå till fliken **Actions**. Bygget startar själv och tar 3–5 minuter.
5. Klicka på det färdiga bygget och ladda ned **Hemmagym-APK** längst ned (en zip med `app-debug.apk`).
6. Skicka filen till mobilen och öppna den. Tillåt "installera okända appar" för den app du öppnar den med.

## Alternativ 2: Android Studio

1. Installera Android Studio och välj **Open**, peka på den här mappen.
2. Vänta tills synkningen är klar och välj **Build → Build APK(s)**.
3. APK:n hamnar i `app/build/outputs/apk/debug/app-debug.apk`.

Från terminalen (kräver JDK 17 och Android SDK): `./gradlew assembleDebug`

## Gott att veta

- **Data:** Appen har egen lagring. Dina pass och din mat från webbversionen följer inte med automatiskt.
- **Export:** Menyn Mer → Exportera sparar en JSON-fil i mappen Nedladdningar.
- **Skärmen** hålls tänd medan appen är öppen (praktiskt under passet).
- **Bakåtknappen** minimerar appen i stället för att stänga den.
- **Fungerar inte i appversionen:** Bluetooth-anslutning till Galaxy Watch och träningspåminnelser.
  Webbläsarens funktioner för dessa finns inte i Androids inbyggda webbvy.
- **Uppdatera:** Ändra filerna i `app/src/main/assets/www/` och bygg om. Höj `versionCode` i `app/build.gradle`
  för att kunna installera över en äldre version.
- APK:n är signerad med en debug-nyckel. Det är okej för eget bruk men inte för Google Play.
