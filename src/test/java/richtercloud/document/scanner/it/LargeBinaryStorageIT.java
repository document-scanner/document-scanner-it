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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.it.entities.LargeBinaryEntity;
import richtercloud.jhbuild.java.wrapper.ActionOnMissingBinary;
import richtercloud.jhbuild.java.wrapper.ArchitectureNotRecognizedException;
import richtercloud.jhbuild.java.wrapper.BuildFailureException;
import richtercloud.jhbuild.java.wrapper.ExtractionException;
import richtercloud.jhbuild.java.wrapper.JHBuildJavaWrapper;
import richtercloud.jhbuild.java.wrapper.MissingSystemBinary;
import richtercloud.jhbuild.java.wrapper.ModuleBuildFailureException;
import richtercloud.jhbuild.java.wrapper.OSNotRecognizedException;
import richtercloud.jhbuild.java.wrapper.download.AutoDownloader;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.LoggerIssueHandler;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.MemorySequentialIdGenerator;
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

    @Test
    public void testLargeBinaryStorage() throws IOException, StorageConfValidationException,
            StorageCreationException,
            StorageException,
            InterruptedException,
            OSNotRecognizedException,
            ArchitectureNotRecognizedException,
            ExtractionException,
            MissingSystemBinary,
            BuildFailureException,
            ModuleBuildFailureException {
        PersistenceStorage<Long> storage = null;
        Locale.setDefault(Locale.ENGLISH);
        try {
            Set<Class<?>> entityClasses = new HashSet<>(Arrays.asList(LargeBinaryEntity.class));
            File databaseDir = Files.createTempDirectory("document-scanner-large-binary-it").toFile();
            FileUtils.forceDelete(databaseDir);
            File schemeChecksumFile = File.createTempFile("document-scanner-large-binary-it", null);
            schemeChecksumFile.delete();
            String persistenceUnitName = "document-scanner-it";
            String username = "document-scanner";
            String password = "document-scanner";
            String databaseName = "document-scanner";
            //build PostgreSQL
            File postgresqlInstallationPrefixDir = Files.createTempDirectory(LargeBinaryStorageIT.class.getSimpleName()).toFile();
            LOGGER.debug(String.format("using '%s' as PostgreSQL installation prefix",
                    postgresqlInstallationPrefixDir.getAbsolutePath()));
            File downloadDir = Files.createTempDirectory(LargeBinaryStorageIT.class.getSimpleName()).toFile();
                //SystemUtils.getUserHome() causes trouble
                //($HOME/jhbuild/checkout might be jhbuilds default extraction
                //directory)
            LOGGER.debug(String.format("using '%s' as JHBuild Java wrapper download directory",
                    downloadDir));
            IssueHandler issueHandler = new LoggerIssueHandler(LOGGER);
            JHBuildJavaWrapper jHBuildJavaWrapper = new JHBuildJavaWrapper(postgresqlInstallationPrefixDir, //installationPrefixDir
                    downloadDir, //downloadDir
                    ActionOnMissingBinary.DOWNLOAD,
                    ActionOnMissingBinary.DOWNLOAD,
                    new AutoDownloader(), //downloader
                    false,
                    true, //silenceStdout
                    true, //silenceStderr
                    issueHandler);
            String moduleName = "postgresql-9.6.3";
            LOGGER.info(String.format("building module %s from JHBuild Java wrapper's default moduleset",
                    moduleName));
            jHBuildJavaWrapper.installModuleset(moduleName);
                //moduleset shipped with jhbuild-java-wrapper
            String initdb = new File(postgresqlInstallationPrefixDir,
                    String.join(File.separator, "bin", "initdb")).getAbsolutePath();
            String postgres = new File(postgresqlInstallationPrefixDir,
                    String.join(File.separator, "bin", "postgres")).getAbsolutePath();
            String createdb = new File(postgresqlInstallationPrefixDir,
                    String.join(File.separator, "bin", "createdb")).getAbsolutePath();
            PostgresqlAutoPersistenceStorageConf storageConf = new PostgresqlAutoPersistenceStorageConf(entityClasses,
                    "localhost", //hostname
                    username,
                    password,
                    databaseName,
                    schemeChecksumFile,
                    databaseDir.getAbsolutePath(),
                    initdb, //initdbBinaryPath
                    postgres, //postgresBinaryPath
                    createdb //createdbBinaryPath
            );
            FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
            storage = new PostgresqlAutoPersistenceStorage(storageConf,
                    persistenceUnitName,
                    1, //parallelQueryCount
                    fieldRetriever,
                    issueHandler
            );
            storage.start();
            long randomSeed = System.currentTimeMillis();
            LOGGER.debug(String.format("random seed is %d", randomSeed));
            Random random = new Random(randomSeed);
            int entityCount = 20;
            for(int i=0; i<entityCount; i++) {
                int mbSize = random.nextInt(64); //64 MB max.
                    //128 MB cause trouble on Travis CI (crash because of
                    //limited `vm.max_map_count` which causes
                    //`Native memory allocation (mmap) failed to map 109576192 bytes for committing reserved memory.`
                    //) and it's not worth figuring this out for now
                int byteCount = 1024*1024*mbSize;
                LOGGER.debug(String.format("generating %d MB random bytes", mbSize));
                byte[] largeRandomBytes = new byte[byteCount];
                random.nextBytes(largeRandomBytes);
                LargeBinaryEntity entity1 = new LargeBinaryEntity(largeRandomBytes);
                LOGGER.debug(String.format("storing large binary entity (%d of %d)", i, entityCount));
                entity1.setId(MemorySequentialIdGenerator.getInstance().getNextId(entity1));
                storage.store(entity1);
            }
            storage.shutdown();
            Thread.sleep(20000);
                //10000 causes
                //`Caused by: org.postgresql.util.PSQLException: FATAL: the database system is starting up`
            storage = new PostgresqlAutoPersistenceStorage(storageConf,
                    persistenceUnitName,
                    1, //parallelQueryCount
                    fieldRetriever,
                    issueHandler
            );
            storage.start();
            LOGGER.debug("querying large binary entity");
            storage.runQueryAll(LargeBinaryEntity.class);
            LOGGER.debug("query completed");
        }finally {
            if(storage != null) {
                storage.shutdown();
            }
        }
    }
}
