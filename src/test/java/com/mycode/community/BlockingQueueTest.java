package com.mycode.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTest {

    public static void main(String[] args) {

        // 实例化阻塞队列（生产者和消费者共有一个阻塞队列）
        BlockingQueue queue = new ArrayBlockingQueue(10);
        // 实例化生产者线程
        // new Producer(queue)为该实例化线程的线程体
        new Thread(new Producer(queue)).start();
        // 实例化消费者线程
        // 模拟多个消费者消费一个生产者工厂
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();

    }
}

// 阻塞队列它是满足生产者和消费者模式的
// 使用阻塞队列是需要生产者线程和消费者线程的

// 生产者 - 是一个线程，需要实现Runnable接口
class Producer implements Runnable {

    private BlockingQueue<Integer> queue;

    // 当实例化生产者线程时，实例化生产者这个类时，要求调用方将阻塞队列传进来
    // 因为这个线程是需要交给阻塞队列去管理
    public Producer (BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {

        try {
            // 模拟 生产者需要频繁不断的往队列中存放数据
            for (int i = 0; i < 100; i++) {
                // 模拟生产，添加一些时间间隔
                Thread.sleep(20);
                // 每20秒生产一个数据，交给队列
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产:" + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

// 消费者
class Consumer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Consumer (BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {

        try {
            while (true) {
                // 消费者消费数据，往往消费间隔时间是不同的，同时往往没有生产者快
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                // 当前消费者线程
                System.out.println(Thread.currentThread().getName() + "消费：" + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
