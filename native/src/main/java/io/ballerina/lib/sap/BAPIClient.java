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

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRepository;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BAPIClient {

    private static final Logger logger = LoggerFactory.getLogger(BAPIClient.class);

    public static Object initializeBAPIClient(BObject bapiClient, String destinationName, String host,
                                              String systemNumber, String jcoClient, String user, String password,
                                              String group, String language) {
        try {
            if (destinationName.equals("TEST_DESTINATION")) {
                logger.debug("Destination name is TEST_DESTINATION");
            } else {
                BallerinaDestinationDataProvider dp = new BallerinaDestinationDataProvider();
                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(dp);
                dp.addDestination(destinationName, host, systemNumber, jcoClient, user, password, group, language);
                JCoDestination destination = JCoDestinationManager.getDestination(destinationName);
                logger.debug("BAPI Client initialized");
                setDestination(bapiClient, destination);
            }
            return null;
        } catch (JCoException e) {
            logger.error("Destination lookup failed!!!!");
            return SAPErrorCreator.fromJCoException(e);
        }
    }
    public static Object execute (Environment env, BObject bapiClient, BString functionName,
                                  BMap<BString, BString> inputParams, BArray outputParams) {
            try {
                if (functionName.toString().equals("TEST_FUNCTION")) {
                    logger.debug("Destination name is TEST_DESTINATION");
                    // Go through the output parameters and set them
                    MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_STRING);
                    BMap outputMap = ValueCreator.createMapValue(mapType);
                    String[] params = outputParams.getStringArray();
                    for (int i = 0; i < params.length; i++) {
                        outputMap.put(StringUtils.fromString(params[i]), StringUtils.fromString(
                                functionName.toString() + "_SAMPLE_OUTPUT"));
                    }
                    return outputMap;
                } else {
                    JCoDestination destination = getDestination(bapiClient);
                    JCoRepository repository = destination.getRepository();
                    JCoFunction function = repository.getFunction(functionName.toString());

                    if (function == null) {
                        throw new RuntimeException("RFC function not found in SAP.");
                    }

                    // Set input parameters if required
                    JCoParameterList importParams = function.getImportParameterList();

                    //Go through the input parameters and set them
                    inputParams.entrySet().forEach(entry -> {
                        importParams.setValue(entry.getKey().toString(), entry.getValue().toString());
                    });

                    // Execute the function
                    function.execute(destination);

                    // Process the results
                    JCoParameterList exportParams = function.getExportParameterList();

                    // Go through the output parameters and set them
                    MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_ANY);
                    BMap<BString, Object> outputMap = ValueCreator.createMapValue(mapType);
                    String[] params = outputParams.getStringArray();
                    for (int i = 0; i < params.length; i++) {
                        outputMap.put(StringUtils.fromString(params[i]), StringUtils.fromString(
                                exportParams.getString(params[i])));
                    }
                    return outputMap;
                }
            } catch (JCoException e) {
                logger.error("Destination lookup failed!!!!");
                return SAPErrorCreator.fromJCoException(e);
            }
    }
    private static void setDestination(BObject bapiClientObject, JCoDestination destination) {
        bapiClientObject.addNativeData("BAPI_DESTINATION", destination);
    }
    private static JCoDestination getDestination(BObject bapiClientObject) {
        return (JCoDestination) bapiClientObject.getNativeData("BAPI_DESTINATION");
    }
}
