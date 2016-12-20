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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class MD5DigestImplementationsIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(MD5DigestImplementationsIT.class);
    public final static String DOWNLOAD_URL = "http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.16-linux-glibc2.5-x86_64.tar.gz";

    public static void main(String[] args) throws IOException {
        //copy classpath resource into file first in order to make certain
        //slow processes visible when reading from file
        File resourceFile = new File("mysql-5.7.16-linux-glibc2.5-x86_64.tar.gz");
        if(!resourceFile.exists()) {
            LOGGER.info(String.format("downloading MySQL from %s", DOWNLOAD_URL));
            FileOutputStream resourceFileOutputStream = new FileOutputStream(resourceFile);
            IOUtils.copy(new URL(DOWNLOAD_URL).openStream(),
                    resourceFileOutputStream);
        }
        InputStream testInputStream = new FileInputStream(resourceFile);
        long time0 = System.currentTimeMillis();
        org.apache.commons.codec.digest.DigestUtils.md5Hex(testInputStream);
        long time1 = System.currentTimeMillis();
        LOGGER.info(String.format("%s took %s ms", org.apache.commons.codec.digest.DigestUtils.class, time1-time0));
        testInputStream = MD5DigestImplementationsIT.class.getResourceAsStream("/mysql-5.7.16-linux-glibc2.5-x86_64.tar.gz");
        long time2 = System.currentTimeMillis();
        org.springframework.util.DigestUtils.md5DigestAsHex(testInputStream);
        long time3 = System.currentTimeMillis();
        LOGGER.info(String.format("%s took %s ms", org.springframework.util.DigestUtils.class, time3-time2));
    }
}
