# GPX Track Bot

## GPX

- [1](https://www.topografix.com/gpx.asp)
- [2](https://www.topografix.com/GPX/1/1/gpx.xsd)
- [3](https://en.wikipedia.org/wiki/GPS_Exchange_Format)
- [4](https://wiki.openstreetmap.org/wiki/GPX)
- [5](https://github.com/mpetazzoni/leaflet-gpx)
- [6](https://github.com/mapbox/leaflet-omnivore)
- [7](https://leafletjs.com/plugins.html)

## Data Structure

- **track**
  - id        str_seq
  - ct        ts
  - ts        ts
  - type      "gpx"
  - user_id   "tg:123456"
  - status    "public"|"private"|"deleted"
  - hash      "sha1(file)"
  - file      {:path ... :size ...}
  - orig      {:telegram {:from {...} :chat {...} :document {...} :caption "..."}}
  - title     "source/telegram/caption ... 80 знаков"
  - descr     "... 1000 знаков ..."
  - season    ["зима"]
  - activity  ["вело" "пеший"]
  - tags      [... all indexed tags ...]
  - ?related   [ track-ids ]
  - time      {:start ... :finish ... :moving ...}
  - distance  {:total 100000}   ;; meters
  - geom      {box? center? bounds? segments}

- **track_var**
  - id  (var name)
  - val (value)

Track points:

- **track_pnt**
  - id        str_id
  - track_id  str_id      ;; references track.id
  - seq       ord
  - coord     [lon,lat]   ;; 2dshpere index
  - elev      999
  - time      orig_time

## BotFather

@gpxtrack_bot:

- name: GPX Track bot
- about: Outdoor tracks: hike, run, ski, bike ...
- description: |
    Автозагрузка GPX треков из чатов или приватно, публикация обновлений в канале, отображение на карте.
- groups: on
- privacy: off
- inline_mode: on
- inline placeholder: "Search tracks ..."
- bot commands

```text
/help - помощь
/list - список
/track nn - информация о треке
```

@gpxtrack
@pgxtrack_chat

www.strava.com/activities/987654321/export_original

### misc

moving time

### TODO

- редактирование заголовка и описания трека
- установка тэгов сезонов / активностей
- установка тэгов местности
- поиск по тэгам и описанию
- разбор GPX
- длина трека, время в пути
- сегменты
- запись точек трека в базу
- bounding box
- поиск по координатам
- отметка "избранное"
- описание / инструкция
- загрузка файлов kml/kmz/plt
- "скриншот" трека на карте
- выгрузка нескольких треков в kmz
