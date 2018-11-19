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
package de.richtercloud.document.scanner.it.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 *
 * @author richter
 */
@Entity
public class LargeBinaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;
    @Lob
    //@Fetch(FetchMode.SELECT) //doesn't help in conjunction with @Lob and
    //@Basic(fetch = FetchType.LAZY)
    //@LazyGroup(value = "binaryGroup") specifying @LazyGroup doesn't fix
    //`Exception in thread "main" java.lang.ClassCastException: org.hibernate.bytecode.enhance.spi.LazyPropertyInitializer$1 cannot be cast to [B`
    //<ref>https://forum.hibernate.org/viewtopic.php?f=1&t=1043618&start=0</ref>
    //in 5.2.4.Final and 5.1.3.Final -> use 5.0.11.Final
    @Basic(fetch = FetchType.LAZY)
    private byte[] binaryData;

    protected LargeBinaryEntity() {
    }

    public LargeBinaryEntity(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }
}
