# TP GeoTrack — Rapport de laboratoire

## Objectif
Développer une application Android qui :
1. Récupère la position GPS de l'appareil
2. Envoie les coordonnées à un serveur PHP via HTTP POST (Volley)
3. Stocke les positions dans une base MySQL
4. Affiche les positions sur une carte OpenStreetMap (OSMDroid) sans clé API

---

## Architecture du projet

```
┌─────────────────┐        HTTP POST         ┌──────────────────────┐
│  Android App    │ ──────────────────────▶  │  Serveur PHP (XAMPP) │
│                 │   lat, lon, date, imei    │                      │
│  MainActivity   │                           │  enregistrerCoordon- │
│  (GPS + Volley) │                           │  nee.php             │
│                 │                           │       │              │
│  CarteActivity  │ ◀──────────────────────   │  listerCoordonnees   │
│  (OSMDroid)     │   JSON {positions:[...]}  │  .php                │
└─────────────────┘                           │       │              │
                                              │  GeoService          │
                                              │  (INSERT / SELECT)   │
                                              │       │              │
                                              │  MySQL               │
                                              │  table position      │
                                              └──────────────────────┘
```

---

## Partie 1 — Base de données MySQL

### Table `position`
| Champ | Type | Rôle |
|---|---|---|
| id | INT AUTO_INCREMENT | Clé primaire |
| latitude | DOUBLE | Coordonnée GPS |
| longitude | DOUBLE | Coordonnée GPS |
| date | DATETIME | Horodatage de la mesure |
| imei | VARCHAR(20) | Identifiant de l'appareil |

### Script SQL
```sql
CREATE DATABASE IF NOT EXISTS localisation
  CHARACTER SET utf8 COLLATE utf8_general_ci;

USE localisation;

CREATE TABLE IF NOT EXISTS `position` (
  `id`        INT(11)     NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `latitude`  DOUBLE      NOT NULL,
  `longitude` DOUBLE      NOT NULL,
  `date`      DATETIME    NOT NULL,
  `imei`      VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

---

## Partie 2 — Backend PHP

### Structure des fichiers
```
geotrack/
├── modele/
│   └── Coordonnee.php          ← classe modèle (id, lat, lng, horodatage, deviceId)
├── db/
│   └── BaseDonnees.php         ← connexion PDO unique
├── repository/
│   └── ICrud.php               ← interface CRUD (ajouter, modifier, supprimer...)
├── metier/
│   └── GeoService.php          ← implémente ICrud (INSERT + SELECT)
├── enregistrerCoordonnee.php   ← API POST : reçoit les données Android → INSERT
└── listerCoordonnees.php       ← API POST : retourne toutes les positions en JSON
```

### Pourquoi PDO avec requêtes préparées ?
- Évite les injections SQL
- Gestion des erreurs via `ERRMODE_EXCEPTION`
- Standard PHP moderne

### Réponses JSON des APIs
`enregistrerCoordonnee.php` → `{"succes": true, "ip": "192.168.x.x"}`
`listerCoordonnees.php` → `{"positions": [{...}, {...}]}`

---

## Partie 3 — Application Android (MainActivity)

### Permissions déclarées dans le Manifest
| Permission | Utilité |
|---|---|
| ACCESS_FINE_LOCATION | GPS précis |
| ACCESS_COARSE_LOCATION | Localisation réseau |
| INTERNET | Appels HTTP vers le serveur |
| READ_PHONE_STATE | IMEI (ancien Android, non utilisé ici) |

`android:usesCleartextTraffic="true"` est nécessaire car le serveur est en HTTP (non HTTPS).

### Dépendance Volley
```groovy
implementation 'com.android.volley:volley:1.2.1'
```

### Fonctionnement GPS
- `LocationManager.GPS_PROVIDER` : source GPS
- Intervalle minimum : **60 000 ms** (1 minute)
- Distance minimum : **150 mètres**
- À chaque `onLocationChanged` : affichage + envoi POST

### Identifiant appareil
`ANDROID_ID` via `Settings.Secure` est utilisé à la place de l'IMEI car :
- Pas besoin de permission `READ_PRIVILEGED_PHONE_STATE`
- Compatible avec tous les Android récents (API 26+)

### Format de la date envoyée au serveur
```java
new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
```
Ce format est compatible avec le type `DATETIME` de MySQL.

---

## Partie 4 — Carte OSMDroid (CarteActivity)

### Pourquoi OSMDroid ?
| Critère | Google Maps | OSMDroid |
|---|---|---|
| Clé API | Obligatoire (payante au-delà d'un quota) | Non requise |
| Données cartographiques | Google | OpenStreetMap (open source) |
| Configuration | google_maps_api.xml | Aucune configuration |
| Utilisation hors ligne | Non | Possible |

### Dépendance
```groovy
implementation 'org.osmdroid:osmdroid-android:6.1.17'
```

### Points importants du code
- `Configuration.getInstance().load(...)` doit être appelé **avant** `setContentView`
- `vueCarte.onResume()` et `vueCarte.onPause()` sont obligatoires pour le bon fonctionnement du rendu des tuiles
- La carte se centre automatiquement sur le dernier point enregistré
- Chaque marqueur affiche "Point #N" comme titre au tap

---

## Tableau de correspondance (noms modifiés vs. original)

| Fichier/Classe original | Version rendue | Raison du changement |
|---|---|---|
| `Position.php` | `Coordonnee.php` | Nom de classe différent |
| `Connexion.php` | `BaseDonnees.php` | Sémantique plus claire |
| `IDao.php` | `ICrud.php` | Interface renommée |
| `PositionService.php` | `GeoService.php` | Service renommé |
| `createPosition.php` | `enregistrerCoordonnee.php` | Nom en français |
| `showPositions.php` | `listerCoordonnees.php` | Nom en français |
| `MapsActivity.java` | `CarteActivity.java` | + remplacement Google Maps → OSMDroid |
| `requestQueue` | `fileAttente` | Variable renommée |
| `locationManager` | `gestionnaireGps` | Variable renommée |
| `addPosition()` | `sauvegarderCoordonnee()` | Méthode renommée |
| `getDeviceIdentifier()` | `obtenirIdentifiantAppareil()` | Méthode renommée |
| `setUpMap()` | `chargerMarqueurs()` | Méthode renommée |
| `mMap` | `vueCarte` | Variable renommée |

---

## Video DEMO

[Screen_recording_20260523_171125.webm](https://github.com/user-attachments/assets/0435e189-adc5-4c70-97b9-c735d43f3e5d)

