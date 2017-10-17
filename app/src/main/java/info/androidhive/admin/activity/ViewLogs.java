package info.androidhive.admin.activity;

import android.content.DialogInterface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import info.androidhive.admin.R;
import info.androidhive.admin.app.AppConfig;
import info.androidhive.admin.app.AppController;

public class ViewLogs extends ListActivity {
    private static final String TAG = ViewLogs.class.getSimpleName();
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> logsList;

    // products JSONArray
    JSONArray Logs = null;

    //Button to clear logs
    private Button btnClearLogs;

    //Swipe refresh
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_logs);

        //declear button
        btnClearLogs = (Button) findViewById(R.id.btnClearlogs);

        //declare swipe refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_logs_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadLogs().execute();
            }
        });

        // Register Button Click event
        btnClearLogs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new AlertDialog.Builder( ViewLogs.this )
                        .setTitle( "This will delete all the Logs" )
                        .setMessage("Are you sure you want to delete ALL the logs")
                        .setPositiveButton( "YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //start background task to delete users
                                clearLog();
                                Log.d( "AlertDialog", "Positive" );
                            }
                        })
                        .setNegativeButton( "NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d( "AlertDialog", "Negative" );
                            }
                        } )
                        .show();


            }
        });
        // Hashmap for ListView
        logsList = new ArrayList<HashMap<String, String>>();
        // Loading products in Background Thread
        new LoadLogs().execute();

    }

    private void clearLog() {
        // Tag used to cancel the request
        String tag_string_req = "req_clearlogs";

        pDialog.setMessage("Clearing Logs ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CLEAR_LOGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Clear Log Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        Toast.makeText(getApplicationContext(), "Logs Successfully Cleared!", Toast.LENGTH_LONG).show();
                        // Launch Add New product Activity
                        Intent i = new Intent(getApplicationContext(),
                                MainActivity.class);
                        // Closing all previous activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);

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
                //dont need parameters
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadLogs extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ViewLogs.this);
            pDialog.setMessage("Loading Logs. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_LOGS, "POST", params);

            // Check your log cat for JSON reponse
            Log.d("All Logs: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    Logs = json.getJSONArray("Logs");
                    logsList.clear();
                    // looping through All Products
                    for (int i = 0; i < Logs.length(); i++) {
                        JSONObject c = Logs.getJSONObject(i);

                        // Storing each json item in variable
                        String ID = c.getString("ID");
                        String Name = c.getString("Name");
                        String Access = c.getString("Access");
                        String TIMESTAMP = c.getString("TIMESTAMP");

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put("ID", ID);
                        map.put("Name", Name);
                        map.put("Access", Access);
                        map.put("TIMESTAMP", TIMESTAMP);

                        // adding HashList to ArrayList
                        logsList.add(map);
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
                    Toast.makeText(getApplicationContext(),
                            "No Logs found, Please register a user or wait until an activity is logged!", Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            //dismiss swipe refresh
            swipeRefreshLayout.setRefreshing(false);
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            ViewLogs.this, logsList,
                            R.layout.list_logs, new String[] { "ID",
                            "Name","Access", "TIMESTAMP"},
                            new int[] { R.id.lid, R.id.name, R.id.access, R.id.time });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

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
