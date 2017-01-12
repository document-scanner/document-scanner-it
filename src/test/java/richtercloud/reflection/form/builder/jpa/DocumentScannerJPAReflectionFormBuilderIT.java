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
package richtercloud.reflection.form.builder.jpa;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.jscience.economics.money.Currency;
import org.jscience.physics.amount.Amount;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Document;
import richtercloud.document.scanner.model.FinanceAccount;
import richtercloud.document.scanner.model.Location;
import richtercloud.document.scanner.model.Payment;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.LoggerMessageHandler;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateEvent;
import richtercloud.reflection.form.builder.fieldhandler.MappedFieldUpdateEvent;
import richtercloud.reflection.form.builder.jpa.idapplier.GeneratedValueIdApplier;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.storage.StorageConfValidationException;
import richtercloud.reflection.form.builder.storage.StorageCreationException;
import richtercloud.reflection.form.builder.storage.StorageException;

/**
 * Does what {@code JPAReflectionFormBuilderIT} in
 * {@code reflection-form-builder-it} does, but with entities of
 * {@code document-scanner} which caused trouble.
 *
 * @author richter
 */
public class DocumentScannerJPAReflectionFormBuilderIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentScannerJPAReflectionFormBuilderIT.class);

    @Test
    public void testOnFieldUpdate() throws IOException, StorageCreationException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, StorageConfValidationException, StorageException, SQLException, InvocationTargetException {
        Set<Class<?>> entityClasses = new HashSet<>(Arrays.asList(Document.class,
                Payment.class));
        File databaseDir = File.createTempFile(DocumentScannerJPAReflectionFormBuilderIT.class.getSimpleName(), null);
        FileUtils.forceDelete(databaseDir);
        //databaseDir mustn't exist for Apache Derby
        String databaseName = databaseDir.getAbsolutePath();
        LOGGER.debug(String.format("database directory: %s", databaseName));
        Connection connection = DriverManager.getConnection(String.format("jdbc:derby:%s;create=true", databaseDir.getAbsolutePath()));
        connection.close();
        File schemeChecksumFile = File.createTempFile(DocumentScannerJPAReflectionFormBuilderIT.class.getSimpleName(), null);
        DerbyEmbeddedPersistenceStorageConf storageConf = new DerbyEmbeddedPersistenceStorageConf(entityClasses, databaseName, schemeChecksumFile);
        String persistenceUnitName = "document-scanner-it";
        JPAFieldRetriever fieldRetriever = new JPACachedFieldRetriever();
        PersistenceStorage storage = new DerbyEmbeddedPersistenceStorage(storageConf,
                persistenceUnitName,
                1, //parallelQueryCount
                fieldRetriever);
        storage.start();
        MessageHandler messageHandler = new LoggerMessageHandler(LOGGER);
        ConfirmMessageHandler confirmMessageHandler = new ConfirmMessageHandler() {
            @Override
            public int confirm(Message message) {
                return JOptionPane.YES_OPTION;//confirm everything
            }

            @Override
            public String confirm(Message message, String... options) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        IdApplier idApplier = new GeneratedValueIdApplier();
        IdGenerator idGenerator = MemorySequentialIdGenerator.getInstance();
        JPAReflectionFormBuilder instance = new JPAReflectionFormBuilder(storage,
                "dialog title",
                messageHandler,
                confirmMessageHandler,
                fieldRetriever,
                idApplier,
                idGenerator,
                new HashMap<>() //warningHandlers
        );

        //entity setup
        Location location = new Location("description");
        Company sender = new Company("name",
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>());
        Company recipient = new Company("name",
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>());
        FinanceAccount senderAccount = new FinanceAccount("1",
                "2",
                "3",
                "4",
                new LinkedList<>(),
                sender);
        FinanceAccount recipientAccount = new FinanceAccount("5",
                "6",
                "7",
                "8",
                new LinkedList<>(),
                recipient);

        //without mappedBy
        //test many-to-many
        Document entityA1 = new Document("comment",
                "identifier",
                new Date(),
                new Date(),
                location,
                false,
                false,
                sender,
                recipient);
        Payment entityB1 = new Payment(Amount.valueOf(0, Currency.EUR),
                new Date(),
                senderAccount,
                recipientAccount);
        Payment entityB2 = new Payment(Amount.valueOf(0, Currency.EUR),
                new Date(),
                senderAccount,
                recipientAccount);
        storage.store(location);
        storage.store(sender);
        storage.store(recipient);
        storage.store(senderAccount);
        storage.store(recipientAccount);
        storage.store(entityB1);
        storage.store(entityB2);
        FieldUpdateEvent event = new MappedFieldUpdateEvent(new LinkedList(Arrays.asList(entityB1, entityB2)),
                Payment.class.getDeclaredField("documents") //mappedField
        );
            //only MappedFieldUpdateEvents are interesting
        Field field = Document.class.getDeclaredField("payments");
        instance.onFieldUpdate(event, field, entityA1);
        storage.store(entityA1); //only store entityA1
        Document entityA1Stored = storage.retrieve(entityA1.getId(),
                Document.class);
        Payment entityB1Stored = storage.retrieve(entityB1.getId(),
                Payment.class);
        Payment entityB2Stored = storage.retrieve(entityB2.getId(),
                Payment.class);
        assertTrue(entityA1Stored.getPayments().contains(entityB1));
        assertTrue(entityA1Stored.getPayments().contains(entityB2));
        assertTrue(entityB1Stored.getDocuments().contains(entityA1));
        assertTrue(entityB2Stored.getDocuments().contains(entityA1));
    }
}
