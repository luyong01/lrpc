package com.ranze.literpc.client.channel;

import com.ranze.literpc.cons.Consts;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import jdk.nashorn.internal.runtime.linker.Bootstrap;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class ChannelPool {
    private BlockingQueue<ChannelWrapper> channels;
    private final Object channelsNotEnough = new Object();
    private InetSocketAddress address;
    private Bootstrap bootstrap;
    private int maxSize = 5;

    public ChannelPool(InetSocketAddress address) {
        this(address, 5);
    }

    public ChannelPool(InetSocketAddress address, int maxSize) {
        this.address = address;
        this.maxSize = maxSize;
        channels = new ArrayBlockingQueue<ChannelWrapper>(maxSize);
    }

    public Channel get() {
        return get(-1, null);
    }

    /**
     * 获取连接
     *
     * @param time 最长等待时间，如果 time <= 0，则表示无最长等待时间
     * @param unit 时间单位
     * @return
     */
    public Channel get(long time, TimeUnit unit) {
        long timeRemain = 0;
        if (time > 0) {
            timeRemain = unit.toMillis(time);
        }

        synchronized (channelsNotEnough) {
            Channel channel = getFreeChannel();
            if (valid(channel)) {
                return channel;
            }

            createChannelIfNecessary();
            channel = getFreeChannel();
            while (!valid(channel) && (timeRemain > 0 || time <= 0)) {
                try {
                    long timeBeforeWait = System.currentTimeMillis();
                    if (time <= 0) {
                        channelsNotEnough.wait();
                    } else {
                        channelsNotEnough.wait(timeRemain);
                        timeRemain -= System.currentTimeMillis() - timeBeforeWait;
                    }

                    channel = getFreeChannel();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return channel;
        }
    }

    private boolean valid(Channel channel) {
        return channel != null;
    }


    private Channel getFreeChannel() {
        for (ChannelWrapper channelWrapper : channels) {
            if (!channelWrapper.isInUse()) {
                channelWrapper.setInUse(true);
                return channelWrapper.getChannel();
            }
        }
        return null;
    }

    private void createChannelIfNecessary() {
        if (channels.size() >= maxSize) {
            return;
        }
        ChannelWrapper channelWrapper = new ChannelWrapper(ChannelFactory.getInstance().create(address));
        channelWrapper.getChannel().attr(Consts.KEY_CHANNELPOOL).set(this);
        channels.add(channelWrapper);
    }

    public void returnToPool(Channel channel) {
        synchronized (channelsNotEnough) {
            for (ChannelWrapper channelWrapper : channels) {
                if (channelWrapper.getChannel() == channel) {
                    channelWrapper.setInUse(false);
                }
            }
            channelsNotEnough.notify();
        }
    }

}
