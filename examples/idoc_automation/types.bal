import ballerina/data.xmldata;

// Define the record for shipment data as fetched from the API
public type ShipmentData record {
    string orderId;
    string productCode;
    int quantity;
    string destination;
};

// Data structure to hold iDoc data
type EDI_DC40 record {
    @xmldata:Attribute
    string SEGMENT = "1";
    string TABNAM = "EDI_DC40";
    string MANDT = "800";
    string DOCNUM;
    string DOCREL = "700";
    string STATUS = "30";
    string DIRECT = "1";
    string OUTMOD = "2";
    string IDOCTYP = "DELVRY03";
    string MESTYP = "DESADV";
    string SNDPOR = "SAPR3";
    string SNDPRT = "LS";
    string SNDPRN = "YOUR_SAP";
    string RCVPOR = "SAPR3";
    string RCVPRT = "LS";
    string RCVPRN = "RECIPIENT_SAP";
};

type E1EDL20 record {
    @xmldata:Attribute
    string SEGMENT = "1";
    string VBELN;
    string NTGEW;
    string GEWEI = "KGM";
    string INCO1;
    string INCO2 = "01";
};

type IDOC record {
    @xmldata:Attribute
    string BEGIN = "1";
    EDI_DC40 EDI_DC40;
    E1EDL20 E1EDL20;
};

type DELVRY03 record {
    IDOC IDOC;
};

