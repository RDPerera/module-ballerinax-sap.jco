package io.ballerina.lib.sap;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BallerinaDestinationDataProvider implements DestinationDataProvider {
    private final Map<String, Properties> destinationProperties = new HashMap<>();

    @Override
    public Properties getDestinationProperties(String destinationName) {
        if (destinationProperties.containsKey(destinationName)) {
            return destinationProperties.get(destinationName);
        } else {
            throw new RuntimeException("Destination " + destinationName + " not found");
        }
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
    }

    @Override
    public boolean supportsEvents() {
        return true;
    }

    public void addDestination(String destinationName, String host, String systemNumber, String client,
                               String user, String password, String group, String language) {
        Properties properties = new Properties();
        properties.setProperty(DestinationDataProvider.JCO_ASHOST, host);
        properties.setProperty(DestinationDataProvider.JCO_SYSNR, systemNumber);
        properties.setProperty(DestinationDataProvider.JCO_CLIENT, client);
        properties.setProperty(DestinationDataProvider.JCO_USER, user);
        properties.setProperty(DestinationDataProvider.JCO_PASSWD, password);
        properties.setProperty(DestinationDataProvider.JCO_LANG, language);
        properties.setProperty(DestinationDataProvider.JCO_GROUP, group);

        destinationProperties.put(destinationName, properties);
    }

    public void removeDestination(String destinationName) {
        destinationProperties.remove(destinationName);
    }
}
