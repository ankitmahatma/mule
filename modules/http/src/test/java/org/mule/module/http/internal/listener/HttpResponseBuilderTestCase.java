/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.http.internal.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mule.module.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mule.module.http.api.requester.HttpStreamingType.NEVER;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.http.internal.domain.response.HttpResponse;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

public class HttpResponseBuilderTestCase extends AbstractMuleTestCase
{

    public static final String EXAMPLE_STRING = "exampleString";
    private MuleContext muleContext;
    private MuleMessage mockMuleMessage;
    private MuleEvent mockEvent;

    @Before
    public void setUp()
    {
        muleContext = mock(MuleContext.class, RETURNS_DEEP_STUBS);
        mockEvent = mock(MuleEvent.class);
        mockMuleMessage = mock(MuleMessage.class);
    }

    @Test
    public void testContentLengthIsOverridden() throws Exception
    {
        HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();
        int contentLengthDifferingPayloadSize = 12;
        mockMuleMessage(new ByteArrayInputStream(EXAMPLE_STRING.getBytes(UTF_8)), contentLengthDifferingPayloadSize);

        HttpResponse httpResponse = httpResponseBuilder.build(new org.mule.module.http.internal.domain.response.HttpResponseBuilder(), mockEvent);
        assertThat(httpResponse.getHeaderValue(CONTENT_LENGTH), is(String.valueOf(EXAMPLE_STRING.length())));
    }

    @Test
    public void streamClosedAfterBeingConvertedToByteArray() throws Exception
    {
        HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();
        httpResponseBuilder.setStatusCode("200");
        httpResponseBuilder.setMuleContext(mock(MuleContext.class));
        httpResponseBuilder.setResponseStreaming(NEVER);
        httpResponseBuilder.init();

        InputStream stream = mock(InputStream.class);
        MuleEvent event = mock(MuleEvent.class, RETURNS_DEEP_STUBS);
        when(event.getMessage().getPayload()).thenReturn(stream);
        when(stream.read(any(byte[].class))).thenReturn(-1);
        when(event.getMessage().getOutboundAttachmentNames().isEmpty()).thenReturn(true);

        httpResponseBuilder.build(new org.mule.module.http.internal.domain.response.HttpResponseBuilder(), event);

        verify(stream).close();
    }

    private void mockMuleMessage(InputStream payload, int contentLength)
    {
        mockMuleMessage = new DefaultMuleMessage(payload, muleContext);
        mockMuleMessage.setOutboundProperty(CONTENT_LENGTH, contentLength);
        when(mockEvent.getMessage()).thenReturn(mockMuleMessage);
    }
}
