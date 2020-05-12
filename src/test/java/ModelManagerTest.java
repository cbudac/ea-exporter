import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by cbudac on 02/01/2016.
 */
@Test
public class ModelManagerTest {
    @Test (dataProvider = "propertiesProvider", dataProviderClass = EATestsDataProvider.class)
    public void modelManagerTest(Properties properties){
        ModelManager modelManager = new ModelManager(properties);
        Assert.assertEquals(2, modelManager.getOriginalFolders().size());
        Assert.assertEquals(2, modelManager.getCurrentGuidAndFolders().size());
        Assert.assertEquals(2, modelManager.getCurrentModelsAndFolders().size());

        Assert.assertTrue(modelManager.getOriginalFolders().contains(properties.get("local_folder") + "/TestModel1"));
        Assert.assertTrue(modelManager.getOriginalFolders().contains(properties.get("local_folder") + "/TestModel2"));

        Assert.assertEquals("TestModel1", modelManager.getModelNameByModelGuid("{guid1}"));
        Assert.assertEquals("TestModel2", modelManager.getModelNameByModelGuid("{guid2}"));
    }

    @Test (dataProvider = "propertiesProvider", dataProviderClass = EATestsDataProvider.class)
    public void testFailedModels (Properties properties){
        ModelManager modelManager = new ModelManager(properties);

        String failedModelName = "TestModel1";
        String failedFolderName = modelManager.getCurrentModelsAndFolders().get(failedModelName);
        String failedModelGuid = modelManager.getModelGuidByModelName(failedModelName);

        Assert.assertEquals(failedModelGuid, "{guid1}");

        Set<String> failedModels = new HashSet<String>();
        failedModels.add(failedModelName);

        modelManager.excludeFailedModels(failedModels);

        Assert.assertFalse(modelManager.getCurrentModelsAndFolders().containsKey(failedModelName));
        Assert.assertTrue(modelManager.getCurrentModelsAndFolders().containsKey("TestModel2"));

        Assert.assertFalse(modelManager.getCurrentGuidAndFolders().containsKey(failedModelGuid));
        Assert.assertTrue(modelManager.getCurrentGuidAndFolders().containsKey("{guid2}"));

        Assert.assertTrue(modelManager.getCurrentFolders().size() == modelManager.getOriginalFolders().size()-1);
    }
}
