import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by cbudac on 02/01/2016.
 */
@Test
public class FolderManagerTest {
    @Test (dataProvider = "moduleManagerProvider", dataProviderClass = EATestsDataProvider.class)
    public void folderManagerTest(ModelManager moduleManager){
        FolderManager folderManager = new FolderManager(moduleManager);

        for(String folder : moduleManager.getOriginalFolders()) {
            File f = new File(folder);
            Assert.assertFalse(f.exists());
        }

        folderManager.executeTmpModuleFolderCreation();

        for(String folder : moduleManager.getOriginalFolders()) {
            File f = new File(folder);
            Assert.assertTrue(f.exists());
        }

        folderManager.executeTmpModuleFolderCleanUp();

        for(String folder : moduleManager.getOriginalFolders()) {
            File f = new File(folder);
            Assert.assertFalse(f.exists());
        }
    }
}
