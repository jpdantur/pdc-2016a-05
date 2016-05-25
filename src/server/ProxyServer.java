package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lelv on 5/21/16.
 */
public class ProxyServer implements Runnable, Queueable{
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private static ExecutorService workerPool;
    private static Queue<KeyModifier> keyModifierQueue;

    private static final int WORKER_POOL = 100;
    private static final long TIMEOUT = 10;

    public ProxyServer(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        workerPool = Executors.newFixedThreadPool(WORKER_POOL);
        keyModifierQueue = new ConcurrentLinkedQueue<>();
    }

    public void run(){

        try{
            while(true){

                if(selector.select(TIMEOUT) == 0){
                    modifyKeys();
                    continue;
                }
                Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    keyIter.remove();

                    key.interestOps(0);
                    workerPool.execute(new PopHandler(key, this));

                    modifyKeys();

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try{
            new Thread(new ProxyServer(7070)).run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void queue(KeyModifier keyModifier) {
        keyModifierQueue.add(keyModifier);
    }

    private void modifyKeys(){
        KeyModifier keyModifier = keyModifierQueue.poll();
        while(keyModifier != null){
            keyModifier.modifyKeys();
            keyModifier = keyModifierQueue.poll();
        }
    }
}
