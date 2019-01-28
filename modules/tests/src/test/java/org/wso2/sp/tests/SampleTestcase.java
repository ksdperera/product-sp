/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.sp.tests;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.siddhi.store.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.store.api.rest.model.Query;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.sp.tests.util.HTTPResponseMessage;
import org.wso2.sp.tests.util.TestUtil;

import java.net.URI;
import javax.ws.rs.core.Response;

import static org.awaitility.Awaitility.await;

/**
 * Integration Tests.
 */
public class SampleTestcase {
    private static final org.apache.log4j.Logger logger = Logger.getLogger(SampleTestcase.class);
    private static final int HTTP_PORT = 7070;
    private static final String API_CONTEXT_PATH = "/stores/query";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final Duration DEFAULT_DURATION = Duration.TEN_SECONDS;
    private static String hostname = "localhost";
    private final Gson gson = new Gson();

    @BeforeClass
    public static void init() {
        hostname = System.getenv("DOCKER_HOST_IP");
    }

    @Test
    public void sampleTestCase1() throws Exception {
        logger.info("Sending events through HTTP Sink");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream TradeStream(creditCardNo string, trader string, tradeInfo string);\n" +
                "@sink(type='http' , publisher.url='http://" + hostname + ":8280/JoinWithStoredData/TradeStream', " +
                "@map(type='json')) \n" +
                "define stream TradeStreamSink(creditCardNo string, trader string, tradeInfo string);";

        String query1 = "" +
                "@info(name = 'query1') " +
                "from TradeStream " +
                "select * " +
                "insert into TradeStreamSink;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query1);
        InputHandler tradeStream = siddhiAppRuntime.getInputHandler("TradeStream");
        siddhiAppRuntime.start();
        tradeStream.send(new Object[]{"1111", "TraderA", "Trade Info"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

        logger.info("Invoke Store API to validate results");
        Event[] expectedEvents = new Event[]{
                new Event(System.currentTimeMillis(), new Object[]{"1111"})};
        testStoreAPI("JoinWithStoredData", "from FraudTable select *", Response.Status.OK, expectedEvents);
    }

    private void testStoreAPI(String app, String strQuery, Response.Status expectedStatus, Event[] expectedEvents)
            throws
            InterruptedException {
        Query query = new Query();
        query.setAppName(app);
        query.setQuery(strQuery);
        testHttpResponse(gson.toJson(query), expectedStatus.getStatusCode(), expectedEvents);
    }

    private void testHttpResponse(String body, int expectedResponseCode, Event[] expectedEvents) {
        await().atMost(DEFAULT_DURATION).until(() -> {
            HTTPResponseMessage httpResponseMessage =
                    sendHRequest(body);
            if (expectedResponseCode == Response.Status.OK.getStatusCode()) {
                ModelApiResponse response =
                        gson.fromJson(httpResponseMessage.getSuccessContent().toString(), ModelApiResponse.class);
                if (httpResponseMessage.getResponseCode() == Response.Status.OK.getStatusCode() &&
                        httpResponseMessage.getContentType().equalsIgnoreCase(CONTENT_TYPE_JSON)) {
                    Assert.assertEquals(response.getRecords().size(), expectedEvents.length,
                            "Number of the records returned is matched!");
                    //TODO: Assert each event
                    return true;
                }
                Assert.fail();
            } else {
                Assert.fail();
            }
            return false;
        });
    }

    private HTTPResponseMessage sendHRequest(String body) {
        URI baseURI = URI.create(String.format("http://%s:%d", hostname, HTTP_PORT));
        TestUtil testUtil = new TestUtil(baseURI, API_CONTEXT_PATH, true, false, HTTP_METHOD_POST,
                CONTENT_TYPE_JSON, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        testUtil.addBodyContent(body);
        return testUtil.getResponse();
    }
}
