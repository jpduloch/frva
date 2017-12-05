package controller.util.bluetooth;

import controller.LiveViewController;

public class ConnectionStateBltOff implements ConnectionState {

  private final LiveViewController liveViewController;

  public ConnectionStateBltOff(LiveViewController liveViewController) {

    this.liveViewController = liveViewController;
  }

  @Override
  public void handle() {
    //TODO evaluate how to do this on OSX
    if (true) {
      liveViewController.displayBluetoothOffDialog(false);
      liveViewController.setState(new ConnectionStateSearching(liveViewController));
    } else {
      liveViewController.displayBluetoothOffDialog(true);
    }
  }
}
