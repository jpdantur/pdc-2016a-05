package proxy.server;

import proxy.handler.HandlerBuilder;
import proxy.handler.ProxyHandler;
import proxy.utils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by root on 5/27/16.
 */
public class ProxyServer implements ServerTools {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private static ExecutorService workerPool;
    private static Queue<KeyModifier> keyModifierQueue;
    private static Set<SelectionKey> otherKeys;
    private HandlerBuilder handlerBuilder;

    private static final int WORKER_POOL = 2;
    private static final long TIMEOUT = 10;

    public ProxyServer(int port, HandlerBuilder handlerBuilder) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        workerPool = Executors.newFixedThreadPool(WORKER_POOL);
        keyModifierQueue = new ConcurrentLinkedQueue<>();

        otherKeys = new HashSet<>();
        this.handlerBuilder = handlerBuilder;
    }

    public void run(){

        try{
            while(true){

                if(selector.select(TIMEOUT) == 0){
                    updateKeys();
                    continue;
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {

                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if(otherKeys.contains(key)){
                        continue;
                    }

                    if(!key.isValid()) continue;

                    key.interestOps(0);

                    int otherInterest = 0;

                    if(key.isReadable() || key.isWritable()) {
                        SelectionKey otherKey = ((ProxyHandler) key.attachment()).getOtherKey();
                        if(otherKey!= null && otherKey.isValid()){
                            otherInterest = otherKey.interestOps();
                            otherKey.interestOps(0);
                            otherKeys.add(otherKey);
                        }
                    }

                    workerPool.execute(new Worker(key, this, otherInterest));

                    //Worker worker = new Worker(key, this);
                    //worker.run();

                    updateKeys();

                }
                otherKeys.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void queue(KeyModifier keyModifier) {
        keyModifierQueue.add(keyModifier);
    }

    @Override
    public ProxyHandler getNewHandler() {
        return handlerBuilder.build();
    }

    private void updateKeys(){
        KeyModifier keyModifier = keyModifierQueue.poll();
        while(keyModifier != null){
            keyModifier.modifyKeys();
            keyModifier = keyModifierQueue.poll();
        }
    }
}
