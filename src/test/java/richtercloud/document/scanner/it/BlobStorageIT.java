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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.persistence.EntityManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.it.entities.EntityBlob;
import richtercloud.document.scanner.it.entities.EntityImageWrapper;
import richtercloud.message.handler.LoggerMessageHandler;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.storage.StorageConfValidationException;
import richtercloud.reflection.form.builder.storage.StorageCreationException;
import richtercloud.reflection.form.builder.storage.StorageException;
import richtercloud.validation.tools.FieldRetriever;

/**
 *
 * @author richter
 */
public class BlobStorageIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(BlobStorageIT.class);

    @Test
    @Ignore
        //fails due to
        //```
        //[EL Warning]: 2017-07-31 00:11:40.03--UnitOfWork(178575564)--Exception [EclipseLink-32] (Eclipse Persistence Services - 2.6.4.v20160829-44060b6): org.eclipse.persistence.exceptions.DescriptorException
        //Exception Description: Trying to set value [[B@44550792] for instance variable [data] of type [java.sql.Blob] in the object.  The specified object is not an instance of the class or interface declaring the underlying field, or an unwrapping conversion has failed.
        //Internal Exception: java.lang.IllegalArgumentException: Can not set java.sql.Blob field richtercloud.document.scanner.it.entities.EntityBlob.data to [B
        //Mapping: org.eclipse.persistence.mappings.DirectToFieldMapping[data-->ENTITYBLOB.DATA]
        //Descriptor: RelationalDescriptor(richtercloud.document.scanner.it.entities.EntityBlob --> [DatabaseTable(ENTITYBLOB)])
        //```
    public void testBlobStorage() throws IOException, SQLException, StorageConfValidationException, StorageCreationException, InterruptedException, StorageException {
        PersistenceStorage<Long> storage = null;
        try {
            MessageHandler messageHandler = new LoggerMessageHandler(LOGGER);
            Set<Class<?>> entityClasses = new HashSet<>(Arrays.asList(EntityImageWrapper.class));
            File databaseDir = Files.createTempDirectory("document-scanner-blob-it").toFile();
            FileUtils.forceDelete(databaseDir);
                //databaseDir mustn't exist for MySQL
            File schemeChecksumFile = File.createTempFile("document-scanner-blob-it", null);
            schemeChecksumFile.delete();
            String persistenceUnitName = "document-scanner-it";
            String username = "document-scanner";
            String password = "document-scanner";
            String databaseName = "document-scanner";
            //Testing PostgreSQL doesn't make sense because it doesn't implement
            //java.sql.Connection.createBlob (see persistence.xml in
            //document-scanner)
    //        PostgresqlAutoPersistenceStorageConf storageConf = new PostgresqlAutoPersistenceStorageConf(entityClasses,
    //                username,
    //                schemeChecksumFile,
    //                databaseDir.getAbsolutePath());
            //Apache Derby is extremely slow
    //        DerbyEmbeddedPersistenceStorageConf storageConf = new DerbyEmbeddedPersistenceStorageConf(entityClasses,
    //                databaseName,
    //                schemeChecksumFile);
            File myCnfFile = File.createTempFile("document-scanner-it-blob-it", null);
            myCnfFile.delete(); //need to delete in order to trigger creation of
                //my.cnf
            MySQLAutoPersistenceStorageConf storageConf = new MySQLAutoPersistenceStorageConf(entityClasses,
                    "localhost", //hostname
                    username,
                    databaseName,
                    schemeChecksumFile);
            storageConf.setBaseDir(new File(DocumentScannerConf.CONFIG_DIR_DEFAULT,
                    "mysql-5.7.16-linux-glibc2.5-x86_64").getAbsolutePath());
            storageConf.setDatabaseDir(databaseDir.getAbsolutePath());
            storageConf.setPassword(password);
            storageConf.setDatabaseName(databaseName);
            storageConf.setMyCnfFilePath(myCnfFile.getAbsolutePath());
            FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
            storage = new MySQLAutoPersistenceStorage(storageConf,
                    persistenceUnitName,
                    1, //parallelQueryCount
                    messageHandler,
                    fieldRetriever
            );
            storage.start();
            long randomSeed = System.currentTimeMillis();
            LOGGER.debug(String.format("random seed is %d", randomSeed));
            Random random = new Random(randomSeed);
            int entityCount = 20;
            for(int i=0; i<entityCount; i++) {
                int mbSize = random.nextInt(256); //256 MB max.
                int byteCount = 1024*1024*mbSize;
                LOGGER.debug(String.format("generating %d MB random bytes", mbSize));
                byte[] largeRandomBytes = new byte[byteCount];
                random.nextBytes(largeRandomBytes);
                EntityManager entityManager = storage.retrieveEntityManager();
                entityManager.getTransaction().begin();
                Blob blob = entityManager.unwrap(Connection.class).createBlob();
                OutputStream blobOutputStream = blob.setBinaryStream(1 //pos (begin
                    //at 1)
                );
                ByteArrayInputStream largeRandomBytesInputStream = new ByteArrayInputStream(largeRandomBytes);
                IOUtils.copy(largeRandomBytesInputStream, blobOutputStream);
                EntityBlob entity1 = new EntityBlob(blob);
                LOGGER.debug(String.format("storing large binary entity (%d of %d)", i, entityCount));
                storage.store(entity1);
                entityManager.getTransaction().commit();
            }
            //shutdown and restart storage in order to simulate persistence
            //across application starts
            storage.shutdown();
            Thread.sleep(5000);
                //workaround for remaining storage process after shutdown
            storage = new MySQLAutoPersistenceStorage(storageConf,
                    persistenceUnitName,
                    1, //parallelQueryCount
                    messageHandler,
                    fieldRetriever
            );
            storage.start();
            LOGGER.debug("querying large binary entity");
            List<EntityBlob> queryResults = storage.runQueryAll(EntityBlob.class);
            LOGGER.debug(String.format("query completed with %d results", queryResults.size()));
            int i=0;
            for(EntityBlob queryResult : queryResults) {
                int mbSize = (int) (queryResult.getData().length()/1024/1024);
                LOGGER.debug(String.format("query result %d has length %d bytes",
                        i,
                        mbSize));
                i++;
            }
        }finally {
            if(storage != null) {
                storage.shutdown();
            }
        }
    }
}
