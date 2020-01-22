package com.fexed.lprb.wq.client;

/**
 * @author Federico Matteoni
 */
public class WQClient {
    public WQClient(int port) {
        WQClientController.client = this;
    }
}
