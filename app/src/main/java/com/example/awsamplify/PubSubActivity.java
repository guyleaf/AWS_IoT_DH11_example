package com.example.awsamplify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;

import java.security.KeyStore;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PubSubActivity extends Activity {

    static final String LOG_TAG = PubSubActivity.class.getCanonicalName();

    // --- Constants to modify per your configuration ---

    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a31mmh9dz5lr6l-ats.iot.us-east-1.amazonaws.com";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;

    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "107590037-keystore.jks";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "107590037";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "107590037";
    // Certificate and key aliases in the KeyStore
    private static final String CLIENT_ID = "107590037APP";

    private static final String PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEowIBAAKCAQEAzybOXmw0TZyond6lq9kjTKwfQTRui7tQX7PR0afLQRtv6/0X\n" +
            "EWzXwDe9RwNy+TY5vcX0fdhHybJyoIsZ/9eqUcGkNWCy79IWWRNYaKZyt2Zra668\n" +
            "CBzUVMPJeP766eyZq9JlfGg0yE3wEbXqfZ/Sm8vT7wlo54KbCXDet2RtGTqQ8fVG\n" +
            "1gKwaHXJJ4zDyiTCsYIbpblptxN9aHyClUhsV1Spt9n52DcxM/RZq8LdCkp8hYTW\n" +
            "adrWSb78RuMgGR19ckvPM0dplkqUKG1qPg9ZXjYS3vYAwJb+m8qS8ZvQGTkvsA8H\n" +
            "E6OCXYZDBLzlQ0anepJ2OPLaWfiJEfdbUb7i1QIDAQABAoIBAHh+gSHpXM8cZfRW\n" +
            "cxWv21mY8SEzh23eChVw+oYyTQdVF1g0wntPTXwwr/ejqPgPM60QfXPKH4/SLdz2\n" +
            "rKvl3dtqgMLPuaXIFVnA5/cPgsDtYWSkohkx88xOil3IK1lwV77bRk8EKletBgy3\n" +
            "PWvPUGlxOZjcZ1nq3ZWzQsl4MAGwwzu5dR9Uw1NQ+fXvJoS+40aYzhpXNXEVYBrh\n" +
            "FRHqKtRdmTgv4iV8E13qIkmkcPACgpHLIuCIkFufqh7sp3cvwHb4EyGaXqTPO1Qy\n" +
            "fPJLsteQblOj7wR83T4ajWYAye9tRbHJN/w4O47Ql3tQpB/g52TE9Ic5bCHP54xi\n" +
            "F4IyfO0CgYEA/dvrO+ySviH3/tc0Bti85toGuO6CHfT1FzOZIAGOxkbvSHTemvRv\n" +
            "JebZGP1TxChzjCc1yufKYTMy/jreq6vRpdCkYDl9BA59ZpPL+9Q3wj4zoc0ORX1n\n" +
            "Xl5nPdBXnbmDWnLm6pxFf7JmliepEsH3QLLPxF8PrjReh+6zBSld4acCgYEA0OYL\n" +
            "wY9xtwESV1GmEEMrnvZ8mK4dTUYlWTXhf5TFbefir7u0fJwjsOXTeEKQvN0rWuaA\n" +
            "EDRSu6OxNFZkgpWN0TvG41YvbBxEhjVVLtpjj0lNEkD/vB6DgrLen/8yltlfc4Io\n" +
            "mluoZHBBSLyZxBA6dfrGiLKVB2sgYNukbAxVzyMCgYAeoryntawb3/OSwJvZu6xI\n" +
            "W8V16eqv/NDbJPH0x9tYGFVmNBxtLNSBCXPnrSK/yHXxUwYN350vz8L2pVob85dQ\n" +
            "YHaF2ko5BBqkF3AFix3Thjgq7ZksT67613+0GQGJ2kp8zuMJKyTD/V6d/llMFzZk\n" +
            "i2/BbgtxZYaG9wqJs+1NfwKBgHq5i9KNWTswT/wopa0MKUZcfNqd/+zz9uEwPGhd\n" +
            "mJd3EcA4QFKs0HMOsmwGf03VibD/leDvPGOsSWD8GFR3VDfGLJRGf9m9yawnDQJX\n" +
            "nwIBCFxe/18fdheEyjGpFal1zxPnNb9pdcZ8BH6c/qEm/5FsYNLSR6dyEjWVFEW+\n" +
            "AXONAoGBANq99aRaF2tn7yQkzCTaxWdH0J++1/e7sUyb3MzS41XMsvAwG86yJOiA\n" +
            "cV8/8RD9KTfVXuvRfjTCsL3+1UvaMk8NafjyznIYTnrmp2hxpc4yYH8Esff6q8hD\n" +
            "dawhhYDPcl4Nz0Y8AjHjSXAaF3c3//bxvHjo3sh1UxNIPsRa3foF\n" +
            "-----END RSA PRIVATE KEY-----";

    private static final String CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDWTCCAkGgAwIBAgIUarhcMtkd+1jZk8X78RygR0TYxDQwDQYJKoZIhvcNAQEL\n" +
            "BQAwTTFLMEkGA1UECwxCQW1hem9uIFdlYiBTZXJ2aWNlcyBPPUFtYXpvbi5jb20g\n" +
            "SW5jLiBMPVNlYXR0bGUgU1Q9V2FzaGluZ3RvbiBDPVVTMB4XDTIxMDQwMTA3MDEw\n" +
            "MFoXDTQ5MTIzMTIzNTk1OVowHjEcMBoGA1UEAwwTQVdTIElvVCBDZXJ0aWZpY2F0\n" +
            "ZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAM8mzl5sNE2cqJ3epavZ\n" +
            "I0ysH0E0bou7UF+z0dGny0Ebb+v9FxFs18A3vUcDcvk2Ob3F9H3YR8mycqCLGf/X\n" +
            "qlHBpDVgsu/SFlkTWGimcrdma2uuvAgc1FTDyXj++unsmavSZXxoNMhN8BG16n2f\n" +
            "0pvL0+8JaOeCmwlw3rdkbRk6kPH1RtYCsGh1ySeMw8okwrGCG6W5abcTfWh8gpVI\n" +
            "bFdUqbfZ+dg3MTP0WavC3QpKfIWE1mna1km+/EbjIBkdfXJLzzNHaZZKlChtaj4P\n" +
            "WV42Et72AMCW/pvKkvGb0Bk5L7APBxOjgl2GQwS85UNGp3qSdjjy2ln4iRH3W1G+\n" +
            "4tUCAwEAAaNgMF4wHwYDVR0jBBgwFoAUBG4AjPu4ksnUFz/fcipyJfm8Ci0wHQYD\n" +
            "VR0OBBYEFMEycMHuZredj83c77r1UaXUxzMXMAwGA1UdEwEB/wQCMAAwDgYDVR0P\n" +
            "AQH/BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQCDHv7dwufwFLbYjv3vCMScuGOu\n" +
            "XG0SO1F32zxwYVQUdidmCP40rkz+wEEWhIkXgtYep3SArAQeeaCT9YYOtlWmhNGN\n" +
            "R8bMiwJvzul2Vm/hw4+o+1OpPJ4WT2a9FCIWANFxh6/EM96uDG5UgcktbNG3c7aW\n" +
            "5Kf8OoG3/7F639jfWeEuyaroqH+Z4Z3Ws51A3FRG0TTtClRRGY/aeuXdSmEIv/lg\n" +
            "mLHpk6Cir38tD4FAVU2ZMGxt+o1HCYBjEhz6vpJAEzsCVx8ln3SI7AfkagWeQsUU\n" +
            "2zsA0jYq9zuqmvHmGO+2PiCc1n6MKFrw9hWLJKACxFCjqPKxAVVoLdjORRMr\n" +
            "-----END CERTIFICATE-----";

    Toast toast;

    EditText txtSubscribe;
    EditText txtTopic;
    EditText txtMessage;

    TextView tvLastMessage;
    TextView tvClientId;
    TextView tvStatus;
    TextView tvResult;

    Button btnConnect;
    Button btnSubscribe;
    Button btnPublish;
    Button btnDisconnect;

    AWSIotMqttManager mqttManager;
    String clientId;
    String keystorePath;
    String keystoreName;
    String keystorePassword;

    KeyStore clientKeyStore = null;
    String certificateId;
    String privateKey;
    String cert;

    Deque<String> dsHistory = new ArrayDeque<>();
    ArrayList<String> asHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSubscribe = (EditText) findViewById(R.id.txtSubscribe);
        txtTopic = (EditText) findViewById(R.id.txtTopic);
        txtMessage = (EditText) findViewById(R.id.txtMessage);

        tvLastMessage = (TextView) findViewById(R.id.tvLastMessage);
        tvClientId = (TextView) findViewById(R.id.tvClientId);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvResult = findViewById(R.id.txtResult);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(connectClick);

        btnSubscribe = (Button) findViewById(R.id.btnSubscribe);
        btnSubscribe.setOnClickListener(subscribeClick);

        btnPublish = (Button) findViewById(R.id.btnPublish);
        btnPublish.setOnClickListener(publishClick);

        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(disconnectClick);

        btnPublish.setEnabled(false);
        btnSubscribe.setEnabled(false);

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = CLIENT_ID;
        tvClientId.setText(clientId);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);
        mqttManager.setAutoReconnect(false);
        keystorePath = getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        privateKey = PRIVATE_KEY;
        cert = CERT;

        try {
            if (!AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName))
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId, cert, privateKey, keystorePath, keystoreName, keystorePassword);
            clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId, keystorePath, keystoreName, keystorePassword);
            btnConnect.setEnabled(true);
            btnDisconnect.setEnabled(false);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }
    }

    View.OnClickListener connectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard(PubSubActivity.this);

            Log.d(LOG_TAG, "clientId = " + clientId);

            try {
                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                        Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == AWSIotMqttClientStatus.Connecting) {
                                    tvStatus.setText("Connecting...");
                                    btnConnect.setEnabled(false);

                                } else if (status == AWSIotMqttClientStatus.Connected) {
                                    btnConnect.setEnabled(false);
                                    btnDisconnect.setEnabled(true);
                                    btnPublish.setEnabled(true);
                                    btnSubscribe.setEnabled(true);
                                    tvStatus.setText("Connected");
                                    tvResult.setText("Connect Success!");

                                } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                    }
                                    tvStatus.setText("Reconnecting");
                                } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                        throwable.printStackTrace();
                                    }
                                    tvStatus.setText("Disconnected");
                                } else {
                                    tvStatus.setText("Disconnected");

                                }
                            }
                        });
                    }
                });

            } catch (final Exception e) {
                Log.e(LOG_TAG, "Connection error.", e);
                tvStatus.setText("Error! " + e.getMessage());
                tvResult.setText("Connect Failed!");
            }
        }
    };

    View.OnClickListener subscribeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard(PubSubActivity.this);

            if(txtSubscribe.getText().toString().matches(""))
            {
                toast = Toast.makeText(PubSubActivity.this,"Please input Topic name",Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            final String topic = txtSubscribe.getText().toString();

            Log.d(LOG_TAG, "topic = " + topic);

            try {
                mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                        new AWSIotMqttNewMessageCallback() {
                            @Override
                            public void onMessageArrived(final String topic, final byte[] data) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String message = new String(data, "UTF-8");
                                            JSONObject jsonObject = new JSONObject(message);
                                            Log.d(LOG_TAG, "Message arrived:");
                                            Log.d(LOG_TAG, "   Topic: " + topic);
                                            Log.d(LOG_TAG, " Message: " + message);

                                            String humid = "Humidity :" + jsonObject.getString("humidity") + " %";
                                            String temp = "Temperature :" + jsonObject.getString("temperature") + " °C";

                                            tvLastMessage.setText(humid + "\n" + temp);

                                            dsHistory.addFirst((humid + ", " + temp));
                                            if(dsHistory.size()>10)
                                                dsHistory.removeLast();

                                        } catch (UnsupportedEncodingException | JSONException e) {
                                            Log.e(LOG_TAG, "Message encoding error.", e);
                                        }
                                    }
                                });
                            }
                        });
                tvResult.setText("Subscribe Success!");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Subscription error.", e);
                tvResult.setText("Subscribe failed!");
            }
        }
    };

    View.OnClickListener publishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard(PubSubActivity.this);
            if(txtMessage.getText().toString().matches(""))
            {
                toast = Toast.makeText(PubSubActivity.this,"Please input range value",Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if(txtTopic.getText().toString().matches(""))
            {
                toast = Toast.makeText(PubSubActivity.this,"Please input Topic name",Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            final String topic = txtTopic.getText().toString();
            final String msg = txtMessage.getText().toString();

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("range", Integer.parseInt(msg));
                mqttManager.publishString(jsonObject.toString(), topic, AWSIotMqttQos.QOS0);
                tvResult.setText("Publish Success!");
            } catch (Exception e) {
                tvResult.setText("Publish Failed!");
                Log.e(LOG_TAG, "Publish error.", e);
            }

        }
    };

    View.OnClickListener disconnectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard(PubSubActivity.this);
            try {
                btnConnect.setEnabled(true);
                btnSubscribe.setEnabled(false);
                btnPublish.setEnabled(false);
                btnDisconnect.setEnabled(false);
                mqttManager.disconnect();
                tvResult.setText("Disconnect Success!");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Disconnect error.", e);
                tvResult.setText("Disconnect failed!");
            }

        }
    };

    public void goHistory(View view)
    {
        Intent intent = new Intent(this,historyActivity.class);
        asHistory.clear();
        asHistory.addAll(dsHistory);

        intent.putStringArrayListExtra("history",asHistory);
        startActivity(intent);
    }

    public void doHideKeyBoard(View view)
    {
        hideKeyboard(this);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}