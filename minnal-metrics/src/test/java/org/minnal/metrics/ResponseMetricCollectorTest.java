/**
 * 
 */
package org.minnal.metrics;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.minnal.core.Application;
import org.minnal.core.config.ApplicationConfiguration;
import org.minnal.core.server.MessageContext;
import org.minnal.core.server.ServerResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * @author ganeshs
 *
 */
public class ResponseMetricCollectorTest {

	private ResponseMetricCollector collector;
	
	private MessageContext context;
	
	private Application application;
	
	private MetricRegistry metricRegistry;
	
	@BeforeMethod
	public void setup() {
		collector = spy(new ResponseMetricCollector());
		context = mock(MessageContext.class);
		application = mock(Application.class);
		ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
		when(configuration.getName()).thenReturn("test");
		when(application.getConfiguration()).thenReturn(configuration);
		metricRegistry = mock(MetricRegistry.class);
		when(context.getApplication()).thenReturn(application);
		MetricRegistries.addRegistry(application, metricRegistry);
	}
	
	@AfterMethod
	public void destroy() {
		MetricRegistries.removeRegistry(application);
	}
	
	@Test
	public void shouldSetStartTimeOnMessageReceive() {
		collector.onReceived(context);
		verify(context).addAttribute(eq(ResponseMetricCollector.START_TIME), any(Long.class));
	}
	
	@Test
	public void shouldSetSuccessfulOnSuccess() {
		ServerResponse response = mock(ServerResponse.class);
		when(response.getStatus()).thenReturn(HttpResponseStatus.OK);
		when(context.getResponse()).thenReturn(response);
		collector.onSuccess(context);
		verify(context).addAttribute(eq(ResponseMetricCollector.SUCCESSFUL), eq(Boolean.TRUE));
	}
	
	@Test
	public void shouldSetNotSuccessfulOn4xx() {
		ServerResponse response = mock(ServerResponse.class);
		when(response.getStatus()).thenReturn(HttpResponseStatus.NOT_FOUND);
		when(context.getResponse()).thenReturn(response);
		collector.onSuccess(context);
		verify(context).addAttribute(eq(ResponseMetricCollector.SUCCESSFUL), eq(Boolean.FALSE));
	}
	
	@Test
	public void shouldLogResponseTimeOnCompletion() {
		when(context.getAttribute(ResponseMetricCollector.SUCCESSFUL)).thenReturn(Boolean.TRUE);
		when(context.getAttribute(ResponseMetricCollector.START_TIME)).thenReturn(System.currentTimeMillis());
		Timer timer = mock(Timer.class);
		doReturn("dummy").when(collector).getMetricName(context, ResponseMetricCollector.RESPONSE_TIME);
		when(metricRegistry.timer("dummy")).thenReturn(timer);
		collector.onComplete(context);
		verify(timer).update(any(Long.class), eq(TimeUnit.NANOSECONDS));
	}
	
	@Test
	public void shouldLogFailureOnCompletion() {
		ServerResponse response = mock(ServerResponse.class);
		when(response.getStatus()).thenReturn(HttpResponseStatus.NOT_FOUND);
		when(context.getResponse()).thenReturn(response);
		when(context.getAttribute(ResponseMetricCollector.SUCCESSFUL)).thenReturn(Boolean.FALSE);
		when(context.getAttribute(ResponseMetricCollector.START_TIME)).thenReturn(System.currentTimeMillis());
		
		Timer timer = mock(Timer.class);
		doReturn("dummy").when(collector).getMetricName(context, ResponseMetricCollector.RESPONSE_TIME);
		when(metricRegistry.timer("dummy")).thenReturn(timer);
		Meter meter = mock(Meter.class);
		doReturn("dummy").when(collector).getMetricName(context, "404");
		when(metricRegistry.meter("dummy")).thenReturn(meter);
		collector.onComplete(context);
		verify(meter).mark();
	}
	
	@Test
	public void shouldLogExceptionOnCompletion() {
		when(context.getAttribute(ResponseMetricCollector.SUCCESSFUL)).thenReturn(null);
		when(context.getApplication()).thenReturn(application);
		
		Meter meter = mock(Meter.class);
		when(metricRegistry.meter("test." + ResponseMetricCollector.EXCEPTIONS)).thenReturn(meter);
		collector.onComplete(context);
		verify(meter).mark();
	}
}
