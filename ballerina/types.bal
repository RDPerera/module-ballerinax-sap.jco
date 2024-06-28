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

# Holds the configuration details needed to create a BAPI connection.
#
# + destinationName - The destination name.
# + host - The SAP host.
# + systemNumber - The SAP system number.
# + jcoClient - The SAP client number.
# + user - The SAP user.
# + password - The SAP password.
# + language - The language key.
# + group - The group name (default: PUBLIC).
public type BAPIConfigurations record {
    @display {label: "Destination Name"}
    string destinationName;
    @display {label: "Host"}
    string host;
    @display {label: "System Number"}
    string systemNumber;
    @display {label: "Client"}
    string jcoClient;
    @display {label: "User"}
    string user;
    @display {label: "Password"}
    string password;
    @display {label: "Language"}
    string language="EN";
    @display {label: "Group"}
    string group="PUBLIC";
};