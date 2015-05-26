package com.blinkedup.geolocationchat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import android.app.ListActivity;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


public class PeopleActivity extends ListActivity implements OnRefreshListener {

	TextView textView;
	LocationManager locationManager;
	MyLocationListener locationListener = new MyLocationListener();
	 
	Criteria criteria;
	String bestProvider;
	String listOfBestProviders;
	DateUtils du;
	
	Button btnUpdate;
	boolean gps_enabled = false;
	boolean network_enabled = false;
	float longitude = 0;
	float latitude = 0;
	String gpsProvider = "";
	
	String ins_user = "";
    float ins_latitude = 0;
    float ins_longitude = 0;
    
	private AsyncTask<String, String, String> mTask;
	
	SwipeRefreshLayout mSwipeRefreshLayout;
	
	//private ProgressDialog pDialog;
	 
    JSONParser jsonParser = new JSONParser();
    // url to create new product
    private static String url_create_product = "http://lpoezy.com/happn/create_geo.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> userList;
    JSONArray nearby_users = null;
    // url to get all products list
    private static String url_get_near = "http://www.lpoezy.com/happn/get_nearby_users.php";
 
    // JSON Node names
    private static final String TAG_GEO_SUCCESS = "success";
    
    private static final String TAG_GEO = "geo";
    private static final String TAG_GEO_PID = "id";
    private static final String TAG_GEO_USER = "user";
    private static final String TAG_GEO_LATITITUDE = "latitude";
    private static final String TAG_GEO_LONGI = "longitude";
    private static final String TAG_GEO_PROVIDER = "gps_provider";
    private static final String TAG_GEO_DATE_CREATE = "date_create";
    private static final String TAG_GEO_FNAME = "firstname";
    private static final String TAG_GEO_LNAME = "lastname";
    private static final String TAG_GEO_BIRTHDAY = "birthday";
    private static final String TAG_GEO_GENDER = "gender";
    private static final String TAG_GEO_DISTANCE = "geo_distance";
    
    
    
	private void _getLocation() {
		Log.e("","Started refreshing");
	    // Get the location manager
	    locationManager = (LocationManager) 
	            getSystemService(LOCATION_SERVICE);
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
	  
		Location location;
	    try{
	    	gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    	Log.e("GPS ENABLED", gps_enabled+"");
	    	}
	    catch(Exception ex){
	    	Log.e("ERROR GPS", ex.getLocalizedMessage());
	    }
	    
        try{
        	network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        	//Log.e("NETWORK ENABLED", network_enabled+"");
        	}
        catch(Exception ex){
        	Log.e("ERROR NETWORK GPS",ex.getLocalizedMessage());
        }
      
        String bestProvider = locationManager.getBestProvider(criteria, true);
	    try {
	    	  if(gps_enabled){
	          		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	          		location = locationManager.getLastKnownLocation(bestProvider);
	          		latitude = (float) location.getLatitude();
	          		longitude = (float) location.getLongitude();
	          		Log.e("LOCATION GPS GEO","Latitude: " + location.getLatitude() + "Longitude: " + location.getLongitude());
	          		gpsProvider = "GPS";
	          		//new CreateNewProduct().execute();
	          		mTask = new CreateNewProduct().execute();
	    	  }
	          else if(network_enabled){
	          		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	          		location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	          		latitude = (float) location.getLatitude();
	          		longitude = (float) location.getLongitude();
	          		Log.e("LOCATION NETWORK GEO","Latitude: " + location.getLatitude() + "Longitude: " + location.getLongitude());
	          		gpsProvider = "Network";
	          		new CreateNewProduct().execute();
	          }
	          else {
	          		Log.e("NO DEVICE","No locator is enabled on the device");
	          		gpsProvider = "ERROR";
	          }
	    } catch (NullPointerException e) {
	    	Log.e("NULL POINTER RETRIEVEING LOCATION","error retrieving location");
	    	gpsProvider = "ERROR";
	    }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
		      @Override
		      public void onRefresh() {
		            
		    	  new Handler().postDelayed(new Runnable() {
		              @Override
		              public void run() {
		            	  _getLocation();
		                 
		              }
		          }, 2000);
		      }
		});
		
		du = new DateUtils();
		// super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		 userList = new ArrayList<HashMap<String, String>>();
		
		 textView = (TextView) findViewById(R.id.textView1);
		 btnUpdate = (Button) findViewById(R.id.btn_update);
	  
		// _getLocation();
		 btnUpdate.setOnClickListener( new OnClickListener() {
				@Override
		            public void onClick(View v) {
					// mTask.cancel(true);
					_getLocation();
					}
				});
		 btnUpdate.setOnLongClickListener(new OnLongClickListener() {
			    public boolean onLongClick(View arg0) {
			    	return true; 
			    }
			});
	}

	private class MyLocationListener implements LocationListener{
		@Override
		public void onLocationChanged(Location location) {
			 // TODO Auto-generated method stub
			 textView.setText("Latitude: " + latitude + "Longitude: " + longitude );

			 
			 Log.e("LOC LISTENER","PROVIDER LOC CHANGED");
			 locationManager.removeUpdates(locationListener);
			
		}
		@Override
		public void onProviderDisabled(String provider) {
			 // TODO Auto-generated method stub
			 Log.e("LOC LISTENER","PROVIDER DISABLED");
		}

		@Override
		public void onProviderEnabled(String provider) {
			 // TODO Auto-generated method stub
			Log.e("LOC LISTENER","PROVIDER ENABLED");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			 // TODO Auto-generated method stub
			Log.e("LOC LISTENER","STATUS CHANGED");
		}
	}
	 	
	protected void onPause(){
		 super.onPause();
		 locationManager.removeUpdates(locationListener);
		 Log.e("ON PAUSE","APP PAUSE");
	} 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
			if (id == R.id.action_settings) {
				return true;
			}
			return super.onOptionsItemSelected(item);
	}
	
	/**
     * Background Async Task to Create new product
     * */
    class CreateNewProduct extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // pDialog = new ProgressDialog(NewProductActivity.this);
           // pDialog.setMessage("Creating Product..");
          //  pDialog.setIndeterminate(false);
        //    pDialog.setCancelable(true);
          //  pDialog.show();
        }
 
        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
             ins_user = "1";
             ins_latitude = latitude;
             ins_longitude = longitude;
            String ins_g_provider =  gpsProvider;
            Date dateNow = new Date();
            
            String curDate = du.convertDateToString(dateNow);
            
            // Building Parameters
            List<NameValuePair> paramsIns = new ArrayList<NameValuePair>();
            
            paramsIns.add(new BasicNameValuePair("p_user", ins_user));
            paramsIns.add(new BasicNameValuePair("p_longitude", ins_longitude+""));
            paramsIns.add(new BasicNameValuePair("p_latitude", ins_latitude+""));
            paramsIns.add(new BasicNameValuePair("p_gps_provider", ins_g_provider));
            paramsIns.add(new BasicNameValuePair("p_date_update", curDate));
            
            Log.e("Will Insert to JSON",ins_user+"");
            Log.e("Will Insert to JSON",ins_longitude+"");
            Log.e("Will Insert to JSON",ins_latitude+"");
            Log.e("Will Insert to JSON",ins_g_provider+"");
            Log.e("Will Insert to JSON",curDate+"");
          
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                    "POST", paramsIns);
 
            // check log cat fro response
            Log.e("Create Response", json.toString());
 
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                	  new LoadAllProducts().execute();
                    // successfully created product
                	// Intent i = getIntent();
                     // send result code 100 to notify about product update
                    // setResult(100, i);
                    // finish();
                	//Toast.makeText(getApplicationContext(), "Done locating", 3).show();
                	//  finish();
                	
                	 
                    // Get listview
                    ListView lv = getListView();
                    
                } 
                else {
                	Log.e("","Cannot parse JSON");
                	//Toast.makeText(getApplicationContext(), "Failed to use location services", 5).show();
                }
            } 
            catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage() +"", 5).show();
            }
 
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            //pDialog.dismiss();
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
                //PLEASE WAIT MESSAGE
            }
     
            /**
             * getting All products from url
             * */
            protected String doInBackground(String... args) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pid", ins_user));
                Log.e("dsfsdfsdsdf", ins_longitude +" dfgdrgdg");
                params.add(new BasicNameValuePair("longitude", ins_longitude +""));
                params.add(new BasicNameValuePair("latitude", ins_latitude +""));
              
                JSONObject json = jParser.makeHttpRequest(url_get_near, "GET", params);
     
                // Check your log cat for JSON reponse
                Log.e("All Products: ", json.toString());
     
                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);
     
                    if (success == 1) {
                        // products found
                        // Getting Array of Products
                    	nearby_users = json.getJSONArray(TAG_GEO);
     
                        // looping through All Products
                        for (int i = 0; i < nearby_users.length(); i++) {
                            JSONObject c = nearby_users.getJSONObject(i);
     
                            // Storing each json item in variable
                            String id = c.getString(TAG_GEO_PID);
                            String fname = c.getString(TAG_GEO_FNAME);
                            String lname = c.getString(TAG_GEO_LNAME);
                            Date bday = du.convertStringToDateToLocal(c.getString(TAG_GEO_BIRTHDAY));
                            String age = du.getAge(bday);
                            int distance =Math.round(Float.parseFloat(c.getString(TAG_GEO_DISTANCE)));
                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();
     
                            // adding each child node to HashMap key => value
                            map.put(TAG_GEO_PID, id);
                            map.put(TAG_GEO_FNAME, fname);
                            map.put(TAG_GEO_LNAME, lname);
                            map.put(TAG_GEO_LNAME, lname);
                            map.put(TAG_GEO_BIRTHDAY, age);
                            map.put(TAG_GEO_DISTANCE, distance + "m away");
                            // adding HashList to ArrayList
                            userList.add(map);
                        }
                    } else {
                    	Log.e("Sorry","No nearby users");
                        // no products found
                        // Launch Add New product Activity
                        //Intent i = new Intent(getApplicationContext(),
                            //    NewProductActivity.class);
                        // Closing all previous activities
                       // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       // startActivity(i);
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
                //pDialog.dismiss();
                // updating UI from Background Thread
            	
            	
                runOnUiThread(new Runnable() {
                	
                    public void run() {  
                    	Log.e("","Printing...");
                    	//if (mSwipeRefreshLayout.isRefreshing()){
                    	//	mSwipeRefreshLayout.setRefreshing(false);
                    	//}
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        ListAdapter adapter = new SimpleAdapter(
                                PeopleActivity.this, userList,
                                R.layout.list_user, new String[] { TAG_GEO_PID,
                                		TAG_GEO_FNAME,TAG_GEO_LNAME,TAG_GEO_BIRTHDAY, TAG_GEO_DISTANCE},
                                new int[] { R.id.pid, R.id.fname, R.id.lname, R.id.age, R.id.distance  });
                        // updating listview
                        setListAdapter(adapter);
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (i==1){
                       // mSwipeRefreshLayout.setRefreshing(false);
                        i = 2;
                        }
                        i=1;
                    }
                });
     
            }
     
        }
        /*private void sortAscending () {
            List<String> sortedMonthsList = Arrays.asList(months);
            Collections.sort(sortedMonthsList);

            months = (String[]) sortedMonthsList.toArray();
        }*/
 
    }
    int i = 0;



	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		  
    	  
	}
}
