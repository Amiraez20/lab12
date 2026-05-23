package com.example.geotrack;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_PERMISSION_GPS = 200;

    private TextView champLatitude, champLongitude;
    private RequestQueue fileAttente;
    private LocationManager gestionnaireGps;

    private static final String URL_ENREGISTREMENT =
            "http://192.168.100.219/geotrack/enregistrerCoordonnee.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        champLatitude  = findViewById(R.id.tvLat);
        champLongitude = findViewById(R.id.tvLon);
        Button boutonCarte = findViewById(R.id.btnCarte);

        fileAttente     = Volley.newRequestQueue(getApplicationContext());
        gestionnaireGps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boutonCarte.setOnClickListener(v ->
                startActivity(new Intent(this, CarteActivity.class)));

        verifierPermission();
    }

    private void verifierPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODE_PERMISSION_GPS);
        } else {
            demarrerSuiviGps();
        }
    }

    @SuppressLint("MissingPermission")
    private void demarrerSuiviGps() {
        gestionnaireGps.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000,
                150,
                new LocationListener() {

                    @Override
                    public void onLocationChanged(Location loc) {
                        double latAct  = loc.getLatitude();
                        double lngAct  = loc.getLongitude();
                        double altAct  = loc.getAltitude();
                        float  precAct = loc.getAccuracy();

                        champLatitude.setText("Latitude : " + latAct);
                        champLongitude.setText("Longitude : " + lngAct);

                        sauvegarderCoordonnee(latAct, lngAct);

                        // %f pour les doubles/float (pas %s)
                        String msg = String.format(
                                getString(R.string.nouvelle_localisation),
                                latAct, lngAct, altAct, precAct);
                        Toast.makeText(getApplicationContext(),
                                msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {
                        Toast.makeText(getApplicationContext(),
                                String.format(getString(R.string.fournisseur_actif), provider),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Toast.makeText(getApplicationContext(),
                                String.format(getString(R.string.fournisseur_inactif), provider),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sauvegarderCoordonnee(final double lat, final double lng) {
        StringRequest requetePost = new StringRequest(
                Request.Method.POST,
                URL_ENREGISTREMENT,
                reponse -> { /* réponse reçue */ },
                erreur  -> Toast.makeText(getApplicationContext(),
                        "Erreur réseau : " + erreur.toString(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> parametres = new HashMap<>();
                SimpleDateFormat formatDate =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                parametres.put("latitude",  String.valueOf(lat));
                parametres.put("longitude", String.valueOf(lng));
                parametres.put("date",      formatDate.format(new Date()));
                parametres.put("imei",      obtenirIdentifiantAppareil());

                return parametres;
            }
        };

        fileAttente.add(requetePost);
    }

    // ANDROID_ID uniquement — pas de TelephonyManager (évite READ_PRIVILEGED_PHONE_STATE)
    private String obtenirIdentifiantAppareil() {
        String androidId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.trim().isEmpty()) {
            return androidId;
        }
        return "APPAREIL_INCONNU";
    }

    @Override
    public void onRequestPermissionsResult(int codeRequete,
                                           String[] permissions,
                                           int[] resultats) {
        super.onRequestPermissionsResult(codeRequete, permissions, resultats);
        if (codeRequete == CODE_PERMISSION_GPS
                && resultats.length > 0
                && resultats[0] == PackageManager.PERMISSION_GRANTED) {
            demarrerSuiviGps();
        } else {
            Toast.makeText(this,
                    "Permission GPS refusée", Toast.LENGTH_LONG).show();
        }
    }
}