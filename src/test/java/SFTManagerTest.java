import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cbudac on 02/01/2016.
 */
@Test
public class SFTManagerTest {
    private FolderManager folderManager;
    private DirectoryArchiver directoryArchiver;
    private ModelManager modelManager;
    private Properties properties;

    @BeforeTest
    public void initialize(){
        this.properties = (Properties)EATestsDataProvider.getProperties()[0][0];
        this.modelManager = (ModelManager)EATestsDataProvider.getModuleManager()[0][0];
        this.folderManager = new FolderManager(modelManager);
        this.directoryArchiver = new DirectoryArchiver(modelManager);
    }

    @BeforeMethod
    public void prepareTest() throws IOException {
        // create the temporary folders
        folderManager.executeTmpModuleFolderCreation();

        // add a file in the temporary folders
        for(String folder : modelManager.getOriginalFolders() ){
            EATestsDataProvider.createTestFile(folder);
        }

        // zip the temporary folders
        this.directoryArchiver.executeDirectoryArchiving();
    }

    @AfterMethod
    public void cleanUp(){
        // remove temporary folders
        folderManager.executeTmpModuleFolderCleanUp();
    }

    @Test()
    public void testSFTManager() throws IOException, JSchException, SftpException {
        SFTManager sftManager = new SFTManager(this.properties, this.modelManager);
        Session session = sftManager.openSession();

        Map<String, String> modelsAndFolders = this.modelManager.getCurrentModelsAndFolders();

        for(Map.Entry<String, String> entry : modelsAndFolders.entrySet()) {
            String folderName = entry.getValue();
            String modelName = entry.getKey();
            String archiveName = modelName + ".zip";

            sftManager.uploadArchive(session, folderName, archiveName );
            Assert.assertTrue(sftManager.fileExists(session, archiveName));

            sftManager.unzipRemoteArchive(session, archiveName, modelName);
            Assert.assertTrue(sftManager.dirExists(session, modelName));

            sftManager.deleteRemoteFolder(session, modelName);
            Assert.assertFalse(sftManager.dirExists(session, modelName));

            sftManager.deleteRemoteArchive(session, archiveName);
            Assert.assertFalse(sftManager.fileExists(session, archiveName));
        }
        sftManager.closeSession(session);
    }


    @Test
    public void executeFileTransfersTest() throws IOException, JSchException {
        SFTManager sftManager = new SFTManager(this.properties, this.modelManager);
        sftManager.executeFileTransfers();

        Map<String, String> modelsAndFolders = this.modelManager.getCurrentModelsAndFolders();

        Session session = sftManager.openSession();
        for(Map.Entry<String, String> entry : modelsAndFolders.entrySet()) {
            String folderName = entry.getValue();
            String modelName = entry.getKey();
            String archiveName = modelName + ".zip";

            Assert.assertFalse(sftManager.fileExists(session, archiveName));
            Assert.assertTrue(sftManager.dirExists(session, modelName));

            sftManager.deleteRemoteFolder(session, modelName);
            Assert.assertFalse(sftManager.dirExists(session, modelName));
        }
        sftManager.closeSession(session);
    }


    @Test(dataProvider = "propertiesAndModuleManagerProvider", dataProviderClass = EATestsDataProvider.class)
    public void executeCommandTest(Properties properties, ModelManager modelManager) throws JSchException, IOException {
        SFTManager sftManager = new SFTManager(properties, modelManager);

        Session session = sftManager.openSession();

        SFTManager.CommandResponse response1 = sftManager.executeCommand(session, "lst");
        Assert.assertEquals(response1.getExitStatus(), 127);

        SFTManager.CommandResponse response2 = sftManager.executeCommand(session, "ls");
        Assert.assertEquals(response2.getExitStatus(), 0);

        sftManager.closeSession(session);
    }
}
