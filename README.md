# News & Events Manager

Java Swing asztali alkalmazás, amely marosvásárhelyi hírportálok RSS hírfolyamaiból híreket tölt le, SQLite adatbázisban tárolja őket, és egy Gmail-szerű felületen lehet őket böngészni, keresni és szerkeszteni.

UMFST Informatika, 2. év – laborfeladat.

## Funkciók

- Hírek letöltése 9 RSS forrásból (`res/rss.json`), HTTP státuszkód-ellenőrzéssel és 5 másodperces timeouttal
- Mentés SQLite adatbázisba, duplikációk nélkül (összetett kulcs: `source + url`)
- Induláskor a mentett hírek azonnal megjelennek; üres adatbázisnál automatikus letöltés
- Refresh gomb: letöltés a háttérben (SwingWorker), töltés-jelzéssel, az ablak nem fagy le
- 1/3–2/3 arányú master-detail felület: lista bal oldalon, részletek jobb oldalon
- Add / Edit / Delete, ellenőrzött adatbevitellel
- Keresés cím és leírás alapján (SQL `LIKE`)
- A cikkek saját borítóképe: az RSS-ből vagy a cikk oldaláról (`og:image`) letöltve, helyi fájlként tárolva; ha nincs, `res/images/default.jpg`
- Olvasott / olvasatlan jelölés, mint a Gmailben: az olvasatlan hír félkövér, kattintás után normál betűs, és ez az adatbázisban is megmarad
- Clear DB megerősítéssel

## Technológiák

Java 21, Swing, SQLite (`sqlite-jdbc`), JDBC, Gson, beépített DOM XML olvasó, `HttpURLConnection`, Maven, Git.

## Futtatás

1. NetBeans-ben nyisd meg a projektet (Maven projekt).
2. **Run Project** (F6). A `Main` osztály indul.
3. Első indításkor a program letölti a híreket és a képeket; utána a mentett hírekkel indul.

A program a projektmappából futtatva találja meg a `res/rss.json`-t és a `res/images/default.jpg`-t. Az adatbázis (`news_events.db`) és a letöltött képek (`res/images/news/`) futás közben jönnek létre a projektmappában, ezért nincsenek a Gitben.

## Projektstruktúra

```text
src/main/java/ro/umfst/app/
 ├── Main.java                       összerakja a programot és megnyitja az ablakot
 ├── model/
 │    ├── ContentItem.java           absztrakt ősosztály: közös mezők, ellenőrzés, edit()
 │    ├── News.java                  hír: forrás, URL, megjelenés dátuma, olvasott-e
 │    └── Event.java                 esemény: időpont és helyszín (a modell kész, a program nem használja)
 ├── database/
 │    ├── Database.java              kapcsolat az SQLite fájlhoz
 │    ├── NewsRepository.java        interfész: mit tud egy hírtároló
 │    └── SQLiteNewsRepository.java  az interfész SQLite megvalósítása (SQL csak itt van)
 ├── service/
 │    ├── NewsService.java           szabályok a felület és a tároló között
 │    └── RssImporter.java           végigmegy a forrásokon, letölt, ment
 ├── network/
 │    ├── HttpHelper.java            HTTP GET, státuszkód, timeout, letöltés
 │    └── ImageDownloader.java       a cikkek képének megkeresése és letöltése
 ├── rss/
 │    ├── RssSource.java             egy RSS forrás (név, URL)
 │    ├── RssSourceLoader.java       az rss.json beolvasása Gson-nal
 │    └── RssReader.java             RSS XML → News objektumok
 └── ui/
      ├── MainWindow.java            főablak, gombok, SwingWorker
      ├── NewsPanel.java             a kiválasztott hír részletei
      ├── NewsForm.java              űrlap az Add és Edit gombhoz
      ├── NewsListRenderer.java      a lista sorai: olvasatlan félkövér, olvasott normál
      └── CoverImageResolver.java    melyik képet kell mutatni

res/
 ├── rss.json                        az RSS források listája
 └── images/
      ├── default.jpg                alapértelmezett borítókép
      └── news/                      letöltött cikkképek (futáskor jön létre, nincs a Gitben)
```

## Rétegek

```text
Swing UI  →  NewsService / RssImporter  →  NewsRepository (interfész)  →  SQLiteNewsRepository  →  SQLite
```

A felület csak a service-eket ismeri, a service-ek csak a `NewsRepository` interfészt. Az SQL kizárólag az `SQLiteNewsRepository`-ban van.

## Adatbázis

Egy tábla, `news`, a laborfeladat mezőivel, plusz egy `is_read` oszlop (0 = olvasatlan, 1 = olvasott). Összetett elsődleges kulcs:

```sql
PRIMARY KEY (source, url)
```

Ugyanabból a forrásból ugyanaz a hír csak egyszer kerülhet be. Az RSS mentés `INSERT OR IGNORE`-t használ, így a már meglévő hírek csendben kimaradnak.

Ha egy régebbi adatbázisban még nincs `is_read` oszlop, a program induláskor hozzáadja (`PRAGMA table_info` + `ALTER TABLE`), így a meglévő hírek megmaradnak.

Az `Event` modellhez (`eventDateTime`, `location`) a tanár kérésére nincs tábla és felület; az osztály a későbbi bővítéshez készült.

## RSS feldolgozás

```text
rss.json → RssSource lista → minden forrásra: HTTP státusz (timeout 5 s)
        → ha 200: letöltés → XML feldolgozás → News objektumok → mentés (duplikáció nélkül)
        → különben: üzenet a státuszkóddal, és a következő forrás
```

A nyelv a hírfolyam `<language>` címkéjéből jön (`ro-RO` → `RO`, `hu` → `HU`). A dátum RFC 1123 formátumból (`Mon, 24 Aug 2026 09:25:47 +0000`) lesz `Timestamp`.

## Borítóképek

A kép helyét a program sorban keresi:

1. az RSS-ben: `<media:content>`, `<media:thumbnail>`, `<enclosure type="image/...">`, vagy az első `<img src>` a cikk HTML-jében;
2. ha ott nincs, és a hír új: a cikk oldalán a `<meta property="og:image">` címke.

A képet a program egyszer tölti le a `res/images/news/` mappába (a fájlnév a kép címéből készül), és a helyi útvonalat menti a `cover_image` oszlopba. A `.webp` képeket kihagyja, mert a Swing nem tudja megjeleníteni őket.

## Hibakezelés

| Hiba | Viselkedés |
|---|---|
| nincs hálózat, ismeretlen szerver, timeout | a forrás kimarad, státusz `-1`, a többi forrás betöltődik |
| HTTP hiba (403, 404, 500 …) | a forrás kimarad, üzenet a kóddal és a jelentésével |
| hibás XML, nem RSS, hiányzó `<language>` | a forrás kimarad érthető üzenettel |
| hiányzó cím vagy link egy hírben | az a hír kimarad, a többi bekerül |
| hiányzó szerző | a hír szerző nélkül kerül be |
| hibás dátum az RSS-ben | a hír dátum nélkül kerül be |
| hibás dátum az űrlapon | hibaüzenet, az űrlap a beírt adatokkal újra megnyílik |
| hiányzó kötelező mező (cím, forrás, URL, nyelv) | a modell konstruktora visszautasítja, hibaüzenet |
| már létező hír kézi felvétele | hibaüzenet |
| SQLite hiba | hibaüzenet-ablak, a program nem áll le |
| a kép nem tölthető le / nem található | a hír a default képpel kerül be |
| hiányzó vagy sérült borítókép | `default.jpg`; ha az is hiányzik, „(no image)" felirat |
| üres keresés | az összes hír látszik |

## OOP és SOLID a projektben

- **Absztrakció, öröklés:** `ContentItem` absztrakt ősosztály, `News extends ContentItem`, `Event extends ContentItem`
- **Egységbezárás, adatrejtés:** privát mezők, ellenőrző konstruktor, vak setterek helyett `edit()` és `markAsRead()` műveletek
- **Polimorfizmus, DIP:** a service-ek a `NewsRepository` interfésztől függenek, a konkrét `SQLiteNewsRepository`-t a `Main` adja át a konstruktorban
- **SRP:** minden osztálynak egy feladata van (HTTP, képletöltés, JSON, XML, adatbázis, felület külön)
- **Kompozíció:** a `NewsService` *birtokol* egy repository-t, nem örököl belőle
