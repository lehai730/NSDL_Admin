package info.androidhive.admin.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.androidhive.admin.R;
import info.androidhive.admin.app.AppConfig;
import info.androidhive.admin.app.AppController;
import info.androidhive.admin.helper.SQLiteHandler;
import info.androidhive.admin.helper.SessionManager;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private TextView txtName;
	private TextView txtEmail;
	private Button btnLogout;
	private Button btnLinkToRegister;
	private Button btnUnlockDoor;
	private ProgressDialog pDialog;

	public static SQLiteHandler db;
	private SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		txtName = (TextView) findViewById(R.id.name);
		txtEmail = (TextView) findViewById(R.id.email);
		btnLogout = (Button) findViewById(R.id.btnLogout);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		btnUnlockDoor = (Button) findViewById(R.id.btnUnlockDoor);
		// Link to Register Screen
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(MainActivity.this,
						RegisterActivity.class);
				startActivity(i);
				finish();
			}
		});

		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		// Register Button Click event
		btnUnlockDoor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String doorID = "1";

				if (!doorID.isEmpty()) {
					unlockDoor(doorID);
				} else {
					Toast.makeText(getApplicationContext(),
							"Please set door ID!", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		// SqLite database handler
		db = new SQLiteHandler(getApplicationContext());

		// session manager
		session = new SessionManager(getApplicationContext());

		if (!session.isLoggedIn()) {
			logoutUser();
		}

		// Fetching user details from SQLite
		HashMap<String, String> user = db.getAdminDetails();

		String firstname = user.get("first_name");
		String lastname = user.get("last_name");


		// Displaying the user details on the screen
		txtName.setText(firstname);
		txtEmail.setText(lastname);

		// Logout button click event
		btnLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				logoutUser();
			}
		});
	}

	/**
	 * Function to store user in MySQL database will post params(tag, name,
	 * email, password) to register url
	 * */
	private void unlockDoor(final String doorID) {
		// Tag used to cancel the request
		String tag_string_req = "req_unlock";

		pDialog.setMessage("Unlocking ...");
		showDialog();

		StringRequest strReq = new StringRequest(Request.Method.POST,
				AppConfig.URL_UNLOCK, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Unlock Response: " + response.toString());
				hideDialog();

				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean("error");
					if (!error) {
						// User successfully stored in MySQL
						// Now store the user in sqlite
						String uid = jObj.getString("uid");

						JSONObject user = jObj.getJSONObject("user");
						int lock = user.getInt("unlockBit");
						if (lock == 1){
							Toast.makeText(getApplicationContext(), "Door successfully unlocked!", Toast.LENGTH_LONG).show();
						}

					} else {

						// Error occurred in registration. Get the error
						// message
						String errorMsg = jObj.getString("error_msg");
						Toast.makeText(getApplicationContext(),
								errorMsg, Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Registration Error: " + error.getMessage());
				Toast.makeText(getApplicationContext(),
						error.getMessage(), Toast.LENGTH_LONG).show();
				hideDialog();
			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("unlockId", doorID);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	private void logoutUser() {
		session.setLogin(false);

		db.deleteUsers();

		// Launching the login activity
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
}
