package network.xyo.sdk.nodes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.nio.ByteBuffer;
import java.security.KeyPair;

import network.xyo.sdk.data.Entry;

public class Sentinel extends Node {

    public interface Listener extends Node.Listener {
        void locationUpdated(Sentinel sentinel, Location location);
    }

    private FusedLocationProviderClient fusedLocationClient;
    private Listener listener;

    public Sentinel(Context context, String host, short apiPort, short pipePort) {
        super(context, host, apiPort, pipePort);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void setListener(Listener listener) {
        super.setListener(listener);
        this.listener = listener;
    }

    private void initiateSelfSignedWitness(final Location location) {
        this._threadPool.execute(new Runnable() {
            @Override
            public void run() {
                Entry entry = new Entry();
                network.xyo.sdk.data.Location locationData = new network.xyo.sdk.data.Location(location);
                ByteBuffer buffer = locationData.toBuffer();
                entry.payloads.add(buffer.array());
                entry.p2keys = publicKeysFromKeyPairs((KeyPair[])(Sentinel.this._keys.toArray(new KeyPair[Sentinel.this._keys.size()])));
                entry.p1Sign(Sentinel.this);
                entry.p2Sign(Sentinel.this);
                Sentinel.this.addEntryToLedger(entry);
                Sentinel.this.spinKeys();
            }
        });
    }

    public void pollLocation() {

        int permissionCheck = ContextCompat.checkSelfPermission(this.context,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            this.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                if (Sentinel.this.listener != null) {
                                    Sentinel.this.listener.locationUpdated(Sentinel.this, location);
                                }
                            }
                        }
                    });

                this.fusedLocationClient.requestLocationUpdates(
                        new LocationRequest().setInterval(10000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                        new LocationCallback() {
                            public void onLocationResult(LocationResult result) {
                                if (Sentinel.this.listener != null) {
                                    Location location = result.getLastLocation();
                                    Sentinel.this.listener.locationUpdated(Sentinel.this, location);
                                    Sentinel.this.initiateSelfSignedWitness(location);
                                }
                            }

                            public void onLocationAvailability(LocationAvailability availability) {
                            }
                        },
                        null /* Looper */);
        }
    }

    @Override
    public String getName() {
        return "Sentinel";
    }
}