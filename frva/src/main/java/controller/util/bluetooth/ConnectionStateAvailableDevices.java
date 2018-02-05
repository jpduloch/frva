/*
 *     This file is part of FRVA
 *     Copyright (C) 2018 Andreas Hüni
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package controller.util.bluetooth;

import controller.LiveViewController;

/**
 * The ConnectionStateAvailableDevices represents the state when the user can
 * select a device on the user interface.
 */
public class ConnectionStateAvailableDevices implements ConnectionState {
  private final LiveViewController liveViewController;

  public ConnectionStateAvailableDevices(LiveViewController liveViewController) {
    this.liveViewController = liveViewController;
  }

  @Override
  public void handle() {
    liveViewController.displayAvailableDevicesDialog(true);
  }
}
