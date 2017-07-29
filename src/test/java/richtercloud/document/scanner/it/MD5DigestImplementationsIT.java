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
import java.nio.charset.Charset;
import java.nio.file.Files;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fills a test file with random data and then runs different MD5 checksum
 * algorithms on it.
 *
 * @author richter
 */
public class MD5DigestImplementationsIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(MD5DigestImplementationsIT.class);

    @Test
    public void testMD5DigestImplementation() throws IOException {
        //copy classpath resource into file first in order to make certain
        //slow processes visible when reading from file
        File resourceFile = Files.createTempFile(MD5DigestImplementationsIT.class.getSimpleName(), //prefix
                null //suffix
        ).toFile();
        int mbCount = 500;
            //1000MB cause trouble on Travis CI and it's not worth figuring this
            //out
        int byteCount = 1024*1024*mbCount;
        LOGGER.debug(String.format("generating a %d MB random string for the checksum test",
                mbCount));
        String randomString = RandomStringUtils.random(byteCount/2);
            //string is created in memory, but 1GB should be fine, otherwise
            //implement streaming solution
        FileOutputStream resourceFileOutputStream = new FileOutputStream(resourceFile);
        IOUtils.write(randomString, resourceFileOutputStream, Charset.defaultCharset());
        InputStream testInputStream = new FileInputStream(resourceFile);
        long time0 = System.currentTimeMillis();
        org.apache.commons.codec.digest.DigestUtils.md5Hex(testInputStream);
        long time1 = System.currentTimeMillis();
        LOGGER.info(String.format("%s took %s ms", org.apache.commons.codec.digest.DigestUtils.class, time1-time0));
        testInputStream = new FileInputStream(resourceFile);
        long time2 = System.currentTimeMillis();
        org.springframework.util.DigestUtils.md5DigestAsHex(testInputStream);
        long time3 = System.currentTimeMillis();
        LOGGER.info(String.format("%s took %s ms", org.springframework.util.DigestUtils.class, time3-time2));
    }
}
