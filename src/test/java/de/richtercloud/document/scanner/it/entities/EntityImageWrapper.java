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
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import de.richtercloud.document.scanner.ifaces.ImageWrapper;

/**
 *
 * @author richter
 */
@Entity
public class EntityImageWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;
    @Basic(fetch = FetchType.EAGER) //avoids `org.eclipse.persistence.exceptions.DescriptorException
    //Exception Description: Trying to set value [[B@3e9803c2] for instance variable [data] of type [java.sql.Blob] in the object.  The specified object is not an instance of the class or interface declaring the underlying field, or an unwrapping conversion has failed.
    //Internal Exception: java.lang.IllegalArgumentException: Can not set java.sql.Blob field EntityImageWrapper.data to [B
    //Mapping: org.eclipse.persistence.mappings.DirectToFieldMapping[data-->BLOBENTITY.BINARYDATA]
    //Descriptor: RelationalDescriptor(EntityImageWrapper --> [DatabaseTable(BLOBENTITY)])
    //Exception in thread "main" javax.persistence.PersistenceException: Exception [EclipseLink-32] (Eclipse Persistence Services - 2.6.4.v20160829-44060b6): org.eclipse.persistence.exceptions.DescriptorException
    //Exception Description: Trying to set value [[B@3e9803c2] for instance variable [data] of type [java.sql.Blob] in the object.  The specified object is not an instance of the class or interface declaring the underlying field, or an unwrapping conversion has failed.
    //Internal Exception: java.lang.IllegalArgumentException: Can not set java.sql.Blob field EntityImageWrapper.data to [B
    //Mapping: org.eclipse.persistence.mappings.DirectToFieldMapping[data-->BLOBENTITY.BINARYDATA]`
    private List<ImageWrapper> data = new LinkedList<>();

    protected EntityImageWrapper() {
    }

    public EntityImageWrapper(List<ImageWrapper> binaryData) {
        this.data = binaryData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setData(List<ImageWrapper> data) {
        this.data = data;
    }

    public List<ImageWrapper> getData() {
        return data;
    }
}