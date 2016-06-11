package proxy.server;

import proxy.handler.ProxyHandler;
import proxy.utils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by root on 5/27/16.
 */
public class Worker implements Runnable{

    private SelectionKey key;
    private ServerTools serverTools;

    public Worker(SelectionKey key, ServerTools serverTools){
        this.key = key;
        this.serverTools = serverTools;
    }


    @Override
    public void run() {
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

    /*private void handleAccept(SelectionKey key) throws IOException{
        System.out.println("---Handling Accept---");

        SocketChannel clientAndProxy = ((ServerSocketChannel) key.channel()).accept();
        if(clientAndProxy == null) return;

        SocketChannel proxyAndServer = SocketChannel.open();

        clientAndProxy.configureBlocking(false);
        proxyAndServer.configureBlocking(false);

        proxyAndServer.connect(new InetSocketAddress("pop.fibertel.com.ar", 110));

        ProxyHandler handlerClient = serverTools.getNewHandler();
        ProxyHandler handlerServer = serverTools.getNewHandler();

        handlerClient.setClient();

        serverTools.queue(new QueuedRegister(clientAndProxy, proxyAndServer, key.selector(), handlerClient, handlerServer));
        serverTools.queue(new QueuedKey(key, SelectionKey.OP_ACCEPT));
    }*/

    private void handleAccept(SelectionKey key) throws IOException{
        System.out.println("---Handling Accept---");

        SocketChannel clientAndProxy = ((ServerSocketChannel) key.channel()).accept();
        if(clientAndProxy == null) return;

        //SocketChannel proxyAndServer = SocketChannel.open();

        clientAndProxy.configureBlocking(false);
        //proxyAndServer.configureBlocking(false);

        //proxyAndServer.connect(new InetSocketAddress("pop.fibertel.com.ar", 110));

        ProxyHandler handlerClient = serverTools.getNewHandler();
        //ProxyHandler handlerServer = serverTools.getNewHandler();

        handlerClient.setClient();

        serverTools.queue(new QueuedRegisterKey(SelectionKey.OP_READ, clientAndProxy, key.selector(), handlerClient));
        serverTools.queue(new QueuedKey(key, SelectionKey.OP_ACCEPT));
    }



    private void handleConnect(SelectionKey key) throws IOException{
        System.out.println("---Handling Connect---");

        SocketChannel socketChannel = (SocketChannel) key.channel();
        if(socketChannel.finishConnect()){
            serverTools.queue(new QueuedKey(key, SelectionKey.OP_READ));
            SelectionKey otherKey = ((ProxyHandler)key.attachment()).getOtherKey();
            serverTools.queue(new QueuedKey(otherKey, SelectionKey.OP_READ));
        }else{
            serverTools.queue(new QueuedKey(key, SelectionKey.OP_CONNECT));
        }
    }

    private void handleRead(SelectionKey key) throws IOException{
        System.out.println("---Handling Read---" + key.toString());
        ProxyHandler handler = (ProxyHandler)key.attachment();

        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = handler.getReadBuffer();

        long bytesRead = channel.read(buffer);

        if(bytesRead == 0){
            serverTools.queue(new QueuedKey(key, SelectionKey.OP_READ));
            return;
        }

        if(bytesRead>0){
            handler.appendBuffer();

            if(handler.analizeData()){
               // handler.appendBuffer();
                if(handler.transformBufferDone() && handler.getOtherKey() != null){
                    handler.resetHandler();
                    handler.transferData();
                    serverTools.queue(new QueuedKey(handler.getOtherKey(), SelectionKey.OP_WRITE));
                }
            }else{
                if(handler.getOtherKey() != null) {
                    handler.resetHandler();
                    handler.transferData();
                    serverTools.queue(new QueuedKey(handler.getOtherKey(), SelectionKey.OP_WRITE));
                }
            }

            serverTools.queue(new QueuedKey(key, SelectionKey.OP_READ));
        }else{
            //if(handler.hasWrittenData()){
                System.out.println("-** Setting Terminated **-");
                //handler.transferData();
                handler.terminate();
                serverTools.queue(new QueuedKey(handler.getOtherKey(), SelectionKey.OP_WRITE));
            /*}else{
                System.out.println("-*- Closing Channels (read) -*-");
                handler.getOtherKey().channel().close();
                handler.getOtherKey().cancel();
            }*/
            channel.close();
            key.cancel();
        }

        handler.doneReading();

    }

    private void handleWrite(SelectionKey key) throws IOException{
        System.out.println("---Handling Write---" + key.toString());
        ProxyHandler handler = (ProxyHandler) key.attachment();

        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = handler.getWriteBuffer();

        if(buffer==null){
            if(handler.isTerminated()){
                System.out.println("-*- Closing Channels (write) -*-");
                channel.close();
                key.cancel();
            }
            return;
        }

        buffer.flip();
        channel.write(buffer);

        if(handler.moreWriteableData(buffer)){
            serverTools.queue(new QueuedKey(key, SelectionKey.OP_WRITE));
        }else{
            System.out.println("no remaining");
            if(handler.isTerminated()){
                System.out.println("-*- Closing Channels (write) -*-");
                channel.close();
                key.cancel();
            }else{
                serverTools.queue(new QueuedKey(key, SelectionKey.OP_READ));
            }
        }

        handler.doneWriting();
    }


}
