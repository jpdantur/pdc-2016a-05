package proxy.handler;

import administrator.Configuration;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Created by root on 5/30/16.
 */
public interface ProxyHandler {

    SelectionKey getOtherKey();
    void setOtherKey(SelectionKey key);
    ByteBuffer getReadBuffer();
    ByteBuffer getWriteBuffer();
    void setWriteBuffer(ByteBuffer bb);
    StringBuffer getStringBuffer();
    void doneReading();
    void doneWriting();
    void setTerminated(boolean terminated);
    boolean isTerminated();
    boolean hasWrittenData();
    void terminate();
    void appendBuffer();
    int transferData();
    boolean isClient();
    void setClient();
    boolean moreWriteableData(ByteBuffer byteBuffer);
    boolean transformBufferDone();
    void resetHandler();
    boolean getReadyToConnect();
    void setReadyToConnect(boolean readyToConnect);
    String getUser();
    void setUser(String user);
    String getPass();
    void setPass(String pass);
    boolean getFinishConnect();
    void setFinishConnect(boolean finishConnect);
    boolean getWrongPass();
    void setWrongPass(boolean wrongPass);

    int writeBufferListSize();

    void setLastLine(String lastLine);
    String getLastLine();

    String getFirstLine();

    void setFirstLine(String firstLine);

    Configuration getConfig();

    boolean getToClose();
    void setToClose(boolean setToClose);

    boolean analizeData();
    void transformData();

}
