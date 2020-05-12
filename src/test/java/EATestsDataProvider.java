import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Create
 * d by cbudac on 02/01/2016.
 */

public class EATestsDataProvider {

    @DataProvider (name = "propertiesProvider")
    public static Object[][] getProperties(){
        Properties properties = new Properties();
        properties.put("port_number", "22");
        properties.put("user_name", "deployment");
        properties.put("password", "univeris");
        properties.put("host_name", "repo.univeris.com");
        properties.put("exec_protocol","exec");
        properties.put("ftp_protocol","sftp");
        properties.put("local_folder", "C:\\EA Published Models");
        properties.put("remote_folder", "sites/atom");
        properties.put("model_ids", "Test Model 1:{guid1},Test Model 2:{guid2}");
        properties.put("ea_dll_path","C:\\EAExporter\\lib");
        return new Object[][] {
                new Object[] {properties }
        };
    }

    @DataProvider (name = "moduleManagerProvider")
    public static Object[][] getModuleManager(){
        ModelManager modelManager = new ModelManager((Properties)getProperties()[0][0]);
        return new Object[][] {
                new Object[] {modelManager }
        };
    }

    @DataProvider (name = "propertiesAndModuleManagerProvider")
    public static Object[][] getPropertiesAndModuleManager(){
        Properties properties = (Properties)getProperties()[0][0];
        ModelManager modelManager = new ModelManager(properties);
        return new Object[][] {
                new Object[] {properties, modelManager }
        };
    }

    public static String createTestFile(String filePath) throws IOException {
        String fileName = filePath + "/testFile.txt";
        File file = new File(fileName);
        file.createNewFile();
        return fileName;
    }
}
