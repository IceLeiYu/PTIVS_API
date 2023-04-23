package com.test.manager;

import com.test.util.ErrorException;
import com.test.util.PageKey;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginManager {
    private static final ConcurrentHashMap<String, Map<String, String>> cookieKeeper = new ConcurrentHashMap<>(); // id, cookies
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36 Edg/112.0.1722.48";

    private final String id;
    private final String pwd;

    public LoginManager(String id, String pwd) throws IOException, ErrorException {
        this.id = id;
        this.pwd = pwd;

        login();
    }

    public void login() throws ErrorException, IOException {
        Connection.Response response = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/main.asp")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .data("txtid", id)
                .data("txtpwd", pwd)
                .data("check", "confirm")
                .execute();

        if (response.url().getPath().equals("/skyweb/main.asp")) {
            throw new ErrorException("cannot login by the provided 'id' and 'pwd'");
        }

        Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/f_head.asp")
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .cookies(response.cookies())
                .execute();

        Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/f_left.asp")
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .cookies(response.cookies())
                .execute();

        cookieKeeper.put(id, response.cookies());
    }

    public Document fetchPageData(final PageKey pageKey) throws IOException, ErrorException {
        return fetchPageData(pageKey, false);
    }

    public Document fetchPageData(final PageKey pageKey, final boolean reFetch) throws IOException, ErrorException {
        Connection.Response response = Jsoup.connect("https://sctnank.ptivs.tn.edu.tw/skyweb/fnc.asp")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .data("fncid", pageKey.id())
                .data("std_id", "")
                .data("local_ip", "")
                .data("contant", "")
                .cookies(cookieKeeper.get(id))
                .execute();

        // cookie expired
        if (response.url().getPath().equals("/skyweb/fnc.asp")) {
            if (reFetch)
                throw new ErrorException("wtf error (0x01)");

            fetchPageData(pageKey, true);
        }

        return response.parse();
    }
}