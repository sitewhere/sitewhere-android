package com.sitewhere.android.mqtt.ui;

/**
 * Allows {@link ConnectivityWizardFragment} to notify owning activity that the connectivity
 * wizard is complete.
 * 
 * @author Derek
 */
public interface IConnectivityWizardListener {

	/**
	 * Called after completion of connectivity wizard.
	 */
	public void onWizardComplete();
}