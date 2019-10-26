package com.example.botonemergencia7mo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ActivityContactos extends AppCompatActivity {
    EditText mNombre, mNumero;
    Button btnGuardar;
    Button btn_enviar;
    ListView lvDatos;
    AsyncHttpClient cliente;
    static  final int PICK_CONTACT_REQUEST=1;
    String url ="https://thermal-profile.000webhostapp.com/obtenerDatos.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);
        mNombre = findViewById(R.id.nombreId);
        mNumero = findViewById(R.id.numeroId);
        btnGuardar = findViewById(R.id.btnEnviarGuardar);
     //   btn_enviar = findViewById(R.id.btn_enviar);
        lvDatos = findViewById(R.id.lvDatos);
        cliente = new AsyncHttpClient();

        botonGuardar();
        obtenerClientes();
    }
    private  void botonGuardar(){
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNombre.getText().toString().isEmpty()|| mNumero.getText().toString().isEmpty()){
                    Toast.makeText(ActivityContactos.this, "Hay campos vacios", Toast.LENGTH_SHORT).show();
                }else {
                    Contactos c = new Contactos();
                    c.setNombre(mNombre.getText().toString().replaceAll(" ","%20"));
                    c.setTelefono(mNumero.getText().toString());
                    agregarContacto(c);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    obtenerClientes();
                }
            }
        });

    }

    private void agregarContacto(Contactos c){
        String url = "https://karlafragoso.000webhostapp.com/Insertar.php?";
        String parametros = "nombre="+c.getNombre()+"&telefono="+c.getTelefono();
        cliente.post(url + parametros, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                if(statusCode == 200) {
                    Toast.makeText(ActivityContactos.this, "Contacto Agregado", Toast.LENGTH_SHORT).show();
                    mNombre.setText("");
                    mNumero.setText("");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

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

              /**  String messageToSend = "Hola Este es un ms de auxilio";
               SmsManager sms = SmsManager.getDefault();
               c.getTelefono();
               sms.sendTextMessage(c.getTelefono(), null, "https://www.google.com.pe/maps?q=loc:" + "\n" + "Ayuda!!, estoy en: " + "\n" + messageToSend, null, null);

*/
            }
            ArrayAdapter<Contactos> a = new ArrayAdapter(this,android.R.layout.simple_list_item_1,lista);
            lvDatos.setAdapter(a);

            lvDatos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Contactos c = lista.get(position);
                    String url = "https://karlafragoso.000webhostapp.com/eliminar.php?id_contacto="+c.getId_contacto();
                    cliente.post(url, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if(statusCode == 200){
                                Toast.makeText(ActivityContactos.this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                obtenerClientes();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });

                    return true;
                }
            });

            lvDatos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Contactos c = lista.get(position);
                    StringBuffer b = new StringBuffer();
                  //  b.append("ID: " + c.getId_contacto() + "\n");
                    b.append("nombre: " + c.getNombre() + "\n");
                    b.append("numero: " + c.getTelefono());
                    AlertDialog.Builder a = new AlertDialog.Builder(ActivityContactos.this);
                    a.setCancelable(true);
                    a.setTitle("Detalle");
                    a.setMessage(b.toString());
                    a.show();
                }
            });


        }catch (Exception e1){
            e1.printStackTrace();
        }

    }


}
