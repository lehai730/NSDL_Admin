package info.androidhive.admin.activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import info.androidhive.admin.R;
import info.androidhive.admin.app.AppConfig;

import static info.androidhive.admin.activity.RegisterActivity.isValidEmail;

public class EditUsers extends AppCompatActivity {

    EditText txtName;
    EditText txtEmail;
    Button btnUpdate;
    Button btnDelete;
    String id , name, email;

    //Progress Dialog
    private ProgressDialog pDialog;

    //JSON parser class
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_users);

        //buttons declare
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        //edit text declare
        txtName = (EditText) findViewById(R.id.update_name);
        txtEmail = (EditText) findViewById(R.id.update_email);

        //getting user details from intent
        Intent i = getIntent();
        //getting extra info from intent
        id = i.getStringExtra("id");
        name = i.getStringExtra("name");
        email = i.getStringExtra("email");

        //set these string up
        txtName.setText(name);
        txtEmail.setText(email);

        //update button click event
        btnUpdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                if(isValidEmail(txtEmail.getText().toString())){
                    //start background task to update users
                    new updateUserDetails().execute(txtName.getText().toString(),txtEmail.getText().toString());
                }else{
                    Toast.makeText(getApplicationContext(), "Please enter a valid email", Toast.LENGTH_LONG).show();
                }

            }
        });

        //delete button click event
        btnDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                new AlertDialog.Builder( EditUsers.this )
                        .setTitle( "Are you sure you want to delete this user?" )
                        .setPositiveButton( "YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //start background task to delete users
                                new deleteUser().execute();
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

    }

    /**
     * Background Async Task to  update User details
     * */
    class updateUserDetails extends AsyncTask<String, String, String> {


        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditUsers.this);
            pDialog.setMessage("Saving product ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        /**
         * Saving Users
         * */
        protected String doInBackground(String... args) {
            String new_name, new_email;
            new_name = args[0];
            new_email = args[1];


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", id));
            params.add(new BasicNameValuePair("name", new_name));
            params.add(new BasicNameValuePair("email", new_email));


            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(AppConfig.URL_UPDATE_USERS,
                    "POST", params);

            // check json success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to update user details", Toast.LENGTH_LONG).show();
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
            // dismiss the dialog once product updated
            pDialog.dismiss();
        }
    }

    /*****************************************************************
     * Background Async Task to Delete Users
     * */
    class deleteUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditUsers.this);
            pDialog.setMessage("Deleting Product...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Deleting product
         * */
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id", id));

                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        AppConfig.URL_DELETE_USERS, "POST", params);

                // check your log for json response
                Log.d("Delete User", json.toString());

                // json success tag
                success = json.getInt("success");
                if (success == 1) {
                    // product successfully deleted
                    // notify previous activity by sending code 100
                    Intent i = getIntent();
                    // send result code 100 to notify about product deletion
                    setResult(100, i);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Failed to delete user", Toast.LENGTH_LONG).show();
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
            // dismiss the dialog once product deleted
            pDialog.dismiss();

        }

    }

}
