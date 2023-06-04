package com.test.HTML_Analyze;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static com.test.HTML_Analyze.Util.dateConvert;

public class ReadAbsent {

    private static final String[] PREVIEW_LABEL = {"年度", "學期", "曠課", "午曠", "環曠", "事假", "病假", "公假", "喪假", "防疫假"};

    @Nullable
    public static JSONObject readAbsent(final Document doc) {
        JSONObject output = new JSONObject();

        try {
            Elements tables = doc.getElementsByTag("table");


            Elements e_preview = tables.get(2).getElementsByTag("tr").get(1).children();
            JSONObject preview = new JSONObject();
            output.put("預覽", preview);
            for (int i = 0; i < 10; i++) {
                preview.put(PREVIEW_LABEL[i], e_preview.get(i).text().trim());
            }

            Elements e_detail = tables.get(4).getElementsByTag("tr");
            JSONArray detail = new JSONArray();
            output.put("細節", detail);
            for (int i = 1; i < e_detail.size(); ++i) {
                Elements children = e_detail.get(i).children();
                JSONObject data = new JSONObject();
                detail.put(data);

                data.put("日期", dateConvert(children.get(1).text().trim().split("/")));

                for (int j = 3; j < 13; ++j) {
                    if (children.get(j).text().trim().equals("0")) continue;


                }

            }


            System.out.println(tables);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }


}