# GPX Track Bot

## Data Structure

- **track**
  - id      str_id
  - ct      ts
  - ts      ts
  - type    "gpx"
  - user_id "tg:123456"
  - status  "public"|"private"|"deleted"
  - file    {:path ... :size ...}
  - source  {:telegram {:from {...} :chat {...} :caption "..."}}
  - info    {:title "source/telegram/caption" :tags [...] :related [...]}

Track points:

- **track_pnt**
  - id      str_id
  - track   str_id
  - seq     ord
  - coord:  [lon,lat]
  - elev    999
  - time    orig_time

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

@gpxtrack
@pgxtrack_chat

www.strava.com/activities/987654321/export_original
