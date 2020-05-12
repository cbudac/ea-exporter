import com.jcraft.jsch.*;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by cbudac on 01/01/2016.
 */
public class SFTManager {
    // FTP Server
    private static final String HOST_NAME = "host_name";
    private static final String PORT_NUMBER = "port_number";
    private static final String FTP_PROTOCOL = "ftp_protocol";
    private static final String EXEC_PROTOCOL = "exec_protocol";
    private static final String REMOTE_FOLDER = "remote_folder";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";

    private static final String DEL_REMOTE_ARCHIVE_CMD = "rm \"{0}/{1}\"";
    private static final String DEL_REMOTE_FOLDER_CMD = "rm -rf \"{0}/{1}\"";
    private static final String UNZIP_REMOTE_ARCHIVE_CMD = "unzip -qq \"{0}/{1}\" -d \"{2}/{3}\"";
    private static final String FILE_EXISTS_CMD = "if test -f \"{0}/{1}\"; then echo 0; else echo 1; fi";
    private static final String DIR_EXISTS_CMD = "if test -d \"{0}/{1}\"; then echo 0; else echo 1; fi";

    private static final Logger log = LogManager.getLogger(SFTManager.class.getSimpleName());

    private String hostName;
    private int portNumber;
    private String userName;
    private String password;
    private String transferProtocol;
    private String remoteFolder;
    private String execProtocol;

    private ModelManager moduleManager;


    public SFTManager(Properties properties, ModelManager moduleManager){
        this.hostName = properties.getProperty(HOST_NAME);
        this.portNumber = Integer.parseInt(properties.getProperty(PORT_NUMBER));
        this.userName = properties.getProperty(USER_NAME);
        this.password = properties.getProperty(PASSWORD);
        this.transferProtocol = properties.getProperty(FTP_PROTOCOL);
        this.remoteFolder = properties.getProperty(REMOTE_FOLDER);
        this.execProtocol = properties.getProperty(EXEC_PROTOCOL);

        this.moduleManager = moduleManager;
    }

    public void executeFileTransfers() {
        long startTime = System.currentTimeMillis();
        log.info("The modules SFTP upload has started");
        try {
            Session session = this.openSession();
            Set<String> failedModuleNames = new HashSet<String>();
            Map<String, String> modulesAndFolders = this.moduleManager.getCurrentModelsAndFolders();
            for (Map.Entry<String, String> entry : modulesAndFolders.entrySet()){
                String moduleName = entry.getKey();
                String folderName = entry.getValue();
                String archiveName = moduleName + ".zip";
                try {
                    if(this.fileExists(session, archiveName)){
                        this.deleteRemoteArchive(session, archiveName);
                    }
                    this.uploadArchive(session, folderName, archiveName);

                    if(this.dirExists(session, moduleName)){
                        this.deleteRemoteFolder(session, moduleName);
                    }

                    this.unzipRemoteArchive(session, archiveName, moduleName);
                    this.deleteRemoteArchive(session, archiveName);
                }
                catch(Exception e){
                    failedModuleNames.add(moduleName);
                    log.debug("The SFTP upload of module {} has failed with exception:", moduleName, e);
                }
            }
            this.moduleManager.excludeFailedModels(failedModuleNames);
            this.closeSession(session);
        }
        catch (Exception e) {
            // an exception at this level completely fails the export
            throw new RuntimeException(e);
        }

        long endTime = System.currentTimeMillis();
        log.info("The modules SFTP upload has ended in {} milliseconds", endTime - startTime);
    }

    /**
     * Uploads the a given file from a given folder to the remote folder.
     * @param session
     * @param folderName
     * @param fileName
     * @throws JSchException
     * @throws SftpException
     */
    void uploadArchive(Session session, String folderName, String fileName) throws JSchException, SftpException {
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel(this.transferProtocol);
        sftpChannel.connect();

        sftpChannel.cd(this.remoteFolder);
        sftpChannel.put(folderName + "/" + fileName, fileName);

        sftpChannel.exit();
    }

    void deleteRemoteFolder(Session session, String fileName)throws IOException, JSchException {
        String command = MessageFormat.format(DEL_REMOTE_FOLDER_CMD, this.remoteFolder, fileName);
        log.info("Executing command: {}", command);
        CommandResponse response = this.executeCommand(session, command);
        if(response.getExitStatus() != 0){
            throw new RuntimeException("Unzip command ("+ command +") failed with exit status: " + response.getExitStatus() + " and message: " + response.getMessage());
        }
    }

    void unzipRemoteArchive(Session session, String fileName, String moduleName) throws IOException, JSchException {
        String command = MessageFormat.format(UNZIP_REMOTE_ARCHIVE_CMD, this.remoteFolder, fileName, this.remoteFolder, moduleName);
        log.info("Executing command: {}", command);
        CommandResponse response = this.executeCommand(session, command);
        if(response.getExitStatus() != 0){
            throw new RuntimeException("Unzip command (" + command + ") failed with exit status: " + response.getExitStatus() + " and message: " + response.getMessage());
        }
    }
    void deleteRemoteArchive(Session session, String fileName )throws IOException, JSchException {
        String command = MessageFormat.format(DEL_REMOTE_ARCHIVE_CMD, this.remoteFolder, fileName);
        log.info("Executing command: {}", command);
        CommandResponse response = this.executeCommand(session, command);
        if(response.getExitStatus() != 0){
            throw new RuntimeException("Delete command ("+ command + ") failed with exit status: " + response.getExitStatus() + " and message: " + response.getMessage());
        }
    }

    boolean fileExists(Session session, String fileName) throws IOException, JSchException {
        String command = MessageFormat.format(FILE_EXISTS_CMD, this.remoteFolder, fileName);
        log.info("Executing command: {}", command);
        CommandResponse response = this.executeCommand(session, command);
        if(response.getExitStatus() != 0){
            throw new RuntimeException("File existence checking command ("+ command + ") failed with exit status: " + response.getExitStatus() + " and message: " + response.getMessage());
        }
        else{
            return response.getMessage().substring(0,1).equals("0") ? true : false;
        }
    }

    boolean dirExists(Session session, String fileName) throws IOException, JSchException {
        String command = MessageFormat.format(DIR_EXISTS_CMD, this.remoteFolder, fileName);
        log.info("Executing command: {}", command);
        CommandResponse response = this.executeCommand(session, command);
        if(response.getExitStatus() != 0){
            throw new RuntimeException("Directory existence checking command ("+ command + ") failed with exit status: " + response.getExitStatus() + " and message: " + response.getMessage());
        }
        else{
            return response.getMessage().substring(0,1).equals("0") ? true : false;
        }
    }

    CommandResponse executeCommand (Session session, String command) throws JSchException, IOException {
        int exitStatus = 0;
        StringWriter errorWriter = new StringWriter();
        WriterOutputStream errorStream = new WriterOutputStream(new BufferedWriter(errorWriter));

        StringBuilder description = new StringBuilder();
        ChannelExec channel = (ChannelExec)session.openChannel(this.execProtocol);
        channel.setCommand(command);

        channel.setInputStream(null);
        channel.setErrStream(errorStream);

        channel.connect();
        InputStream in = channel.getInputStream();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                description.append(new String(tmp, 0, i, Charset.defaultCharset()));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                exitStatus = channel.getExitStatus();
                break;
            }
            try {
                Thread.sleep(500);
            }
            catch (Exception ee) {
            }
        }
        channel.disconnect();

        description.append(errorWriter.toString());
        return new CommandResponse(exitStatus, description.toString());
    }

    Session openSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(this.userName, this.hostName, this.portNumber);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(this.password);
        session.connect();
        return session;
    }

    void closeSession(Session session){
        session.disconnect();
    }

    public static class CommandResponse{
        int exitStatus = 0;
        String message;
        private CommandResponse(int exitStatus, String message){
            this.exitStatus = exitStatus;
            this.message = message;
        }
        int getExitStatus(){
            return exitStatus;
        }
        String getMessage(){
            return message;
        }
    }
}

