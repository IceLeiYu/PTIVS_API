package com.test.handler;

import com.test.manager.JSONResponseManager;
import com.test.util.ErrorException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.test.Main.favicon;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static String getTime() {
        return "[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "]";
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(getTime() + " Connected: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) {
        if (obj instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) obj;

            String uri = request.uri();
            HttpMethod method = request.method();
            HttpHeaders headers = request.headers();
            String realIP = headers.get("CF-Connecting-IP");
            System.out.println(getTime() + ' ' + method + ": " + uri + " (" + realIP + ")");

            String[] args = uri.split("/");

            if (uri.equals("/favicon.ico")) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(favicon));
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/x-icon");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, favicon.length);
                response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=31557600");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            if (!args[1].equals("ptivs")) {
                sendError(ctx, "unsupported uri", HttpResponseStatus.NOT_FOUND);
                return;
            }

            try {
                if (method == HttpMethod.POST) {
                    new PostHandler(ctx, request);
                } else if (method == HttpMethod.GET) {
                    new GetHandler(ctx, request);
                } else {
                    sendError(ctx, "Unsupported request method: " + method.name(), HttpResponseStatus.METHOD_NOT_ALLOWED);
                }
            } catch (ErrorException e) {
                sendError(ctx, e.getMessage(), e.status);
            } catch (IOException e) {
                sendError(ctx, e.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            sendError(ctx, "unknown error", HttpResponseStatus.BAD_REQUEST);
        }

        if (ctx.channel().isOpen()) {
            sendError(ctx, "cannot found", HttpResponseStatus.BAD_REQUEST);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendError(ChannelHandlerContext ctx, String message, HttpResponseStatus status) {
        JSONResponseManager response = new JSONResponseManager(ctx);
        response.status = status;
        response.error = message;
        ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
    }
}

