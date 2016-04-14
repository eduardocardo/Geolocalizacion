package com.dani.geolocalizacion;

//Aplicación Android que permite visualizar la posición global del dispositivo móvil,
//mostrando los datos de latitud, longitud, altura y precisión de la señal,
//y posteriormente trasladar dichos datos a Google Maps.
//
//Esta es la clase que se inicializara al entrar en la aplicacion a modo de portada, simulando la carga
//de datos.

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import java.util.Timer;
import java.util.TimerTask;


public class SplashScreen extends Activity {

    /*Variable que inicializa el tiempo de retraso que simulará la carga de la aplicación.*/
    private static final long SPLASH_SCREEN_DELAY = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_splash_screen);

        /*Se declara e inicializa la clase TimerTask, que permitirá lanzar una nueva Activity
        al finalizar el tiempo de espera, además de cerrar la Activity actual. */
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        /*Se declara e inicializa la clase Timer, que posibilita la programación de la tarea a lanzar.
        El objeto creado, invocará al método schedule(), que recibirá entre sus parámetros la tarea
        a realizar y el tiempo de espera hasta la ejecución de dicha tarea.*/
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}
