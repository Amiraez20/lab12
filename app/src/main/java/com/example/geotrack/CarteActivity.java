package com.example.geotrack;

import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class CarteActivity extends AppCompatActivity {
    private MapView vueCarte;
    private RequestQueue fileAttenteReseau;
    private final String urlListe =
            "http://192.168.100.219/geotrack/listerCoordonnees.php";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Initialisation OSMDroid (obligatoire avant setContentView)
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        setContentView(R.layout.activity_carte);

        vueCarte = findViewById(R.id.vueCarte);
        vueCarte.setTileSource(TileSourceFactory.MAPNIK);
        vueCarte.setMultiTouchControls(true);
        vueCarte.getController().setZoom(15.0);

        fileAttenteReseau = Volley.newRequestQueue(getApplicationContext());
        chargerMarqueurs();
    }

    private void chargerMarqueurs(){
        JsonObjectRequest requete = new JsonObjectRequest(
                Request.Method.POST, urlListe, null,
                reponse -> {
                    try {
                        JSONArray tableau = reponse.getJSONArray("positions");
                        for(int i=0; i<tableau.length(); i++){
                            JSONObject elem   = tableau.getJSONObject(i);
                            double latMq = elem.getDouble("latitude");
                            double lngMq = elem.getDouble("longitude");

                            Marker marqueur = new Marker(vueCarte);
                            marqueur.setPosition(new GeoPoint(latMq, lngMq));
                            marqueur.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marqueur.setTitle("Point #"+(i+1));
                            vueCarte.getOverlays().add(marqueur);
                        }
                        // Centrer sur le dernier point si la liste n'est pas vide
                        if(tableau.length() > 0){
                            JSONObject dernier = tableau.getJSONObject(tableau.length()-1);
                            vueCarte.getController().setCenter(
                                    new GeoPoint(
                                            dernier.getDouble("latitude"),
                                            dernier.getDouble("longitude")
                                    )
                            );
                        }
                        vueCarte.invalidate();
                    } catch(Exception ex){ ex.printStackTrace(); }
                },
                erreur -> {}
        );
        fileAttenteReseau.add(requete);
    }

    @Override protected void onResume(){
        super.onResume(); vueCarte.onResume();
    }
    @Override protected void onPause(){
        super.onPause(); vueCarte.onPause();
    }
}
