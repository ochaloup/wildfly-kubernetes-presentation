package org.jboss.as.quickstarts.ejb.mock;

import org.jboss.logging.Logger;

import javax.transaction.xa.Xid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

final class MockXAResourceStorage {
    private static Logger LOG = Logger.getLogger(MockXAResourceStorage.class);

    private MockXAResourceStorage(File logStorageFile) throws IllegalAccessException {
        throw new IllegalAccessException("utility class, do not instantiate");
    }

    /**
     * Replaces content of the file {@link #getMockXAResourceTxnLogStore}
     * with the specified collection of xids.
     */
    static synchronized void writeToDisk(Collection<Xid> xidsToDisk) {
        Collection<Xid> writtableXidCollection = new ArrayList<>(xidsToDisk);
        File logStorageFile = getMockXAResourceTxnLogStore();
        LOG.infof("writing to disk xids: %s[number: %s] records to %s",
                writtableXidCollection, writtableXidCollection.size(), logStorageFile.getAbsolutePath());

        try (FileOutputStream fos = new FileOutputStream(logStorageFile); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(writtableXidCollection);
        } catch (IOException e) {
            LOG.errorf("Cannot write xids '%s' to file '%s'", writtableXidCollection, logStorageFile, e);
        }
    }

    @SuppressWarnings("unchecked")
    static synchronized Collection<Xid> recoverFromDisk() {
        Collection<Xid> recoveredXids = new HashSet<>();
        File logStorageFile = getMockXAResourceTxnLogStore();

        if (!logStorageFile.exists()) {
            LOG.warnf("file %s does not exists - no data can be recovered", logStorageFile.getAbsolutePath());
            return recoveredXids;
        }

        try (FileInputStream fis = new FileInputStream(logStorageFile); ObjectInputStream ois = new ObjectInputStream(fis)) {
            Collection<Xid> xids = (Collection<Xid>) ois.readObject();
            recoveredXids.addAll(xids);
        } catch (Exception e) {
            LOG.error("Cannot read xids for recovery from file " + logStorageFile, e);
            return recoveredXids;
        }

        LOG.infof("Number of xids for recovery is %d.\nContent: %s", recoveredXids.size(), recoveredXids);
        return recoveredXids;
    }

    private static File getMockXAResourceTxnLogStore() {
        String jbossServerDataDirName = System.getProperty("jboss.server.data.dir");
        File logDir = new File(jbossServerDataDirName);
        if (jbossServerDataDirName == null || !logDir.exists()) {
            throw new IllegalStateException("Cannot get directory for the MockXAResource txn log storage "
                    + "which is expected to be defined by property 'jboss.server.data.dir' and comes with value '" + jbossServerDataDirName + "'");
        }

        File logFile = new File(logDir, MockXAResource.class.getSimpleName());

        LOG.debugf("Using file %s for saving state of the %s XA resource", logFile, MockXAResource.class.getName());
        return logFile;
    }
}
