package nl.tweeenveertig.openstack.command.object;

import nl.tweeenveertig.openstack.command.core.BaseCommandTest;
import nl.tweeenveertig.openstack.exception.CommandException;
import nl.tweeenveertig.openstack.exception.NotFoundException;
import nl.tweeenveertig.openstack.model.ObjectInformation;
import org.apache.http.Header;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static nl.tweeenveertig.openstack.headers.object.ObjectMetadata.X_OBJECT_META_PREFIX;
import static nl.tweeenveertig.openstack.headers.object.ObjectLastModified.LAST_MODIFIED;
import static nl.tweeenveertig.openstack.headers.object.Etag.ETAG;
import static nl.tweeenveertig.openstack.headers.object.ObjectContentLength.CONTENT_LENGTH;
import static nl.tweeenveertig.openstack.headers.object.ObjectContentType.CONTENT_TYPE;
import static nl.tweeenveertig.openstack.headers.object.DeleteAt.X_DELETE_AT;
import static org.mockito.Mockito.when;

public class ObjectInformationCommandTest extends BaseCommandTest {

    @Before
    public void setup() throws IOException {
        super.setup();
        prepareMetadata();
    }

    private void prepareMetadata() {
        List<Header> headers = new ArrayList<Header>();
        prepareHeader(response, X_OBJECT_META_PREFIX+ "Description", "Photo album", headers);
        prepareHeader(response, X_OBJECT_META_PREFIX+ "Year", "1984", headers);
        prepareHeader(response, LAST_MODIFIED, "Mon, 03 Sep 2012 05:40:33 GMT");
        prepareHeader(response, ETAG, "cae4ebb15a282e98ba7b65402a72f57c", headers);
        prepareHeader(response, CONTENT_LENGTH, "654321", headers);
        prepareHeader(response, CONTENT_TYPE, "image/png", headers);
        prepareHeader(response, X_DELETE_AT, "Mon, 03 Sep 2012 07:40:33 GMT", headers);
        when(response.getAllHeaders()).thenReturn(headers.toArray(new Header[headers.size()]));
    }

    @Test(expected = CommandException.class)
    public void illegalDate() {
        prepareHeader(response, LAST_MODIFIED, "I'm not a date!");
        when(statusLine.getStatusCode()).thenReturn(200);
        new ObjectInformationCommand(this.account, httpClient, defaultAccess, account.getContainer("containerName"), getObject("objectName")).call();
    }

    @Test
    public void getInfoSuccess() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(200);
        ObjectInformation info = new ObjectInformationCommand(this.account, httpClient, defaultAccess, account.getContainer("containerName"), getObject("objectName")).call();
        assertEquals("Photo album", info.getMetadata("Description"));
        assertEquals("1984", info.getMetadata("Year"));
        assertEquals("Mon, 03 Sep 2012 05:40:33 GMT", info.getLastModified());
        assertEquals("cae4ebb15a282e98ba7b65402a72f57c", info.getEtag());
        assertEquals(654321, info.getContentLength());
        assertEquals("image/png", info.getContentType());
        assertEquals("Mon, 03 Sep 2012 07:40:33 GMT", info.getDeleteAt().getHeaderValue());
    }

    @Test (expected = NotFoundException.class)
    public void createContainerFail() throws IOException {
        checkForError(404, new ObjectInformationCommand(this.account, httpClient, defaultAccess, account.getContainer("containerName"), getObject("objectName")));
    }

    @Test (expected = CommandException.class)
    public void unknownError() throws IOException {
        checkForError(500, new ObjectInformationCommand(this.account, httpClient, defaultAccess, account.getContainer("containerName"), getObject("objectName")));
    }

    @Test
    public void isSecure() throws IOException {
        isSecure(new ObjectInformationCommand(this.account, httpClient, defaultAccess,
                account.getContainer("containerName"), getObject("objectName")));
    }

}
