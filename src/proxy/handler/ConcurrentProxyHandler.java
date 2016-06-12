package proxy.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by root on 5/27/16.
 */
public abstract class ConcurrentProxyHandler implements ProxyHandler{

    private SelectionKey otherKey;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private StringBuffer stringBuffer;
    private ConcurrentLinkedDeque<ByteBuffer> writeQueue;

    private boolean terminated;
    private boolean isClient;

    private boolean readyToConnect;
    private String user = null;
    private String pass = null;
    private boolean toClose = false;
    private boolean finishConnect = false;
    private boolean wrongPass = false;

    private static final int BUFFER_SIZE = 1;

    public ConcurrentProxyHandler(){
        this.readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.stringBuffer = new StringBuffer();
        this.terminated = false;
        this.isClient = false;
        this.writeQueue = new ConcurrentLinkedDeque<>();
    }

    public SelectionKey getOtherKey(){
        return this.otherKey;
    }

    public void setOtherKey(SelectionKey key){
        this.otherKey = key;
    }

    public ByteBuffer getReadBuffer(){
        return this.readBuffer;
    }

    public ByteBuffer getWriteBuffer(){
        ByteBuffer buffer = writeQueue.pollFirst();
<<<<<<< HEAD
        //System.out.println("el valor es: ");
        //TODO despues de borrar emails y luego hacer quit, intellj me lanzo un nullpointer y en terminal no se temrina la conexion
        //System.out.println( new String(buffer.array()));
=======
>>>>>>> Multiplexor
        return buffer;
    }

    public void setWriteBuffer(ByteBuffer bb){
        /*this.writeBuffer = bb;
        System.out.println("++++++++++++++");
        //System.out.println("NO EL KEY " + this.getOtherKey().toString());
        System.out.println("Nuevo Write BUffer con:");
        System.out.println(new String(writeBuffer.array()));
        System.out.println("++++++++++++++");*/

        //System.out.println( new String(bb.array()));
        writeQueue.addLast(bb);
    }

    public boolean moreWriteableData(ByteBuffer byteBuffer){
        if(byteBuffer.hasRemaining()){
            byteBuffer.compact();
            writeQueue.addFirst(byteBuffer);
            return true;
        }else{
            return writeQueue.size() != 0;
        }
    }

    public Object getOtherHandler(){
        return otherKey.attachment();
    }

    public boolean isClient(){
        return isClient;
    }

    public void setClient(){
        this.isClient = true;
    }

    public StringBuffer getStringBuffer(){
        return this.stringBuffer;
    }

    public void doneReading(){

    }

    public void doneWriting(){

    }

    public void setTerminated(boolean terminated){
        this.terminated = terminated;
    }

    public boolean isTerminated(){
        return terminated;
    }

    public boolean hasWrittenData(){
        if (((ConcurrentProxyHandler)this.getOtherKey().attachment()).getWriteBuffer().hasRemaining()){
            System.out.println("true lindo");
            return true;
        }
        if(stringBuffer.length()>0){
            System.out.println("true feo");
            transferData();
            return true;
        }
        System.out.println("false horrible");
        return false;
    }

    public void terminate() {
<<<<<<< HEAD
        //System.out.println("SET TERMINATED");
        ((ConcurrentProxyHandler)this.getOtherKey().attachment()).setTerminated(true);
=======
        System.out.println("SET TERMINATED");
        if(otherKey != null) {
            ((ConcurrentProxyHandler) this.getOtherKey().attachment()).setTerminated(true);
        }
>>>>>>> Multiplexor
    }

    public void appendBuffer() {
        //System.out.println("append");
        //if(!readBuffer.hasRemaining()) return;

        readBuffer.flip();
        while(readBuffer.hasRemaining()){
            stringBuffer.append((char) readBuffer.get());
        }

        readBuffer.clear();

        //System.out.println("---String buffer---");
        //System.out.println(stringBuffer.toString());
    }

    public int transferData() {

        ByteBuffer otherWriteBuffer = ByteBuffer.allocate(stringBuffer.length());

        otherWriteBuffer.put(stringBuffer.toString().getBytes());

<<<<<<< HEAD
        ((ConcurrentProxyHandler) getOtherKey().attachment()).setWriteBuffer(otherWriteBuffer);
        //System.out.println("SETEO 0 ");
=======
        ConcurrentProxyHandler otherHandler = ((ConcurrentProxyHandler) getOtherKey().attachment());

        if(((ConcurrentProxyHandler)otherKey.attachment()).getOtherKey() != null){
            otherHandler.setWriteBuffer(otherWriteBuffer);
        }

        System.out.println("SETEO 0 ");
>>>>>>> Multiplexor
        stringBuffer.setLength(0);
        return writeQueue.size();
    }

    public boolean getReadyToConnect(){
        return readyToConnect;
    }
    public void setReadyToConnect(boolean readyToConnect){
        this.readyToConnect = readyToConnect;
    }

    public String getUser(){
        return user;
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getPass(){
        return this.pass;
    }

    public void setPass(String pass){
        this.pass = pass;
    }

    public boolean getToClose(){
        return toClose;
    }

    public void setToClose(boolean toClose){
        this.toClose = toClose;
    }

    public boolean getFinishConnect(){
        return finishConnect;
    }

    public void setFinishConnect(boolean finishConnect){
        this.finishConnect = finishConnect;
    }

    public boolean getWrongPass(){
        return this.wrongPass;
    }

    public void setWrongPass(boolean wrongPass){
        this.wrongPass = wrongPass;
    }

}
