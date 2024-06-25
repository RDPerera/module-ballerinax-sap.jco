package io.ballerina.lib.sap;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static com.sap.conn.jco.ext.DataProviderException.Reason.IO_ERROR;

public class BallerinaDestinationDataProvider implements DestinationDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaDestinationDataProvider.class);

    @Override
    public Properties getDestinationProperties(String s) throws DataProviderException {
        Properties props = new Properties();

        URL resource = getClass().getClassLoader().getResource("cloud.jcoDestination");
        File file = new File(resource.getFile());
        try (FileInputStream input = new FileInputStream(file)) {
            props.load(input);
            return props;
        } catch (IOException e) {
            logger.error("Error while loading server configuration from: " + file.getPath(), e);
            throw new DataProviderException(IO_ERROR, "Destination file not found", e);
        }
    }

    @Override
    public boolean supportsEvents() {
        return false;
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener destinationDataEventListener) {

    }
}
