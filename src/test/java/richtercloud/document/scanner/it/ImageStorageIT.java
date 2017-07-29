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
package richtercloud.document.scanner.it;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
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
import javafx.embed.swing.JFXPanel;
import javax.cache.Caching;
import javax.swing.ImageIcon;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.DefaultOCRSelectPanel;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCRSelectPanel;
import richtercloud.document.scanner.it.entities.EntityByteArray;
import richtercloud.document.scanner.it.entities.EntityImageIcon;
import richtercloud.document.scanner.it.entities.EntityImageWrapper;
import richtercloud.document.scanner.model.imagewrapper.CachingImageWrapper;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.MemorySequentialIdGenerator;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlAutoPersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlAutoPersistenceStorageConf;
import richtercloud.reflection.form.builder.storage.StorageConfValidationException;
import richtercloud.reflection.form.builder.storage.StorageCreationException;
import richtercloud.reflection.form.builder.storage.StorageException;
import richtercloud.validation.tools.FieldRetriever;

/**
 *
 * @author richter
 */
public class ImageStorageIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(ImageStorageIT.class);

    @Test
    public void testImageStorage() throws IOException, StorageException, SQLException, InterruptedException, StorageConfValidationException, StorageCreationException {
        try {
            new JFXPanel();
                //- necessary in order to avoid
                //`java.lang.IllegalStateException: Toolkit not initialized`
                //- should be run here in order to be catchable at a useful
                //location; figure out how to do this if there's more than this
                //one test
            File databaseDir = Files.createTempDirectory(ImageStorageIT.class.getSimpleName()).toFile();
            FileUtils.forceDelete(databaseDir);
                //databaseDir mustn't exist for Apache Derby
            LOGGER.debug(String.format("database directory is %s", databaseDir.getAbsolutePath()));
            File schemeChecksumFile = File.createTempFile(ImageStorageIT.class.getSimpleName(), "scheme-checksum");
            LOGGER.debug(String.format("scheme checksum file is %s", schemeChecksumFile.getAbsolutePath()));
            File imageStorageDir = Files.createTempDirectory(ImageStorageIT.class.getSimpleName()).toFile();
            LOGGER.debug(String.format("image storage directory is %s", imageStorageDir.getAbsolutePath()));
            Connection connection = DriverManager.getConnection(String.format("jdbc:derby:%s;create=true", databaseDir.getAbsolutePath()));
            connection.close();
            Set<Class<?>> entityClasses = new HashSet<Class<?>>(Arrays.asList(EntityByteArray.class,
                    EntityImageIcon.class));
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
            String persistenceUnitName = "document-scanner-it";
            FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
            final PersistenceStorage<Long> derbyEmbeddedStorage1 = new DerbyEmbeddedPersistenceStorage(storageConf,
                    persistenceUnitName,
                    1, //parallelQueryCount
                    fieldRetriever
            );
            derbyEmbeddedStorage1.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("shutting down storage");
                derbyEmbeddedStorage1.shutdown();
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
            },
                    String.format("%s shutdown hook", ImageStorageIT.class.getSimpleName())
            ));
            List<ImageIcon> imageIcons = new LinkedList<>();
            InputStream pdfInputStream = ImageStorageIT.class.getResourceAsStream("/image_data.pdf");
            assert pdfInputStream != null;
            PDDocument document = PDDocument.load(pdfInputStream);
            @SuppressWarnings("unchecked")
            List<OCRSelectPanel> oCRSelectPanels = new LinkedList<>();
            List<ImageWrapper> imageWrappers = new LinkedList<>();
            byte[] data;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page=0; page<document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                ImageWrapper imageWrapper = new CachingImageWrapper(databaseDir,
                        image);
                OCRSelectPanel oCRSelectPanel = new DefaultOCRSelectPanel(imageWrapper,
                        DocumentScannerConf.PREFERRED_SCAN_RESULT_PANEL_WIDTH_DEFAULT);
                oCRSelectPanels.add(oCRSelectPanel);
                ImageIcon imageIcon = new ImageIcon(image);
                objectOutputStream.writeObject(imageIcon);
                imageIcons.add(imageIcon);
                imageWrappers.add(new CachingImageWrapper(imageStorageDir, image));
            }
            document.close();
            data = outputStream.toByteArray();

            EntityByteArray entityA = new EntityByteArray(1L, data);
            EntityByteArray entityA2 = new EntityByteArray(3L, data);
            EntityImageIcon entityB = new EntityImageIcon(2L, imageIcons);
            EntityImageIcon entityB2 = new EntityImageIcon(4L, imageIcons);
            EntityImageWrapper entityC1 = new EntityImageWrapper(imageWrappers);
            EntityImageWrapper entityC2 = new EntityImageWrapper(imageWrappers);
            long time0 = System.currentTimeMillis();
            entityA.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityA));
            derbyEmbeddedStorage1.store(entityA);
            long time1 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA: %d ms", time1-time0));
            entityB.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityB));
            derbyEmbeddedStorage1.store(entityB);
            long time2 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityB: %d ms", time2-time1));
            //store another time in order to figure out caching effects
            entityA2.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityA2));
            derbyEmbeddedStorage1.store(entityA2);
            long time3 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA2: %d ms", time3-time2));
            entityB2.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityB2));
            derbyEmbeddedStorage1.store(entityB2);
            long time4 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityB2: %d ms", time4-time3));
            entityC1.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityC1));
            derbyEmbeddedStorage1.store(entityC1);
            long time5 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityC1: %d ms", time5-time4));
            entityC2.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityC2));
            derbyEmbeddedStorage1.store(entityC2);
            long time6 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityC2: %d ms", time6-time5));
            LOGGER.info(String.format("size of entityA's data: %d KiB", entityA.getData().length/1024));

            long randomSeed = System.currentTimeMillis();
            LOGGER.info(String.format("random seed is: %d", randomSeed));
            Random random = new Random(randomSeed);
            byte[] referenceBytes = new byte[data.length];
            random.nextBytes(referenceBytes);
            EntityByteArray entityA3 = new EntityByteArray(5L, referenceBytes);
            EntityByteArray entityA4 = new EntityByteArray(6L, referenceBytes);
            long time7 = System.currentTimeMillis();
            entityA3.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityA3));
            derbyEmbeddedStorage1.store(entityA3);
            long time8 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA3: %d ms", time8-time7));
            entityA4.setId(MemorySequentialIdGenerator.getInstance().getNextId(entityA4));
            derbyEmbeddedStorage1.store(entityA4);
            long time9 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA4: %d ms", time9-time8));
            derbyEmbeddedStorage1.shutdown();

            //test whether EntityImagerWrapper is deserializable
            PersistenceStorage<Long> derbyEmbeddedStorage2 = new DerbyEmbeddedPersistenceStorage(storageConf,
                    persistenceUnitName,
                    1, //parallelQueryCount
                    fieldRetriever
            );
            derbyEmbeddedStorage2.start();
            List<EntityImageWrapper> queryResults = derbyEmbeddedStorage2.runQueryAll(EntityImageWrapper.class);
            assert queryResults.size() == 2;
            EntityImageWrapper queryResult0 = queryResults.get(0);
            List<ImageWrapper> queryResult0Data = queryResult0.getData();
            for(ImageWrapper queryResult0Datum : queryResult0Data) {
                LOGGER.info(String.format("inspect image wrapper file %s ms", queryResult0Datum.getStorageFile()));
            }
            derbyEmbeddedStorage2.shutdown();

            //test PostgreSQL
            File databaseDirPostgresql = Files.createTempDirectory(ImageStorageIT.class.getSimpleName()).toFile();
            FileUtils.forceDelete(databaseDirPostgresql);
                //an inexisting database directory triggers creation of database
                //with initdb
            LOGGER.debug(String.format("PostgreSQL database directory is %s", databaseDirPostgresql.getAbsolutePath()));
            String initdb = "/usr/lib/postgresql/9.6/bin/initdb";
            String postgres = "/usr/lib/postgresql/9.6/bin/postgres";
            String createdb = "createdb";
            String databaseName = "image-storage-it";
            String username = "docu";
            String password = "docu";
            PostgresqlAutoPersistenceStorageConf postgresqlPersistenceStorageConf = new PostgresqlAutoPersistenceStorageConf(entityClasses,
                    "postgres", //username
                    password,
                    databaseName,
                    schemeChecksumFile,
                    databaseDirPostgresql.getAbsolutePath(), //databaseDir
                    initdb,
                    postgres,
                    createdb);
            PersistenceStorage<Long> postgresqlStorage = new PostgresqlAutoPersistenceStorage(postgresqlPersistenceStorageConf,
                    persistenceUnitName,
                    1, //parallelQueryCount
                    fieldRetriever
            );
            postgresqlStorage.start();
            time0 = System.currentTimeMillis();
            postgresqlStorage.store(entityA);
            time1 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA: %d ms", time1-time0));
            postgresqlStorage.store(entityB);
            time2 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityB: %d ms", time2-time1));
            //store another time in order to figure out caching effects
            postgresqlStorage.store(entityA2);
            time3 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA2: %d ms", time3-time2));
            postgresqlStorage.store(entityB2);
            time4 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityB2: %d ms", time4-time3));
            LOGGER.info(String.format("size of entityA's data: %d KiB", entityA.getData().length/1024));

            time9 = System.currentTimeMillis();
            postgresqlStorage.store(entityA3);
            time8 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA3: %d ms", time8-time9));
            postgresqlStorage.store(entityA4);
            time9 = System.currentTimeMillis();
            LOGGER.info(String.format("time for storing entityA4: %d ms", time9-time8));
            postgresqlStorage.shutdown();
            Caching.getCachingProvider().close();
        }catch(UnsupportedOperationException ex) {
            //`new JFXPanel()` for JavaFX toolkit initialization causes
            //`java.lang.UnsupportedOperationException: Unable to open DISPLAY`
            //instead of HeadlessException (which is a subclass of
            //UnsupportedOperationException
            LOGGER.warn("UnsupportedOperationException indicates that the test is run on a headless machine, e.g. a CI service",
                    ex);
        }
    }
}
