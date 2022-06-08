# [Tiny Television Time Tracker](https://github.com/warren-bank/Android-Tiny-Television-Time-Tracker)
> <small>aka: _TV Tracker_</small><br>
> <small>forked from: <a href="https://github.com/ltGuillaume/DroidShows">ltGuillaume/DroidShows</a></small>

### Tiny Television Time Tracker: A Reboot of DroidShows

This fork adds quite a bunch of features, among which a data API migration from TheTVDB v1 to TMDB v3.

### DroidShows: A Reboot of DroidSeries Offline TV Shows Tracker

<img src="etc/icon/icon6.png" align="right"/><img src="etc/icon/material.png" align="right"/>
This fork adds quite a bunch of features, among which an improved interface, a menu overflow button, pinning, quick search and filters, a separate archive/backlog list, a last seen episodes log, swipe gestures, more show information, backup/restore, double episode entries clean-up, faster updating, cover and fan art view, a modern launcher icon and it speeds up responsiveness significantly by (more) efficient SQL queries and some threading. DroidShows only needs an internet connection when updating the show info or watching full-size posters.

__Some pointers__:

* Don't forget to update all shows regularly (pull-to-refresh)
* Context menu has more options (long-press item or use overflow buttons)
* Swipe left-to-right to go back

__In Shows Overview__:

* Tap poster for next episode info
* Double tap for for show info
* Long-press poster external resource (to \*[url]s directly, or list)
* Swipe right-to-left to mark next as seen

__In Show Details__:

* Tap poster for full-screen view
* Click full-screen poster image for fan art
* Long-press full-screen poster to open in external app

__In Show/Episode Details__:

* Tap IMDb rating to view in IMDb App when installed, or on IMDb's mobile webpage

![DroidShows Screenshot](./etc/icon/screenshot.png)

__Shows Overview__:

\+ Put a "\*" before external source URLs to open them directly via a double tap on the poster (e.g. "\*battlestarwiki.org")<br>
\+ Optionally show "1x1 | Next airing: 1x4 on Jan 1, 2017" (Left = next episode for you to watch; Right = first episode to be aired)<br>
\+ Showing "[aired unwatched] of [total unwatched]"<br>
\+ Added separate archive/backlog to keep shows you're not currently watching out of your way<br>
\+ Added icon ic_menu_view for show/hide toggled<br>
\+ Status of show in details, and † in overview if show is not continuing<br>
\+ If show position changed, scroll back to show after [Mark next episode as seen] and Seasons list<br>
\+ Option to only update shows' latest season<br>
\+ Context items to view show details on FANDOM (Wikia), Rotten Tomatoes, Wikipedia and IMDb<br>
\+ Add your own per-show external resources (links to Wikia, blogs etc.)<br>
\+ Pin shows to the top of the list for easy access<br>
\+ Tap cover for next episode's info, long-press for show info, double tap for external resources<br>
\+ Swipe right-to-left to mark next episode as seen (shows confirmation toast)<br>
\+ Option to include specials in unwatched count<br>
\+ Mark next episode seen via swipe now vibrates<br>
\+ Optionally determine next episode by first unseen overall, or by last marked as watched<br>
\+ Undo function (until full exit)<br>
\+ A log that shows the last shows you've marked as seen<br>
\+ Showing middot · when all new episodes are aired<br>
\+ New show & episode details views<br>
\+ View full size poster and fan art<br>
\+ Quick search (filter)<br>
\+ Exclude shows without unseen aired episodes<br>
\+ Added overflow buttons for easier access to context menus<br>
\+ Pull-To-Refresh to update shows<br>
\* Sorting shows by first unseen episode<br>
\* Clarified toggle and sort options<br>
\* Not showing "null" entries from DB<br>
\* Posters now fill row height, aspect ratios independent of screen's

__Seasons/Episodes list__:

\+ Showing "[aired] of [season episodes]"<br>
\+ Aired date in episodes list<br>
\+ Date of when episode was marked as seen is shown next to checkmark<br>
\+ Click on episode title for details, on checkmark to change seen state<br>
\+ Automatically scroll to current season / first unwatched episode<br>
\* Big performance improvement for entirely rewritten Seasons activity: is now almost instant

__Add show__:

\+ Icon resized rate_star_med_on_holo_dark for added shows<br>
\+ Icon ic_menu_add for new shows<br>
\+ Choose the synopsis language per show<br>
\* Fixed search not working after initial search<br>
\* Centered icons vertically in search results<br>
\* Large-size posters aren't cached, to save space in /data/data/

__Update__:

\* Prevent double episode entries

__Other__:

\+ Click on an episode air date to add it to your calendar<br>
\+ Menu (overflow) button should show up in Android 3.0+<br>
\+ Dutch, French, German, Spanish and Russian translations<br>
\+ Choose which synopsis language to fetch from TMDB<br>
\+ Modern looking layout<br>
\+ Swipe left-to-right acts as back button<br>
\+ Animations that help understand the app's structure<br>
\+ Backup/restore database<br>
\+ Automatically create backups (max. once a day)<br>
\* Date/time format according to locale<br>
\* Big performance improvement for Overview activity (values are kept up-to-date in series table)<br>
\* Some progress dialogs cancelable<br>
\* Update of all shows continues when navigating away from DroidShows<br>
\* Screen off/rotating screen/navigating away from app during update poses no problems anymore<br>
\* [Exit] removes app from memory<br>
\* Fixed UI glitches/styles<br>
\* Code clean-up (all layouts revised)

__Copyright Ownership and Public Licensing__:

&copy;2010 [Carlos Limpinho, Paulo Cabido](https://code.google.com/p/droidseries) under GPLv3<br>
Modified by [Mikael Berthe](https://gitorious.org/droidseries/mckaels-droidseries)<br>
&copy;2014-2018 [Guillaume](https://github.com/ltGuillaume/DroidShows) under GPLv3<br>
&copy;2022 [Warren Bank](https://github.com/warren-bank) under GPLv3<br>
New icon is a mix of work by [Thrasos Varnava](https://iconeasy.com/icon/tv-shows-2-icon) and [Taenggo](https://wallalay.com/wallpapers-for-android-67-177682-desktop-background.html)

__TMDB API Attribution__:

This product uses the [TMDB API](https://www.themoviedb.org/documentation/api) but is not endorsed or certified by TMDB.
