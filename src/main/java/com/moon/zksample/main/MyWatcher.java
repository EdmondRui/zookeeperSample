package com.moon.zksample.main;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        System.out.println("触发warcher，节点路径为：" + event.getPath());
    }
}
