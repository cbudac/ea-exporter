import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cbudac on 01/01/2016.
 */
public class FolderManager {
    private static final Logger log = LogManager.getLogger(FolderManager.class.getSimpleName());
    private ModelManager modelManager;

    public FolderManager(ModelManager modelManager){
        this.modelManager = modelManager;
    }

    /**
     * This method is used to create the temporary folders that will host the EA model files.
     */
    public void executeTmpModuleFolderCreation(){
        long startTime = System.currentTimeMillis();
        log.debug("The temporary local folders creation has started");

        Set<String> failedModelNames = new HashSet<String>();
        Map<String, String> modelsAndFolders = this.modelManager.getCurrentModelsAndFolders();
        for (Map.Entry<String, String> entry : modelsAndFolders.entrySet()) {
            String modelName = entry.getKey();
            String folderName = entry.getValue();
            boolean folderCreated = false;
            try {
                File folder = new File(folderName);
                folderCreated =folder.mkdir();
            }
            catch (Exception e){
                failedModelNames.add(modelName);
                log.debug("The creation of folder '{}' has failed with exception:", folderName, e);
            }
            if(!folderCreated){
                failedModelNames.add(modelName);
                log.debug("The creation of folder '{}' has failed", folderName);
            }
            else{
                log.debug("Folder '{}' was created.", folderName);
            }
        }
        this.modelManager.excludeFailedModels(failedModelNames);

        long endTime = System.currentTimeMillis();
        log.debug("The temporary local folders creation has ended in {} milliseconds", endTime - startTime);
    }

    /**
     * Prepares the local (work) folder by removing any file of folder from it but the logs folder
     */
    public void executeTmpModuleFolderCleanUp(){
        long startTime = System.currentTimeMillis();
        log.debug("Clean up of local temporary folder has started");

        File localFolder = new File(this.modelManager.getLocalFolder());

        File[] tmp = localFolder.listFiles();
        for(int i=0; i<tmp.length; i++){
            if(!tmp[i].getName().equals("logs")){
                FileUtils.deleteQuietly(tmp[i]);
                log.debug("File or folder '{}' was deleted.", tmp[i].getAbsolutePath());
            }
        }

        long endTime = System.currentTimeMillis();
        log.debug("Clean up of local temporary folder has ended in {} milliseconds", endTime - startTime);
    }
}
