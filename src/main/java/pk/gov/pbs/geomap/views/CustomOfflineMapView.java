package pk.gov.pbs.geomap.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;

import egolabsapps.basicodemine.offlinemap.Interfaces.GeoPointListener;
import egolabsapps.basicodemine.offlinemap.Utils.ScreenUtils;
import pk.gov.pbs.geomap.R;
import pk.gov.pbs.geomap.utils.CustomMapUtils;

public class CustomOfflineMapView extends OfflineMapView {
    public static final String LOCATION_PROVIDER_GEO_PICKER = "geoPicker";
    private GeoPointListener mGeoPointListener;
    protected boolean isAnimatePickerAdded = false;
    protected boolean isAnimatePickerActive = false;
    protected Context context;
    protected GeoPointListener animatedLocationGeoPointListenerContainer;

    public CustomOfflineMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        animatedLocationGeoPointListenerContainer = geoPoint -> {
            if (isAnimatePickerActive)
                mGeoPointListener.onGeoPointRecieved(geoPoint);
        };
    }

    @Override
    public void setAnimatedLocationPicker(boolean isAnimatedLocationPickerActive, GeoPointListener geoPointListener, CustomMapUtils mapUtils) {
        if (mapUtils != null && isAnimatedLocationPickerActive && !isAnimatePickerAdded) {
            ImageView locationToggle = new ImageView(context);
            ImageView locationCircle = new ImageView(context);
            LayoutParams paramsForCircle = new LayoutParams(
                    (int) ScreenUtils.pxFromDp(16.0F, context),
                    (int)ScreenUtils.pxFromDp(16.0F, context)
            );
            paramsForCircle.gravity = 17;
            locationCircle.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_circle));
            locationCircle.setLayoutParams(paramsForCircle);
            locationCircle.setTag("LocationBottomCircle");

            LayoutParams paramsForToggle = new LayoutParams(
                    (int)ScreenUtils.pxFromDp(50.0F, context),
                    (int)ScreenUtils.pxFromDp(50.0F, context)
            );
            paramsForToggle.gravity = 17;
            paramsForToggle.bottomMargin = (int)ScreenUtils.pxFromDp(24.0F, context);
            locationToggle.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_location_pin));
            locationToggle.setTag("LocationPin");

            locationToggle.setLayoutParams(paramsForToggle);
            addView(locationCircle);
            addView(locationToggle);
            this.mGeoPointListener = geoPointListener;
            mapUtils.setAnimatedView(locationToggle, animatedLocationGeoPointListenerContainer);
            this.isAnimatePickerAdded = true;
            this.isAnimatePickerActive = true;
        }

        if (this.isAnimatePickerAdded) {
            if (!isAnimatedLocationPickerActive) {
                getRootView().findViewWithTag("LocationBottomCircle").setVisibility(View.GONE);
                getRootView().findViewWithTag("LocationPin").setVisibility(View.GONE);
                this.isAnimatePickerActive = false;
            } else {
                getRootView().findViewWithTag("LocationBottomCircle").setVisibility(View.VISIBLE);
                getRootView().findViewWithTag("LocationPin").setVisibility(View.VISIBLE);
                this.isAnimatePickerActive = true;
            }
        }
    }

    public Marker addMarker(GeoPoint position, int icon){
        Marker marker = new Marker(getMapUtils().getMap());
        marker.setIcon(AppCompatResources.getDrawable(getContext(), icon));
        marker.setPosition(position);
        getMapUtils().getMap().getOverlays().add(marker);
        getMapUtils().getMap().invalidate();
        return marker;
    }

    public Marker addMarker(GeoPoint position){
        Marker marker = new Marker(getMapUtils().getMap());
        marker.setIcon(AppCompatResources.getDrawable(getContext(), R.drawable.ic_location_pin));
        marker.setPosition(position);
        getMapUtils().getMap().getOverlays().add(marker);
        getMapUtils().getMap().invalidate();
        return marker;
    }

    public FolderOverlay loadGeoJSON(File geoJSON){
        if (geoJSON == null || !geoJSON.exists())
            return null;

        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(geoJSON);

        return addKmlDocumentOverlay(kmlDocument);
    }

    public FolderOverlay loadGeoJSON(String geoJSON){
        if (geoJSON == null)
            return null;

        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(geoJSON);

        return addKmlDocumentOverlay(kmlDocument);
    }

    public KmlDocument parseGeoJson(String geoJson){
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(geoJson);
        return kmlDocument;
    }

    public FolderOverlay addKmlDocumentOverlay(KmlDocument kmlDocument){
        return addKmlDocumentOverlay(kmlDocument, mapUtils.getMap().getOverlays().size());
    }

    public FolderOverlay addKmlDocumentOverlay(KmlDocument kmlDocument, int index){
        Drawable defaultMarker = AppCompatResources.getDrawable(getContext(), org.osmdroid.bonuspack.R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, Color.argb(125,0,0,180) , 5f, Color.argb(50,0,0,100)); // line color = 0x901010AA
        FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(getMapUtils().getMap(), defaultStyle, null, kmlDocument);

        getMapUtils().getMap().getOverlays().add(index, geoJsonOverlay);
        getMapUtils().getMap().invalidate();

        return geoJsonOverlay;
    }

    public static class MapUtils {
        public static Marker addMarker(MapView map, GeoPoint position, int icon){
            Marker marker = new Marker(map);
            marker.setIcon(AppCompatResources.getDrawable(map.getContext(), icon));
            marker.setPosition(position);
            map.getOverlays().add(marker);
            map.invalidate();
            return marker;
        }

        public static Marker addMarker(MapView map, GeoPoint position){
            Marker marker = new Marker(map);
            marker.setIcon(AppCompatResources.getDrawable(map.getContext(), R.drawable.ic_location_pin));
            marker.setPosition(position);
            map.getOverlays().add(marker);
            map.invalidate();
            return marker;
        }

        public static FolderOverlay loadGeoJSON(MapView map, File geoJSON){
            if (geoJSON == null || !geoJSON.exists())
                return null;

            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseGeoJSON(geoJSON);

            return addKmlDocumentOverlay(map, kmlDocument);
        }

        public static FolderOverlay loadGeoJSON(MapView map, String geoJSON){
            if (geoJSON == null)
                return null;

            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseGeoJSON(geoJSON);

            return addKmlDocumentOverlay(map, kmlDocument);
        }

        public static KmlDocument parseGeoJson(String geoJson){
            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseGeoJSON(geoJson);
            return kmlDocument;
        }

        public static KmlDocument parseGeoJson(File geoJsonFile){
            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseGeoJSON(geoJsonFile);
            return kmlDocument;
        }

        public static FolderOverlay addKmlDocumentOverlay(MapView map, KmlDocument kmlDocument){
            return addKmlDocumentOverlay(map, kmlDocument, map.getOverlays().size());
        }

        public static FolderOverlay addKmlDocumentOverlay(MapView map, KmlDocument kmlDocument, int index){
            Drawable defaultMarker = AppCompatResources.getDrawable(map.getContext(), org.osmdroid.bonuspack.R.drawable.marker_default);
            Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
            Style defaultStyle = new Style(defaultBitmap, Color.argb(125,0,0,180) , 5f, Color.argb(50,0,0,100)); // line color = 0x901010AA
            FolderOverlay geoJsonOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument);

            map.getOverlays().add(index, geoJsonOverlay);
            map.invalidate();

            return geoJsonOverlay;
        }
    }
}
