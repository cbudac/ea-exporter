import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DirectoryArchiver {
    private static final Logger log = LogManager.getLogger(DirectoryArchiver.class.getSimpleName());

    private ModelManager moduleManager;

    public DirectoryArchiver(ModelManager moduleManager){
        this.moduleManager = moduleManager;
    }

    public void executeDirectoryArchiving(){
        long startTime = System.currentTimeMillis();
        log.debug("The directories archiving has started");

        Set<String> failedModuleNames = new HashSet<String>();
        Map<String, String> modelsAndFolders = this.moduleManager.getCurrentModelsAndFolders();
        for(Map.Entry<String, String> entry : modelsAndFolders.entrySet()){
            String modelName = entry.getKey();
            String modelTmpFolderName = entry.getValue();
            try {
                ZipUtil.pack(new File(modelTmpFolderName), new File(modelTmpFolderName + ".zip"));
                FileUtils.moveFile(new File(modelTmpFolderName + ".zip"), new File(modelTmpFolderName + "/" + modelName + ".zip"));
            }
            catch (Exception e){
                failedModuleNames.add(modelName);
                log.debug("The archiving of temporary directory '{}' has failed with exception:", modelTmpFolderName, e);
            }
        }
        this.moduleManager.excludeFailedModels(failedModuleNames);

        long endTime = System.currentTimeMillis();
        log.debug("The directories archiving has ended in {} milliseconds", endTime - startTime);
    }
}