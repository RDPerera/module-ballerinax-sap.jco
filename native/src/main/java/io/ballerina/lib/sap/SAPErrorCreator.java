package io.ballerina.lib.sap;
import com.sap.conn.jco.JCoException;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;

import static io.ballerina.lib.sap.ModuleUtils.getModule;
public class SAPErrorCreator {
    public static BError fromJCoException(JCoException e) {
        return fromJavaException("JCo Error: " + e.getMessage(), e);
    }
    private static BError fromJavaException(String message, Throwable cause) {
        return fromBError(message, ErrorCreator.createError(cause));
    }
    public static BError fromBError(String message, BError cause) {
        return ErrorCreator.createDistinctError("JCo Error: ", getModule(), StringUtils.fromString(message), cause);
    }
}