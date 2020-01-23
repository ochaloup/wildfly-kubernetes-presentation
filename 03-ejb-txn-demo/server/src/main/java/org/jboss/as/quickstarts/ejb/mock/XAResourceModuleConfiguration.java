package org.jboss.as.quickstarts.ejb.mock;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.transaction.xa.XAResource;
import java.util.Vector;

/**
 * For {@link com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule}
 * would be configured for our test {@link javax.transaction.xa.XAResource}
 */
@Singleton
@Startup
public class XAResourceModuleConfiguration {
    private static final Logger log = Logger.getLogger(XAResourceModuleConfiguration.class);

    @PostConstruct
    public void onStartup(){
        XAResourceModuleConfiguration.MockXAResourceRecoveryHelper.getRecoveryModule()
                .addXAResourceRecoveryHelper(MockXAResourceRecoveryHelper.INSTANCE);
        log.infof("XAResourceRecovery module for MockXAResource configured");
        MockXAResource.preparedXids.addAll(MockXAResourceStorage.recoverFromDisk());
    }

    /**
     * Instance of {@link XAResourceRecoveryHelper} which gives a chance to the Narayana recovery manager
     * to load the {@link MockXAResource} during recovery.
     *
     * A note: for this would work fine after deployment to WildFly we need to define
     * dependency of jboss transaction module 'org.jboss.jts' for this war module.
     * This is done by descriptor jboss-deployment-structure.xml.
     */
    static class MockXAResourceRecoveryHelper implements XAResourceRecoveryHelper {
        static final MockXAResourceRecoveryHelper INSTANCE = new MockXAResourceRecoveryHelper();
        private static final MockXAResource mockXARecoveringInstance = new MockXAResource();

        private MockXAResourceRecoveryHelper() {
            if(INSTANCE != null) {
                throw new IllegalStateException("singleton instance can't be instantiated twice");
            }
        }

        @Override
        public boolean initialise(String p) throws Exception {
            // this is never called during standard processing
            return true;
        }

        @Override
        public XAResource[] getXAResources() throws Exception {
            return new XAResource[] { mockXARecoveringInstance };
        }

        static XARecoveryModule getRecoveryModule() {
            for (RecoveryModule recoveryModule : ((Vector<RecoveryModule>) RecoveryManager.manager().getModules())) {
                if (recoveryModule instanceof XARecoveryModule) {
                    return (XARecoveryModule) recoveryModule;
                }
            }
            return null;
        }
    }
}
