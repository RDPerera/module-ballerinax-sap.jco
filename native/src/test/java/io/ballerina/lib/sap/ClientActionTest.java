package io.ballerina.lib.sap;

import io.ballerina.runtime.api.*;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.Parameter;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.lib.sap.RFCClient.execute;


public class ClientActionTest {

    @Test
    void invokeRFC() {
        execute(new Environment() {
            @Override
            public String getFunctionName() {
                return null;
            }

            @Override
            public Parameter[] getFunctionPathParameters() {
                return new Parameter[0];
            }

            @Override
            public Future markAsync() {
                return null;
            }

            @Override
            public Runtime getRuntime() {
                return null;
            }

            @Override
            public Module getCurrentModule() {
                return null;
            }

            @Override
            public int getStrandId() {
                return 0;
            }

            @Override
            public Optional<String> getStrandName() {
                return Optional.empty();
            }

            @Override
            public StrandMetadata getStrandMetadata() {
                return null;
            }

            @Override
            public void setStrandLocal(String s, Object o) {

            }

            @Override
            public Object getStrandLocal(String s) {
                return null;
            }

            @Override
            public Repository getRepository() {
                return null;
            }
        });
    }


}
