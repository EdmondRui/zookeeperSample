package com.moon.zksample.controller;

import com.moon.zksample.lock.CuratorIdWorker;
import com.moon.zksample.lock.DistributedLockByZookeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zk")
public class MyZkController {

    @Autowired
    private DistributedLockByZookeeper distributedLockByZookeeper;

    @Autowired
    private CuratorIdWorker curatorIdWorker;

    private final static String PATH = "test";

    @GetMapping("/lock1")
    public Boolean getLock1() {
        Boolean flag;
        distributedLockByZookeeper.acquireDistributedLock(PATH);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            flag = distributedLockByZookeeper.releaseDistributedLock(PATH);
        }
        flag = distributedLockByZookeeper.releaseDistributedLock(PATH);
        return flag;
    }

    @GetMapping("/lock2")
    public Boolean getLock2() {
        Boolean flag;
        distributedLockByZookeeper.acquireDistributedLock(PATH);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            flag = distributedLockByZookeeper.releaseDistributedLock(PATH);
        }
        flag = distributedLockByZookeeper.releaseDistributedLock(PATH);
        return flag;
    }

    @GetMapping("/nextId")
    public Long nextId() {
       return curatorIdWorker.nextId();
    }
}
