package com.example.botonemergencia7mo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    Button btnEnviarGuardar;

    TextView mensaje1;
    TextView mensaje2;
    AsyncHttpClient cliente;
    static  final int PICK_CONTACT_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEnviarGuardar = findViewById(R.id.btnEnviarGuardar);
        cliente = new AsyncHttpClient();
        obtenerClientes();
        new Contactos();

        Button contactos = findViewById(R.id.contactos);
        mensaje1 = (TextView) findViewById(R.id.mensaje_id);
        mensaje2 = (TextView) findViewById(R.id.mensaje_id2);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }

      contactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contac = new Intent(MainActivity.this, ActivityContactos.class);
               MainActivity.this.startActivity(contac);

            }
        });

        //codigo del boton de envio del mensaje

        btnEnviarGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              obtenerClientes();
            }
        });

    }  //Aqui termina el OnCreate

    private void obtenerClientes(){
        String url ="https://karlafragoso.000webhostapp.com/obtenerDatos.php";
        cliente.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    listarClientes(new String (responseBody));

                }


            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


    private void listarClientes(String respuesta){
        final ArrayList<Contactos> lista = new ArrayList<Contactos>();
        try {
            JSONArray jsonArreglo = new JSONArray(respuesta);
            Log.e("arreglo", String.valueOf(jsonArreglo));
            for(int i=0; i<jsonArreglo.length();i++) {
                Contactos c = new Contactos();
                c.setId_contacto(jsonArreglo.getJSONObject(i).getInt("id_contacto"));
                c.setNombre(jsonArreglo.getJSONObject(i).getString("nombre"));
                c.setTelefono(jsonArreglo.getJSONObject(i).getString("telefono"));
                lista.add(c);

                String messageToSend = "Hola Este es un ms de auxilio";
                android.telephony.gsm.SmsManager sms = SmsManager.getDefault();
                c.getTelefono();
                sms.sendTextMessage(c.getTelefono(), null, "https://www.google.com.pe/maps?q=loc:" + "\n" + "Ayuda!!, estoy en: " + "\n" + messageToSend, null, null);

                Log.e("envio mensaje", String.valueOf(btnEnviarGuardar));
            }
        }catch (Exception e1){
            e1.printStackTrace();
        }

    }






    //Apartir de aqui empezamos a obtener la direciones y coordenadas
    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
        mensaje1.setText("Localizacion agregada");
        mensaje2.setText("");
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    mensaje2.setText(DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public MainActivity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.getLatitude();
            loc.getLongitude();
            String Text = "Lat = " + loc.getLatitude() + "\n Long = " + loc.getLongitude();
            mensaje1.setText(Text);
            this.mainActivity.setLocation(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            mensaje1.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            mensaje1.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }




}