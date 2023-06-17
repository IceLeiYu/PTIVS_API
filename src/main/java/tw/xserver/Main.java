package tw.xserver;

import tw.xserver.handler.ClientHandler;
import tw.xserver.handler.RateLimitHandler;
import tw.xserver.manager.RefreshCache;
import tw.xserver.manager.CertificateManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLException;
import java.io.*;
import java.sql.SQLException;
import java.util.Base64;

import static tw.xserver.handler.ClientHandler.getTime;

public class Main {
    public static byte[] favicon;
    public static String defaultID;
    public static String defaultPWD;
    private static SslContext sslCtx;
    private static int port;
    public static CertificateManager certificate;
    public static RefreshCache cache;

    public Main(String[] args) throws SSLException, SQLException {
        port = Integer.parseInt(args[0]);
        defaultID = args[1];
        defaultPWD = new String(Base64.getDecoder().decode(args[2]));
        sslCtx = SslContextBuilder.forServer(
                new File("./key/certchain.pem"), new File("./key/privatekey.pem")
        ).build();
        favicon = loadFavicon("./icon/favicon-32x32.png");

        certificate = new CertificateManager();
        cache = new RefreshCache();
    }

    public static void main(String[] args) throws Exception {
        new Main(args).run();
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 創建一個線程池，用於處理連接請求
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 創建一個線程池，用於處理連接請求
        try {
            ServerBootstrap b = new ServerBootstrap(); // 創建對象，用於配置伺服器參數

            b.group(bossGroup, workerGroup) // 指定網路事件處理器線程池
                    .channel(NioServerSocketChannel.class) // 指定服務端使用的通訊協議
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 指定每個客戶端連接的處理器
                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) { // 添加自定義的伺服器處理器
                            final ChannelPipeline pipeline = ch.pipeline();
                            if (!ch.remoteAddress().getAddress().getHostAddress().equals("127.0.0.1")) { // 設定加解密器
                                pipeline.addLast(sslCtx.newHandler(ch.alloc()));
                            }

                            pipeline.addLast(new HttpServerCodec()); // 設定編解碼器
                            pipeline.addLast(new RateLimitHandler()); // 設定流量限制
                            pipeline.addLast(new HttpObjectAggregator(65536));// 整合輸出入資料
                            pipeline.addLast(new ClientHandler()); // 處理資料
                        }
                    });
            ChannelFuture f = b.bind(port).sync(); // 綁定至端口

            System.out.println(getTime() + " server started");

            f.channel().closeFuture().sync(); // 等待程式結束並關閉端口
        } finally {
            bossGroup.shutdownGracefully(); // 關閉線程池
            workerGroup.shutdownGracefully(); // 關閉線程池
        }
    }

    private byte[] loadFavicon(String path) {
        try (InputStream in = new FileInputStream(path)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}