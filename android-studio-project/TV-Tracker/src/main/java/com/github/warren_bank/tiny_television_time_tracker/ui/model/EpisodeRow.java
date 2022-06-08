package com.github.warren_bank.tiny_television_time_tracker.ui.model;

import java.util.Date;

public class EpisodeRow {
  public int    id;
  public String name;
  public String aired;
  public Date   airedDate;
  public int    seen;

  public EpisodeRow(int id, String name, String aired, Date airedDate, int seen) {
    this.id        = id;
    this.name      = name;
    this.aired     = aired;
    this.airedDate = airedDate;
    this.seen      = seen;
  }
}
