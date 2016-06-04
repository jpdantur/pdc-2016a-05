package proxy.utils;

import proxy.handler.ProxyHandler;

/**
 * Created by root on 5/27/16.
 */
public interface ServerTools {

    void queue(KeyModifier keyModifier);
    ProxyHandler getNewHandler();
}
