package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lelv on 5/21/16.
 */
public class PopHandler implements Runnable{

    private SelectionKey key;
    private Queueable queueable;


    public PopHandler(SelectionKey key, Queueable queueable){
        this.key = key;
        this.queueable= queueable;
    }

    public void run(){
        System.out.println("Nace thread: " + Thread.currentThread().getId());
        try {
            if(!key.isValid()) return;

            if(key.isAcceptable()){
                handleAccept(key);
            }else if(key.isConnectable()){
                handleConnect(key);
            }else if (key.isReadable()){
                handleRead(key);
            }else if (key.isValid() && key.isWritable()){
                handleWrite(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Muere thread: " + Thread.currentThread().getId());
    }

    public void handleAccept(SelectionKey key) throws IOException {
        System.out.println("---Handling Accept---");

        SocketChannel clientToProxy = ((ServerSocketChannel) key.channel()).accept();
        if(clientToProxy == null) return;

        SocketChannel proxyToServer = SocketChannel.open();

        clientToProxy.configureBlocking(false);
        proxyToServer.configureBlocking(false);

        proxyToServer.connect(new InetSocketAddress("localhost", 110));

        queueable.queue(new QueuedRegister(clientToProxy, proxyToServer, key.selector()));
        queueable.queue(new QueuedKey(key, SelectionKey.OP_ACCEPT));
        //key.selector().wakeup();
    }

    public void handleConnect(SelectionKey key) throws IOException {
        System.out.println("---Handling Connect---");
        SocketChannel socketChannel = (SocketChannel) key.channel();
        if(socketChannel.finishConnect()){
            ((KeyData)key.attachment()).setConnected(true);
            //key.interestOps(SelectionKey.OP_READ);
            queueable.queue(new QueuedKey(key, SelectionKey.OP_READ));

        }else{
            queueable.queue(new QueuedKey(key, SelectionKey.OP_CONNECT));
        }

    }

    public void handleRead(SelectionKey key) throws IOException {
        System.out.println("---Handling Read--- " + key.toString());
        KeyData nc = (KeyData) key.attachment();

        if(!nc.isConnected()) return;

        SelectionKey otherKey = nc.getSelectionKey();
        SocketChannel actualChannel = (SocketChannel) key.channel();

        ByteBuffer buf = nc.getByteBuffer();
        long bytesRead = actualChannel.read(buf);

        System.out.println(new String(buf.array()));
        if(bytesRead > 0){
            if(nc.isTransform()) {
                //nc.append(buf);
            }
            //otherKey.interestOps(SelectionKey.OP_WRITE);
            queueable.queue(new QueuedKey(otherKey, SelectionKey.OP_WRITE));
        }

        if(bytesRead == -1){
            actualChannel.close();
            key.cancel();

            if(buf.hasRemaining()){
                System.out.println("-** Setting terminated **-");
                nc.setTerminated();
                //otherKey.interestOps(SelectionKey.OP_WRITE);
                queueable.queue(new QueuedKey(otherKey, SelectionKey.OP_WRITE));
            }else{
                System.out.println("-*- Closing Channels (read)-*-");
                otherKey.channel().close();
                otherKey.cancel();

            }



        }else{
            queueable.queue(new QueuedKey(key, SelectionKey.OP_READ));
        }



    }

    public void handleWrite(SelectionKey key) throws IOException {
        System.out.println("---Handling Write--- " + key.toString());

        KeyData nc = (KeyData) key.attachment();
        SocketChannel actualChannel = (SocketChannel) key.channel();
        ByteBuffer buf = nc.getByteBuffer();
        buf.flip();
        actualChannel.write(buf);

        if (!buf.hasRemaining()) {
            if(nc.isTerminated()){
                System.out.println("-*- Closing Channels (write) -*-");
                actualChannel.close();
                key.cancel();
                return;
            }
            //key.interestOps(SelectionKey.OP_READ);
            queueable.queue(new QueuedKey(key, SelectionKey.OP_READ));
        }else{
            queueable.queue(new QueuedKey(key, SelectionKey.OP_WRITE));
        }

        buf.compact();

    }
}
