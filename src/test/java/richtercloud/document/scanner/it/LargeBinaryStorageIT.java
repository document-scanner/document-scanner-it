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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.it.entities.LargeBinaryEntity;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlAutoPersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlAutoPersistenceStorageConf;
import richtercloud.reflection.form.builder.storage.StorageConfValidationException;
import richtercloud.reflection.form.builder.storage.StorageCreationException;
import richtercloud.reflection.form.builder.storage.StorageException;
import richtercloud.validation.tools.FieldRetriever;

/**
 * Shows that freeing of memory after {@link PersistenceStorage#shutdown() }
 * works well.
 *
 * @author richter
 */
public class LargeBinaryStorageIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(LargeBinaryStorageIT.class);

    public static void main(String[] args) throws IOException, StorageConfValidationException, StorageCreationException, StorageException, InterruptedException {
        Set<Class<?>> entityClasses = new HashSet<>(Arrays.asList(LargeBinaryEntity.class));
        File databaseDir = File.createTempFile("document-scanner-large-binary-it", null);
        databaseDir.delete();
        File schemeChecksumFile = File.createTempFile("document-scanner-large-binary-it", null);
        schemeChecksumFile.delete();
        String persistenceUnitName = "document-scanner-it";
        String username = "document-scanner";
        String password = "document-scanner";
        String databaseName = "document-scanner";
        Pair<String, String> bestPostgresqlBaseDir = PostgresqlAutoPersistenceStorageConf.findBestInitialPostgresqlBasePath();
            //@TODO: add discovery for other OS and allow specification as system property
        if(bestPostgresqlBaseDir == null) {
            throw new IllegalArgumentException("no PostgreSQL initdb binary could be found (currently only Debian-based systems with PostgreSQL binaries in /usr/lib/postgresql/[version] are supported.");
        }
        PostgresqlAutoPersistenceStorageConf storageConf = new PostgresqlAutoPersistenceStorageConf(entityClasses,
                username,
                schemeChecksumFile,
                databaseDir.getAbsolutePath(),
                bestPostgresqlBaseDir.getKey(),
                bestPostgresqlBaseDir.getValue());
        storageConf.setPassword(password);
        storageConf.setDatabaseName(databaseName);
        FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
        PersistenceStorage storage = new PostgresqlAutoPersistenceStorage(storageConf,
                persistenceUnitName,
                1, //parallelQueryCount
                fieldRetriever
        );
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
            LargeBinaryEntity entity1 = new LargeBinaryEntity(largeRandomBytes);
            LOGGER.debug(String.format("storing large binary entity (%d of %d)", i, entityCount));
            storage.store(entity1);
        }
        storage.shutdown();
        Thread.sleep(2000);
        storage = new PostgresqlAutoPersistenceStorage(storageConf,
                persistenceUnitName,
                1, //parallelQueryCount
                fieldRetriever
        );
        LOGGER.debug("querying large binary entity");
        storage.runQueryAll(LargeBinaryEntity.class);
        LOGGER.debug("query completed");
        storage.shutdown();
    }
}
