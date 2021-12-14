package pk.gov.pbs.geomap.utils;

import org.osmdroid.views.MapView;

import egolabsapps.basicodemine.offlinemap.Utils.MapUtils;

public interface CustomMapListener {
    void mapLoadSuccess(MapView mapView, CustomMapUtils mapUtils);

    void mapLoadFailed(String ex);
}
