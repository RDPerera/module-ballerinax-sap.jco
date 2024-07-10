/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.sap;

import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocXMLProcessor;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.idoc.jco.JCoIDocHandler;
import com.sap.conn.idoc.jco.JCoIDocHandlerFactory;
import com.sap.conn.idoc.jco.JCoIDocServer;
import com.sap.conn.idoc.jco.JCoIDocServerContext;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerTIDHandler;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static io.ballerina.runtime.api.TypeTags.STRING_TAG;
import static io.ballerina.runtime.api.TypeTags.UNION_TAG;
import static io.ballerina.runtime.api.utils.TypeUtils.getReferredType;

public class IDocDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final String JCO_STARTED_SERVICES = "JCO_STARTED_SERVICES";
    private static final String ON_IDOC = "onIDoc";
    private static final String ON_ERROR = "onError";
    private final JCoIDocServer server;
    private final BObject service;
    private final Runtime runtime;

    public IDocDispatcher(BObject service, JCoIDocServer server, Runtime runtime) {
        this.server = server;
        this.service = service;
        this.runtime = runtime;

    }

    public static BMap<BString, Object> createAndPopulateMessageRecord(String xmlContent, Type type) {
        RecordType recordType = getRecordType(type);
        Type intendedType = TypeUtils.getReferredType(recordType.getFields().get("content").getFieldType());
        BMap<BString, Object> iDocRecord = ValueCreator.createRecordValue(recordType);
        Object value = getValueWithIntendedType(intendedType, xmlContent.getBytes(StandardCharsets.UTF_8));
        if (value instanceof BError) {
            throw SAPErrorCreator.createError("Error occurred while creating the message record", (BError) value);
        }
        iDocRecord.put(StringUtils.fromString("content"), value);
        if (type.getTag() == TypeTags.INTERSECTION_TAG) {
            iDocRecord.freezeDirect();
        }
        return iDocRecord;
    }

    private static RecordType getRecordType(Type type) {
        if (type.getTag() == TypeTags.INTERSECTION_TAG) {
            return (RecordType) TypeUtils.getReferredType(((IntersectionType) (type)).getConstituentTypes().get(0));
        }
        return (RecordType) type;
    }

    public static Object getValueWithIntendedType(Type type, byte[] value) {
        String strValue = new String(value, StandardCharsets.UTF_8);
        try {
            switch (type.getTag()) {
                case STRING_TAG:
                    return StringUtils.fromString(strValue);
                case TypeTags.XML_TAG:
                    return XmlUtils.parse(strValue);
                case TypeTags.ANYDATA_TAG:
                    return ValueCreator.createArrayValue(value);
                case TypeTags.RECORD_TYPE_TAG:
                    return ValueUtils.convert(JsonUtils.parse(strValue), type);
                case UNION_TAG:
                    if (hasStringType((UnionType) type)) {
                        return StringUtils.fromString(strValue);
                    }
                    return getValueFromJson(type, strValue);
                case TypeTags.ARRAY_TAG:
                    if (TypeUtils.getReferredType(((ArrayType) type).getElementType()).getTag() == TypeTags.BYTE_TAG) {
                        return ValueCreator.createArrayValue(value);
                    }
                    /*-fallthrough*/
                default:
                    return getValueFromJson(type, strValue);
            }
        } catch (BError bError) {
            throw SAPErrorCreator.createError(String.format("Data binding failed: %s", bError.getMessage()), bError);
        }
    }

    private static boolean hasStringType(UnionType type) {
        return type.getMemberTypes().stream().anyMatch(memberType -> memberType.getTag() == STRING_TAG);
    }

    private static Object getValueFromJson(Type type, String stringValue) {
        return ValueUtils.convert(JsonUtils.parse(stringValue), type);
    }

    private static MethodType getAttachedFunctionType(BObject serviceObject, String functionName) {
        MethodType function = null;
        MethodType[] resourceFunctions = ((ObjectType) getReferredType(
                TypeUtils.getType(serviceObject))).getMethods();
        for (MethodType resourceFunction : resourceFunctions) {
            if (functionName.equals(resourceFunction.getName())) {
                function = resourceFunction;
                break;
            }
        }
        return function;
    }

    public void receiveIDoc(BObject listenerObject) {
        try {
            server.setIDocHandlerFactory(new BallerinaIDocHandlerFactory());
            server.setTIDHandler(new BallerinaTidHandler());
            BallerinaThrowableListener listener = new BallerinaThrowableListener();
            server.addServerErrorListener(listener);
            server.addServerExceptionListener(listener);
            @SuppressWarnings("unchecked")
            ArrayList<BObject> startedServices =
                    (ArrayList<BObject>) listenerObject.getNativeData(JCO_STARTED_SERVICES);
            startedServices.add(service);
        } catch (Throwable e) {
            logger.error("Error while processing IDoc", e);
        }
    }

    private Object[] getResourceParameters(MethodType onIDocFunction, String xmlContent) {
        Parameter[] parameters = onIDocFunction.getParameters();
        Object[] args = new Object[parameters.length * 2];
        int index = 0;
        for (Parameter parameter : parameters) {
            Type referredType = getReferredType(parameter.type);
            switch (referredType.getTag()) {
                case TypeTags.INTERSECTION_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                    args[index++] = createAndPopulateMessageRecord(xmlContent, referredType);
                    break;
                default:
                    args[index++] = null;
                    break;
            }
            args[index++] = true;
        }
        return args;
    }

    public void invokeOnIDoc(Callback callback, Type returnType, Object... args) {

        Module module = ModuleUtils.getModule();
        StrandMetadata metadata = new StrandMetadata(
                module.getOrg(), module.getName(), module.getMajorVersion(), ON_IDOC);
        ObjectType serviceType = (ObjectType) getReferredType(TypeUtils.getType(service));
        if (serviceType.isIsolated() && serviceType.isIsolated(ON_IDOC)) {
            runtime.invokeMethodAsyncConcurrently(service, ON_IDOC, null, metadata, callback, null,
                    returnType, args);
        } else {
            runtime.invokeMethodAsyncSequentially(service, ON_IDOC, null, metadata, callback, null,
                    returnType, args);
        }
    }

    public void invokeOnError(Type returnType, Object... args) {
        Module module = ModuleUtils.getModule();
        StrandMetadata metadata = new StrandMetadata(
                module.getOrg(), module.getName(), module.getMajorVersion(), ON_ERROR);
        ObjectType serviceType = (ObjectType) getReferredType(TypeUtils.getType(service));
        if (serviceType.isIsolated() && serviceType.isIsolated(ON_ERROR)) {
            runtime.invokeMethodAsyncConcurrently(service, ON_ERROR, null, metadata, null, null,
                    returnType, args);
        } else {
            runtime.invokeMethodAsyncSequentially(service, ON_ERROR, null, metadata, null, null,
                    returnType, args);
        }
    }

    static class BallerinaTidHandler implements JCoServerTIDHandler {

        public boolean checkTID(JCoServerContext serverCtx, String tid) {
            logger.info("checkTID called for TID=" + tid);
            return true;
        }

        public void confirmTID(JCoServerContext serverCtx, String tid) {
            logger.info("confirmTID called for TID=" + tid);
        }

        public void commit(JCoServerContext serverCtx, String tid) {
            logger.info("commit called for TID=" + tid);
        }

        public void rollback(JCoServerContext serverCtx, String tid) {
            logger.info("rollback called for TID=" + tid);
        }
    }

    static class BallerinaThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {

        private static final Logger logger = LoggerFactory.getLogger(BallerinaThrowableListener.class);

        @Override
        public void serverErrorOccurred(JCoServer jCoServer, String s, JCoServerContextInfo jCoServerContextInfo,
                                        Error error) {
            logger.error("Server error occurred: " + error.getMessage());
        }

        @Override
        public void serverExceptionOccurred(JCoServer jCoServer, String s, JCoServerContextInfo jCoServerContextInfo,
                                            Exception e) {
            logger.error("Server exception occurred: " + e.getMessage());
        }
    }

    class BallerinaIDocHandler implements JCoIDocHandler {

        public void handleRequest(JCoServerContext serverCtx, IDocDocumentList idocList) {

            StringWriter stringWriter = new StringWriter();
            try {
                IDocXMLProcessor xmlProcessor =
                        JCoIDoc.getIDocFactory().getIDocXMLProcessor();
                xmlProcessor.render(idocList, stringWriter,
                        IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF);
                String xmlContent = stringWriter.toString();
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Callback callback = new SAPResourceCallback(countDownLatch);
                try {
                    MethodType onIDocFunction = getAttachedFunctionType(service, "onIDoc");
                    Type returnType = onIDocFunction.getReturnType();
                    Object[] args = getResourceParameters(onIDocFunction, xmlContent);
                    invokeOnIDoc(callback, returnType, args);
                    countDownLatch.await();
                } catch (InterruptedException | BError exception) {
                    MethodType onErrorFunction = getAttachedFunctionType(service, "onError");
                    Type returnType = onErrorFunction.getReturnType();
                    if (exception instanceof BError) {
                        invokeOnError(returnType, exception);
                    } else {
                        invokeOnError(returnType, SAPErrorCreator.createError(exception.getMessage(), exception));
                    }
                }

            } catch (Throwable thr) {
                logger.error("Error while processing IDoc", thr);
            } finally {
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    logger.error("Error while closing the string writer", e);
                }
            }
        }
    }

    class BallerinaIDocHandlerFactory implements JCoIDocHandlerFactory {

        private final JCoIDocHandler handler = new BallerinaIDocHandler();

        public JCoIDocHandler getIDocHandler(JCoIDocServerContext serverCtx) {
            return handler;
        }
    }
}
