import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by cbudac on 01/01/2016.
 */
public class ModelManager {
    private static final String MODEL_IDS = "model_ids";
    private static final String LOCAL_FOLDER = "local_folder";

    private Set<String[]> currentEntries = new HashSet<String[]>();
    private Set<String[]> originalEntries = new HashSet<String[]>();
    private String localFolder;

    private static final Logger log = LogManager.getLogger(ModelManager.class.getSimpleName());

    public ModelManager(Properties properties){
        this.localFolder = properties.getProperty(LOCAL_FOLDER);
        String[] modelIds = properties.getProperty(MODEL_IDS).split(",");

        log.debug("The following Enterprise Architect models are considered for exporting:");
        for (int i = 0; i < modelIds.length; i++) {
            String[] modelInfo = modelIds[i].split(":");
            String modelName = modelInfo[0];
            String modelGUID = modelInfo[1];

            String modelTmpFolderName = localFolder + "/" + modelName;

            currentEntries.add(new String[]{modelName, modelGUID, modelTmpFolderName});
            originalEntries.add(new String[]{modelName, modelGUID, modelTmpFolderName});

            log.debug("\tmodel name: '{}', model GUID: {}, model temp folder: '{}'", modelName, modelGUID, modelTmpFolderName);
        }
    }

    /**
     * Exclude modules that have failed operations from further processing.
     * @param failedModelNames
     */
    public void excludeFailedModels(Set<String> failedModelNames){
        if(!failedModelNames.isEmpty()) {
            log.debug("The following models will be excluded from further processing: {}", failedModelNames);
            Set<String[]> keysToRemove = new HashSet<String[]>();
            for (String modelName : failedModelNames) {
                for(String[] entry :this.currentEntries){
                    if(entry[0].equals(modelName)){
                        keysToRemove.add(entry);
                    }
                }
            }
            for(String[] key : keysToRemove){
                this.currentEntries.remove(key);
            }
        }
    }

    public Collection<String> getCurrentFolders(){
        Set<String> currentFolders = new HashSet<String>();
        for(String [] value : currentEntries){
            currentFolders.add(value[2]);
        }
        return currentFolders;
    }

    public Set<String> getOriginalFolders(){
        Set<String> originalFolders = new HashSet<String>();
        for(String [] value : originalEntries){
            originalFolders.add(value[2]);
        }
        return originalFolders;
    }

    public Map<String, String> getCurrentModelsAndFolders(){
        Map<String, String> map = new HashMap<String, String>();
        for(String [] value : currentEntries){
            map.put(value[0], value[2]);
        }
        return map;
    }

    public Map<String, String> getCurrentGuidAndFolders(){
        Map<String, String> map = new HashMap<String, String>();
        for(String [] value : currentEntries){
            map.put(value[1], value[2]);
        }
        return map;
    }

    public String getModelNameByModelGuid(String modelGuid){
        for(String[] value : this.originalEntries){
            if(value[1].equals(modelGuid)){
                return value[0];
            }
        }
        return null;
    }

    public String getModelGuidByModelName(String modelName){
        for(String[] value : this.originalEntries){
            if(value[0].equals(modelName)){
                return value[1];
            }
        }
        return null;
    }

    public String getLocalFolder(){
        return this.localFolder;
    }

}
