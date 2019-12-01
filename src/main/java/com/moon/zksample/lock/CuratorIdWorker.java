package com.moon.zksample.lock;

import com.moon.zksample.config.WrapperZk;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CuratorIdWorker implements InitializingBean {

    private static final String ID_NODE = "/ID";

    @Autowired
    CuratorFramework curatorFramework;

    @Autowired
    WrapperZk wrapperZk;

    public Long nextId() {
        Long id = null;
        try {
            DistributedAtomicLong atomicLong = new DistributedAtomicLong(curatorFramework, ID_NODE,
                    new RetryNTimes(wrapperZk.getRetryCount(), wrapperZk.getElapsedTimeMs()));
            AtomicValue<Long> sequence = atomicLong.increment();
            if (sequence.succeeded()) {
                Long seq = sequence.postValue();
                log.info("threadId={}, sequence={}", Thread.currentThread().getId(), seq);
                id = seq;
                return seq;
            } else {
                log.warn("threadId={}, no sequence", Thread.currentThread().getId());
            }
        } catch (Exception e) {
            log.error("acquire section exception.", e);
        }
        return id;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        curatorFramework = curatorFramework.usingNamespace("id-namespace");
        String path = "/myId";
        try {
            if (curatorFramework.checkExists().forPath(path) == null) {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path);
            }
            log.info("root path 创建成功");
        } catch (Exception e){
            log.error("connect zookeeper fail，please check the log >> {}", e.getMessage(), e);
        }
    }
}
