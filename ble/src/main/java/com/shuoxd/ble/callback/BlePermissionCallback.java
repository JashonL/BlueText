package com.shuoxd.ble.callback;

/**
 * Result of BLE runtime permission request.
 */
public interface BlePermissionCallback {

    void onGranted();

    /**
     * @param neverAskAgain true if user selected "Don't ask again" for at least one permission
     */
    void onDenied(boolean neverAskAgain);
}
