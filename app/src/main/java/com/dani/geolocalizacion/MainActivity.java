package com.dani.geolocalizacion;
//Aplicación Android que permite visualizar la posición global del dispositivo móvil,
//mostrando los datos de latitud, longitud, altura y precisión de la señal,
//y posteriormente se intentará trasladar dichos datos a Google Maps.
//
//Daniel Álvarez Vaquero.

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*Clase MainActivity, que hereda de la clase base Activity, y que permite mostrar los datos asociados a la localización del dispositivo GPS.*/
/*La interface OnRequestPersimssionResulCAllback será la encargada de solicitar los permisos en tiempo de ejecucion que necesitará la app*/

public class MainActivity extends Activity implements OnRequestPermissionsResultCallback {

    private static final int PERMISO_ACCESS_FINE_LOCATION = 0;

    /*Se declaran las variables que se asociarán a los compontes gráficos de la aplicacion*/
    private TextView tvLatitud, tvLongitud, tvPrecision, tvAltura, tvPorDefecto, direccion, datosProv;
    private Button btnLocalizar, siguienteProveedor, mejorProve, estadoProv, mapa;
    /*Se declara la clase encargada de proporcionar acceso al servicio de localización del sistema.*/
    private LocationManager locManager;
    /*Interfaz encargada de recibir las notificaciones del LocationManager cuando se cambia la localización.*/
    private LocationListener locListener;
    /*Vista del layout donde se mostrarn las notificaciones de los permisos*/
    private View vista;
    /*Variables donde guardaremos la longitud y latitud de la dirección actualizada*/
    private Double longitud;
    private Double latitud;
    private int var = 0;
    private String proveedorBuscado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//Ventana sin título.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); ///Cambio la orientación del dispositivo.
        setContentView(R.layout.activity_main);

        /*Se asignan los componentes definidos en el layout a cada variable definida.*/
        tvLatitud = (TextView) findViewById(R.id.tvLatitud);
        tvLongitud = (TextView) findViewById(R.id.tvLongitud);
        tvPrecision = (TextView) findViewById(R.id.tvPrecision);
        tvAltura = (TextView) findViewById(R.id.tvAltura);
        tvPorDefecto = (TextView) findViewById(R.id.tvPorDefecto);
        vista = findViewById(R.id.main_layout);
        btnLocalizar = (Button) findViewById(R.id.button);
        siguienteProveedor = (Button) findViewById(R.id.btt_proveedores);
        direccion = (TextView) findViewById(R.id.tv_direccion);
        datosProv = (TextView) findViewById(R.id.tv_datosProveedor);
        mejorProve = (Button) findViewById(R.id.btt_mejorProv);
        estadoProv = (Button) findViewById(R.id.btt_estadoProv);
        mapa = (Button) findViewById(R.id.btt_mapa);



        /*Se comprueba la versión del dispositivo en que se lanza la aplicación.Si la versión del dispositivo es mayor o igual a la 6.0 en clave*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Comprobamos si tenemos los permisos de localización exigidos, de no tenerlos se solicitan al usuario
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_ACCESS_FINE_LOCATION);
            } else {
                //En caso de que la version sea mayor o igual a la 6.0 pero si que tengamos listos los permisos, continuamos.
                rastreoGPS();
            }
        } else {
            //Si la version es menor a la 6.0 directamete se continua invocando al metodo rastreoGPS().
            rastreoGPS();
        }
        rastreoGPS();




        /*Evento On Click que se encarga de, enviar los datos de latitud y longitud recogidos en la Activity principal, a la Activity
        que mostrará la posición de manera visual en un mapa.*/
        mapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("longitud",longitud);
                intent.putExtra("latitud", latitud);
                startActivity(intent);
            }
        });


        /*Evento que transformará nuestros datos de longitud y latitud en una dirección física completa*/
        btnLocalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Se declara y crea un objeto de la clase Geocoder, encargada de transformar  los datos de latitud y longitud obtenidos en una dirección física en el mapa
                (y viceversa).*/
                Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    /*Se almacena en una colección de objetos Address, las direcciones dadas a partir de los datos de latitud y longitud proporcionados.
                    Como último parámetro se establece el número máximo de direcciones que serán devueltas. Se recomiendan números pequeños entre 1 y 5.*/

                    Address direccionActual;
                    List<Address> listaDirecciones = geoCoder.getFromLocation(Double.valueOf(tvLatitud.getText().toString()), Double.valueOf(tvLongitud.getText().toString()), 1);
                    if (listaDirecciones.size() > 0) {
                        direccionActual = listaDirecciones.get(0);
                     /*Se asignan los datos de dirección, código postal, ciudad y país.*/
                        MainActivity.this.direccion.setText(direccionActual.getAddressLine(0)
                                + " " + direccionActual.getPostalCode()
                                + " " + direccionActual.getLocality()
                                + ", " + direccionActual.getCountryName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        /*Evento que se encarga de mostrar en el TextView los datos de todos los proveedores que encuentra el dispositivo*/
        siguienteProveedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Obtenemos una lista de todos los proveedores conocidos por el dispositivo*/
                List<String> listaProviders = locManager.getAllProviders();
                /*Iteramos la lista mostrando los datos en cada pasada de un proveedor distinto de los que tenemos.*/
                if(var < listaProviders.size()){
                    LocationProvider proveedor = locManager.getProvider(listaProviders.get(var));
                    datosProv.setText("PROVEEDOR: " + proveedor.getName().toString()
                                    + " \nPRECISION " + proveedor.getAccuracy()
                                    + " \nGASTO BATERIA " + proveedor.getPowerRequirement()
                    );
                }else{
                    datosProv.setText("No existe mas proveedores");
                }
                var++;
            }
        });

        /*Evento que se encarga de mostrar en el TextView los datos que bajo unos criterios consideramos el mejor proveedor*/
        mejorProve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Criteria criterios = new Criteria();
                criterios.setAccuracy(Criteria.ACCURACY_FINE); //Precision alta
                criterios.setAltitudeRequired(false); //Nos proporciona altitud
                criterios.setPowerRequirement(Criteria.POWER_LOW);//Poco gasto de bateria.

                //Mejor proveedor segun los criterios seleccionados, el segundo parametro indica si queremos que nos muestre
                //solo los proveedores que están actualmente activados en nuestro dispositivo, o todos los existentes.
                proveedorBuscado = locManager.getBestProvider(criterios, false);
                //Lista de mejores proveedores según los criterios seleccionados
                //List<String> listProvidersCrit = locManager.getProviders(criterios, false);

                LocationProvider p = locManager.getProvider(proveedorBuscado);
                if(p != null){
                    datosProv.setText("PROVEEDOR: " + p.getName().toString()
                                    + " \nPRECISION " + p.getAccuracy()
                                    + " \nGASTO BATERIA " + p.getPowerRequirement()
                    );
                }else{
                    datosProv.setText("No existe proveedor con las características seleccionadas");
                }
            }
        });

        //Evento que se encarga de mostrarnos el estado del provedor que se ha seleccionadao en funcion de los criterios asignados
        //con el boton mejor proveedor

        estadoProv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locManager.isProviderEnabled(proveedorBuscado)){
                    datosProv.setText("HABILITADO");
                }else{
                    datosProv.setText("DESHABILITADO");
                }
            }
        });
    }

    /*Método encargado de actualizar la posición del dispositivo GPS cuando este cambie de localización.*/
    @TargetApi(Build.VERSION_CODES.M)
    private void rastreoGPS() {
        //Si la versión del dispositivo es mayor o igual a la 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Y si no tiene los permisos necesarios
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Se solicitan los permisos
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_ACCESS_FINE_LOCATION);
            }
        }

        /*Se asigna a la clase LocationManager el servicio a partir del nombre.*/
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        /*Se declara y asigna a la clase Location la última posición conocida proporcionada por el proveedor.*/
        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //Se llama al método que muestra la posición
        mostrarPosicion(loc);
        //Se define la interfaz LocationListener, que deberá implementarse con los siguientes métodos.
        locListener = new LocationListener() {
            //Método que será llamado cuando cambie la localización.
            @Override
            public void onLocationChanged(Location location) {
                mostrarPosicion(location);
            }

            //Método que será llamado cuando se produzcan cambios en el estado del proveedor.
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            //Método que será llamado cuando el proveedor esté habilitado para el usuario.
            @Override
            public void onProviderEnabled(String provider) {
            }

            //Método que será llamado cuando el proveedor esté deshabilitado para el usuario.
            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        /*Por último se llama al método encargado establecer la localización actualizada, recibiendo como parámetros de entrada
         - el nombre del proveedor,
         - el intervalo de tiempo entre cada actualización en milisegundos,
         - distancia en metros entre localizaciones actualizadas,
         - y la variable de tipo LocationListener

        que actualizará la localización en caso de producirse nuevos cambios.*/

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locListener);

    }

    /*Método que recibe como parámetro de entrada una variable de tipo Location, y que permitirá
    mostrar los diferentes datos de la ubicación geográfica del dispositivo. En el supuesto de no
    tener habilitada la opción de ubicación, se establecerán valores por defecto (dichos valores se almacenarán en un
    array de datos de tipo String).*/
    private String[] mostrarPosicion(Location loc) {
        String[] datos;

        ;
        if (loc != null) { //Si la localización que se pasa no es null, porque se ha encontrado.
            tvPorDefecto.setText("(valores GPS)");
            tvLatitud.setText(String.valueOf(loc.getLatitude()));
            tvLongitud.setText(String.valueOf(loc.getLongitude()));
            longitud = loc.getLongitude();
            latitud = loc.getLatitude();
            tvAltura.setText(String.valueOf(loc.getAltitude()));
            tvPrecision.setText(String.valueOf(loc.getAccuracy()));
            datos = new String[]{String.valueOf(loc.getLongitude()), String.valueOf(loc.getLatitude())};
        } else { //Si no se asignan valores por defecto.
            tvPorDefecto.setText("(valores por defecto)");
            datos = new String[]{String.valueOf(40.4167754), String.valueOf(-3.7037901999999576), "Posición por defecto"};
            tvLatitud.setText(String.valueOf(40.4167754));
            tvLongitud.setText(String.valueOf(-3.7037901999999576));
            tvAltura.setText(String.valueOf(15.00));
            tvPrecision.setText(String.valueOf(1.0));
        }
        return datos;
    }

    /**
     * Método que será llamado cuando el usuario acepte el permiso requeridos.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            /*Identificador para realizar la tarea localización*/
            case PERMISO_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permiso aceptado, se actua en consecuencia.
                    rastreoGPS();
                    Snackbar.make(vista, "Permiso establecido", Snackbar.LENGTH_LONG).show();
                } else {
                    //Permiso denegado..
                    Snackbar.make(vista, "Permiso denegado, no se podrá actualizar la posicion del GPS", Snackbar.LENGTH_LONG).show();
                }
                return;
        }
    }

}