# GPX Track Bot

## Data Structure

- **track**
  - id      str_seq
  - ct      ts
  - ts      ts
  - type    "gpx"
  - user_id "tg:123456"
  - status  "public"|"private"|"deleted"
  - hash    "sha1(file)"
  - file    {:path ... :size ...}
  - orig    {:telegram {:from {...} :chat {...} :document {...} :caption "..."}}
  - info    {:title "source/telegram/caption" :tags [...] :related [...], :num_seg 999}
  - geom    {box? center? bounds?}

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
