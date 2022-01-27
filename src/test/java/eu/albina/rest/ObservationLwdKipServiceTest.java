package eu.albina.rest;

import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class ObservationLwdKipServiceTest {

	@Test
	public void testLayer() throws Exception {
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>(Collections.singletonMap("f", "json")));
		new ObservationLwdKipService().get("layers", uriInfo);
	}

}
