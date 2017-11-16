package controller.util.treeviewitems;

/**
 * Created by patrick.wigger on 03.11.17.
 */
public class FrvaTreeHourItem extends FrvaTreeItem {

  private int hour;

  public FrvaTreeHourItem(String name, String hour) {
    super(name);
    this.hour = Integer.parseInt(hour);
  }

  public FrvaTreeHourItem(int hour) {
    super(hour + ":00-" + ((hour + 1) % 24) + ":00 ");
    this.hour = hour;
  }

  @Override
  public String serialize() {
    return "-1" + ";" + this.getDepth() + ";" + this.getValue().toString() + ";" + hour + ";";
  }

  @Override
  public int getDepth() {
    return 6;
  }

  @Override
  public void createChildren() {

  }

  public int getHour() {
    return hour;
  }
}
