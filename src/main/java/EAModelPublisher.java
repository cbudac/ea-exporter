import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparx.Repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by cbudac on 01/01/2016.
 */
public class EAModelPublisher {
    // EA Repo
    private static final String REPO_CONNECTION = "repo_connection";

    private static final Logger log = LogManager.getLogger(EAModelPublisher.class.getSimpleName());

    private String repoConnection;
    private ModelManager moduleManager;

    public EAModelPublisher(Properties properties, ModelManager moduleManager){
        this.moduleManager = moduleManager;
        this.repoConnection = properties.getProperty(REPO_CONNECTION);
    }

    /**
     * This method is called to publish EA module as html files.
     */
    public void executeModulePublishing(){
        long startTime = System.currentTimeMillis();
        log.debug("The models publishing has started");

        Repository repository = new Repository();
        repository.OpenFile(repoConnection);

        Set<String> failedModuleNames = new HashSet<String>();
        Map<String, String> guidAndFolders = this.moduleManager.getCurrentGuidAndFolders();

        for(Map.Entry<String, String> entry : guidAndFolders.entrySet()) {
            String moduleGuid = entry.getKey();
            String folderName = entry.getValue();
            try {
                repository.GetProjectInterface().RunHTMLReport(moduleGuid, folderName, "PNG", "<none>", ".html");
            }
            catch(RuntimeException e){
                String moduleName = this.moduleManager.getModelNameByModelGuid(moduleGuid);
                failedModuleNames.add(moduleName);
                log.debug("The publishing of module {} has failed with exception:", moduleName, e);
            }
        }
        this.moduleManager.excludeFailedModels(failedModuleNames);
        repository.CloseFile();
        repository.Exit();

        long endTime = System.currentTimeMillis();
        log.debug("The models publishing has ended in {} milliseconds", endTime - startTime);
    }
}
