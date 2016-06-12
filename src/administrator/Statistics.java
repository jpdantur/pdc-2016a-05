package administrator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matias on 6/11/16.
 */
public class Statistics {
    private static Statistics instance = null;
    private static AtomicLong bytesTransferred;
    private static AtomicLong accesses;

    protected Statistics() {
    }

    public static Statistics getInstance() {
        if(instance == null) {
            bytesTransferred = new AtomicLong(0);
            accesses = new AtomicLong(0);
            instance = new Statistics();
        }
        return instance;
    }

    public long getBytesTransferred() {
        return bytesTransferred.get();
    }

    public void addBytesTransferred(long bytesTrans) {
        Statistics.bytesTransferred.addAndGet(bytesTrans);
    }

    public long getAccesses() {
        return accesses.get();
    }

    public void addAccess() {
        Statistics.accesses.incrementAndGet();
    }
}
