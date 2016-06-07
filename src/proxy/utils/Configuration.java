package proxy.utils;

/**
 * Created by matias on 6/5/16.
 */
public class Configuration {
    private static Configuration instance = null;
    private static Properties prop;
    private static long bytesTransferred;
    private static long accesses;

    protected Configuration() {
        this.prop = JAXBParser.getProperties();
    }
    public static Configuration getInstance() {
        bytesTransferred = 0;
        accesses = 0;
        if(instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public Properties getProp() {
        return this.prop;
    }

    public void addBytesTransferred(long bytes) {
        bytesTransferred += bytes;
    }

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public void addAccess() {
        accesses += 1;
    }

    public long getAccesses() { return accesses; }
}
