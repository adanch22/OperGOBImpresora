package com.example.opergobimpresora;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    // ------ variables ---------------->
    private Connection printerConnection;
    private RadioButton btRadioButton;
    private ZebraPrinter printer;
    private TextView statusField;
    private EditText macAddress, ipDNSAddress, portNumber, claveUsuario;
    private Button testButton;
    private FileInputStream fis;

    private Integer TipoTicket = 0;
    public String URL = "", URLBASE="";
    private String claveUsuarioTexto;

    private SharedPreferences myPreferences;
    // ------ variables ---------------->
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity);

        macAddress = (EditText) this.findViewById(R.id.macInput);
        //claveUsuario = (EditText) this.findViewById(R.id.Usuario);
        //macAddress.setText(SettingsHelper.getBluetoothAddress(this));


        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        statusField = (TextView) this.findViewById(R.id.statusText);
        btRadioButton = (RadioButton) this.findViewById(R.id.bluetoothRadio);
        btRadioButton.setChecked(true);
        if (btRadioButton.isChecked() == true) {
            //toggleEditField(macAddress, true);
        } else {
            //toggleEditField(macAddress, false);
        }
        testButton = (Button) this.findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
               if (getMacAddressFieldText().length()== 0 || getClaveUsuarioFieldText().length() == 0){
                   Toast.makeText(getApplicationContext(), "Advertencia. Debes realizar la configuración inicial", Toast.LENGTH_LONG).show();
               }else{
                   new Thread(new Runnable() {
                       public void run() {

                           enableTestButton(false);
                           Looper.prepare();
                           doConnectionTest();
                           Looper.loop();
                           Looper.myLooper().quit();
                       }
                   }).start();
               }



            }
        });

        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.bluetoothRadio) {
                  // toggleEditField(macAddress, true);

                } else {
                  //  toggleEditField(macAddress, false);
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent (this, OptionsActivity.class);
            startActivityForResult(intent, 0);
           //return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void toggleEditField(EditText editText, boolean set) {
        editText.setEnabled(set);
        editText.setFocusable(set);
        editText.setFocusableInTouchMode(set);
    }

    private void enableTestButton(final boolean enabled) {
        runOnUiThread(new Runnable() {
            public void run() {
                testButton.setEnabled(enabled);
            }
        });
    }

    private String getTcpPortNumber() {
        return portNumber.getText().toString();
    }

    private void doConnectionTest() {
        printer = connect();
        if (printer != null) {
            sendTestLabel();
        } else {
            disconnect();
        }
    }

    private boolean isBluetoothSelected() {
        return btRadioButton.isChecked();

    }

    private String getMacAddressFieldText() {
        //return macAddress.getText().toString();
        return  myPreferences.getString("MAC", "");
    }

    private String getClaveUsuarioFieldText() {
        //return claveUsuario.getText().toString();
        return  myPreferences.getString("USUARIO", "");
    }

    private int getTipoAppFieldText() {
        //return claveUsuario.getText().toString();
        return  myPreferences.getInt("TIPOTICKET", 0);
    }

    private String getURLBaseFieldText() {
        //return claveUsuario.getText().toString();
        return  myPreferences.getString("URL", "");
    }

    //Conectar con impresora
    public ZebraPrinter connect() {
        setStatus("Conectando...", Color.RED);
        printerConnection = null;
        if (isBluetoothSelected()) {
            printerConnection = new BluetoothConnection(getMacAddressFieldText());
            SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());
        } else {
           /* try {
                int port = Integer.parseInt(getTcpPortNumber());
                printerConnection = new TcpConnection(getTcpAddress(), port);
                SettingsHelper.saveIp(this, getTcpAddress());
                SettingsHelper.savePort(this, getTcpPortNumber());
            } catch (NumberFormatException e) {
                setStatus("Port Number Is Invalid", Color.RED);
                return null;
            }*/
        }

        try {
            printerConnection.open();
            setStatus("Conectando...", Color.GREEN);
        } catch (ConnectionException e) {
            setStatus("Error! Desconectando...", Color.RED);
            DemoSleeper.sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus("Buscando Lenguaje de Impresora", Color.GREEN);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus("Lenguaje " + pl, Color.BLUE);
            } catch (ConnectionException e) {
                setStatus("Lenguaje de Impresora Desconocido", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus("Lenguaje de Impresora Desconocido", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

    public void disconnect() {
        try {
            setStatus("Desconectando", Color.RED);
            if (printerConnection != null) {
                printerConnection.close();
            }
            setStatus("Estatus: Esperando conexión...", Color.RED);
        } catch (ConnectionException e) {
            setStatus("Error! Desconectando", Color.RED);
        } finally {
            enableTestButton(true);
        }
    }

    private void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                statusField.setBackgroundColor(color);
                statusField.setText(statusMessage);
            }
        });
        DemoSleeper.sleep(1000);
    }


    private void sendTestLabel() {
        try {
            byte[] configLabel = getConfigLabel();//byte[] configLabel = getConfigLabelPrueba();
            printerConnection.write(configLabel);
            setStatus("Enviando Datos...",  Color.BLUE);
            DemoSleeper.sleep(1500);
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                setStatus(friendlyName, Color.MAGENTA);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            setStatus(e.getMessage(), Color.RED);
        } finally {
            disconnect();
        }
    }


    private byte[] getConfigLabel() {
        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;

        if (printerLanguage == PrinterLanguage.ZPL) {
            //String texto = "^XA^FO17,15^ADN, 11, 12^FD VACIO...^FS^XZ";
            String lectura = "";//"^XA^FO15,15^ADN, 10, 10^FD ERROR DE LECTURA(M.INTERNA)...^FS^XZ";

            try {
                //URLConnection conn = new URL("http://138.122.99.182/TenaSD.NetEnvironment/CodeZPL/JLCASTIL/ImprimirCUC.txt").openConnection();
                TipoTicket = getTipoAppFieldText();
                URLBASE = getURLBaseFieldText();
                String UrlBaseString = URLBASE.replace(" ", "");
                if (URLBASE.length() == 0){
                    URL = "http://138.122.99.182/TenaSD.NetEnvironment/CodeZPL/"+ getClaveUsuarioFieldText()+"/ImprimirCUC.txt";
                }else{
                    URL = URLBASE +"/CodeZPL/" + getClaveUsuarioFieldText()+"/ImprimirCUC.txt";
                }
                /*if (TipoTicket == 1){
                    if (URLBASE.length() == 0){
                        URL = "http://138.122.99.182/TenaSD.NetEnvironment/CodeZPL/"+ getClaveUsuarioFieldText()+"/ImprimirCUC.txt";
                        //URL = "http://201.131.20.44/Prueba_Reynosa/TenaSD/CodeZPL/"+ getClaveUsuarioFieldText()+"/ImprimirCUC.txt";
                    }else{
                        URL = URLBASE +"TenaSD/CodeZPL/" + getClaveUsuarioFieldText()+"/ImprimirCUC.txt";
                    }

                }else if(TipoTicket == 2){
                    if (URLBASE.length() == 0){
                        //URL = "http://201.131.20.44/Prueba_Reynosa/MTMovil/CodeZPL/"+ getClaveUsuarioFieldText()+"/ImprimirCUC.txt";
                    }else{
                        URL = URLBASE + "MTMovil/CodeZPL/"+ getClaveUsuarioFieldText() + "/ImprimirCUC.txt";
                    }
                }*/

                URLConnection conn = new URL(URL).openConnection();
                InputStream in = conn.getInputStream();
                lectura = readStream(in);
            } catch (MalformedURLException e) {
                Log.w("", "MALFORMED URL EXCEPTION");
                lectura = "^XA^PON^PW400^MNN^LL200^LH0,0^CF0,20^FO30,30^FR^FD"+"Error de lectura en ticket..." + "^FS^LL30^XZ";
            } catch (IOException e) {
                Log.w(e.getMessage(), e);
                //lectura = "^XA^PON^PW400^MNN^LL%d^LH0,0^FO30,30^FR^FDError de Lectura..^FS^LL30^XZ";
                lectura = "^XA^PON^PW400^MNN^LL200^LH0,0^CF0,20^FO30,30^FR^FD"+"Error de lectura en ticket..."  + "^FS^LL30^XZ";
            }
/*
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = getApplicationContext().openFileInput("ImprimirCUC.txt");
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder= new StringBuilder();
                while ((lectura = bufferedReader.readLine()) != null){
                    stringBuilder.append(lectura).append("\n");
                }
            } catch (FileNotFoundException e) {
                lectura = "^XA^FO15,15^ADN, 10, 10^FD ERROR DE LECTURA(M.INTERNA)...^FS^XZ";
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            configLabel = lectura.getBytes();
            //configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".getBytes();
            /*String tmpHeader =
                    "^XA" +
                    "^PON^PW400^MNN^LL%d^LH0,0" + "\r\n" +
                    "^FO50,50" + "\r\n" + "^A0,N,70,70" + "\r\n" + "^FD Municipio de Reynosa" + "\r\n" +
                    "^FO50,130" + "\r\n" + "^A0,N,35,35" + "\r\n" + "^FD Direccion de Inspeccion y vigilancia^FS" + "\r\n" +
                    "^FO50,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FD Cajero:^FS" + "\r\n" +
                    "^FO225,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FD OPERGOB^FS" + "\r\n" +
                    "^FO50,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDADAN CHAVEZ OLIVERA^FS" + "\r\n" +
                   // "^FO225,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FD%s^FS" + "\r\n" +
                    "^FO50,300" + "\r\n" + "^GB350,5,5,B,0^FS"  + "^XZ";
            configLabel = tmpHeader.getBytes();*/
            //configLabel = "^XA^FO17,15^ADN, 11, 12^FD EOS SOLUCIONES^FS^FO17, 60^ADN, 11, 7^FD Prueba  de impresion ^FS^FO17, 120^ADN, 11, 7^BCN, 80, Y, Y, N^FD *1-3-43* ^FS ^FO17, 250^ADN, 11, 7^FD Desarrollador: adan chavez  ^FS ^FO17, 300^ADN, 11, 7^FD Sistema OperGOB ^FS ^FO17, 400^ADN, 11, 7^FD Fin de prueba de impresion^FS ^XZ ".getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }
 // Funcion para leer
    public String readStream(InputStream in) throws IOException {
        BufferedReader r = null;
        r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        if (r != null) {
            r.close();
        }
        in.close();
        return total.toString();
    }

}
