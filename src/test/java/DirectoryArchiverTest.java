import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by cbudac on 31/12/2015.
 */
@Test
public class DirectoryArchiverTest {
    @Test(dataProvider = "moduleManagerProvider", dataProviderClass = EATestsDataProvider.class)
    public void directoryArchiverTest(ModelManager moduleManager) throws IOException {
        FolderManager folderManager = new FolderManager(moduleManager);
        folderManager.executeTmpModuleFolderCreation();

        DirectoryArchiver da = new DirectoryArchiver(moduleManager);

        Set<String> folders = moduleManager.getOriginalFolders();

        for(String folder : folders){
            String fileName = EATestsDataProvider.createTestFile(folder);
            Assert.assertTrue(new File(fileName).exists());
        }

        da.executeDirectoryArchiving();

        Map<String, String> archivesAndFolders = moduleManager.getCurrentModelsAndFolders();
        for(Map.Entry<String, String> entry : archivesAndFolders.entrySet()){
            String folderName = entry.getValue();
            String archiveName = entry.getKey();
            Assert.assertTrue(new File(folderName + "/" + archiveName + ".zip").exists());
        }

        folderManager.executeTmpModuleFolderCleanUp();
    }


}
