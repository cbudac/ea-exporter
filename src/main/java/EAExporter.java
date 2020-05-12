import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Created by cbudac on 30/12/2015.
 */
class EAExporter {
    private static final Logger log = LogManager.getLogger(EAExporter.class.getSimpleName());
    
    private static final String EA_EXPORTER_PROPERTIES = "eaexporter.properties";
    private static final String EA_DLL_PATH = "ea_dll_path";
    private ModelManager moduleManager;
    private FolderManager folderManager;
    private EAModelPublisher modelPublisher;
    private DirectoryArchiver directoryArchiver;
    private SFTManager sftManager;

    public static void main(String[] args) {
        EAExporter eaExporter = null;
        try {
            eaExporter = new EAExporter();
        }
        catch (Exception e) {
            log.error("The EA export process failed initialization: ", e);
        }
        if(eaExporter != null) {
            eaExporter.exportEAModules();
        }
    }

    public EAExporter() throws IOException, NoSuchFieldException, IllegalAccessException {
        Properties properties = this.loadConfiguration();
        this.moduleManager = new ModelManager(properties);
        this.folderManager = new FolderManager(moduleManager);
        this.modelPublisher = new EAModelPublisher(properties, moduleManager);
        this.directoryArchiver = new DirectoryArchiver(moduleManager);
        this.sftManager = new SFTManager(properties, moduleManager);

        // load native library
        loadEADLL(properties);
    }

    /**
     * This method connects to the EA Repository and publishes a model in a HTML format
     */
    public void exportEAModules() {
        long startTime = System.currentTimeMillis();
        log.debug("The export of EA modules has started.");

        try {
            // ensure local folder is empty
            this.folderManager.executeTmpModuleFolderCleanUp();

            // create one folder for each EA module to be published
            this.folderManager.executeTmpModuleFolderCreation();

            // execute the publishing of the models from Enterprise Architect
            this.modelPublisher.executeModulePublishing();

            // execute the archiving of the model folders
            this.directoryArchiver.executeDirectoryArchiving();

            // execute the file transfers
            this.sftManager.executeFileTransfers();
        }
        catch(Exception e){
            log.error("The export of EA modules has failed with the following exception:", e);
        }

        finally {
            // clean up temporary files created on the local folder - quiet clean
            this.folderManager.executeTmpModuleFolderCleanUp();

            long endTime = System.currentTimeMillis();
            log.debug("The export of EA modules has ended in {} milliseconds.", endTime-startTime);
        }
    }

    Properties loadConfiguration() throws IOException{
        Properties properties = new Properties();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(EAExporter.class.getClassLoader().getResourceAsStream(EA_EXPORTER_PROPERTIES), Charset.defaultCharset()));
            properties.load(reader);
        }
        finally {
            if(reader != null){
                reader.close();
            }
        }
        return properties;
    }

    public static void loadEADLL(Properties properties) throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("java.library.path", (String)properties.get(EA_DLL_PATH));
        Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(null, null);
    }
}