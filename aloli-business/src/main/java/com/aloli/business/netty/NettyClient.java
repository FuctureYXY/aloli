package com.aloli.business.netty;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;


@Slf4j
@Component
public class NettyClient {


    private EventLoopGroup group = new NioEventLoopGroup();

    /**
     *@Fields DELIMITER : 自定义分隔符，服务端和客户端要保持一致
     */
    public static final String DELIMITER = "@@";

    /**
     * @Fields hostIp : 服务端ip
     */
    private String hostIp = "127.0.0.1";

    /**
     * @Fields port : 服务端端口
     */
    private int port= 8888;

    /**
     * @Fields socketChannel : 通道
     */
    private SocketChannel socketChannel;


    /**
     *@Fields clientHandlerInitilizer : 初始化
     */
    @Autowired
    private NettyClientHandlerInitilizer clientHandlerInitilizer;

    /**
     * 启动客户端
     */
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void start() {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                // 指定Channel
                .channel(NioSocketChannel.class)
                // 服务端地址
                .remoteAddress(hostIp, port)
                // 将小的数据包包装成更大的帧进行传送，提高网络的负载,即TCP延迟传输
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 将小的数据包包装成更大的帧进行传送，提高网络的负载,即TCP延迟传输
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(clientHandlerInitilizer);

        // 连接
        ChannelFuture channelFuture = bootstrap.connect();
        //客户端断线重连逻辑
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    log.info("连接Netty服务端成功...");
                }else {
                    log.info("连接Netty服务端失败，进行断线重连...");
                    final EventLoop loop =future.channel().eventLoop();
                    loop.schedule(new Runnable() {
                        @Override
                        public void run() {
                            log.info("连接正在重试...");
                            start();
                        }
                    }, 20, TimeUnit.SECONDS);
                }
            }
        });
        socketChannel = (SocketChannel) channelFuture.channel();
    }


    /**
     * 消息发送
     */
    public void sendMsg(String  message) {
        ChannelFuture future = socketChannel.writeAndFlush(message+NettyClient.DELIMITER);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    log.debug("===========发送成功");
                }else {
                    log.debug("------------------发送失败");
                }
            }
        });
    }
}
