/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.components;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import javax.cache.Caching;
import javax.swing.ImageIcon;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import static org.mockito.Mockito.mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.CachingImageWrapper;
import richtercloud.document.scanner.gui.DefaultOCRSelectPanel;
import richtercloud.document.scanner.gui.DefaultOCRSelectPanelPanel;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCRSelectPanel;
import richtercloud.document.scanner.ocr.OCREngineFactory;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlPersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlPersistenceStorageConf;
import richtercloud.reflection.form.builder.storage.StorageException;

/**
 *
 * @author richter
 */
public class ImageStorageIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(ImageStorageIT.class);

    public static void main(String[] args) throws IOException, StorageException, SQLException, InterruptedException {
        File databaseDir = new File("/tmp/image-storage-it");
        File schemeChecksumFile = new File("/tmp/image-storage-it-checkum-file");
        File imageStorageDir = new File("/tmp/image-storage-dir");
        Connection connection = DriverManager.getConnection(String.format("jdbc:derby:%s;create=true", databaseDir.getAbsolutePath()));
        connection.close();
        Set<Class<?>> entityClasses = new HashSet<Class<?>>(Arrays.asList(EntityA.class,
                EntityB.class));
//        DerbyNetworkPersistenceStorageConf storageConf = new DerbyNetworkPersistenceStorageConf(entityClasses,
//                "localhost",
//                schemeChecksumFile);
//        storageConf.setDatabaseDir(databaseDir);
//        storageConf.setPassword("sa");
//        PersistenceStorage storage = new DerbyNetworkPersistenceStorage(storageConf,
//                "richtercloud_document-scanner-it_jar_1.0-SNAPSHOTPU");
        DerbyEmbeddedPersistenceStorageConf storageConf = new DerbyEmbeddedPersistenceStorageConf(entityClasses,
                databaseDir.getAbsolutePath(),
                schemeChecksumFile);
        String persistenceUnitName = "richtercloud_document-scanner-it_jar_1.0-SNAPSHOTPU";
        final PersistenceStorage storage = new DerbyEmbeddedPersistenceStorage(storageConf, persistenceUnitName);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("shutting down storage");
            storage.shutdown();
            LOGGER.info("storage shut down");
            try {
                FileUtils.deleteDirectory(databaseDir);
                LOGGER.info(String.format("database directory '%s' deleted", databaseDir.getAbsolutePath()));
            } catch (IOException ex) {
                LOGGER.info(String.format("deletion of database directory '%s' failed, see nested exception for details", databaseDir.getAbsolutePath()),
                         ex);
            }
            try {
                FileUtils.deleteDirectory(imageStorageDir);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ImageStorageIT.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));
        File imageInputFile = new File(ImageStorageIT.class.getResource("/image_data.pdf").getFile());
        List<ImageIcon> imageIcons = new LinkedList<>();
        InputStream pdfInputStream = new FileInputStream(imageInputFile);
        PDDocument document = PDDocument.load(pdfInputStream);
        @SuppressWarnings("unchecked")
        List<PDPage> pages = document.getDocumentCatalog().getAllPages();
        List<OCRSelectPanel> oCRSelectPanels = new LinkedList<>();
        for (PDPage page : pages) {
            BufferedImage image = page.convertToImage();
            ImageWrapper imageWrapper = new CachingImageWrapper(databaseDir,
                    image);
            OCRSelectPanel oCRSelectPanel = new DefaultOCRSelectPanel(imageWrapper,
                    DocumentScannerConf.PREFERRED_WIDTH_DEFAULT);
            oCRSelectPanels.add(oCRSelectPanel);
            imageIcons.add(new ImageIcon(image));
        }
        document.close();
        //Mocking parts which are not functional for the test
        File documentFile = mock(File.class);
        OCREngineFactory oCREngineFactory = mock(OCREngineFactory.class);
        OCREngineConf oCREngineConf = mock(OCREngineConf.class);
        DocumentScannerConf documentScannerConf = mock(DocumentScannerConf.class);
        MainPanelScanResultPanelFetcher fetcher = new MainPanelScanResultPanelFetcher(new DefaultOCRSelectPanelPanel(oCRSelectPanels,
                documentFile,
                oCREngineFactory,
                oCREngineConf,
                documentScannerConf));
        byte[] data = fetcher.fetch();
        EntityA entityA = new EntityA(1L, data);
        EntityA entityA2 = new EntityA(3L, data);
        EntityB entityB = new EntityB(2L, imageIcons);
        EntityB entityB2 = new EntityB(4L, imageIcons);
        long time0 = System.currentTimeMillis();
        storage.store(entityA);
        long time1 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA: %d", time1-time0));
        storage.store(entityB);
        long time2 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityB: %d", time2-time1));
        //store another time in order to figure out caching effects
        storage.store(entityA2);
        long time3 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA2: %d", time3-time2));
        storage.store(entityB2);
        long time4 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityB2: %d", time4-time3));
        LOGGER.info(String.format("size of entityA's data: %d KiB", entityA.getData().length/1024));

        long randomSeed = System.currentTimeMillis();
        LOGGER.info(String.format("random seed is: %d", randomSeed));
        Random random = new Random(randomSeed);
        byte[] referenceBytes = new byte[data.length];
        random.nextBytes(referenceBytes);
        EntityA entityA3 = new EntityA(5L, referenceBytes);
        EntityA entityA4 = new EntityA(6L, referenceBytes);
        long time5 = System.currentTimeMillis();
        storage.store(entityA3);
        long time6 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA3: %d", time6-time5));
        storage.store(entityA4);
        long time7 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA4: %d", time7-time6));
        storage.shutdown();

        //test PostgreSQL
        File databaseDirPostgresql = new File("/tmp/image-storage-it-postgresql");
        String initdb = "/usr/lib/postgresql/9.5/bin/initdb";
        String postgres = "/usr/lib/postgresql/9.5/bin/postgres";
        String createdb = "createdb";
        String databaseName = "image-storage-it";
        String username = "docu";
        String password = "docu";
        File passwordFile = File.createTempFile("image-storage-it-postgres", "suffix");
        Files.write(Paths.get(passwordFile.getAbsolutePath()), password.getBytes(), StandardOpenOption.WRITE);
        ProcessBuilder initdbProcessBuilder = new ProcessBuilder(initdb, String.format("--username=%s", username),
                String.format("--pwfile=%s", passwordFile.getAbsolutePath()),
                databaseDirPostgresql.getAbsolutePath());
        Process initdbProcess = initdbProcessBuilder.start();
        initdbProcess.waitFor();
        //fix `FATAL:  could not create lock file "/var/run/postgresql/.s.PGSQL.5432.lock": Keine Berechtigung`
        File postgresqlConfFile = new File(databaseDirPostgresql, "postgresql.conf");
        try {
            Files.write(Paths.get(postgresqlConfFile.getAbsolutePath()), "\nunix_socket_directories = '/tmp'\n".getBytes(), StandardOpenOption.APPEND);
        }catch (IOException ex) {
            LOGGER.error(String.format("unexpected exception during writing to PostgreSQL configuration file '%s', see nested exception for details", postgresqlConfFile.getAbsolutePath()), ex);
        }
        IOUtils.copy(initdbProcess.getInputStream(), System.out);
        IOUtils.copy(initdbProcess.getErrorStream(), System.err);
        ProcessBuilder postgresProcessBuilder = new ProcessBuilder(postgres,
                "-D", databaseDirPostgresql.getAbsolutePath(),
                "-h", "localhost",
                "-p", "5432");
        Process postgresProcess = postgresProcessBuilder.start();
        Thread postgresThread = new Thread(() -> {
            try {
                postgresProcess.waitFor();
                IOUtils.copy(postgresProcess.getInputStream(), System.out);
                IOUtils.copy(postgresProcess.getErrorStream(), System.err);
            } catch (InterruptedException | IOException ex) {
                LOGGER.error("unexpected exception, see nested exception for details", ex);
            }
        });
        postgresThread.start();
        boolean success = false;
        while(!success) {
            LOGGER.info("Running createdb");
            ProcessBuilder createdbProcessBuilder = new ProcessBuilder(createdb,
                    String.format("--host=%s", "localhost"),
                    String.format("--username=%s", username),
                    databaseName);
            Process createdbProcess = createdbProcessBuilder.start();
            createdbProcess.waitFor();
            IOUtils.copy(createdbProcess.getInputStream(), System.out);
            IOUtils.copy(createdbProcess.getErrorStream(), System.err);
            if(createdbProcess.exitValue() == 0) {
                LOGGER.info("createdb succeeded");
                success = true;
            }else {
                LOGGER.info("createdb failed (server might not be up yet, trying again in 1 s");
                Thread.sleep(1000);
            }
        }
        PostgresqlPersistenceStorageConf postgresqlPersistenceStorageConf = new PostgresqlPersistenceStorageConf(entityClasses,
                "postgres",
                schemeChecksumFile);
        postgresqlPersistenceStorageConf.setDatabaseName(databaseName);
        postgresqlPersistenceStorageConf.setUsername(username);
        postgresqlPersistenceStorageConf.setPassword(password);
        PersistenceStorage postgresqlStorage = new PostgresqlPersistenceStorage(postgresqlPersistenceStorageConf,
                persistenceUnitName);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            postgresProcess.destroy();
            try {
                postgresProcess.waitFor();
            } catch (InterruptedException ex) {
                LOGGER.error("waiting for termination of postgres process failed, see nested exception for details", ex);
            }
            try {
                postgresThread.join();
                //should handle writing to stdout and stderr
            } catch (InterruptedException ex) {
                LOGGER.error("unexpected exception, see nested exception for details", ex);
            }
            try {
                FileUtils.deleteDirectory(databaseDirPostgresql);
                LOGGER.info(String.format("database directory '%s' deleted", databaseDirPostgresql.getAbsolutePath()));
            } catch (IOException ex) {
                LOGGER.info(String.format("deletion of database directory '%s' failed, see nested exception for details", databaseDirPostgresql.getAbsolutePath()),
                         ex);
            }
        }));
        time0 = System.currentTimeMillis();
        postgresqlStorage.store(entityA);
        time1 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA: %d", time1-time0));
        postgresqlStorage.store(entityB);
        time2 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityB: %d", time2-time1));
        //store another time in order to figure out caching effects
        postgresqlStorage.store(entityA2);
        time3 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA2: %d", time3-time2));
        postgresqlStorage.store(entityB2);
        time4 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityB2: %d", time4-time3));
        LOGGER.info(String.format("size of entityA's data: %d KiB", entityA.getData().length/1024));

        time5 = System.currentTimeMillis();
        postgresqlStorage.store(entityA3);
        time6 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA3: %d", time6-time5));
        postgresqlStorage.store(entityA4);
        time7 = System.currentTimeMillis();
        LOGGER.info(String.format("time for storing entityA4: %d", time7-time6));
        postgresqlStorage.shutdown();
        Caching.getCachingProvider().close();
    }
}
