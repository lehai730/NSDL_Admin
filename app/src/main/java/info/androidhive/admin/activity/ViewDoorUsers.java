package info.androidhive.admin.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import info.androidhive.admin.R;
import info.androidhive.admin.app.AppConfig;

public class ViewDoorUsers extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> doorUserList;

    // products JSONArray
    JSONArray doorusers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_door_users);

        // Hashmap for ListView
        doorUserList = new ArrayList<HashMap<String, String>>();
        // Loading products in Background Thread
        new LoadAllProducts().execute();
        // Get listview
        ListView lv = getListView();

    }



    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ViewDoorUsers.this);
            pDialog.setMessage("Loading Door Users. Please wait...");
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
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_DOORUSER, "POST", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt("success");

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    doorusers = json.getJSONArray("doorUsers");

                    // looping through All Products
                    for (int i = 0; i < doorusers.length(); i++) {
                        JSONObject c = doorusers.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString("id");
                        String unique_id = c.getString("unique_id");
                        String name = c.getString("name");
                        String email = c.getString("email");
                        String last_login = c.getString("last_login");
                        String access_type = c.getString("access_type");

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put("id", id);
                        map.put("unique_id", unique_id);
                        map.put("name", name);
                        map.put("email", email);
                        map.put("last_login", last_login);
                        map.put("access_type", access_type);

                        // adding HashList to ArrayList
                        doorUserList.add(map);
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            RegisterActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
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
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            ViewDoorUsers.this, doorUserList,
                            R.layout.list_door_users, new String[] { "id",
                            "name","email", "last_login"},
                            new int[] { R.id.pid, R.id.name, R.id.email, R.id.last_tap });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }


}
