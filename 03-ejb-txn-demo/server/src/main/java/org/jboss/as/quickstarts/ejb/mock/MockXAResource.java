package org.jboss.as.quickstarts.ejb.mock;

import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.logging.Logger;

/**
 * Mock {@link XAResource} which can be used for enlisting it to a transaction
 * and asking two-phase commit to be run.
 *
 * Reason of existence:
 * A transaction normally uses 1PC optimization when only one resource (e.g. only database perist)
 * is part of the transaction. When 1PC optimization is used then only XA commit call is invoked.
 * For transaction recovery could be verified we need 2PC which is usually used with two and more
 * enlisted resources.
 * The other reason for having this XAResource implemented is the fact that it could be used
 * for intentionally establishing an error condition during transaction processing, see the {@link TestAction}.
 *
 * Usability:
 * Keep in mind this XAResource is for the demonstration and testing purposes.
 * It's not capable to do any persistence over JVM restarts and it's not considered by the recovery processing.
 */
public class MockXAResource implements XAResource, Serializable {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(MockXAResource.class);

    static final Collection<Xid> preparedXids = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger commitCount = new AtomicInteger();

    public enum TestAction {
        NONE,
        PREPARE_THROW_XAER_RMERR, PREPARE_THROW_XAER_RMFAIL, PREPARE_THROW_UNKNOWN_XA_EXCEPTION,
        COMMIT_THROW_XAER_RMERR, COMMIT_THROW_XAER_RMFAIL, COMMIT_THROW_UNKNOWN_XA_EXCEPTION,
        PREPARE_JVM_HALT, COMMIT_JVM_HALT
    }

    protected TestAction testAction;
    private int transactionTimeout;

    public MockXAResource() {
        this(TestAction.NONE);
    }

    public MockXAResource(TestAction testAction) {
        log.debugf("Creating %s with test action %s", this, testAction);
        this.testAction = testAction;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        log.debugf("prepare '%s' xid: [%s]", this, xid);

        switch (testAction) {
            case PREPARE_THROW_XAER_RMERR:
                log.debugf("at prepare '%s' throws XAException(XAException.XAER_RMERR)", xid);
                throw new XAException(XAException.XAER_RMERR);
            case PREPARE_THROW_XAER_RMFAIL:
                log.debugf("at prepare '%s' throws XAException(XAException.XAER_RMFAIL)", xid);
                throw new XAException(XAException.XAER_RMFAIL);
            case PREPARE_THROW_UNKNOWN_XA_EXCEPTION:
                log.debugf("at prepare '%s' throws XAException(null)", xid);
                throw new XAException(null);
            case PREPARE_JVM_HALT:
                log.debugf("at prepare '%s' halting JVM", xid);
                Runtime.getRuntime().halt(1);
            case NONE:
            default:
                preparedXids.add(xid);
                MockXAResourceStorage.writeToDisk(preparedXids);
                return XAResource.XA_OK;
        }
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        log.debugf("commit '%s' xid:[%s], %s one phase", this, xid, onePhase ? "with" : "without");

        switch (testAction) {
            case COMMIT_THROW_XAER_RMERR:
                log.debugf("at commit '%s' throws XAException(XAException.XAER_RMERR)", xid);
                throw new XAException(XAException.XAER_RMERR);
            case COMMIT_THROW_XAER_RMFAIL:
                log.debugf("at commit '%s' throws XAException(XAException.XAER_RMFAIL)", xid);
                throw new XAException(XAException.XAER_RMFAIL);
            case COMMIT_THROW_UNKNOWN_XA_EXCEPTION:
                log.debugf("at commit '%s' throws XAException(null)", xid);
                throw new XAException(null);
            case COMMIT_JVM_HALT:
                log.debugf("at commit '%s' halting JVM", xid);
                Runtime.getRuntime().halt(1);
            case NONE:
            default:
                preparedXids.remove(xid);
                MockXAResourceStorage.writeToDisk(preparedXids);
        }

        log.tracef("Number of successful commit for MockXAResource is: %d",
                commitCount.incrementAndGet());
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        log.debugf("rollback '%s' xid: [%s]", this, xid);
        preparedXids.remove(xid);
        MockXAResourceStorage.writeToDisk(preparedXids);
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        log.debugf("start '%s' xid: [%s], flags: %s", this, xid, flags);
        // currentXid = xid;
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        log.debugf("end '%s' xid:[%s], flag: %s", this, xid, flags);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        log.debugf("forget '%s' xid:[%s]", this, xid);
        preparedXids.remove(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        log.debugf("getTransactionTimeout: '%s' returning timeout: %s", this, transactionTimeout);
        return transactionTimeout;
    }

    @Override
    public boolean isSameRM(XAResource xares) throws XAException {
        log.tracef("isSameRM returning false to xares: %s", xares);
        return false;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        log.debugf("recover '%s' with flags: %s, returning list of xids '%s'", this, flag, preparedXids);
        return preparedXids.toArray(new Xid[preparedXids.size()]);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        log.tracef("setTransactionTimeout: setting timeout: %s", seconds);
        this.transactionTimeout = seconds;
        return true;
    }

    /**
     * Returns number of successfully committed {@link MockXAResource}s.
     *
     * @return number of committed.
     */
    public static int getCommitCount() {
        return MockXAResource.commitCount.get();
    }
}
