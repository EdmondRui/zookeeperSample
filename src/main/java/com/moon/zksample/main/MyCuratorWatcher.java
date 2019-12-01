package com.moon.zksample.main;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

public class MyCuratorWatcher implements CuratorWatcher {
    @Override
    public void process(WatchedEvent event) throws Exception {
        System.out.println("触发warcher，节点路径为：" + event.getPath());
    }
}
