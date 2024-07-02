// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//import ballerina/uuid;
import ballerina/jballerina.java as java;

# A Ballerina client for SAP BAPI/RFC.
@display {label: "BAPI/RFC Client", iconPath: "icon.png"}
public isolated client class BAPIClient {

    # Initializes the connector.
    # 
    # + configurations - The configurations required to initialize the BAPI client.
    # + return - An error if the initialization fails.
    public isolated function init(*JCoDestinationConfig configurations) returns Error? {
        //string destinationId = uuid:createType4AsString();
        string destinationId = "TEST_DESTINATION";
        configurations["destinationId"] = destinationId;
        check initializeBAPIClient(self, configurations);
    }

    # Executes the BAPI/RFC function.
    # 
    # + functionName - The name of the function to be executed.
    # + inputParams - The input parameters for the function.
    # + outputParamType - The output parameters for the function.
    # + return - An error if the execution fails.
    isolated remote function execute(string functionName,record {} inputParams,typedesc<record {}> outputParamType = <>) returns outputParamType|Error? = @java:Method{
        'class: "io.ballerina.lib.sap.BAPIClient"
    } external;
}

isolated function initializeBAPIClient(BAPIClient bapiClient,JCoDestinationConfig configurations) returns Error? = @java:Method {
    'class: "io.ballerina.lib.sap.BAPIClient"
} external;