package server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Created by lelv on 5/20/16.
 */
public class KeyData {

    private SelectionKey selectionKey;
    private ByteBuffer byteBuffer;
    private boolean connected;
    private boolean terminated;
    private boolean transform;
    private StringBuffer stringBuffer = null;

    public KeyData(ByteBuffer bb){
        this.selectionKey = null;
        this.byteBuffer = bb;
        this.connected = true;
        this.terminated = false;
        this.transform = false;
    }

    public KeyData(SelectionKey sk, ByteBuffer bb){
        this.selectionKey = sk;
        this.byteBuffer = bb;
        this.connected = false;
        this.terminated = false;
        this.transform = true;
        this.stringBuffer = new StringBuffer();
    }

    public SelectionKey getSelectionKey(){
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey sk){
        this.selectionKey = sk;
    }

    public ByteBuffer getByteBuffer(){
        return this.byteBuffer;
    }

    public boolean isConnected(){
        return connected;
    }

    public void setConnected(boolean connected){
        this.connected = connected;
    }

    public void setTerminated(){
        ((KeyData)this.selectionKey.attachment()).terminate();
    }

    private void terminate(){
        this.terminated = true;
    }

    public boolean isTerminated(){
        return this.terminated;
    }

    public boolean isTransform(){
        return this.transform;
    }

    public void append(ByteBuffer buffer){
        System.out.println("append");
        if(!buffer.hasRemaining()) return;

        buffer.flip();
        while(buffer.hasRemaining()){
            stringBuffer.append((char) buffer.get());
        }

        System.out.println("---String buffer---");
        System.out.println(stringBuffer.toString());
    }

}
