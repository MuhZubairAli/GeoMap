package pk.gov.pbs.geomap;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class LocationUtils {

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

    //find midpoint between two geo coordinates
    public static void midPoint(double lat1,double lon1,double lat2,double lon2){
        double dLon = Math.toRadians(lon2 - lon1);
        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
    
        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
    
        //print out in degrees
        System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
    }
    
    public static boolean isValidLocation(Location location){
        return location != null && location.getLatitude() != 0.0D && location.getLongitude() != 0.0D;
    }

    public static boolean isValidLocation(GeoPoint location){
        return location != null && location.getLatitude() != 0.0D && location.getLongitude() != 0.0D;
    }
}
