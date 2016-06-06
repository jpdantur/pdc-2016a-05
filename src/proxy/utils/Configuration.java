package proxy.utils;

/**
 * Created by matias on 6/5/16.
 */
public class Configuration {
    private static Configuration instance = null;
    private static Properties prop;
    private static long bytesSend;
    private static long bytesRcvd;

    protected Configuration() {
        this.prop = JAXBParser.getProperties();
    }
    public static Configuration getInstance() {
        bytesRcvd = 0;
        bytesSend = 0;
        if(instance == null) {
            instance = new Configuration();
        }

        return instance;
    }

    public Properties getProp() {
        return this.prop;
    }

    public void addBytesRcvd(long bytes) {
        bytesRcvd += bytes;
    }

    public void addBytesSend(long bytes) {
        bytesSend += bytes;
    }

    public long getBytesSend() {
        return bytesSend;
    }

    public long getBytesRcvd() {
        return bytesRcvd;
    }
}
