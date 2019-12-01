package com.moon.zksample.main;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class CuratorOperator {

    public CuratorFramework client = null;

    public static final String zkServerPath = "129.211.26.83:2181";

    /**
     * 实例化zk客户端
     */
    public CuratorOperator () {
        /**
         * 同步创建zk示例，原生api是异步的
         *
         * curator连接zookeeper的策略:ExponentialBackoffRetry
         * baseSleepTimMs:初始sleep的时间
         * maxRetries:最大重试次数
         * maxSleepMs:最大重试时间
         */
//        CuratorConfig retryPolicy = new ExponentialBackoffRetry(1000, 5);

        /**
         * curator连接zookeeper的策略:RetryNTimes
         * n:重试的次数
         * sleepMsBetweenRetries:每次间隔重试时间
         */
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);

        /**
         * curator连接zookeeper的策略:RetryOneTime
         * sleepMsBetweenRetry:每次间隔重试时间
         */
//        CuratorConfig retryPolicy = new RetryOneTime(3000);

        /**
         * 永远重试，不推荐使用
         */
//        CuratorConfig retryPolicy = new RetryForever(3000);

        /**
         * curator连接zookeeper的策略:RetryUntilElapsed
         * maxElapsedTimeMs:最大重试时间
         * sleepMsBetweenRetries:每次间隔重试时间
         * 重试时间超过maxElapsedTimeMs后，就不重试
         */
//        CuratorConfig retryPolicy = new RetryUntilElapsed(2000, 3000);

        client = CuratorFrameworkFactory.builder()
                .connectString(zkServerPath)
                .sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
                .namespace("workspace")
                .build();
        client.start();
    }

    public void closeZKClient() {
        if (client != null) {
            this.client.close();
        }
    }

    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorOperator cto = new CuratorOperator();
        boolean isZkCuratorStarted = cto.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中":"已关闭"));

        // 创建节点
        String nodePath = "/super/moon";
        byte[] data = "superme".getBytes();
        cto.client.create().creatingParentsIfNeeded()
           .withMode(CreateMode.PERSISTENT)
           .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
           .forPath(nodePath, data);

        // 更新节点数据
        byte[] newData = "batmen".getBytes();
        cto.client.setData()
                .withVersion(2)
                .forPath(nodePath, newData);

        // 读取节点数据
        Stat stat = new Stat();
        byte[] data2 = cto.client.getData().storingStatIn(stat).forPath(nodePath);
        System.out.println("节点" + nodePath + "的数据为:" + new String(data2));
        System.out.println("节点" + nodePath + "的版本为:" + stat.getVersion());

        // 读取子节点
        List<String> childNodes = cto.client.getChildren().forPath(nodePath);

        System.out.println("开始打印子节点");
        for (String s : childNodes) {
            System.out.println(s);
        }

        // 判断节点是否存在，如果不存在则为空
        Stat statExist = cto.client.checkExists().forPath(nodePath);
        System.out.println(statExist);

        // 删除节点
        cto.client.delete()
                .guaranteed()                   // 如果删除失败，那么在后面还是会继续删除，直到成功
                .deletingChildrenIfNeeded()     // 如果有子节点，就删除
                .withVersion(2)
                .forPath(nodePath);

        Thread.sleep(3000);

        // watcher事件，当使用usingWatcher的时候，监听只会触发一次，监听完毕后就销毁
        cto.client.getData().usingWatcher(new MyCuratorWatcher()).forPath(nodePath);
        cto.client.getData().usingWatcher(new MyWatcher()).forPath(nodePath);

        // 为节点添加watcher
        // NodeCache:监听数据节点的变更，会触发事件
        final NodeCache nodeCache = new NodeCache(cto.client, nodePath);
        // buildInitial:初始化的时候获取node的值并且缓存
        nodeCache.start(true);
        if (nodeCache.getCurrentData() != null) {
            System.out.println("节点初始化数据为：" + new String(nodeCache.getCurrentData().getData()));
        } else {
            System.out.println("节点初始化数据为空");
        }
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                String data = new String(nodeCache.getCurrentData().getData());
                System.out.println("节点路径：" + nodeCache.getCurrentData().getPath() + "数据为" + data);
            }
        });


        // 为子节点添加watcher
        // PathChildrenCache: 监听数据节点的增删改，会触发事件
        String childNodePathCache = nodePath;
        final PathChildrenCache childrenCache = new PathChildrenCache(cto.client, childNodePathCache, true);
        /**
         * StartMode:初始化方式
         * POST_INITIALIZED_EVENT:异步初始化，初始化之后会触发事件
         * NORMAL:异步初始化
         * BUILD_INITIAL_CACHE:同步初始化
         */
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        List<ChildData> childDataList = childrenCache.getCurrentData();
        System.out.println("当前数据节点的子节点数据列表：");
        for (ChildData cd : childDataList) {
            String childData = new String(cd.getData());
            System.out.println(childData);
        }

        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {
                    System.out.println("子节点初始化OK...");
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                    System.out.println("添加子节点：" + event.getData().getPath());
                    System.out.println("子节点数据为：" + new String(event.getData().getData()));
                }else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    System.out.println("删除子节点：" + event.getData().getPath());
                }else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                    System.out.println("修改子节点路径：" + event.getData().getPath());
                    System.out.println("修改子节点数据为：" + new String(event.getData().getData()));
                }
            }
        });


        cto.closeZKClient();
        boolean isZkCuratorStarted2 = cto.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted2 ? "连接中":"已关闭"));
    }
}
