package com.sitewhere.android;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Network utility methods.
 * 
 * @author Derek
 */
public class NetworkUtils {


	/**
	 * Indicates whether the device is online.
	 * 
	 * @return
	 */
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if ((cm.getActiveNetworkInfo() != null) && (cm.getActiveNetworkInfo().isAvailable())
				&& (cm.getActiveNetworkInfo().isConnected())) {
			return true;
		}
		return false;
	}
}