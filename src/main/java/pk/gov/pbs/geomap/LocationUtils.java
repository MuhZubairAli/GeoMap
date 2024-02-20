package pk.gov.pbs.geomap;

import android.location.Location;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.util.GeoPoint;

import java.util.List;

public final class LocationUtils {

    public static boolean isPointInPolygon(GeoPoint p, List<GeoPoint> polygon ) {
        //Closed shape always have more than 2 points
        if (polygon.size() <3)
            return false;

        double minX = polygon.get(0).getLongitude();
        double maxX = polygon.get(0).getLongitude();
        double minY = polygon.get(0).getLatitude();
        double maxY = polygon.get(0).getLatitude();

        for ( int i = 1 ; i < polygon.size() ; i++ ) {
            minX = Math.min(polygon.get(i).getLongitude(), minX );
            maxX = Math.max(polygon.get(i).getLongitude(), maxX );
            minY = Math.min(polygon.get(i).getLatitude(), minY );
            maxY = Math.max(polygon.get(i).getLatitude(), maxY );
        }

        if ( p.getLongitude() < minX || p.getLongitude() > maxX || p.getLatitude() < minY || p.getLatitude() > maxY ) {
            return false;
        }

        // https://wrf.ecse.rpi.edu/Research/Short_Notes/pnpoly.html
        boolean inside = false;
        for ( int i = 0, j = polygon.size() - 1 ; i < polygon.size() ; j = i++ ) {
            if ( ( polygon.get(i).getLatitude() > p.getLatitude() ) != ( polygon.get(j).getLatitude() > p.getLatitude() ) &&
                    p.getLongitude() < ( polygon.get(j).getLongitude() - polygon.get(i).getLongitude() ) * ( p.getLatitude() - polygon.get(i).getLatitude() ) / ( polygon.get(j).getLatitude() - polygon.get(i).getLatitude() ) + polygon.get(i).getLongitude() ) {
                inside = !inside;
            }
        }

        return inside;
    }

    public static boolean isPointInPolygon(GeoPoint p, KmlDocument kmlDocument ) {
        for (KmlFeature feature : kmlDocument.mKmlRoot.mItems) {
            KmlPlacemark placemark = (KmlPlacemark) feature;
            if (placemark.mGeometry != null && isPointInPolygon(p, placemark.mGeometry.mCoordinates))
                return true;
        }
        return false;
    }

    //find midpoint between two geo coordinates
    public static GeoPoint getMidPoint(double lat1,double lon1,double lat2,double lon2){
        double dLon = Math.toRadians(lon2 - lon1);
        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
    
        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        return new GeoPoint(Math.toDegrees(lat3), Math.toDegrees(lon3));
    }

    public static GeoPoint getMidPoint(GeoPoint gPoint1, GeoPoint gPoint2){
        return getMidPoint(gPoint1.getLatitude(), gPoint1.getLongitude(), gPoint2.getLatitude(), gPoint2.getLongitude());
    }

    public static float getDistanceFromBoundary(GeoPoint location, KmlDocument geoPolygons){
        float minDist = Float.MAX_VALUE;
        for (KmlFeature feature : geoPolygons.mKmlRoot.mItems){
            KmlPlacemark placemark = (KmlPlacemark) feature;
            if (placemark.mGeometry != null && placemark.mGeometry.mCoordinates != null && placemark.mGeometry.mCoordinates.size() > 2){
                float dist = getDistanceBetweenGeoPoints(location,
                        getNearestPointFromLine(location,
                                getNearestLine(location, placemark.mGeometry.mCoordinates)));
                if (dist < minDist)
                    minDist = dist;
            }
        }
        return minDist;
    }

    public static GeoPoint getNearestPointFromBoundary(GeoPoint location, KmlDocument geoPolygons){
        GeoPoint nearestPoint = null;
        float minDist = Float.MAX_VALUE;

        for (KmlFeature feature : geoPolygons.mKmlRoot.mItems){
            KmlPlacemark placemark = (KmlPlacemark) feature;
            if (placemark.mGeometry != null && placemark.mGeometry.mCoordinates != null && placemark.mGeometry.mCoordinates.size() > 0){
                GeoPoint geoPoint = getNearestPointFromLine(location,
                        getNearestLine(location, placemark.mGeometry.mCoordinates));
                float dist = getDistanceBetweenGeoPoints(location, geoPoint);
                if (dist < minDist) {
                    minDist = dist;
                    nearestPoint = geoPoint;
                }
            }
        }
        return nearestPoint;
    }

    private static GeoPoint getNearestPointFromLine(GeoPoint location, GeoPoint[] lineGeoPoints){
        GeoPoint
                gpA = lineGeoPoints[0],
                gpB = lineGeoPoints[1],
                gpX = getMidPoint(lineGeoPoints[0], lineGeoPoints[1]);
        float
                distA = getDistanceBetweenGeoPoints(location, gpA),
                distB = getDistanceBetweenGeoPoints(location, gpB),
                distX = getDistanceBetweenGeoPoints(location, gpX);

        int dist = Math.round(getDistanceBetweenGeoPoints(gpA, gpB));
        int logDis = 0;
        for (; dist != 0; logDis++)
            dist >>= 1;

        for (int i = 0; i <= logDis; i++){
            if (distA < distB) {
                gpB = gpX;
                distB = distX;
            } else {
                gpA = gpX;
                distA = distX;
            }
            gpX = getMidPoint(gpA, gpB);
            distX = getDistanceBetweenGeoPoints(location, gpX);
        }
        return gpX;
    }

    private static GeoPoint[] getNearestLine(GeoPoint location, List<GeoPoint> polygonPoints){
        float minDist = Float.MAX_VALUE;
        int minInd = 0;
        for (int i=0; i < polygonPoints.size(); i++){
            float[] result = new float[1];
            Location.distanceBetween(
                    location.getLatitude(),
                    location.getLongitude(),
                    polygonPoints.get(i).getLatitude(),
                    polygonPoints.get(i).getLongitude(),
                    result);
            if (minDist > result[0]) {
                minDist = result[0];
                minInd = i;
            }
        }

        float pDist, nDist;
        int pInd, nInd;

        pInd = (minInd == 0) ? (polygonPoints.size() - 1) : minInd - 1;
        nInd = (minInd == (polygonPoints.size() - 1)) ?  0 : minInd + 1;

        float[] result = new float[1];
        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                polygonPoints.get(pInd).getLatitude(),
                polygonPoints.get(pInd).getLongitude(),
                result);
        pDist = result[0];

        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                polygonPoints.get(nInd).getLatitude(),
                polygonPoints.get(nInd).getLongitude(),
                result);
        nDist = result[0];

        return pDist < nDist ? new GeoPoint[]{polygonPoints.get(pInd), polygonPoints.get(minInd)} :
                new GeoPoint[]{polygonPoints.get(minInd), polygonPoints.get(nInd)};
    }

    public static float getDistanceBetweenGeoPoints(GeoPoint point1, GeoPoint point2){
        float[] result = new float[1];
        Location.distanceBetween(
                point1.getLatitude(),
                point1.getLongitude(),
                point2.getLatitude(),
                point2.getLongitude(),
                result);
        return result[0];
    }

    public static boolean isValidLocation(Location location){
        return location != null && location.getLatitude() != 0.0D && location.getLongitude() != 0.0D;
    }

    public static boolean isValidLocation(GeoPoint location){
        return location != null && location.getLatitude() != 0.0D && location.getLongitude() != 0.0D;
    }
}
