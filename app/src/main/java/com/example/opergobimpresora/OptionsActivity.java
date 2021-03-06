package com.example.opergobimpresora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

public class OptionsActivity extends AppCompatActivity {

    private Button testButton;
    public EditText inMac, inUsuario, inURL;
    //private RadioButton ComercioRadio, MultaRadio;
    public String StringinMac, StringinUsuario, StringinURL;
    //private int TipoTicket=0;
    public RadioButton btRadioButton;
    public  RadioButton btRadioButton2;
    public Switch inSwich;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        //Instancia de Objeto de Preferencias

        testButton =  (Button) findViewById(R.id.InGuardar);
        inMac = (EditText) findViewById(R.id.InMac);
        inURL = (EditText) findViewById(R.id.InURL);
        inUsuario = (EditText) findViewById(R.id.InUsuario);
        //ComercioRadio = (RadioButton) findViewById(R.id.inComercioRadio);
        //MultaRadio = (RadioButton) findViewById(R.id.inTransitoRadio);

        // --------Swicth ----------->
        inSwich = (Switch) findViewById(R.id.switch1);
        inSwich.setChecked(false);
        if(inSwich.isChecked()) {
            inURL.setEnabled(true);
            inUsuario.setEnabled(true);
            inMac.setEnabled(true);
        }else{
            inURL.setEnabled(false);
            inUsuario.setEnabled(false);
            inMac.setEnabled(false);
        }
        // --------Swicth ----------->
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor myEditor = myPreferences.edit();

        if (inURL.getText().toString().length() == 0){
            inURL.setText("http://201.131.20.44/Prueba_Reynosa/");
            //myEditor.putString("URL","http://201.131.20.44/Prueba_Reynosa/" );
            //myEditor.commit();
            //Toast.makeText(getApplicationContext(),"Debes ingresar dirección MAC",Toast.LENGTH_LONG).show();
        }


        StringinMac = myPreferences.getString("MAC","");
        StringinUsuario = myPreferences.getString("USUARIO", "");
        //TipoTicket = 0;
       // TipoTicket = myPreferences.getInt("TIPOTICKET", 0);
        StringinURL = myPreferences.getString("URL", "");
        inMac.setText(StringinMac);
        inUsuario.setText(StringinUsuario);
        inURL.setText(StringinURL);
/*      if(TipoTicket == 1){
            ComercioRadio.setChecked(true);
            MultaRadio.setChecked(false);
        }else if(TipoTicket == 2){
            ComercioRadio.setChecked(false);
            MultaRadio.setChecked(true);
        }*/

        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StringinMac = inMac.getText().toString();
                StringinUsuario = inUsuario.getText().toString();
                StringinURL = inURL.getText().toString();
                if (inMac.getText().toString().length() == 0){
                    Toast.makeText(getApplicationContext(),"Debes ingresar dirección MAC",Toast.LENGTH_LONG).show();
                }else if(inUsuario.getText().toString().length() ==0) {
                    Toast.makeText(getApplicationContext(),"Debes ingresar clave de usuario",Toast.LENGTH_LONG).show();

                }else{
                    SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor myEditor = myPreferences.edit();
                    myEditor.putString("MAC",StringinMac );
                    myEditor.putString("USUARIO", StringinUsuario);
                    //myEditor.putInt("TIPOTICKET", TipoTicket); //Se elimino el tipo de Ticket para introducir la URLBase como en la APP OperGOB
                    myEditor.putString("URL", StringinURL);
                    myEditor.commit();

                    Toast.makeText(getApplicationContext(),"La configuracion se guardó correctamente",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent (getApplicationContext(), MainActivity.class);
                    startActivityForResult(intent, 0);
                }
                /*
                    }else if(TipoTicket < 1) {
                    Toast.makeText(getApplicationContext(),"Debes seleccionar tipo de aplicación",Toast.LENGTH_LONG).show();
                */

            }
        });


       /* RadioGroup radioGroupTipoApp = (RadioGroup) this.findViewById(R.id.inradioGroupTipoApp);
        radioGroupTipoApp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroupTipoApp, int checkedId) {
                if (checkedId == R.id.inComercioRadio) {
                    TipoTicket = 1;

                } else {
                    TipoTicket = 2;

                }
            }
        });*/
    }
    // -------- evento Button regresar --------------------->
    public void onBackPressed(){
        Toast.makeText(getApplicationContext(),"Debes guardar la configuración",Toast.LENGTH_LONG).show();
        // do something here and don't write super.onBackPressed()

    }
    // -------- evento Button regresar --------------------->
    public void Onclick(View view) {
        if (view.getId()== R.id.switch1){
            if(inSwich.isChecked()) {
                inMac.setEnabled(true);
                inURL.setEnabled(true);
                inUsuario.setEnabled(true);
            }else{
                inURL.setEnabled(false);
                inUsuario.setEnabled(false);
                inMac.setEnabled(false);
            }
        }
    }


}
