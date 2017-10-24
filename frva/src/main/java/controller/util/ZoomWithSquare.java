package controller.util;

import java.util.logging.Logger;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class ZoomWithSquare implements ZoomLineChart {

  private final Logger logger = Logger.getLogger("FRVA");

  private final LineChart<Double, Double> lineChart;
  private final NumberAxis yaxis;
  private final NumberAxis xaxis;
  private Rectangle zoomRect;
  private double borderLeft;
  private double borderRight;
  private double borderTop;
  private double borderBottom;
  Point2D zoomStartPoint;

  AnchorPane anchorPane;

  private enum Mode {
    Zoom,
    Translate
  }

  Mode currentMouseMode;

  Line linex;
  Line liney;

  /**
   * Creates and adds Zoomfunctionality  to a LineChart and its axes.
   *
   * @param zommableNode Linechart to Zoom.
   * @param xaxis        xAxis to Zoom.
   * @param yaxis        xAxis to Zoom.
   */
  public ZoomWithSquare(LineChart<Double, Double> zommableNode,
                        NumberAxis xaxis, NumberAxis yaxis) {
    this.lineChart = zommableNode;
    this.xaxis = xaxis;
    this.yaxis = yaxis;

    anchorPane = (AnchorPane) lineChart.getParent();

    addMouseListeners();
  }

  private void addMouseListeners() {

    lineChart.setOnMouseEntered(event -> {
      borderLeft = yaxis.getWidth();
      borderRight = lineChart.getWidth();
      borderTop = 0;
      borderBottom = lineChart.getHeight() - xaxis.getHeight();
    });


    lineChart.setOnMouseMoved(event -> {
      if (isInChartRange(event)) {
        if (event.isControlDown()) {
          currentMouseMode = Mode.Translate;
          lineChart.setCursor(Cursor.HAND);
        } else {
          currentMouseMode = Mode.Zoom;

          Image image = new Image("images/cursorZoom.png");

          lineChart.setCursor(new ImageCursor(image,
              image.getWidth() / 2,
              image.getHeight() / 2));
        }
      } else {
        currentMouseMode = null;
        lineChart.setCursor(Cursor.DEFAULT);
      }
    });


    lineChart.setOnMousePressed(event -> {
      if (isInChartRange(event)) {
        if (Mode.Translate.equals(currentMouseMode)) {
          System.out.println("Translate");
        }

        if (Mode.Zoom.equals(currentMouseMode)) {
          zoomStartPoint = new Point2D(event.getX(), event.getY());
          zoomRect = new Rectangle(event.getX(), event.getY(), 0, 0);
          zoomRect.setOpacity(0.5);

          anchorPane.getChildren().add(zoomRect);
        }
      }
    });


    lineChart.setOnMouseDragged(event -> {
      if (isInChartRange(event)) {
        if (Mode.Translate.equals(currentMouseMode)) {
          System.out.println("Translate");
        }
        if (Mode.Zoom.equals(currentMouseMode)) {
          //Right
          if (event.getX() > zoomStartPoint.getX()) {
            zoomRect.setWidth(event.getX() - zoomRect.getX());
          } else {
            zoomRect.setWidth(zoomStartPoint.getX() - event.getX());
            zoomRect.setX(event.getX());
          }

          if (event.getY() > zoomStartPoint.getY()) {
            zoomRect.setHeight(event.getY() - zoomRect.getY());
          } else {
            zoomRect.setHeight(zoomStartPoint.getY() - event.getY());
            zoomRect.setY(event.getY());
          }
        }
      }
    });

    lineChart.setOnMouseReleased(event -> {
      if (isInChartRange(event)) {
        if (Mode.Translate.equals(currentMouseMode)) {
          translate();
        }

        if (Mode.Zoom.equals(currentMouseMode)) {
          Point2D upperLeft = new Point2D(zoomRect.getX() - borderLeft,
              zoomRect.getY() - borderTop);
          Point2D lowerRight = new Point2D(zoomRect.getX() - borderLeft + zoomRect.getWidth(),
              zoomRect.getY() - borderTop + zoomRect.getHeight());

          if (anchorPane.getChildren().remove(zoomRect)) {
            zoomIn(upperLeft, lowerRight);
          }
        }
      }
    });
  }

  private boolean isInChartRange(MouseEvent event) {
    return event.getX() > borderLeft && event.getX() < borderRight
        && event.getY() > borderTop && event.getY() < borderBottom;
  }


  private Point2D calculateAxisFromPoint(Point2D point2D) {
    double pxWidth = xaxis.getWidth();
    double pxHeigth = yaxis.getHeight();

    double onXaxis = ((xaxis.getUpperBound() - xaxis.getLowerBound()) / pxWidth) * (point2D.getX());
    double onYaxis = (
        (yaxis.getUpperBound() - yaxis.getLowerBound()) / pxHeigth) * (pxHeigth - (point2D.getY()));

    System.out.println(onXaxis + " " + onYaxis);
    return new Point2D(onXaxis, onYaxis);
  }


  private void zoomIn(Point2D upperLeft, Point2D lowerRight) {
    xaxis.setAutoRanging(false);
    yaxis.setAutoRanging(false);

    Point2D upperLeftAxis = calculateAxisFromPoint(upperLeft);
    Point2D lowerRightAxis = calculateAxisFromPoint(lowerRight);

    xaxis.setUpperBound(Math.round(lowerRightAxis.getX() + xaxis.getLowerBound()));
    yaxis.setUpperBound(Math.round(upperLeftAxis.getY() + yaxis.getLowerBound()));
    xaxis.setLowerBound(Math.round(upperLeftAxis.getX() + xaxis.getLowerBound()));
    yaxis.setLowerBound(Math.round(lowerRightAxis.getY() + yaxis.getLowerBound()));
  }

  private void zoomOut() {
    xaxis.setAutoRanging(true);
    yaxis.setAutoRanging(true);
  }

  private void translate() {

  }


}
