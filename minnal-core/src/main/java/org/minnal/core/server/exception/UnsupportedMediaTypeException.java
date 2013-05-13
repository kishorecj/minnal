/**
 * 
 */
package org.minnal.core.server.exception;

import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.minnal.core.Response;

import com.google.common.base.Joiner;
import com.google.common.net.MediaType;

/**
 * @author ganeshs
 *
 */
public class UnsupportedMediaTypeException extends ApplicationException {
	
	private Set<MediaType> expectedTypes;

	private static final long serialVersionUID = 1L;
	
	public UnsupportedMediaTypeException(Set<MediaType> expectedTypes) {
		super(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
		this.expectedTypes = expectedTypes;
	}

	@Override
	public void handle(Response response) {
		super.handle(response);
		response.addHeader(HttpHeaders.Names.ACCEPT, Joiner.on(", ").join(expectedTypes));
	}

	/**
	 * @return the expectedTypes
	 */
	public Set<MediaType> getExpectedTypes() {
		return expectedTypes;
	}

}
