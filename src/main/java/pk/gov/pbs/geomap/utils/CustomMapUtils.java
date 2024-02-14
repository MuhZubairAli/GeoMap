package pk.gov.pbs.geomap.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import egolabsapps.basicodemine.offlinemap.Interfaces.GeoPointListener;
import egolabsapps.basicodemine.offlinemap.Utils.FileUtils;
import pk.gov.pbs.utils.ExceptionReporter;

public class CustomMapUtils implements FileUtils.FileTransferListener {
    private final MapView map;
    private final CustomMapListener mapListener;
    private View animatedView;
    private final Activity activity;
    private IMapController mapController;

    public MapView getMap() {
        return map;
    }

    public CustomMapUtils(CustomMapListener mapListener, Activity activity) {
        this.mapListener = mapListener;
        this.activity = activity;
        map = new MapView(activity);
        setupMap();
    }

    public void setInitialZoom(double zoomLevel) {
        if (map != null) {
            if (mapController == null)
                mapController = new MapController(map);
            mapController.zoomTo(zoomLevel);
        }
    }

    public void setInitialPosition(GeoPoint initialPosition, double zoomLvl) {
        if (map != null) {
            IMapController mapController = new MapController(map);
            mapController.setCenter(initialPosition);
            mapController.zoomTo(zoomLvl);
            map.getController().animateTo(initialPosition);
        }
    }

    public void setInitialPositionX(GeoPoint initialPosition) {
        if (map != null) {
            IMapController mapController = new MapController(map);
            mapController.setCenter(initialPosition);
            map.getController().animateTo(initialPosition);
        }
    }

    private void setupMap() {
        map.setUseDataConnection(true);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(5.0);
        map.setMaxZoomLevel(19.0);
        if (hasOfflineSources())
            setMapOfflineSources();
        else {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.getController().setZoom(13.0);
        }
        //setMapOfflineSourceTest();// test function for MBTiles archive, current it is abandoned
    }


    private void setMapOfflineSourceTest() {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {
            File[] list = f.listFiles();
            if (list != null) {
                List<File> files = new ArrayList<>();

                for (File aList : list) {
                    if (aList.isDirectory()) {
                        continue;
                    }
                    String name = aList.getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue;
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue;
                    }
                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) {
                        files.add(aList);
                    }
                }

                File[] fileArray = new File[files.size()];
                fileArray = files.toArray(fileArray);

                try {
                    OfflineTileProvider tileProvider = new OfflineTileProvider(
                            new SimpleRegisterReceiver(activity),
                            fileArray
                    );
                    map.setTileProvider(tileProvider);
                    String source = "";
                    IArchiveFile[] archives = tileProvider.getArchives();
                    if (archives.length > 0) {
                        Set<String> tileSources = archives[0].getTileSources();
                        if (!tileSources.isEmpty()) {
                            source = tileSources.iterator().next();
                            map.setTileSource(FileBasedTileSource.getSource(source));
                        } else {
                            map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                        }
                    } else {
                        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                    }
                    map.invalidate();
                    mapListener.mapLoadSuccess(map, this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mapListener.mapLoadFailed(ex.toString());
                }
            }
        } else {
//            if (!FileUtils.isMapFileExists()) {
//                FileUtils.copyMapFilesToSdCard(activity, new FileUtils.FileTransferListener() {
//                    @Override
//                    public void onLoadFailed() {
//                        //WARNING Fabric.getInstance() custom event
//                        mapListener.mapLoadFailed("");
//                    }
//
//                    @Override
//                    public void onLoadSuccess() {
//                        setMapOfflineSource();
//                    }
//                });
//            }..
        }
    }


    public static boolean hasOfflineSources(){
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {
            File[] list = f.listFiles();
            if (list != null) {
                List<File> mapFilesList = new ArrayList<>();

                for (File aList : list) {
                    if (aList.isDirectory()) {
                        continue;
                    }

                    String name = aList.getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue;
                    }

                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue;
                    }

                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) {
                        mapFilesList.add(aList);
                    }
                }
                return mapFilesList.size() > 0;
            }
        }

        return false;
    }

    public void setMapOfflineSources() {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {
            File[] list = f.listFiles();
            if (list != null) {
                List<File> mapFilesList = new ArrayList<>();

                for (File aList : list) {
                    if (aList.isDirectory()) {
                        continue;
                    }

                    String name = aList.getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue;
                    }

                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue;
                    }

                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) {
                        mapFilesList.add(aList);
                    }
                }

                try {
                    if (mapFilesList.size() == 0) {
                        final MapBoxTileSource tileSource = new MapBoxTileSource();
                        tileSource.retrieveAccessToken(activity);
                        tileSource.retrieveMapBoxMapId(activity);
                        map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
                        map.setTileSource(tileSource);
                        map.invalidate();
                        mapListener.mapLoadSuccess(map, this);
                        return;
                    }

                    File[] mapFilesArray = new File[mapFilesList.size()];
                    mapFilesArray = mapFilesList.toArray(mapFilesArray);

                    OfflineTileProvider tileProvider = new OfflineTileProvider(
                            new SimpleRegisterReceiver(activity),
                            mapFilesArray
                    );
                    map.setTileProvider(tileProvider);
                    String source = "";
                    IArchiveFile[] archives = tileProvider.getArchives();
                    if (archives.length > 0) {
                        Set<String> tileSources = archives[0].getTileSources();
                        if (!tileSources.isEmpty()) {
                            source = tileSources.iterator().next();
                            map.setTileSource(FileBasedTileSource.getSource(source));
                        } else {
                            map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                        }
                    } else {
                        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                    }
                    map.invalidate();
                    mapListener.mapLoadSuccess(map, this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mapListener.mapLoadFailed(ex.toString());
                }
            }
        } else {
            Toast.makeText(activity, "Maps directory do not exists, kindly download relevant map in order to use maps.", Toast.LENGTH_SHORT).show();
            try {
                if (!f.mkdirs())
                    Toast.makeText(activity, "Failed to create maps directory", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                ExceptionReporter.printStackTrace(e);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addMapPickerAnimation(@Nullable View animatedView, GeoPointListener geoPointListener) {
        if (animatedView == null)
            return;
        float targetY = animatedView.getY();
        final ObjectAnimator animator = ObjectAnimator.ofFloat(animatedView, "translationY", targetY, targetY - 100f);
        final ObjectAnimator animator2 = ObjectAnimator.ofFloat(animatedView, "translationY", targetY - 100f, targetY);
        animator.setDuration(500);
        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (geoPointListener != null)
                    geoPointListener.onGeoPointRecieved(map.getProjection().getBoundingBox().getCenter());
            }
        });
        map.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                animator.start();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                animator2.start();
            }
            return false;
        });
    }

    private View getAnimatedView() {
        return animatedView;
    }

    public void setAnimatedView(View animatedView, GeoPointListener geoPointListener) {
        this.animatedView = animatedView;
        addMapPickerAnimation(animatedView, geoPointListener);
    }

    public IMapController getMapController(){
        return mapController;
    }

    @Override
    public void onLoadFailed() {
        mapListener.mapLoadFailed("Asset files cannot copied");
    }

    @Override
    public void onLoadSuccess() {
        setupMap();
    }
}