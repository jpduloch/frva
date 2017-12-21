package controller;

import controller.util.DeviceStatus;
import controller.util.bluetooth.ConnectionState;
import controller.util.bluetooth.ConnectionStateConnecting;
import controller.util.bluetooth.ConnectionStateDisconnecting;
import controller.util.bluetooth.ConnectionStateInit;
import controller.util.bluetooth.ConnectionStateSearching;
import controller.util.liveviewparser.CommandAny;
import controller.util.liveviewparser.CommandAutoMode;
import controller.util.liveviewparser.CommandGetConfiguration;
import controller.util.liveviewparser.CommandManualMeasurement;
import controller.util.liveviewparser.CommandManualMode;
import controller.util.liveviewparser.CommandSetInterval;
import controller.util.liveviewparser.CommandSetItegrationTime;
import controller.util.liveviewparser.CommandSetTime;
import controller.util.liveviewparser.LiveDataParser;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.StreamConnection;
import model.FrvaModel;
import model.data.MeasureSequence;


public class LiveViewController {
  private final Logger logger = Logger.getLogger("FRVA");
  private final FrvaModel model;
  private Node activeView;
  private ConnectionStateInit connectionStateInit;
  private String deviceName;

  private DeviceStatus deviceStatus = new DeviceStatus();
  private List<ServiceRecord[]> availableServiceRecords;
  private ServiceRecord[] selectedServiceRecord;
  private StreamConnection openStreamConnection;
  private final ObservableList<XYChart.Series<Double, Double>> lineChartData;

  private ObjectProperty<ConnectionState> state = new SimpleObjectProperty<>();

  private LiveDataParser liveDataParser;

  @FXML
  private ListView<MeasureSequence> measurementListView;

  @FXML
  private LineChart<Double, Double> datachart;

  @FXML
  private NumberAxis xaxis;

  @FXML
  private NumberAxis yaxis;

  @FXML
  private TextArea miniTerminalTextArea;

  @FXML
  private Label systemNameLabel;

  @FXML
  private Label systemTimeLabel;

  @FXML
  private Label gpsPositionLabel;

  @FXML
  private Label currentCommandLabel;

  @FXML
  private TextField sendAnyCommandField;

  @FXML
  private Button sendAnyCommandButton;

  @FXML
  private Button autoModeButton;

  @FXML
  private Button setTimeButton;

  @FXML
  private HBox msgBoxBltOff;

  @FXML
  private Button msgBoxBltOffRefreshButton;

  @FXML
  private HBox msgBoxSearching;

  @FXML
  private HBox msgBoxDevices;

  @FXML
  private VBox msgBoxDevicesList;

  @FXML
  private Button msgBoxDevicesRefreshButton;

  @FXML
  private Button bltDisconnectButton;

  @FXML
  private Button manualModeButton;

  @FXML
  private Button manualMeasurementButton;

  @FXML
  private Button setIntegrationTimeButton;

  @FXML
  private Button setIntervalButton;


  private MeasureSequence selectedMeasurement;


  /**
   * Constructor.
   *
   * @param model the one and only model.
   */
  public LiveViewController(FrvaModel model) {
    connectionStateInit = new ConnectionStateInit(this);
    state.setValue(connectionStateInit);
    this.model = model;
    liveDataParser = new LiveDataParser(this, model);
    lineChartData = FXCollections.observableArrayList();

  }

  @FXML
  private void initialize() {
    defineButtonActions();
    initializeLayout();
    addBindings();
    addListeners();
  }


  private void initializeLayout() {
    datachart.setAnimated(false);
    datachart.setCreateSymbols(false);
    datachart.setAlternativeRowFillVisible(false);
    datachart.setLegendVisible(false);
    datachart.setData(lineChartData);
  }


  private void addBindings() {
    systemNameLabel.textProperty().bind(deviceStatus.systemnameProperty());
    gpsPositionLabel.textProperty().bind(deviceStatus.gpsInformationProperty());

    measurementListView.setItems(model.getLiveSequences());

    manualMeasurementButton.disableProperty().bind(liveDataParser.acceptingCommandsProperty());

    setIntegrationTimeButton.disableProperty().bind(liveDataParser.acceptingCommandsProperty());
    setIntervalButton.disableProperty().bind(liveDataParser.acceptingCommandsProperty());
    setTimeButton.disableProperty().bind(liveDataParser.acceptingCommandsProperty());
  }


  private void defineButtonActions() {
    sendAnyCommandButton.setOnAction(event -> {
      String command = sendAnyCommandField.getText();
      sendAnyCommandField.setText("");
      liveDataParser.addCommandToQueue(new CommandAny(liveDataParser, model, command));
    });

    autoModeButton.setOnAction(event -> {
      liveDataParser.addCommandToQueue(new CommandAutoMode(liveDataParser, model));
    });

    manualModeButton.setOnAction(event -> {
      liveDataParser.addCommandToQueue(new CommandManualMode(liveDataParser, model));
    });

    manualMeasurementButton.setOnAction(event -> {
      liveDataParser.addCommandToQueue(new CommandManualMeasurement(liveDataParser, model));
    });


    setTimeButton.setOnAction(event -> {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Set Time & Date");
      alert.setHeaderText("Do you want to set time and date on the connected device \n "
          + "to your current systemtime?");
      alert.setContentText(null);

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        liveDataParser.addCommandToQueue(new CommandSetTime(liveDataParser, model, null));
      }
    });

    setIntegrationTimeButton.setOnAction(event -> {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Set integration time");
      dialog.setHeaderText(null);
      dialog.setContentText("Set integration time to [ms]:");

      Optional<String> result = dialog.showAndWait();
      if (result.isPresent()) {
        liveDataParser.addCommandToQueue(
            new CommandSetItegrationTime(liveDataParser, model, Integer.valueOf(result.get())));
        liveDataParser.addCommandToQueue(
            new CommandGetConfiguration(liveDataParser, model));
      }
    });

    setIntervalButton.setOnAction(event -> {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Set interval between measurements");
      dialog.setHeaderText(null);
      dialog.setContentText("Set the interval between measurements to [s]:");

      Optional<String> result = dialog.showAndWait();
      if (result.isPresent()) {
        liveDataParser.addCommandToQueue(
            new CommandSetInterval(liveDataParser, model, Integer.valueOf(result.get())));
        liveDataParser.addCommandToQueue(
            new CommandGetConfiguration(liveDataParser, model));
      }
    });
  }


  private void addListeners() {
    model.activeViewProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue.equals(activeView) && state.getValue().equals(connectionStateInit)) {
        state.getValue().handle();
      }
    });

    state.addListener(observable -> {
      logger.info("New state is: " + state.getValue().getClass().getSimpleName());
      state.getValue().handle();
    });


    measurementListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    measurementListView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          selectedMeasurement = newValue;
          redrawGraph(selectedMeasurement);
        });


    model.getLiveSequences().addListener(new ListChangeListener<MeasureSequence>() {
      @Override
      public void onChanged(Change<? extends MeasureSequence> c) {
        while (c.next()) {
          c.getAddedSubList().forEach(o -> {
            measurementListView.getSelectionModel().select(o);
          });

        }
      }
    });
  }


  /**
   * Displays a dialog to indicate that the BLuetooth is off.
   *
   * @param active true displays, false not.
   */
  public void displayBluetoothOffDialog(boolean active) {
    msgBoxBltOff.setVisible(active);
    msgBoxBltOffRefreshButton.setOnAction(event -> state.getValue().handle());
  }


  /**
   * Dialog to displays all availble Devices with a SPP.
   *
   * @param active true to show the dialog.
   */
  public void displayAvailableDevicesDialog(boolean active) {
    if (active) {
      if (availableServiceRecords.size() > 0) {
        msgBoxDevicesList.getChildren().clear();
      }
      availableServiceRecords.forEach(serviceRecords -> {
        final String[] deviceAddress = {serviceRecords[0].getHostDevice().getBluetoothAddress()};
        final String[] deviceName = {deviceAddress[0]};
        Runnable r = new Runnable() {
          @Override
          public void run() {
            try {
              deviceName[0] = serviceRecords[0].getHostDevice().getFriendlyName(false);

            } catch (IOException e) {

              e.printStackTrace();
            }
          }
        };

        Thread t = new Thread(r);
        t.start();

        Label namelabel = null;
        try {
          t.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        namelabel = new Label(deviceName[0]);
        namelabel.setStyle("-fx-font-weight: bold");
        String[] split = deviceAddress[0].split("(?<=\\G..)");
        Label addresslabel = new Label(Arrays.toString(split)
            .replace(", ", ":"));
        VBox buttonLabaleBox = new VBox();
        buttonLabaleBox.getChildren().addAll(namelabel, addresslabel);
        Button serviceRecordButton = new Button();
        serviceRecordButton.setGraphic(buttonLabaleBox);
        serviceRecordButton.setPrefWidth(200);
        serviceRecordButton.setPrefHeight(50);

        String finalDeviceName = deviceName[0];
        serviceRecordButton.setOnAction((ActionEvent event) -> {
          deviceStatus.setSystemname(finalDeviceName);
          setSelectedServiceRecord(serviceRecords);
          displayAvailableDevicesDialog(false);
          setState(new ConnectionStateConnecting(this));
        });
        msgBoxDevicesList.getChildren().add(serviceRecordButton);
      });
    }

    msgBoxDevices.setVisible(active);

    msgBoxDevicesRefreshButton.setOnAction(event -> {
      setState(new ConnectionStateSearching(this));
      displayAvailableDevicesDialog(false);
    });
  }

  /**
   * Dialog displayed during deviceSearch.
   *
   * @param active true to show the dialog.
   */
  public void displaySearchingDialog(boolean active) {
    msgBoxSearching.setVisible(active);
  }

  /**
   * Enables the DisconnectButton on the view.
   */
  public void enableBltDisconnectButton() {
    bltDisconnectButton.setDisable(false);
    bltDisconnectButton.setOnAction(event -> {
      bltDisconnectButton.setDisable(true);
      setState(new ConnectionStateDisconnecting(this));
    });
  }


  public void setActiveView(Node activeView) {
    this.activeView = activeView;
  }

  public void setState(ConnectionState state) {
    this.state.setValue(state);
  }

  public void setAvailableServiceRecords(List<ServiceRecord[]> availableServiceRecords) {
    this.availableServiceRecords = availableServiceRecords;
  }

  public void setSelectedServiceRecord(ServiceRecord[] selectedServiceRecord) {
    this.selectedServiceRecord = selectedServiceRecord;
  }

  public ServiceRecord[] getSelectedServiceRecord() {
    return selectedServiceRecord;
  }

  public void setOpenStreamConnection(StreamConnection openStreamConnection) {
    this.openStreamConnection = openStreamConnection;
  }

  public StreamConnection getOpenStreamConnection() {
    return openStreamConnection;
  }

  public TextArea getMiniTerminalTextArea() {
    return miniTerminalTextArea;
  }

  public void setLiveDataParser(LiveDataParser liveDataParser) {
    this.liveDataParser = liveDataParser;
  }

  public LiveDataParser getLiveDataParser() {
    return liveDataParser;
  }


  public DeviceStatus getDeviceStatus() {
    return deviceStatus;
  }

  public void setCurrentCommandLabel(String text) {
    Platform.runLater(() -> this.currentCommandLabel.setText(text));
  }

  /**
   * Refreshes the graph with the give measureSequence.
   *
   * @param sequence the sequenc to draw.
   */
  public void redrawGraph(MeasureSequence sequence) {
    Platform.runLater(() -> {
      lineChartData.clear();
    });


    Set<Map.Entry<MeasureSequence.SequenceKeyName, double[]>> entries = null;

    entries = sequence.getData().entrySet();

    for (Map.Entry<MeasureSequence.SequenceKeyName, double[]> entry : entries) {
      double[] data = entry.getValue();
      LineChart.Series<Double, Double> series = new LineChart.Series<Double, Double>();
      series.setName(sequence.getSequenceUuid() + "/" + entry.getKey());

      for (int i = 0; i < data.length; i++) {
        if (data[i] != Double.POSITIVE_INFINITY && data[i] != Double.NEGATIVE_INFINITY) {
          double x = i;
          double y = data[i];
          series.getData().add(new XYChart.Data<>(x, y));
        }
      }

      Platform.runLater(() -> {
        lineChartData.add(series);
      });

    }

  }

  public void refreshList() {
    measurementListView.refresh();
  }
}
