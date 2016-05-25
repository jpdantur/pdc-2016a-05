package server;

import java.nio.channels.SelectionKey;

/**
 * Created by lelv on 5/25/16.
 */
public class QueuedKey implements KeyModifier{

    private SelectionKey key;
    private int interest;

    public QueuedKey(SelectionKey key, int interest) {
        this.key = key;
        this.interest = interest;
    }

    @Override
    public void modifyKeys() {
        if(key == null || !key.isValid()) return;
        key.interestOps(interest);
    }
}
