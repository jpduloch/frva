package controller.util.treeviewitems;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import model.data.SdCard;

/**
 * Created by patrick.wigger on 12.10.17.
 */
public class FrvaTreeRootItem extends FrvaTreeItem {

  public FrvaTreeRootItem(String name) {
    super(name);
  }

  /**
   * Creates children as part of the LazyLoading procedure.
   * @param list of which the treeview should be created from.
   * @param createFull if treeview should not be created lazy.
   */
  public void createChildren(List<SdCard> list, boolean createFull) {

    List<String> deviceNames = list.stream().map(f -> (f.getDeviceSerialNr()))
        .collect(Collectors.toList());
    boolean containesItemAlready = false;
    for (String deviceName : deviceNames) {
      FrvaTreeDeviceItem deviceItem = new FrvaTreeDeviceItem(deviceName, deviceName);
      Iterator it = this.getChildren().iterator();
      while (it.hasNext()) {
        Object currentElement = it.next();
        if (deviceItem.equals(currentElement)) {
          deviceItem = (FrvaTreeDeviceItem) currentElement;
          containesItemAlready = true;
        }
      }
      if (!containesItemAlready) {
        this.getChildren().add(deviceItem);
      }
      deviceItem.createChildren(list, createFull);
    }
  }

  @Override
  public void removeMeasureSequence() {
  }

  @Override
  public void addMeasureSequence() {
  }
}