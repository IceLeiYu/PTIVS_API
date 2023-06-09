package tw.xserver.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import tw.xserver.manager.AuthManager;
import tw.xserver.manager.CacheManager;
import tw.xserver.manager.JSONResponseManager;
import tw.xserver.util.ErrorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static tw.xserver.Main.cache;
import static tw.xserver.Util.getTime;

class PostHandler {
    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;

    public PostHandler(ChannelHandlerContext ctx, FullHttpRequest request) throws ErrorException {
        /* 初始化 */
        this.ctx = ctx;
        this.request = request;

        login();
    }

    private void login() throws ErrorException {
        String[] args = request.uri().split("/");
        /* 過濾請求 */
        if (!args[2].equals("login")) return;

        /* 檢查參數 */
        Map<String, String> data = readContent(request.content().toString(StandardCharsets.UTF_8));
        String id, pwd;
        if (data.containsKey("id")) {
            id = data.get("id");
        } else {
            throw new ErrorException("missing required parameters: id");
        }

        if (data.containsKey("pwd")) {
            pwd = data.get("pwd");
        } else {
            throw new ErrorException("missing required parameters: pwd");
        }


        HttpHeaders headers = request.headers();
        String realIP = headers.get("CF-Connecting-IP");
        if (realIP == null)
            realIP = headers.get("Host").split(":")[0];

        /* 初始回覆管理器 */
        JSONResponseManager response = new JSONResponseManager(ctx);
        AuthManager authManager = null;
        try {
            /* 登入帳號 */
            authManager = new AuthManager(id, pwd, realIP);

            /* 添加 Cookie */
            response.cookies.add(authManager.cookie);

            /* 補上個人資料 */
            response.json
                    .put("token", authManager.cookie.value())
                    .put("data", authManager.profile);
        } catch (ErrorException e) {
            response.status = e.status;
            response.error = e.getMessage();
        } catch (IOException e) {
            response.status = HttpResponseStatus.BAD_REQUEST;
            response.error = e.getMessage();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            /* 回覆並結束連線 */
            ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
            if (authManager != null) {
                System.out.println(getTime() + " POST: login " + authManager.id);
            }
        }
    }

    private Map<String, String> readContent(final String content) {
        Map<String, String> response = new HashMap<>();
        for (String i : content.split("&")) {
            String[] data = i.split("=");
            if (data.length == 2) {
                response.put(data[0], data[1]);
            }
        }
        return response;
    }
}

