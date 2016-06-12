package proxy.utils;

import proxy.handler.ProxyHandler;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by wally on 6/10/16.
 */
public class QueuedRegisterKey implements KeyModifier {

    private int interest;
    private SocketChannel socketChannel;
    private Selector selector;
    private ProxyHandler handler;
    private SelectionKey otherKey = null;

    public QueuedRegisterKey(int interest, SocketChannel socketChannel, Selector selector, ProxyHandler handler){
        this.interest = interest;
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.handler = handler;
    }
    public QueuedRegisterKey(int interest, SocketChannel socketChannel, Selector selector, ProxyHandler handler, SelectionKey otherKey){
        this.interest = interest;
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.handler = handler;
        this.otherKey = otherKey;
    }

    @Override
    public void modifyKeys() {
        try {
            SelectionKey key = socketChannel.register(selector, interest, handler);
            handler.setOtherKey(otherKey);
            /*if(otherKey != null) {
                ((ProxyHandler) (otherKey.attachment())).setOtherKey(key);
            }*/
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }
}
