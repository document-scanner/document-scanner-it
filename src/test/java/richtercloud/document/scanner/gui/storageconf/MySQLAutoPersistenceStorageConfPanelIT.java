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
package richtercloud.document.scanner.gui.storageconf;

import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorageConf;

/**
 *
 * @author richter
 */
public class MySQLAutoPersistenceStorageConfPanelIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(MySQLAutoPersistenceStorageConfPanelIT.class);

    /**
     * Test of mySQLDownload method, of class MySQLAutoPersistenceStorageConfPanel.
     */
    @Test
    public void testMySQLDownload() throws IOException {
        LOGGER.debug("Running download test for Linux 32-bit");
        //Linux 32-bit
        String downloadURL = MySQLAutoPersistenceStorageConfPanel.DOWNLOAD_URL_LINUX_32;
        String downloadTarget = MySQLAutoPersistenceStorageConfPanel.MYSQL_DOWNLOAD_TARGET_LINUX_32;
        int extractionMode = MySQLAutoPersistenceStorageConfPanel.EXTRACTION_MODE_TAR_GZ;
        String extractionDir = MySQLAutoPersistenceStorageConfPanel.MYSQL_EXTRACTION_TARGET_LINUX_32;
        String md5Sum = MySQLAutoPersistenceStorageConfPanel.MD5_SUM_LINUX_32;
        MySQLAutoPersistenceStorageConf storageConf = mock(MySQLAutoPersistenceStorageConf.class);
        MessageHandler messageHandler = mock(MessageHandler.class);
        ConfirmMessageHandler confirmMessageHandler = mock(ConfirmMessageHandler.class);
        MySQLAutoPersistenceStorageConfPanel instance = new MySQLAutoPersistenceStorageConfPanel(
                storageConf,
                messageHandler,
                confirmMessageHandler,
                false);
        boolean expResult = true;
        boolean result = instance.mySQLDownload(downloadURL,
                downloadTarget,
                extractionMode,
                extractionDir,
                md5Sum);
        assertEquals(expResult, result);
        LOGGER.debug("Running download test for Linux 64-bit");
        //Linux 64-bit
        downloadURL = MySQLAutoPersistenceStorageConfPanel.DOWNLOAD_URL_LINUX_64;
        downloadTarget = MySQLAutoPersistenceStorageConfPanel.MYSQL_DOWNLOAD_TARGET_LINUX_64;
        extractionMode = MySQLAutoPersistenceStorageConfPanel.EXTRACTION_MODE_TAR_GZ;
        extractionDir = MySQLAutoPersistenceStorageConfPanel.MYSQL_EXTRACTION_TARGET_LINUX_64;
        md5Sum = MySQLAutoPersistenceStorageConfPanel.MD5_SUM_LINUX_64;
        instance = new MySQLAutoPersistenceStorageConfPanel(
                storageConf,
                messageHandler,
                confirmMessageHandler,
                false);
        result = instance.mySQLDownload(downloadURL,
                downloadTarget,
                extractionMode,
                extractionDir,
                md5Sum);
        assertEquals(expResult, result);
        LOGGER.debug("Running download test for Windows 32-bit");
        //Windows 32-bit
        downloadURL = MySQLAutoPersistenceStorageConfPanel.DOWNLOAD_URL_WINDOWS_32;
        downloadTarget = MySQLAutoPersistenceStorageConfPanel.MYSQL_DOWNLOAD_TARGET_WINDOWS_32;
        extractionMode = MySQLAutoPersistenceStorageConfPanel.EXTRACTION_MODE_ZIP;
        extractionDir = MySQLAutoPersistenceStorageConfPanel.MYSQL_EXTRACTION_TARGET_WINDOWS_32;
        md5Sum = MySQLAutoPersistenceStorageConfPanel.MD5_SUM_WINDOWS_32;
        instance = new MySQLAutoPersistenceStorageConfPanel(
                storageConf,
                messageHandler,
                confirmMessageHandler,
                false);
        result = instance.mySQLDownload(downloadURL,
                downloadTarget,
                extractionMode,
                extractionDir,
                md5Sum);
        assertEquals(expResult, result);
        LOGGER.debug("Running download test for Windows 64-bit");
        //Windows 64-bit
        downloadURL = MySQLAutoPersistenceStorageConfPanel.DOWNLOAD_URL_WINDOWS_64;
        downloadTarget = MySQLAutoPersistenceStorageConfPanel.MYSQL_DOWNLOAD_TARGET_WINDOWS_64;
        extractionMode = MySQLAutoPersistenceStorageConfPanel.EXTRACTION_MODE_ZIP;
        extractionDir = MySQLAutoPersistenceStorageConfPanel.MYSQL_EXTRACTION_TARGET_WINDOWS_64;
        md5Sum = MySQLAutoPersistenceStorageConfPanel.MD5_SUM_WINDOWS_64;
        instance = new MySQLAutoPersistenceStorageConfPanel(
                storageConf,
                messageHandler,
                confirmMessageHandler,
                false);
        result = instance.mySQLDownload(downloadURL,
                downloadTarget,
                extractionMode,
                extractionDir,
                md5Sum);
        assertEquals(expResult, result);
        LOGGER.debug("Running download test for Mac OSX 64-bit");
        //Mac OSX 64-bit
        downloadURL = MySQLAutoPersistenceStorageConfPanel.DOWNLOAD_URL_MAC_OSX_64;
        downloadTarget = MySQLAutoPersistenceStorageConfPanel.MYSQL_DOWNLOAD_TARGET_MAC_OSX_64;
        extractionMode = MySQLAutoPersistenceStorageConfPanel.EXTRACTION_MODE_TAR_GZ;
        extractionDir = MySQLAutoPersistenceStorageConfPanel.MYSQL_EXTRACTION_TARGET_MAC_OSX_64;
        md5Sum = MySQLAutoPersistenceStorageConfPanel.MD5_SUM_MAC_OSX_64;
        instance = new MySQLAutoPersistenceStorageConfPanel(
                storageConf,
                messageHandler,
                confirmMessageHandler,
                false);
        result = instance.mySQLDownload(downloadURL,
                downloadTarget,
                extractionMode,
                extractionDir,
                md5Sum);
        assertEquals(expResult, result);
    }
}
