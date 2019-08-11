package com.ranze.literpc.server;

import com.google.common.util.concurrent.RateLimiter;
import com.google.protobuf.Message;
import com.ranze.literpc.exception.ErrorEnum;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class WorkerThreadPool {
    private static volatile WorkerThreadPool INSTANCE;
    private ExecutorService executorService;
    private int workThreadNum = Runtime.getRuntime().availableProcessors() + 1;
    private BlockingDeque<Runnable> blockingDeque;
    private AtomicInteger threadNum = new AtomicInteger(1);

    private WorkerThreadPool() {
        blockingDeque = new LinkedBlockingDeque<>();
        executorService = new ThreadPoolExecutor(workThreadNum, workThreadNum, 60, TimeUnit.SECONDS,
                blockingDeque, threadFactory("work-thread-", true));
        ((ThreadPoolExecutor) executorService).prestartAllCoreThreads();
    }

    public static WorkerThreadPool getInstance() {
        if (INSTANCE == null) {
            synchronized (WorkerThreadPool.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WorkerThreadPool();
                }
            }
        }
        return INSTANCE;
    }

    private ThreadFactory threadFactory(final String name, final boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name + threadNum.getAndIncrement());
            result.setDaemon(daemon);
            return result;
        };
    }

    public void submit(Runnable runnable) {
        executorService.submit(runnable);
    }

    public static class Task implements Runnable {
        private ChannelHandlerContext context;
        private RpcRequest rpcRequest;

        public Task(ChannelHandlerContext context, RpcRequest rpcRequest) {
            this.context = context;
            this.rpcRequest = rpcRequest;
        }

        @Override
        public void run() {
            log.info("{} begin to handle request {}", Thread.currentThread().getName(), rpcRequest);
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setCallId(rpcRequest.getCallId());
            rpcResponse.setCompressType(rpcRequest.getCompressType());
            try {
                ServiceInfo serviceInfo = ServiceManager.getInstance().getService(rpcRequest.getService().getCanonicalName(),
                        rpcRequest.getMethod().getName());
                RateLimiter rateLimiter = serviceInfo.getRateLimiter();
                // if rate limiter set
                if (rateLimiter != null) {
                    if (rateLimiter.tryAcquire()) {
                        callService(rpcRequest, rpcResponse, serviceInfo);
                    } else {
                        log.info("Process request {} cause rate limit");
                        rpcResponse.setException(new RpcException(ErrorEnum.SERVICE_BUSY));
                    }
                } else {
                    callService(rpcRequest, rpcResponse, serviceInfo);
                }
            } catch (InvocationTargetException e) {
                log.info("Precess request {} cause exception {}", rpcRequest, e.getMessage());
                rpcResponse.setException(new RpcException(ErrorEnum.SERVICE_EXCEPTION.getCode(), e.getTargetException().getMessage()));
            } catch (IllegalAccessException e) {
                log.info("Precess request {} cause exception {}", rpcRequest, e.getMessage());
                rpcResponse.setException(new RpcException(ErrorEnum.SERVICE_EXCEPTION.getCode(), e.getMessage()));
            }


            ChannelFuture channelFuture = context.channel().writeAndFlush(rpcResponse);
            channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        log.debug("Write response success");
                    } else {
                        log.warn("Write response error {}", future.cause().getMessage());
                    }
                }
            });

        }

        private void callService(RpcRequest rpcRequest, RpcResponse rpcResponse, ServiceInfo serviceInfo)
                throws InvocationTargetException, IllegalAccessException {
            Object target = serviceInfo.getTarget();
            Message result = (Message) rpcRequest.getMethod().invoke(target, rpcRequest.getArgs());

            log.info("Process request {}, result={}", rpcRequest, Objects.toString(result));

            rpcResponse.setResult(result);
        }
    }
}
