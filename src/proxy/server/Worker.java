package proxy.server;

import administrator.Configuration;
import administrator.Statistics;
import org.apache.log4j.Logger;
import proxy.handler.ProxyHandler;
import proxy.utils.*;

import javax.print.attribute.standard.MediaSize;
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
    private final static Logger logger = Logger.getLogger(Worker.class);
    private SelectionKey key;
    private ServerTools serverTools;
    Statistics stat;
    Configuration config;

    public Worker(SelectionKey key, ServerTools serverTools){
        this.key = key;
        this.serverTools = serverTools;
        stat = Statistics.getInstance();
        config = Configuration.getInstance();
    }

    @Override
    public void run() {
        //System.out.println("Nace thread: " + Thread.currentThread().getId());
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
            logger.debug(e.getStackTrace());
        }
        //System.out.println("Muere thread: " + Thread.currentThread().getId());
    }

    private void handleAccept(SelectionKey key) throws IOException {
        System.out.println("---Handling Accept---");

        SocketChannel clientAndProxy = ((ServerSocketChannel) key.channel()).accept();
        if (clientAndProxy == null) return;

        SocketChannel proxyAndServer = SocketChannel.open();

        clientAndProxy.configureBlocking(false);
        proxyAndServer.configureBlocking(false);

        proxyAndServer.connect(new InetSocketAddress("localhost", 110));

        ProxyHandler handlerClient = serverTools.getNewHandler();
        ProxyHandler handlerServer = serverTools.getNewHandler();
        handlerClient.setClient();

        //access BIEN COLOCADO ACA :V
        stat.addAccess();

        serverTools.queue(new QueuedRegister(clientAndProxy, proxyAndServer, key.selector(), handlerClient, handlerServer));
        serverTools.queue(new QueuedKey(key, SelectionKey.OP_ACCEPT));
    }

//    private void handleAccept(SelectionKey key) throws IOException{
//        System.out.println("---Handling Accept---");
//
//        SocketChannel clientAndProxy = ((ServerSocketChannel) key.channel()).accept();
//        if(clientAndProxy == null) return;
//
//        //SocketChannel proxyAndServer = SocketChannel.open();
//
//        clientAndProxy.configureBlocking(false);
//        //proxyAndServer.configureBlocking(false);
//
//        //proxyAndServer.connect(new InetSocketAddress("pop.fibertel.com.ar", 110));
//
//        ProxyHandler handlerClient = serverTools.getNewHandler();
//        //ProxyHandler handlerServer = serverTools.getNewHandler();
//
//        handlerClient.setClient();
//
//        ByteBuffer bb = ByteBuffer.wrap("+OK ready\r\n".getBytes());
//        bb.compact();
//
//        handlerClient.setWriteBuffer(bb);
//
//        serverTools.queue(new QueuedRegisterKey(SelectionKey.OP_WRITE, clientAndProxy, key.selector(), handlerClient));
//        serverTools.queue(new QueuedKey(key, SelectionKey.OP_ACCEPT));
//    }



    private void handleConnect(SelectionKey key) throws IOException{
        //System.out.println("---Handling Connect---");

        SocketChannel socketChannel = (SocketChannel) key.channel();
        if(socketChannel.finishConnect()){
            ProxyHandler handler = ((ProxyHandler)key.attachment());
            SelectionKey otherKey = handler.getOtherKey();
            ProxyHandler otherHandler = ((ProxyHandler)otherKey.attachment());
            String user = otherHandler.getUser();
            String pass = otherHandler.getPass();

            ByteBuffer bbUser = ByteBuffer.wrap(("user " + user + "\r\n").getBytes());
            bbUser.compact();
            handler.setWriteBuffer(bbUser);

            ByteBuffer bbPass = ByteBuffer.wrap(("pass " + pass + "\r\n").getBytes());
            bbPass.compact();
            handler.setWriteBuffer(bbPass);



            serverTools.queue(new QueuedKey(key, SelectionKey.OP_WRITE));
            serverTools.queue(new QueuedKey(otherKey, SelectionKey.OP_READ));
        }else{
            serverTools.queue(new QueuedKey(key, SelectionKey.OP_CONNECT));
        }
    }

    private void handleRead(SelectionKey key) throws IOException{
        //System.out.println("---Handling Read---" + key.toString());
        ProxyHandler handler = (ProxyHandler)key.attachment();

        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = handler.getReadBuffer();

        long bytesRead = channel.read(buffer);

        if (bytesRead == 0) {
            serverTools.queue(new QueuedKey(key, SelectionKey.OP_READ));
            return;
        }

<<<<<<< HEAD
        //System.out.println("AGREGO access en HANDLEREAD.");

        if(bytesRead>0){
=======
        if (bytesRead > 0) {
>>>>>>> Multiplexor
            handler.appendBuffer();

            if (handler.analizeData()) {
                // handler.appendBuffer();

                if (handler.transformBufferDone() && handler.getOtherKey() != null) {
                    handler.resetHandler();
                    handler.transferData();
                    serverTools.queue(new QueuedKey(handler.getOtherKey(), SelectionKey.OP_WRITE));
                }
            } else {
                if(handler.getFinishConnect()){
                    ((ProxyHandler)handler.getOtherKey().attachment()).setOtherKey(key);
                } else if(handler.getWrongPass()){

                    handler.setWrongPass(false);
                    ProxyHandler otherHandler = ((ProxyHandler)handler.getOtherKey().attachment());
                    otherHandler.setUser(null);
                    otherHandler.setPass(null);
                    serverTools.queue(new QueuedKey(handler.getOtherKey(), SelectionKey.OP_WRITE));
                    handler.setOtherKey(null);
                    channel.close();
                    key.cancel();
                    return;
                }
                if (handler.getOtherKey() != null) {
                    handler.resetHandler();
                    handler.transferData();
                    serverTools.queue(new QueuedKey(handler.getOtherKey(), SelectionKey.OP_WRITE));
                } else if (!handler.getReadyToConnect()) {
                    serverTools.queue(new QueuedKey(key, SelectionKey.OP_WRITE));
                    if(handler.getToClose()){
                        handler.setTerminated(true);
                    }
                    return;
                }
            }

            if (handler.getReadyToConnect()) {

                handler.setReadyToConnect(false);

                //falta que me fije a que servidor !!!!
                SocketChannel proxyAndServer = SocketChannel.open();
                proxyAndServer.configureBlocking(false);
<<<<<<< HEAD
                proxyAndServer.connect(new InetSocketAddress(config.getConfiguration().getServername(),
                        config.getConfiguration().getPOP3port()));
//              proxyAndServer.connect(new InetSocketAddress("pop.fibertel.com.ar", 110));
=======
                proxyAndServer.connect(new InetSocketAddress("localhost", 110));
>>>>>>> Multiplexor
                ProxyHandler handlerServer = serverTools.getNewHandler();

                serverTools.queue(new QueuedRegisterKey(SelectionKey.OP_CONNECT, proxyAndServer, key.selector(), handlerServer, key));
            }

            serverTools.queue(new QueuedKey(key, SelectionKey.OP_READ));
        } else {
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
            logger.debug("channel close OF " + key.toString());
            key.cancel();
        }

        handler.doneReading();

    }

    private void handleWrite(SelectionKey key) throws IOException{
        //System.out.println("---Handling Write---" + key.toString());
        ProxyHandler handler = (ProxyHandler) key.attachment();

        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = handler.getWriteBuffer();

        if(buffer==null){
            if(handler.isTerminated()){
                //System.out.println("-*- Closing Channels (write) -*-");
                logger.debug("channel close OF " + key.toString());
                channel.close();
                key.cancel();
            }
            return;
        }
        buffer.flip();
        long bytesWritten = channel.write(buffer);

        stat.addBytesTransferred(bytesWritten);

        if(handler.moreWriteableData(buffer)){
            serverTools.queue(new QueuedKey(key, SelectionKey.OP_WRITE));
        }else{
            //System.out.println("no remaining");
            if(handler.isTerminated()){
                //System.out.println("-*- Closing Channels (write) -*-");
                logger.debug("channel close OF " + key.toString());
                channel.close();
                key.cancel();
            }else{
                serverTools.queue(new QueuedKey(key, SelectionKey.OP_READ));
            }
        }

        handler.doneWriting();
    }



}
