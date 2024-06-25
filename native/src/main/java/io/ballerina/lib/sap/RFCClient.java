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
import com.sap.conn.jco.ext.DestinationDataProvider;
import io.ballerina.runtime.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RFCClient {

    private static final Logger logger = LoggerFactory.getLogger(RFCClient.class);

    private RFCClient() {
    }

    public static void execute(Environment env) {
        try {
            DestinationDataProvider dp = new BallerinaDestinationDataProvider();
            if (!com.sap.conn.jco.ext.Environment.isDestinationDataProviderRegistered()) {
                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(dp);
            }
            JCoDestination destination = JCoDestinationManager.getDestination("cloud");
            JCoRepository repository = destination.getRepository();
            JCoFunction function = repository.getFunction("BAPI_SALESORDER_GETLIST");

            if (function == null) {
                throw new RuntimeException("RFC function not found in SAP.");
            }

            // Set input parameters if required
            JCoParameterList importParams = function.getImportParameterList();
            importParams.setValue("PARAM_NAME", "PARAM_VALUE");

            // Execute the function
            function.execute(destination);

            // Process the results
            JCoParameterList exportParams = function.getExportParameterList();
            String result = exportParams.getString("RESULT_PARAM_NAME");
            System.out.println("Result: " + result);

        } catch (JCoException e) {
            logger.error("Destination lookup failed!!!!");
        }
    }
}
