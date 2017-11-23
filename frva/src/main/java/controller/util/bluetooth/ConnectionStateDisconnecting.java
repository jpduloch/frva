package controller.util.bluetooth;

import controller.LiveViewController;
import java.io.IOException;

public class ConnectionStateDisconnecting extends AbstractConnectionState {
  private final LiveViewController liveViewController;

  public ConnectionStateDisconnecting(LiveViewController liveViewController) {
    this.liveViewController = liveViewController;
  }

  @Override
  public void handle() {
    try {
      BluetoothConnection.closeConnection(liveViewController.getOpenStreamConnection());
    } catch (IOException e) {
      e.printStackTrace();
    }
    liveViewController.setOpenStreamConnection(null);
    liveViewController.setSelectedServiceRecord(null);
    liveViewController.setState(new ConnectionStateSearching(liveViewController));
  }
}
