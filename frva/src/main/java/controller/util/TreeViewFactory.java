package controller.util;

import controller.util.TreeviewItems.FrvaTreeDayItem;
import controller.util.TreeviewItems.FrvaTreeDeviceItem;
import controller.util.TreeviewItems.FrvaTreeHourItem;
import controller.util.TreeviewItems.FrvaTreeMeasurementItem;
import controller.util.TreeviewItems.FrvaTreeMonthItem;
import controller.util.TreeviewItems.FrvaTreeRootItem;
import controller.util.TreeviewItems.FrvaTreeSdCardItem;
import controller.util.TreeviewItems.FrvaTreeYearItem;
import java.util.Iterator;
import java.util.List;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import model.FrvaModel;
import model.data.MeasureSequence;
import model.data.SdCard;

/**
 * Created by patrick.wigger on 26.10.17.
 */
public class TreeViewFactory {


  /**
   * Creates a treeview.
   *
   * @param sdCard    SdCard containing the data.
   * @param treeView  view on which the data should be attached to
   * @param model     the model of the application
   */

  public static void extendTreeView(SdCard sdCard, TreeView<FrvaTreeRootItem> treeView,
                                    FrvaModel model) {


    String currentDevice = "";


    FrvaTreeDeviceItem checkBoxDeviceItem = new FrvaTreeDeviceItem(sdCard.getDeviceSerialNr(), sdCard.getDeviceSerialNr());

    int sdCardCount = 0;
    FrvaTreeSdCardItem sdCardItem = new FrvaTreeSdCardItem(sdCard);

    int yearlyCount = 0;
    FrvaTreeYearItem yearItem = new FrvaTreeYearItem("Year");

    int monthlyCount = 0;
    FrvaTreeMonthItem monthItem = new FrvaTreeMonthItem("Month");

    int dailyCount = 0;
    FrvaTreeDayItem dayItem = new FrvaTreeDayItem("Day");

    int hourlyCount = 0;
    FrvaTreeHourItem hourItem = new FrvaTreeHourItem(-1);

    TreeItem root = treeView.getRoot();

    root.getChildren().add(checkBoxDeviceItem);
    checkBoxDeviceItem.getChildren().add(sdCardItem);

    for (MeasureSequence ms : sdCard.getMeasureSequences()) {
      boolean newItem = false;

      if (!ms.getYear().equals(yearItem.getYear())) {
        yearItem.setContainingMeasureSequences(yearlyCount);
        yearlyCount = 0;
        yearItem = new FrvaTreeYearItem(ms.getYear());
        sdCardItem.getChildren().add(yearItem);
        newItem = true;
      }

      if (!ms.getMonth().equals(monthItem.getMonth()) || newItem) {
        monthItem.setContainingMeasureSequences(monthlyCount);
        monthlyCount = 0;
        monthItem = new FrvaTreeMonthItem(ms.getMonth());
        yearItem.getChildren().add(monthItem);
        newItem = true;
      }

      if (!ms.getDate().equals(dayItem.getDay()) || newItem) {
        dayItem.setContainingMeasureSequences(dailyCount);
        dailyCount = 0;
        dayItem = new FrvaTreeDayItem(ms.getDate());
        monthItem.getChildren().add(dayItem);
        newItem = true;
      }

      if (ms.getHour() != (hourItem.getHour()) || newItem) {
        hourItem.setContainingMeasureSequences(hourlyCount);
        hourlyCount = 0;
        hourItem = new FrvaTreeHourItem(ms.getHour());
        dayItem.getChildren().add(hourItem);
      }

      hourItem.getChildren().add(new  FrvaTreeMeasurementItem("ID" + ms.getId() + " - " + ms.getTime(), ms, ms.getId(), ms.getContainingFile(), model));

      sdCardCount++;
      yearlyCount++;
      monthlyCount++;
      dailyCount++;
      hourlyCount++;

    }
    hourItem.setContainingMeasureSequences(hourlyCount);
    dayItem.setContainingMeasureSequences(dailyCount);
    monthItem.setContainingMeasureSequences(monthlyCount);
    sdCardItem.setContainingMeasureSequences(sdCardCount);

    sdCardItem.setContainingMeasureSequences(sdCardCount);
    treeView.setShowRoot(false);


  }


}
