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

import java.awt.EventQueue;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.TypedQuery;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import static org.mockito.Mockito.*;
import richtercloud.document.scanner.components.annotations.CommunicationTree;
import richtercloud.document.scanner.components.annotations.OCRResult;
import richtercloud.document.scanner.components.annotations.ScanResult;
import richtercloud.document.scanner.components.annotations.Tags;
import richtercloud.document.scanner.components.tag.TagStorage;
import richtercloud.document.scanner.gui.DefaultMainPanel;
import richtercloud.document.scanner.gui.DocumentScannerFieldHandler;
import richtercloud.reflection.form.builder.jpa.storage.NoOpFieldInitializer;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.MainPanel;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.model.WorkflowItem;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandlingException;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.idapplier.GeneratedValueIdApplier;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.storage.FieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.typehandler.TypeHandler;

/**
 * Integration test for resizability of class components.
 * @author richter
 */
public class AutoOCRValueDetectionReflectionFormBuilderIT {

    /**
     * Test of getComboBoxModelMap method, of class AutoOCRValueDetectionReflectionFormBuilder.
     */
    public static void testResizability() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, FieldHandlingException, InterruptedException {
        //There's no mocking in integration tests, but for the GUI test it's
        //fine.
        Set<Class<?>> testClasses = new HashSet<>(Arrays.asList(DocumentScannerExtensionsTestClass.class,
                DatePickerTestClass.class,
                AmountMoneyPanelTestClass.class,
                ListTestClass.class,
                QueryPanelTestClass.class,
                PrimitivesTestClass.class));
        PersistenceStorage storage = mock(PersistenceStorage.class);
        when(storage.isClassSupported(any(Class.class))).thenReturn(true);
        for(Class<?> testClass : testClasses) {
            TypedQuery typedQuery = mock(TypedQuery.class);
            when(typedQuery.getResultList())
                    .thenReturn(new LinkedList<>(Arrays.asList(new EntityB())),
                            new LinkedList<>(Arrays.asList(new EntityB())),
                            new LinkedList<>(Arrays.asList(new EntityB())),
                            new LinkedList<>(Arrays.asList(new EntityB())),
                            new LinkedList<>(Arrays.asList(new EntityB())));
            when(typedQuery.setMaxResults(anyInt()))
                    .thenReturn(typedQuery);
            MessageHandler messageHandler = mock(MessageHandler.class);
            ConfirmMessageHandler confirmMessageHandler = mock(ConfirmMessageHandler.class);
            JPACachedFieldRetriever fieldRetriever = new JPACachedFieldRetriever();
            IdApplier idApplier = new GeneratedValueIdApplier();

            AutoOCRValueDetectionReflectionFormBuilder instance = new AutoOCRValueDetectionReflectionFormBuilder(storage,
                    "fieldDescriptionDialogTitle",
                    messageHandler, confirmMessageHandler,
                    fieldRetriever, idApplier,
                    new HashMap<Class<?>, WarningHandler<?>>(), //warningHandlers
                    new HashMap<Class<? extends JComponent>, ValueSetter<?,?>>() //valueSetterMapping
            );
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage = mock(AmountMoneyUsageStatisticsStorage.class);
            AmountMoneyCurrencyStorage amountMoneyCurrencyStorage = mock(AmountMoneyCurrencyStorage.class);
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever = mock(AmountMoneyExchangeRateRetriever.class);
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping = new HashMap<>();
            OCRResultPanelFetcher oCRResultPanelFetcher = mock(OCRResultPanelFetcher.class);
            ScanResultPanelFetcher scanResultPanelFetcher = mock(ScanResultPanelFetcher.class);
            DocumentScannerConf documentScannerConf = mock(DocumentScannerConf.class);
            final JFrame frame = new JFrame();
            Window oCRProgressMonitorParent = frame;
            Set<Class<?>> entityClasses = new HashSet<Class<?>>(Arrays.asList(DocumentScannerExtensionsTestClass.class));
            Class<?> primaryClassSelection = DocumentScannerExtensionsTestClass.class;
            OCREngine oCREngine = mock(OCREngine.class);
            TagStorage tagStorage = mock(TagStorage.class);
            Map<Class<?>, WarningHandler<?>> warningHandlers = new HashMap<>();
            FieldInitializer fieldInitializer = new NoOpFieldInitializer();
            MainPanel mainPanel = new DefaultMainPanel(entityClasses,
                    primaryClassSelection,
                    storage,
                    amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    amountMoneyExchangeRateRetriever,
                    messageHandler,
                    confirmMessageHandler,
                    frame,
                    oCREngine,
                    typeHandlerMapping,
                    documentScannerConf,
                    oCRProgressMonitorParent,
                    tagStorage,
                    idApplier,
                    warningHandlers,
                    fieldInitializer);
            int initialQueryLimit = 20;
            String bidirectionalHelpDialogTitle = "Title";
            DocumentScannerFieldHandler fieldHandler = DocumentScannerFieldHandler.create(amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    amountMoneyExchangeRateRetriever,
                    messageHandler,
                    confirmMessageHandler,
                    typeHandlerMapping,
                    storage,
                    fieldRetriever,
                    oCRResultPanelFetcher,
                    scanResultPanelFetcher,
                    documentScannerConf,
                    oCRProgressMonitorParent,
                    entityClasses,
                    primaryClassSelection,
                    mainPanel,
                    tagStorage,
                    idApplier,
                    warningHandlers,
                    initialQueryLimit,
                    bidirectionalHelpDialogTitle,
                    fieldInitializer);

            Object entityToUpdate = testClass.newInstance();
            ReflectionFormPanel testPanel = instance.transformEntityClass(testClass,
                    entityToUpdate,
                    fieldHandler);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE
                //must not be EXIT_ON_CLOSE because that terminates the
                //application and closes all JFrames
            );
            GroupLayout frameLayout = new GroupLayout(frame.getContentPane());
            frame.getContentPane().setLayout(frameLayout);
            frameLayout.setHorizontalGroup(frameLayout.createSequentialGroup().addComponent(testPanel));
            frameLayout.setVerticalGroup(frameLayout.createSequentialGroup().addComponent(testPanel));
            frame.pack();
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.setVisible(true);
                }
            });
        }
    }

    private static class DocumentScannerExtensionsTestClass {
        @OCRResult
        private String a;
        @ScanResult
        private String b;
        @CommunicationTree
        private List<WorkflowItem> c = new LinkedList<>();
        @Tags
        private Set<String> d = new HashSet<>();

        DocumentScannerExtensionsTestClass() {
        }
    }

    private static class DatePickerTestClass {
        private java.sql.Date a;
        private java.util.Date b;

        DatePickerTestClass() {
        }
    }

    private static class AmountMoneyPanelTestClass {
        private Amount<Money> a;

        AmountMoneyPanelTestClass() {
        }
    }

    private static class ListTestClass {
        private List<String> a;
        private List<Boolean> b;
        private List<Float> c;
        private List<Double> d;
        private List<Integer> e;
        private List<Short> f;
        private List<Byte> g;
        private List<Long> h;

        ListTestClass() {
        }
    }

    private static class QueryPanelTestClass {
        @OneToOne
        private EntityB a;
        @ManyToOne
        private EntityB b;
        @OneToMany
        private List<EntityB> c;
        @ManyToMany
        private List<EntityB> d;

        QueryPanelTestClass() {
        }
    }

    private static class PrimitivesTestClass {
        private int a;
        private Integer b;
        private boolean c;
        private Boolean d;
        private float e;
        private Float f;
        private double g;
        private Double h;
        private byte i;
        private Byte j;
        private long k;
        private Long l;
        private short m;
        private Short n;

        PrimitivesTestClass() {
        }
    }

    private static class EntityB {
        private String a = "kfldsafklödjsaklfjdsklafjsdklajfklsdjafklösdjalö";
        private String b = "klfdösjafkldjslaköfjdklsajfklsdjaflöksdjfklösdjf";
        private String c = "kfldösafkldsjflösdajfklösdjfklösdjafklösdjaklöf";
    }

    /**
     * Needs to be in {@code main} method because otherwise {@code JFrame}s
     * are killed by test runner.
     * @param args
     */
    public static void main(String[] args) {
        try {
            testResizability();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | FieldHandlingException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
