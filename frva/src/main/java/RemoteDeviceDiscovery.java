import com.intel.bluetooth.RemoteDeviceHelper;
import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 * Minimal Device Discovery example.
 * <p>
 * On linux x64 :
 * sudo apt-get install libbluetooth-dev
 */
public class RemoteDeviceDiscovery {

  public static final Vector/*<RemoteDevice>*/ devicesDiscovered = new Vector();

  public static void main(String[] args) throws IOException, InterruptedException {

    final Object inquiryCompletedEvent = new Object();

    devicesDiscovered.clear();

    DiscoveryListener listener = new DiscoveryListener() {

      public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
        devicesDiscovered.addElement(btDevice);
        try {
          System.out.println("     name " + btDevice.getFriendlyName(false));
          RemoteDeviceHelper.authenticate(btDevice);
          System.out.println(cod.getMajorDeviceClass());

        } catch (IOException cantGetDeviceName) {
          System.out.println(cantGetDeviceName.getMessage());
        }
      }

      public void inquiryCompleted(int discType) {
        System.out.println("Device Inquiry completed!");
        synchronized (inquiryCompletedEvent) {
          inquiryCompletedEvent.notifyAll();
        }
      }

      public void serviceSearchCompleted(int transID, int respCode) {
      }

      public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
      }
    };

    synchronized (inquiryCompletedEvent) {

      boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
//      LocalDevice.getLocalDevice().
      if (started) {
        System.out.println("wait for device inquiry to complete...");
        inquiryCompletedEvent.wait();
        System.out.println(devicesDiscovered.size() + " device(s) found");
      }


//      devicesDiscovered.forEach(o -> {
//        RemoteDevice a = (RemoteDevice)o;
//
//        System.out.println(a.isAuthenticated());
//
//        try {
//          a.authenticate();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//
//      });


    }
  }

}
