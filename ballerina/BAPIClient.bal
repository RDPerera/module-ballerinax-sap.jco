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

import ballerina/jballerina.java as java;

# A Ballerina client for SAP BAPI/RFC.
@display {label: "BAPI/RFC Client", iconPath: "icon.png"}
public isolated client class BAPIClient {
    private final readonly & string destinationName;
    private final readonly & string host;
    private final readonly & string systemNumber;
    private final readonly & string jcoClient;
    private final readonly & string user;
    private final readonly & string password;
    private final readonly & string group;
    private final readonly & string language;

    # Initializes the connector.
    # 
    # + configurations - The configurations required to initialize the BAPI client.
    # + return - An error if the initialization fails.
    public isolated function init(BAPIConfigurations configurations) returns Error? {
        self.destinationName = configurations.destinationName;
        self.host = configurations.host;
        self.systemNumber = configurations.systemNumber;
        self.jcoClient = configurations.jcoClient;
        self.user = configurations.user;
        self.password = configurations.password;
        self.group = configurations.group;
        self.language = configurations.language;
        check initializeBAPIClient(self, java:fromString(self.destinationName), java:fromString(self.host), java:fromString(self.systemNumber), java:fromString(self.jcoClient), java:fromString(self.user),
            java:fromString(self.password), java:fromString(self.group), java:fromString(self.language));
    }

    # Executes the BAPI/RFC function.
    # 
    # + functionName - The name of the function to be executed.
    # + inputParams - The input parameters for the function.
    # + outputParams - The output parameters for the function.
    # + return - An error if the execution fails.
    isolated remote function execute(string functionName, map<string> inputParams, string[] outputParams) returns map<string>|Error? = @java:Method{
        'class: "io.ballerina.lib.sap.BAPIClient"
    } external;
}

isolated function initializeBAPIClient(BAPIClient bapiClient,handle destinationName,handle host,handle systemNumber,handle jcoClient,handle user,handle password,handle group,handle language) returns Error? = @java:Method {
    'class: "io.ballerina.lib.sap.BAPIClient"
} external;