package pk.gov.pbs.geomap.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.style.Pulse;
import egolabsapps.basicodemine.offlinemap.Interfaces.GeoPointListener;
import egolabsapps.basicodemine.offlinemap.Interfaces.PermissionResultListener;
import egolabsapps.basicodemine.offlinemap.Interfaces.PermissionResultListener.PermissionListener;
import egolabsapps.basicodemine.offlinemap.R.drawable;
import egolabsapps.basicodemine.offlinemap.R.styleable;
import egolabsapps.basicodemine.offlinemap.Utils.PermissionUtils;
import egolabsapps.basicodemine.offlinemap.Utils.ScreenUtils;
import egolabsapps.basicodemine.offlinemap.Views.PermissionActivity;
import pk.gov.pbs.geomap.utils.CustomMapListener;
import pk.gov.pbs.geomap.utils.CustomMapUtils;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class OfflineMapView extends FrameLayout implements CustomMapListener, PermissionListener {
    protected CustomMapListener mapListener;
    protected Activity activity;
    protected CustomMapUtils mapUtils;
    protected final SpinKitView kitView;
    protected boolean isAnimatePickerAdded = false;
    protected double attrLong;
    protected double attrLat;
    protected double attrZoomLevel;

    public void init(Activity activity, CustomMapListener mapListener) {
        this.activity = activity;
        this.mapListener = mapListener;
        attrZoomLevel = 14.0f;
        if (!PermissionUtils.hasPermissions(activity)) {
            activity.startActivity(new Intent(activity, PermissionActivity.class));
        } else {
            this.mapUtils = new CustomMapUtils(this, activity);
        }
    }

    public void setInitialPositionAndZoom(@NonNull GeoPoint initialPosition, double zoomLevel) {
        if (this.mapUtils != null) {
            this.mapUtils.setInitialPosition(initialPosition, zoomLevel);
        }

    }

    public void setAnimatedLocationPicker(boolean isAnimatedLocationPickerActive, GeoPointListener geoPointListener, CustomMapUtils mapUtils) {
        if (this.activity != null && mapUtils != null && isAnimatedLocationPickerActive && !this.isAnimatePickerAdded) {
            ImageView locationToggle = new ImageView(this.activity);
            ImageView locationCircle = new ImageView(this.activity);
            LayoutParams paramsForCircle = new LayoutParams((int)ScreenUtils.pxFromDp(16.0F, this.activity), (int)ScreenUtils.pxFromDp(16.0F, this.activity));
            paramsForCircle.gravity = 17;
            locationCircle.setImageDrawable(this.activity.getResources().getDrawable(drawable.location_oval_icon));
            locationCircle.setLayoutParams(paramsForCircle);
            LayoutParams paramsForToggle = new LayoutParams((int)ScreenUtils.pxFromDp(50.0F, this.activity), (int)ScreenUtils.pxFromDp(50.0F, this.activity));
            paramsForToggle.gravity = 17;
            paramsForToggle.bottomMargin = (int)ScreenUtils.pxFromDp(24.0F, this.activity);
            locationToggle.setImageDrawable(this.activity.getResources().getDrawable(drawable.taskpicke_icon));
            locationToggle.setLayoutParams(paramsForToggle);
            this.addView(locationCircle);
            this.addView(locationToggle);
            mapUtils.setAnimatedView(locationToggle, geoPointListener);
            this.isAnimatePickerAdded = true;
        }

    }

    public OfflineMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, styleable.OfflineMapView, 0, 0);

        try {
            this.attrZoomLevel = a.getFloat(styleable.OfflineMapView_zoomLevel, -1.0F);
            this.attrLat = a.getFloat(styleable.OfflineMapView_initialFocusLatitude, -1.0F);
            this.attrLong = a.getFloat(styleable.OfflineMapView_initialFocusLongitude, -1.0F);
        } finally {
            a.recycle();
        }

        PermissionResultListener.getInstance().setListener(this);
        this.kitView = new SpinKitView(context);
        Pulse kitStyle = new Pulse();
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = 17;
        this.kitView.setLayoutParams(params);
        this.kitView.setIndeterminateDrawable(kitStyle);
        this.addView(this.kitView);
    }

    @Override
    public void mapLoadSuccess(MapView mapView, CustomMapUtils mapUtils) {
        mapView.setLayoutParams(new LayoutParams(-1, -1));
        this.addView(mapView);
        this.kitView.setVisibility(GONE);
        if (this.mapUtils == null) {
            this.mapUtils = mapUtils;
        }

        if (this.attrLong != -1.0F && this.attrLat != -1.0F) {
            mapUtils.setInitialPositionX(new GeoPoint( this.attrLat, this.attrLong));
        }

        if (this.attrZoomLevel != -1.0F) {
            mapUtils.setInitialZoom(this.attrZoomLevel);
        }

        this.mapListener.mapLoadSuccess(mapView, mapUtils);
    }

    public void mapLoadFailed(String ex) {
        this.kitView.setVisibility(GONE);
        this.mapListener.mapLoadFailed(ex);
    }

    public void onAccept() {
        this.mapUtils = new CustomMapUtils(this, this.activity);
    }

    public void onReject() {
        Toast.makeText(this.activity, "error: permission not granted", Toast.LENGTH_SHORT).show();
    }

    public CustomMapUtils getMapUtils() {
        return this.mapUtils;
    }
}
