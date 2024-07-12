import ballerina/data.xmldata;
import ballerina/http;
import ballerina/io;
import ballerinax/sap.jco;

configurable jco:DestinationConfig config = ?;
configurable string apiEndpoint = ?;

public function main() returns error? {

    // Initialize SAP client and Logistics API client
    jco:Client sapClient = check new (config);
    http:Client logisticsApi = check new (apiEndpoint);

    // Get latest shipment data from logistics API
    ShipmentData[] shipments = check logisticsApi->get("/shipments/latest");

    // Iterate through the shipment data and send iDocs to SAP
    foreach ShipmentData shipment in shipments {

        // Transform shipment data to iDoc format
        DELVRY03 iDocRecord = transform(shipment);

        // Convert iDoc record to XML
        xml iDoc = check xmldata:toXml(iDocRecord);

        // Write iDoc to a file and send to SAP
        check io:fileWriteXml("resources/generated_iDocs/" + shipment.orderId + ".xml", iDoc);
        check sapClient->sendIDoc(iDoc.toString());
        io:println("iDoc sent for Order ID: ", shipment.orderId);
    }
}

function transform(ShipmentData shipmentData) returns DELVRY03 => {
    IDOC: {
        EDI_DC40: {
            DOCNUM: shipmentData.orderId
        },
        E1EDL20: {
            VBELN: shipmentData.orderId,
            NTGEW: shipmentData.quantity.toString(),
            INCO1: shipmentData.destination
        }
    }

};
