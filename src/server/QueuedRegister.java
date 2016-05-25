package server;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by lelv on 5/25/16.
 */
public class QueuedRegister implements KeyModifier{

    private SocketChannel clientToProxy;
    private SocketChannel proxyToServer;
    private Selector selector;

    public QueuedRegister(SocketChannel clientToProxy, SocketChannel proxyToServer, Selector selector) {
        this.clientToProxy = clientToProxy;
        this.proxyToServer = proxyToServer;
        this.selector = selector;
    }

    public void modifyKeys(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        KeyData ncClient = new KeyData(buffer);

        SelectionKey otherKey = null;
        try {
            SelectionKey clientKey = clientToProxy.register(selector, SelectionKey.OP_READ, ncClient);
            otherKey = proxyToServer.register(selector, SelectionKey.OP_CONNECT, new KeyData(clientKey, buffer));
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

        ncClient.setSelectionKey(otherKey);
    }
}
