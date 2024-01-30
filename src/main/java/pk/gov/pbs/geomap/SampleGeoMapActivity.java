package pk.gov.pbs.geomap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.IBinder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.Style;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

import egolabsapps.basicodemine.offlinemap.Interfaces.GeoPointListener;
import pk.gov.pbs.geomap.utils.CustomMapListener;
import pk.gov.pbs.geomap.utils.CustomMapUtils;
import pk.gov.pbs.geomap.views.CustomOfflineMapView;
import pk.gov.pbs.utils.Constants;
import pk.gov.pbs.utils.location.ILocationChangeCallback;
import pk.gov.pbs.utils.location.LocationService;

public class SampleGeoMapActivity extends AppCompatActivity implements CustomMapListener, GeoPointListener {
    private static final String TAG = "MainActivity";
    private boolean mAutoLocation = true;
    CustomOfflineMapView offlineMapView;
    IMapController mapController;
    CustomMapUtils mapUtils;
    ArrayList<KmlDocument> mBoundaries;
    ArrayList<GeoPoint> locations;

    private Runnable mAfterLocationServiceStartCallback;
    protected LocationService mLocationService = null;
    protected ServiceConnection mLocationServiceConnection = null;
    ILocationChangeCallback callback;

    private final BroadcastReceiver GPS_PROVIDER_ACCESS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(LocationService.BROADCAST_RECEIVER_ACTION_LOCATION_CHANGED)) {
                Log.d(TAG, "onReceive: Broadcast Received for change of location");
                Location location = intent.getParcelableExtra(LocationService.BROADCAST_EXTRA_LOCATION_DATA);
                if (!isFinishing() && !isDestroyed())
                    SampleGeoMapActivity.this.callback.onLocationChange(location);
            }else
                showToast("Provider disabled");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(pk.gov.pbs.geomap.R.layout.activity_sample_geo_map);

        offlineMapView = findViewById(R.id.map);
        offlineMapView.init(this, this);

        locations = new ArrayList<>();
        locations.add(new GeoPoint(33.679398679965736, 73.03464969598426));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.BROADCAST_RECEIVER_ACTION_LOCATION_CHANGED);
        intentFilter.addAction(LocationService.BROADCAST_RECEIVER_ACTION_PROVIDER_DISABLED);
        registerReceiver(GPS_PROVIDER_ACCESS, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationService();
    }

    @Override
    public void mapLoadSuccess(MapView mapView, CustomMapUtils mapUtils) {
        startLocationService();
        this.mapUtils = mapUtils;
        mapController = mapView.getController();
        offlineMapView.setInitialPositionAndZoom(new GeoPoint(33.679398679965736, 73.03464969598426), 9);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        Marker marker = new Marker(mapView);
        marker.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_location_pin));
        
        /*marker.setDraggable(true);
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(MainActivity.this,"Drag end at " + marker.getPosition().toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(MainActivity.this,"Drag start from " + marker.getPosition().toString(), Toast.LENGTH_LONG).show();
            }
        });*/

//        Marker marker = new Marker(mapView);
//        marker.setPosition(new GeoPoint(33.679398679965736, 73.03464969598426));
//        //marker.setIcon(getResources().getDrawable(R.drawable.galata_tower));
//
//        // marker.setImage(drawable);
//
//        marker.setTitle("Hello PBS");
//        marker.showInfoWindow();
//        mapView.getOverlays().add(marker);
//        marker.setOnMarkerClickListener((marker1, mapView1) -> {
//            offlineMapView.setAnimatedLocationPicker(true, this, MainActivity.this.mapUtils);
//            return false;
//        });

        //Attempt to add accuracy circle on map
        Polygon circle = new Polygon(mapView);
        //circle.setFillColor(0x901010AA);
        //Color.parseColor("#ARGB");
        int fillColor = Color.parseColor("#663322DD");
        circle.setFillColor(fillColor);
        circle.setStrokeColor(Color.BLACK);
        circle.setStrokeWidth(3);

        //Adding GeoJSON layer on map
        //--------------------------------------------------------
        String json = "{\"type\": \"FeatureCollection\",\"features\": [{\"type\": \"Feature\",\"properties\": {\"id\": 1,\"desc\": \"Sector F11\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[72.97445297241211,33.68578204940791],[72.98466682434082,33.6719256448053],[73.00080299377442,33.680711150104247],[72.99041748046875,33.69420907950361],[72.97445297241211,33.68578204940791]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 2,\"desc\": \"Sector F10\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[72.99196243286133,33.69485178517179],[73.00200462341309,33.681425379371479],[73.0177116394043,33.68942434176882],[73.01788330078125,33.6904955748537],[73.00792694091797,33.70334933033437],[72.99196243286133,33.69485178517179]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 3,\"desc\": \"Fatima Jinah Park (F9)\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.00809860229492,33.70342073471579],[73.01788330078125,33.690424159730117],[73.01942825317383,33.6904955748537],[73.03607940673828,33.69949340559815],[73.026123046875,33.712774195477468],[73.00809860229492,33.70342073471579]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 5,\"desc\": \"Sector F8\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.02689552307129,33.71313117761419],[73.03633689880371,33.69949340559815],[73.05376052856446,33.70863309423801],[73.04380416870116,33.72205524868729],[73.02689552307129,33.71313117761419]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 6,\"desc\": \"Sector F7\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.04431915283203,33.72248358076708],[73.05384635925293,33.708918693837869],[73.07144165039061,33.717985987331157],[73.06131362915039,33.73126391736321],[73.04431915283203,33.72248358076708]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 7,\"desc\": \"Sector F6\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.06217193603516,33.731763584299638],[73.07195663452149,33.718414339715177],[73.08826446533203,33.72698093855922],[73.08354377746582,33.7331911880856],[73.08320045471192,33.734476011184899],[73.08242797851563,33.73554668240412],[73.0821704864502,33.736760093633908],[73.08225631713867,33.7374738569288],[73.08302879333496,33.738687240901487],[73.08345794677735,33.73997198169568],[73.08345794677735,33.74097121123314],[73.08277130126953,33.74239866180931],[73.06217193603516,33.731763584299638]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 10,\"desc\": \"Sector G11\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[72.98569679260254,33.6711399054834],[72.99505233764649,33.65806700735442],[73.01256179809569,33.667211101197548],[73.00294876098633,33.679996914903529],[72.98569679260254,33.6711399054834]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 11,\"desc\": \"Sector G10\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.00312042236328,33.68028260969583],[73.01273345947266,33.667353969952149],[73.02878379821778,33.67585423373371],[73.0191707611084,33.68878159550887],[73.00312042236328,33.68028260969583]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 12,\"desc\": \"Sector G9\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.0195140838623,33.68885301199737],[73.02886962890625,33.67578280644601],[73.04637908935547,33.684853597257838],[73.04637908935547,33.68578204940791],[73.03719520568848,33.698136650178508],[73.0195140838623,33.68885301199737]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 13,\"desc\": \"Sector G8\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.0374526977539,33.69842228467894],[73.04715156555176,33.68521069542541],[73.06139945983887,33.69228093365666],[73.06268692016602,33.696779873333479],[73.0547046661377,33.70756208727991],[73.0374526977539,33.69842228467894]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 14,\"desc\": \"Sector G7\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.05487632751465,33.70784769044128],[73.06397438049317,33.69578012931697],[73.06577682495117,33.69628000277926],[73.06654930114746,33.69628000277926],[73.06740760803223,33.6959943611569],[73.08234214782715,33.70342073471579],[73.07204246520996,33.71698649012371],[73.05487632751465,33.70784769044128]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 15,\"desc\": \"Sector G6\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.07255744934082,33.71712927615144],[73.0825138092041,33.703492139037859],[73.09916496276856,33.7124172118567],[73.08929443359375,33.72576738903661],[73.07255744934082,33.71712927615144]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 18,\"desc\": \"Shakar Pariyan\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.06577682495117,33.69435190340085],[73.08740615844727,33.66435367627463],[73.09289932250977,33.67149706061054],[73.09616088867188,33.67835415142453],[73.10457229614258,33.68221102471215],[73.10989379882811,33.68878159550887],[73.11040878295899,33.69835087614285],[73.10199737548827,33.71320257386344],[73.06577682495117,33.69435190340085]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 20,\"desc\": \"Sector H10\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.01273345947266,33.66406792856204],[73.02337646484375,33.65049381900442],[73.04054260253906,33.65992448007282],[73.03041458129883,33.674497105121798],[73.01273345947266,33.66406792856204]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 21,\"desc\": \"Sector H9\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.0312728881836,33.67463996177377],[73.04174423217774,33.65992448007282],[73.05976867675781,33.66878264444685],[73.048095703125,33.68321092658007],[73.0312728881836,33.67463996177377]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 22,\"desc\": \"Sector H8\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.04912567138672,33.68321092658007],[73.06062698364258,33.66978270247389],[73.0755615234375,33.67792559926798],[73.06577682495117,33.69420907950361],[73.06217193603516,33.69235234723729],[73.06217193603516,33.689495757723218],[73.04912567138672,33.68321092658007]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 30,\"desc\": \"Sector I10\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.02389144897461,33.65020802526355],[73.03573608398438,33.63720340625724],[73.05204391479492,33.645921005279948],[73.04191589355469,33.65906718995458],[73.02389144897461,33.65020802526355]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 31,\"desc\": \"Sector I9\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.0426025390625,33.6594958360814],[73.05204391479492,33.64634971688594],[73.06955337524414,33.65535216740787],[73.06028366088867,33.66806831016572],[73.0426025390625,33.6594958360814]]]}},{\"type\": \"Feature\",\"properties\": {\"id\": 32,\"desc\": \"Sector I8\"},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[73.06131362915039,33.6689255105912],[73.0697250366211,33.65606660727673],[73.08671951293946,33.66421080253697],[73.07676315307617,33.677068488547018],[73.06131362915039,33.6689255105912]]]}}]}";
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(json);

        mBoundaries = new ArrayList<>();
        mBoundaries.add(kmlDocument);

        Drawable defaultMarker = getResources().getDrawable(org.osmdroid.bonuspack.R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap,Color.argb(125,0,180,0) , 5f, 0x20AA1010); // line color = 0x901010AA
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mapView, defaultStyle, null, kmlDocument);
        mapView.getOverlays().add(geoJsonOverlay);

        mapView.invalidate();
        //---------------------------------------------------------


        findViewById(R.id.btnLocate).setOnClickListener((view) -> {
            mapController.animateTo(locations.get(locations.size()-1));
        });

        findViewById(R.id.btnLocate).setOnLongClickListener((view) -> {
            mAutoLocation = !mAutoLocation;
            offlineMapView.setAnimatedLocationPicker(!mAutoLocation, this, mapUtils);
//            if (!mAutoLocation)
//                setAnimatedPickerMode();
//            else
//                unsetAnimatedPickerMode();

            //marker.setDraggable(!mAutoLocation);
            marker.setVisible(mAutoLocation);
            circle.setVisible(mAutoLocation);
            //setAnimatedPickerMode(!mAutoLocation);
            //view.setBackgroundResource(R.drawable.ic_location_disabled);
            mapView.invalidate();
            return false;
        });

        callback = (Location mLocation) -> {
            GeoPoint gp = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
            if (locations != null)
                locations.add(gp);

            if (mAutoLocation) {
                mapController.animateTo(gp);

                if (!mapView.getOverlays().contains(marker))
                    mapView.getOverlays().add(marker);

                if (!mapView.getOverlays().contains(circle))
                    mapView.getOverlays().add(circle);

                marker.setPosition(gp);
                circle.setPoints(Polygon.pointsAsCircle(gp, mLocation.getAccuracy()));
            }

            //marker.setTitle(gp.toDoubleString() + " (Accuracy -> "+mLocation.getAccuracy()+")");
            //marker.showInfoWindow();
            mapView.invalidate();
        };

        verifyCurrentLocation(callback);
    }

    @Override
    public void mapLoadFailed(String ex) {
        Log.e("ex:", ex);
    }

    ArrayList<GeoPoint> gps = new ArrayList<>();
    Gson gs = new GsonBuilder().create();
    @Override
    public void onGeoPointRecieved(GeoPoint p) {
        gps.add(p);
//        GeoPoint[] points = new GeoPoint[gps.size()];
//        gps.toArray(points);

        String json = gs.toJson(gps);

        ArrayList<KmlFeature> bounds = mBoundaries.get(0).mKmlRoot.mItems;
        for (int i = 0; i < bounds.size(); i++){
            ArrayList<GeoPoint> polygons = ((KmlPlacemark) bounds.get(i)).mGeometry.mCoordinates;
            if (LocationUtils.isPointInPolygon(p, polygons)) {
                showToast("Point found inside " + bounds.get(i).mExtendedData.get("desc"));
                return;
            }
        }

        showToast("Point is not inside any of the highlighted areas");
    }

    protected void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(GeoPoint p){
        showToast("GPS Coordinates ("+p.getLatitude()+","+p.getLongitude()+")");
    }

    protected void startLocationService(){
        mLocationServiceConnection =  new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) service;
                mLocationService = binder.getService();

                if(!mLocationService.isNetworkEnabled() && !mLocationService.isGPSEnabled()){
                    Log.d(TAG, "onServiceConnected: Network provider not enabled or permission denied");
                }

                if (mAfterLocationServiceStartCallback != null)
                    mAfterLocationServiceStartCallback.run();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mLocationService = null;
            }
        };

        if (mLocationService == null) {
            Intent locationServiceIntent = new Intent(this, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(locationServiceIntent);
            bindService(locationServiceIntent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    protected void stopLocationService(){
        if (mLocationService != null){
            unbindService(mLocationServiceConnection);
        }
    }

    public void verifyCurrentLocation(@Nullable ILocationChangeCallback callback){
        if(Constants.DEBUG_MODE && false) {
            if (callback != null) {
                Location location = null;
                if (mLocationService != null)
                    location = mLocationService.getLocation();

                if (location == null)
                    location = new Location(LocationManager.GPS_PROVIDER);

                callback.onLocationChange(location);
            }
            return;
        }
        if (mLocationService != null) {
            checkLocationAndRunCallback(callback);
        } else {
            startLocationService();
            mAfterLocationServiceStartCallback = () -> {
                checkLocationAndRunCallback(callback);
            };
        }
    }

    private void checkLocationAndRunCallback(ILocationChangeCallback callback){
        if (mLocationService.getLocation() == null) {
            if (callback != null)
                mLocationService.addLocalLocationChangedCallback(this, callback);
        } else {
            if (callback != null)
                callback.onLocationChange(mLocationService.getLocation());
        }
    }

}