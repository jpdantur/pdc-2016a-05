package proxy.handler;

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
    void transferData();
    boolean isClient();
    void setClient();
    boolean moreWriteableData(ByteBuffer byteBuffer);
    boolean transformBufferDone();
    void resetHandler();


    boolean analizeData();
    void transformData();

}
