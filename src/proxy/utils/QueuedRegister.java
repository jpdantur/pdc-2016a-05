package proxy.utils;

import administrator.Configuration;
import proxy.handler.ProxyHandler;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by root on 5/27/16.
 */
public class QueuedRegister implements KeyModifier{

    private SocketChannel clientAndProxy;
    private SocketChannel proxyAndServer;
    private Selector selector;
    private ProxyHandler handlerClient;
    private ProxyHandler handlerServer;

    public QueuedRegister(SocketChannel clientAndProxy, SocketChannel proxyAndServer,
                          Selector selector, ProxyHandler handlerC, ProxyHandler handlerS) {
        this.clientAndProxy = clientAndProxy;
        this.proxyAndServer = proxyAndServer;
        this.selector = selector;
        this.handlerClient = handlerC;
        this.handlerServer = handlerS;
    }

    public void modifyKeys(){
        try {
            SelectionKey clientKey = clientAndProxy.register(selector, SelectionKey.OP_READ, handlerClient);
            SelectionKey serverKey = proxyAndServer.register(selector, SelectionKey.OP_CONNECT, handlerServer);
            handlerClient.setOtherKey(serverKey);
            handlerServer.setOtherKey(clientKey);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }
}
