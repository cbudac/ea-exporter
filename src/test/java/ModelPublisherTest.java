import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Properties;

/**
 * Created by cbudac on 02/01/2016.
 */
@Test
public class ModelPublisherTest {
    private ModelManager modelManager;
    private FolderManager folderManager;

    @Test
    public void executeModulePublishingTest() throws NoSuchFieldException, IllegalAccessException {
        EAModelPublisher eaModelPublisher = new EAModelPublisher(this.getProperties(), modelManager);
        eaModelPublisher.executeModulePublishing();

        for(String folder : modelManager.getOriginalFolders()){
            File dir = new File(folder);
            Assert.assertNotEquals(dir.list().length, 0);
        }
    }

    @BeforeTest
    public void prepareTest(){
        this.folderManager = new FolderManager(this.modelManager);
        this.folderManager.executeTmpModuleFolderCreation();
    }

    @AfterTest
    public void cleanUp(){
        this.folderManager.executeTmpModuleFolderCleanUp();
    }

    @BeforeTest
    public void initialize() throws IllegalAccessException, NoSuchFieldException {
        EAExporter.loadEADLL(this.getProperties());
        this.modelManager = this.getModuleManager(this.getProperties());
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put("port_number", "22");
        properties.put("user_name", "deployment");
        properties.put("password", "univeris");
        properties.put("host_name", "repo.univeris.com");
        properties.put("exec_protocol", "exec");
        properties.put("ftp_protocol", "sftp");
        properties.put("local_folder", "C:\\EA Published Models");
        properties.put("remote_folder", "sites/atom");
        properties.put("model_ids", "RoR Subsystem:{9DC4D5EF-52E2-4740-ABF3-518CB2389BAC}");
        properties.put("ea_dll_path", "C:\\EAExporter\\lib");
        properties.put("repo_connection", "G4EA --- DBType=4;Connect=Provider=MSDASQL.1;Persist Security Info=False;Data Source=G4EA;LazyLoad=1;");
        return properties;
    }

    public ModelManager getModuleManager (Properties properties){
        ModelManager modelManager = new ModelManager(properties);
        return modelManager;
    }
}
