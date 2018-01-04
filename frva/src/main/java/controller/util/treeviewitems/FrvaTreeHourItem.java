package controller.util.treeviewitems;

/**
 * Created by patrick.wigger on 03.11.17.
 */
public class FrvaTreeHourItem extends FrvaTreeItem {

  private int hour;

  public FrvaTreeHourItem(int hour) {
    super(hour + ":00-" + ((hour + 1) % 24) + ":00 ");
    this.hour = hour;
  }



  public int getHour() {
    return hour;
  }

}