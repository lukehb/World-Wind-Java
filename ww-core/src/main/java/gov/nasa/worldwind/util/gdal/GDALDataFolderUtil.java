package gov.nasa.worldwind.util.gdal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

/**
 * Some operation in WW appear to use this GDAL data path.
 * What I have done is packaged it as a resource with the shipped jar.
 * When the GDALUtils runs it will extract the contents of the gdal_data
 * folder in the jar to a known temporary location.
 * @see GDALUtils
 * @author Luke Bermingham
 */
public class GDALDataFolderUtil {

    private static final Logger log = Logger.getLogger(GDALDataFolderUtil.class.getSimpleName());

    private static final String gdalFolderName = "gdal";
    private static final String internalGdalResourcePath = "gdal_data/";

    private GDALDataFolderUtil(){}

    public static File getGDALDataPath(){

        String tempDir = System.getProperty("java.io.tmpdir");
        File gdalDataFolder = Paths.get(tempDir, gdalFolderName).toFile();
        if(!gdalDataFolder.exists()){
            try {
                if(gdalDataFolder.mkdir()){
                    log.info("Created directory: " + gdalDataFolder.getAbsolutePath());
                }
                else{
                    log.warning("Could not create directory: " + gdalDataFolder.getAbsolutePath());
                }
            } catch (Exception e) {
                log.warning("Failed creating gdal folder: " + e.getMessage());
            }
        }
        else{
            log.info("Gdal data path at: " + gdalDataFolder.getAbsolutePath());
        }

        //folder now exists extract contents of resources folder to this folder
        try {
            File[] gdalFiles = getGdalFiles();
            for (File internalGdalFile : gdalFiles) {
                File externalGdalFile = new File(gdalDataFolder, internalGdalFile.getName());
                //note: copy does nothing if the file already exists
                Files.copy(
                        internalGdalFile.toPath(),
                        externalGdalFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
        } catch (URISyntaxException e) {
            log.warning("Failed to get gdal_data files: " + e.getMessage());
        } catch (IOException e){
            log.warning("Failed to create/copy gdal file: " + e.getMessage());
        }

        return gdalDataFolder;
    }

    private static File[] getGdalFiles() throws URISyntaxException {
        return new File[]{
            getRes("coordinate_axis.csv"),
            getRes("cubewerx_extra.wkt"),
            getRes("ecw_cs.wkt"),
            getRes("ellipsoid.csv"),
            getRes("epsg"),
            getRes("epsg.wkt"),
            getRes("esri"),
            getRes("esri.extra"),
            getRes("esri_extra.wkt"),
            getRes("gcs.csv"),
            getRes("gcs.override.csv"),
            getRes("gdal_datum.csv"),
            getRes("gdalicon.png"),
            getRes("GDALLogoBW.svg"),
            getRes("GDALLogoColor.svg"),
            getRes("GDALLogoGS.svg"),
            getRes("GL27"),
            getRes("gt_datum.csv"),
            getRes("gt_ellips.csv"),
            getRes("header.dxf"),
            getRes("IGNF"),
            getRes("LICENSE.TXT"),
            getRes("nad.lst"),
            getRes("nad27"),
            getRes("nad83"),
            getRes("other.extra"),
            getRes("pcs.csv"),
            getRes("pcs.override.csv"),
            getRes("prime_meridian.csv"),
            getRes("proj_def.dat"),
            getRes("projop_wparm.csv"),
            getRes("s57agencies.csv"),
            getRes("s57attributes.csv"),
            getRes("s57attributes_aml.csv"),
            getRes("s57attributes_iw.csv"),
            getRes("s57expectedinput.csv"),
            getRes("s57objectclasses.csv"),
            getRes("s57objectclasses_aml.csv"),
            getRes("s57objectclasses_iw.csv"),
            getRes("seed_2d.dgn"),
            getRes("seed_3d.dgn"),
            getRes("stateplane.csv"),
            getRes("trailer.dxf"),
            getRes("unit_of_measure.csv"),
            getRes("world")
        };
    }

    private static File getRes(String gdalFile) throws URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(internalGdalResourcePath + gdalFile);
        if(url == null){
            throw new IllegalArgumentException("Could not find the resource at: " + gdalFile);
        }
        return new File(url.toURI());
    }

}
