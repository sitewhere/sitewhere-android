package com.sitewhere.android.mqtt.ui;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sitewhere.android.NetworkUtils;
import com.sitewhere.android.mqtt.R;
import com.sitewhere.rest.model.system.Version;
import com.sitewhere.rest.service.SiteWhereClient;
import com.sitewhere.spi.ISiteWhereClient;
import com.sitewhere.spi.SiteWhereException;

/**
 * Fragment that allows the user to choose the host/port of the SiteWhere instance to interact with.
 * 
 * @author Derek
 */
public class SiteWhereHostChooserFragment extends Fragment {

	/** Tag for logging */
	private static final String TAG = "SiteWhereHostChooser";

	/** Color for error messages */
	private static final String ERROR_COLOR = "#550000";

	/** Color for success messages */
	private static final String SUCCESS_COLOR = "#005500";

	/** Text field that holds host URI */
	private EditText apiUri;

	/** Button for verifying API */
	private Button apiVerifyButton;

	/** Layout for verification info */
	private LinearLayout apiVerifyGroup;

	/** Message indicating we are verifying API URL */
	private TextView apiVerifyMessage;

	/** Check mark indicating server verified */
	private ImageView apiVerifyCheck;

	/** Spinner progress for API verify */
	private ProgressBar apiVerifyProgress;

	/** Layout for MQTT hostname info */
	private LinearLayout mqttHostGroup;

	/** MQTT title */
	private TextView mqttTitle;

	/** MQTT label */
	private TextView mqttLabel;

	/** Text field that holds MQTT URI */
	private EditText mqttUri;

	/** Button for verifying MQTT */
	private Button mqttVerifyButton;

	/** Layout for MQTT verification info */
	private LinearLayout mqttVerifyGroup;

	/** Message indicating we are verifying MQTT URL */
	private TextView mqttVerifyMessage;

	/** Check mark indicating MQTT verified */
	private ImageView mqttVerifyCheck;

	/** Spinner progress for MQTT verify */
	private ProgressBar mqttVerifyProgress;

	/** Used to handle thread execution */
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public SiteWhereHostChooserFragment() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
	 * android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.sitewhere_host, container, false);
		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setupApiFields();
		setupMqttFields();
	}

	/**
	 * Set up field for verifying API access.
	 */
	protected void setupApiFields() {
		// Get reference to API hostname text field.
		apiUri = (EditText) getActivity().findViewById(R.id.sitewhere_api);

		// Get reference to 'verify' button.
		apiVerifyButton = (Button) getActivity().findViewById(R.id.sitewhere_api_submit);
		apiVerifyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onHostVerifyButtonClicked(v);
			}
		});

		// Get reference API verify group.
		apiVerifyGroup = (LinearLayout) getActivity().findViewById(R.id.sitewhere_api_verify_grp);

		// Get reference 'verify message' text view.
		apiVerifyMessage = (TextView) getActivity().findViewById(R.id.sitewhere_api_verify);

		// Get reference to check indicator for API verify.
		apiVerifyCheck = (ImageView) getActivity().findViewById(
				R.id.sitewhere_host_api_verify_check);

		// Get reference to progress indicator for API verify.
		apiVerifyProgress = (ProgressBar) getActivity().findViewById(
				R.id.sitewhere_api_verify_progress);
	}

	/**
	 * Set up field for verifying API access.
	 */
	protected void setupMqttFields() {
		// Get reference MQTT title.
		mqttTitle = (TextView) getActivity().findViewById(R.id.sitewhere_mqtt_title);

		// Get reference MQTT title.
		mqttLabel = (TextView) getActivity().findViewById(R.id.sitewhere_mqtt_label);

		// Get reference MQTT host group.
		mqttHostGroup = (LinearLayout) getActivity().findViewById(R.id.sitewhere_mqtt_host_grp);

		// Get reference to MQTT host text field.
		mqttUri = (EditText) getActivity().findViewById(R.id.sitewhere_mqtt);

		// Get reference to 'verify' button.
		mqttVerifyButton = (Button) getActivity().findViewById(R.id.sitewhere_mqtt_submit);
		mqttVerifyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onMqttVerifyButtonClicked(v);
			}
		});

		// Get reference API verify group.
		mqttVerifyGroup = (LinearLayout) getActivity().findViewById(R.id.sitewhere_mqtt_verify_grp);

		// Get reference 'verify message' text view.
		mqttVerifyMessage = (TextView) getActivity().findViewById(R.id.sitewhere_mqtt_verify);

		// Get reference to check indicator for MQTT verify.
		mqttVerifyCheck = (ImageView) getActivity().findViewById(R.id.sitewhere_mqtt_verify_check);

		// Get reference to progress indicator for MQTT verify.
		mqttVerifyProgress = (ProgressBar) getActivity().findViewById(
				R.id.sitewhere_mqtt_verify_progress);
	}

	/**
	 * Verifies connection by creating the API URL and testing a call via the client.
	 * 
	 * @param v
	 */
	protected void onHostVerifyButtonClicked(View v) {
		if (!NetworkUtils.isOnline(getActivity())) {
			InterfaceUtils.showAlert(getActivity(), R.string.sitewhere_no_network_message,
					R.string.sitewhere_no_network_title);
			return;
		}

		// Disable button until processing is complete.
		apiVerifyButton.setEnabled(false);

		String uri = apiUri.getText().toString();
		if (uri.length() == 0) {
			apiUri.setError("Enter a value for the remote SiteWhere server address.");
			apiVerifyGroup.setVisibility(View.GONE);
			return;
		}

		// Calculate API URL and create client for testing.
		String api = "http://" + uri + "/sitewhere/api/";
		apiVerifyMessage.setTextColor(Color.parseColor(SUCCESS_COLOR));
		apiVerifyMessage.setText("Verifying SiteWhere API available for URI: '" + api + "' ...");

		// Make the group visible.
		apiVerifyGroup.setVisibility(View.VISIBLE);
		apiVerifyProgress.setVisibility(View.VISIBLE);

		SiteWhereClient client = new SiteWhereClient(api, "admin", "password", 4000);
		executor.submit(new HostVerifier(client));
	}

	/**
	 * Show error message encountered while verifying SiteWhere server access.
	 * 
	 * @param error
	 * @param t
	 */
	protected void handleVerifyError(String error, Throwable t) {
		apiVerifyButton.setEnabled(true);
		apiVerifyProgress.setVisibility(View.GONE);
		apiVerifyMessage.setTextColor(Color.parseColor(ERROR_COLOR));
		apiVerifyMessage.setText("Server verify failed: " + error);
		Log.e(TAG, error, t);
	}

	/**
	 * Show success message for verifying SiteWhere server access.
	 * 
	 * @param message
	 */
	protected void handleVerifySuccess(Version version) {
		apiVerifyProgress.setVisibility(View.GONE);
		apiVerifyCheck.setVisibility(View.VISIBLE);
		apiVerifyMessage.setTextColor(Color.parseColor(SUCCESS_COLOR));
		apiVerifyMessage.setText("SiteWhere server (version " + version.getVersionIdentifier()
				+ ") verified.");
		Log.d(TAG, "SiteWhere server reported version " + version.getVersionIdentifier() + ".");

		showMqttHostFields();
	}

	/**
	 * Show fields for entering MQTT host.
	 */
	protected void showMqttHostFields() {
		mqttTitle.setVisibility(View.VISIBLE);
		mqttLabel.setVisibility(View.VISIBLE);
		mqttHostGroup.setVisibility(View.VISIBLE);

		String apiUriStr = apiUri.getText().toString();
		String[] apiParts = apiUriStr.split("[:]+");
		if (apiParts.length > 0) {
			mqttUri.setText(apiParts[0] + ":1883");
		}
	}

	/**
	 * Verifies MQTT connection by trying to establish a connection.
	 * 
	 * @param v
	 */
	protected void onMqttVerifyButtonClicked(View v) {
		if (!NetworkUtils.isOnline(getActivity())) {
			InterfaceUtils.showAlert(getActivity(), R.string.sitewhere_no_network_message,
					R.string.sitewhere_no_network_title);
			return;
		}

		// Disable button until processing is complete.
		mqttVerifyButton.setEnabled(false);

		String uri = mqttUri.getText().toString();
		if (uri.length() == 0) {
			mqttUri.setError("Enter a value for the remote MQTT broker address.");
			mqttVerifyGroup.setVisibility(View.GONE);
			return;
		}

		// Calculate API URL and create client for testing.
		String api = "http://" + uri;
		mqttVerifyMessage.setTextColor(Color.parseColor(SUCCESS_COLOR));
		mqttVerifyMessage.setText("Verifying MQTT broker available for URI: '" + api + "' ...");

		// Make the group visible.
		mqttVerifyGroup.setVisibility(View.VISIBLE);
		mqttVerifyProgress.setVisibility(View.VISIBLE);

		String mqttUriStr = mqttUri.getText().toString();
		String[] mqttParts = mqttUriStr.split("[:]+");
		String broker = null;
		int port = 1883;
		if (mqttParts.length > 0) {
			broker = mqttParts[0];
		}
		if (mqttParts.length > 1) {
			try {
				port = Integer.parseInt(mqttParts[1]);
			} catch (NumberFormatException e) {
				mqttVerifyMessage.setTextColor(Color.parseColor(ERROR_COLOR));
				mqttVerifyMessage.setText("Invalid MQTT broker port value.");
				return;
			}
		}

		if (broker == null) {
			mqttVerifyMessage.setTextColor(Color.parseColor(ERROR_COLOR));
			mqttVerifyMessage.setText("Invalid MQTT broker hostname.");
			return;
		}

		executor.submit(new MqttVerifier(broker, port));
	}

	/**
	 * Show error message encountered while verifying MQTT broker access.
	 * 
	 * @param error
	 * @param t
	 */
	protected void handleMqttError(String error, Throwable t) {
		mqttVerifyButton.setEnabled(true);
		mqttVerifyProgress.setVisibility(View.GONE);
		mqttVerifyMessage.setTextColor(Color.parseColor(ERROR_COLOR));
		mqttVerifyMessage.setText("MQTT verify failed: " + error);
		Log.e(TAG, error, t);
	}

	/**
	 * Show success message for verifying MQTT broker access.
	 * 
	 * @param message
	 */
	protected void handleMqttSuccess() {
		mqttVerifyProgress.setVisibility(View.GONE);
		mqttVerifyCheck.setVisibility(View.VISIBLE);
		mqttVerifyMessage.setTextColor(Color.parseColor(SUCCESS_COLOR));
		mqttVerifyMessage.setText("MQTT broker connectivity verified.");
	}

	/**
	 * Thread that verifies that the SiteWhere server is available.
	 * 
	 * @author Derek
	 */
	private class HostVerifier implements Runnable {

		private ISiteWhereClient client;

		public HostVerifier(ISiteWhereClient client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				final Version version = client.getSiteWhereVersion();
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						handleVerifySuccess(version);
					}
				});
			} catch (final SiteWhereException e) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						handleVerifyError("SiteWhere version call failed.", e);
					}
				});
			} catch (final RuntimeException e) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						handleVerifyError("Unable to connect to server.", e.getCause());
					}
				});
			} catch (final Throwable t) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						handleVerifyError("Unexpected exception.", t);
					}
				});
			}
		}
	}

	/**
	 * Thread that verifies that the MQTT broker is available.
	 * 
	 * @author Derek
	 */
	private class MqttVerifier implements Runnable {

		/** MQTT broker hostname */
		private String hostname;

		/** MQTT broker port */
		private int port;

		public MqttVerifier(String hostname, int port) {
			this.hostname = hostname;
			this.port = port;
		}

		@Override
		public void run() {
			try {
				MQTT mqtt = new MQTT();
				mqtt.setConnectAttemptsMax(1);
				mqtt.setReconnectAttemptsMax(0);
				mqtt.setKeepAlive((short) 300);
				Log.d(TAG, "Connecting to MQTT...");

				mqtt.setHost(this.hostname, this.port);
				BlockingConnection connection = mqtt.blockingConnection();
				connection.connect();
				connection.disconnect();
				Log.d(TAG, "Connected to MQTT.");

				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						handleMqttSuccess();
					}
				});
			} catch (final URISyntaxException e) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						handleMqttError("MQTT broker hostname invalid.", e);
					}
				});
			} catch (final Throwable t) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						handleMqttError("Unable to connect to MQTT broker.", t);
					}
				});
			}
		}
	}
}