package com.github.warren_bank.tiny_television_time_tracker.ui.model;

import java.util.Date;

import android.graphics.Bitmap;

public class TVShowItem {
  private int     serieId;
  private String  name;
  private int     seasonCount;
  private String  status;
  private String  language;
  private String  iconFilePath;
  private Bitmap  iconBitmap;

  private boolean archived;
  private boolean pinned;
  private String  extResources;

  private int     unwatched;
  private int     unwatchedAired;
  private Date    nextAir;
  private String  nextEpisode;
  private Date    unwatchedLastAired;
  private String  unwatchedLastEpisode;

  // ----------------------
  // Only used by Log Mode:
  // ----------------------
  private int    episodeId;
  private String episodeName;
  private String episodeSeen;
  // ----------------------

  public TVShowItem(int serieId, String name, int seasonCount, String status, String language, String iconFilePath, Bitmap iconBitmap, boolean archived, boolean pinned, String extResources, int unwatched, int unwatchedAired, Date nextAir, String nextEpisode, Date unwatchedLastAired, String unwatchedLastEpisode) {
    this.serieId              = serieId;
    this.name                 = name;
    this.seasonCount          = seasonCount;
    this.status               = status;
    this.language             = language;
    this.iconFilePath         = iconFilePath;
    this.iconBitmap           = iconBitmap;

    this.archived             = archived;
    this.pinned               = pinned;
    this.extResources         = extResources;

    this.unwatched            = unwatched;
    this.unwatchedAired       = unwatchedAired;
    this.nextAir              = nextAir;
    this.nextEpisode          = nextEpisode;
    this.unwatchedLastAired   = unwatchedLastAired;
    this.unwatchedLastEpisode = unwatchedLastEpisode;
  }

  // no setter
  public int getSerieId() {
    return this.serieId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getSNumber() {
    return this.seasonCount;
  }

  public void setSNumber(int seasonCount) {
    this.seasonCount = seasonCount;
  }

  public String getShowStatus() {
    return this.status;
  }

  public void setShowStatus(String status) {
    this.status = status;
  }

  // no setter
  public String getLanguage() {
    return this.language;
  }

  public String getIcon() {
    return this.iconFilePath;
  }

  public void setIcon(String iconFilePath) {
    this.iconFilePath = iconFilePath;
  }

  public Bitmap getDIcon() {
    return this.iconBitmap;
  }

  public void setDIcon(Bitmap iconBitmap) {
    this.iconBitmap = iconBitmap;
  }

  public boolean getArchived() {
    return this.archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public boolean getPinned() {
    return this.pinned;
  }

  public void setPinned(boolean pinned) {
    this.pinned = pinned;
  }

  public String getExtResources() {
    return this.extResources;
  }

  public void setExtResources(String extResources) {
    this.extResources = extResources;
  }

  public int getUnwatched() {
    return this.unwatched;
  }

  public void setUnwatched(int unwatched) {
    this.unwatched = unwatched;
  }

  public int getUnwatchedAired() {
    return this.unwatchedAired;
  }

  public void setUnwatchedAired(int unwatchedAired) {
    this.unwatchedAired = unwatchedAired;
  }

  public Date getNextAir() {
    return this.nextAir;
  }

  public void setNextAir(Date nextAir) {
    this.nextAir = nextAir;
  }

  public String getNextEpisode() {
    return this.nextEpisode;
  }

  public void setNextEpisode(String nextEpisode) {
    this.nextEpisode = nextEpisode;
  }

  public Date getUnwatchedLastAired() {
    return this.unwatchedLastAired;
  }

  public void setUnwatchedLastAired(Date unwatchedLastAired) {
    this.unwatchedLastAired = unwatchedLastAired;
  }

  public String getUnwatchedLastEpisode() {
    return this.unwatchedLastEpisode;
  }

  public void setUnwatchedLastEpisode(String unwatchedLastEpisode) {
    this.unwatchedLastEpisode = unwatchedLastEpisode;
  }

  // ----------------------
  // Only used by Log Mode:
  // ----------------------

  public int getEpisodeId() {
    return this.episodeId;
  }

  public void setEpisodeId(int episodeId) {
    this.episodeId = episodeId;
  }

  public String getEpisodeName() {
    return this.episodeName;
  }

  public void setEpisodeName(String episodeName) {
    this.episodeName = episodeName;
  }

  public String getEpisodeSeen() {
    return this.episodeSeen;
  }

  public void setEpisodeSeen(String episodeSeen) {
    this.episodeSeen = episodeSeen;
  }
}
