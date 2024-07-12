import ballerina/io;
import ballerina/xmldata;
import ballerinax/sap.jco;

// Configurable variables to hold the configuration values from Config.toml
configurable jco:DestinationConfig sapConfig = ?;

// Initialize iDoc listener
listener jco:Listener idocListener = new (sapConfig);

// Service to process incoming iDocs
service jco:Service on idocListener {
    remote function onIDoc(xml iDoc) returns error? {
        // Parse iDoc XML to iDoc record
        ORDERS05 iDocRecord = check xmldata:fromXml(iDoc);

        // Transform iDoc to internal order format
        InternalOrder internalOrder = transform(iDocRecord);

        // Process the internal order in inventory system (logic not shown)
        check processOrder(internalOrder);
    }

    remote function onError(error err) returns error? {
        io:println("Error processing iDoc: ", err.message());
    }
}

function transform(ORDERS05 orders05) returns InternalOrder => {
    quantity: (orders05.IDOC.E1EDP01).length(),
    supplierId: orders05.IDOC.EDI_DC40.SNDPRT,
    partId: orders05.IDOC.EDI_DC40.DOCNUM,
    orderDate: orders05.IDOC.E1EDK01.DOCDAT
};
