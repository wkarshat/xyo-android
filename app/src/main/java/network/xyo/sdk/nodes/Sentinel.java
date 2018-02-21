package network.xyo.sdk.nodes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class Sentinel extends Node {

    public interface Listener {
        void locationUpdated();
    }

    private FusedLocationProviderClient mFusedLocationClient;
    private Listener mListener;

    public Sentinel(Context context, String host, short apiPort, short pipePort) {
        super(context, host, apiPort, pipePort);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void pollLocation() {

        int permissionCheck = ContextCompat.checkSelfPermission(this.context,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                if (mListener != null) {
                                    mListener.locationUpdated();
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public String getName() {
        return "Sentinel";
    }
}