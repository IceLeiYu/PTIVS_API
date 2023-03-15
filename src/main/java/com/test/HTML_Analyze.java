package com.test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class HTML_Analyze {
    public static Map<String, JSONObject> profileData = new HashMap<>();

    public static JSONObject getProfile(Elements userDatas) {
        JSONObject output = new JSONObject();
        Elements userData = userDatas.last().children();
        output.put("name", userDatas.first().child(0).text().trim().split(" ： ")[1]);
        output.put("semester", userData.get(0).text().trim());
        output.put("class", userData.get(1).text().trim());
        output.put("id", userData.get(2).text().trim().split("：")[1]);
        return output;
    }

    public static JSONObject getHistoryRewards(String responseContent, final String id) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");
            JSONObject profile = profileData.getOrDefault(
                    id,
                    getProfile(tables.get(0).getElementsByTag("tr"))
            );
            output.put("profile", profile);

            // put preview data
            JSONArray previewJSON = new JSONArray();
            output.put("preview", previewJSON);
            Elements previewRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < previewRaw.size() - 3; ++i) {
                JSONObject tmp = new JSONObject();
                Elements children = previewRaw.get(i).children();
                // 110-1
                tmp.put("semester", children.get(0).text().trim() +
                        '-' + children.get(1).text().trim());

                tmp.put("major_merit", getInt(children.get(2).text().trim()));
                tmp.put("minor_merit", getInt(children.get(3).text().trim()));
                tmp.put("commendation", getInt(children.get(4).text().trim()));
                tmp.put("good_point", getInt(children.get(5).text().trim()));
                tmp.put("major_demerit", getInt(children.get(6).text().trim()));
                tmp.put("minor_demerit", getInt(children.get(7).text().trim()));
                tmp.put("admonition", getInt(children.get(8).text().trim()));
                tmp.put("bad_point", getInt(children.get(9).text().trim()));

                previewJSON.put(tmp);
            }

            // put reward detail
            int detailPos;
            JSONArray rewardDetailJSON = new JSONArray();
            output.put("reward_detail", rewardDetailJSON);
            Elements detailRaw = tables.get(3).getElementsByTag("tr");
            for (detailPos = 2; detailPos < detailRaw.size() - 3; ++detailPos) {
                JSONObject tmp = new JSONObject();
                rewardDetailJSON.put(tmp);

                Elements children = detailRaw.get(detailPos).children();
                if (children.size() == 1) break;

                tmp.put("check_time", getTimeFormat(children.get(0).text().trim()));
                tmp.put("occur_time", getTimeFormat(children.get(1).text().trim()));
                tmp.put("description", children.get(2).text().trim());
                tmp.put("type", children.get(3).text().trim());
                tmp.put("major_merit", getInt(children.get(4).text().trim()));
                tmp.put("minor_merit", getInt(children.get(5).text().trim()));
                tmp.put("commendation", getInt(children.get(6).text().trim()));
                tmp.put("good_point", getInt(children.get(7).text().trim()));
                tmp.put("add_score", getInt(children.get(8).text().trim()));
                tmp.put("note", children.get(11).text().trim());
            }

            JSONArray punishDetailJSON = new JSONArray();
            output.put("punish_detail", punishDetailJSON);
            for (detailPos += 2; detailPos < detailRaw.size(); ++detailPos) {
                JSONObject tmp = new JSONObject();
                punishDetailJSON.put(tmp);
                Elements children = detailRaw.get(detailPos).children();
                if (children.size() == 1) {
                    // no punished
                    break;
                }
                tmp.put("check_time", getTimeFormat(children.get(0).text().trim()));
                tmp.put("occur_time", getTimeFormat(children.get(1).text().trim()));
                tmp.put("description", children.get(2).text().trim());
                tmp.put("type", children.get(3).text().trim());
                tmp.put("major_demerit", getInt(children.get(4).text().trim()));
                tmp.put("minor_demerit", getInt(children.get(5).text().trim()));
                tmp.put("admonition", getInt(children.get(6).text().trim()));
                tmp.put("bad_point", getInt(children.get(7).text().trim()));
                tmp.put("remove_score", getInt(children.get(8).text().trim()));
                tmp.put("note", children.get(11).text().trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static JSONObject getClubs(final String responseContent, final String id) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONObject profile = profileData.getOrDefault(
                    id,
                    getProfile(tables.get(0).getElementsByTag("tr"))
            );
            output.put("profile", profile);

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);
            Elements detailRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < detailRaw.size(); i++) {
                JSONObject tmp = new JSONObject();
                detailJSON.put(tmp);

                Elements children = detailRaw.get(i).children();

                String semester = children.get(0).text().trim().split("學年")[0];
                if (children.get(1).text().trim().equals("第一學期")) {
                    semester += "-1";
                } else if (children.get(1).text().trim().equals("第二學期")) {
                    semester += "-2";
                }

                tmp.put("semester", semester);
                tmp.put("name", children.get(2).text().trim());
                tmp.put("group", children.get(3).text().trim());
                tmp.put("position", children.get(5).text().trim());
                tmp.put("score", getInt(children.get(6).text().trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static JSONObject getCadres(final String responseContent, final String id) {
        JSONObject output = new JSONObject();

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONObject profile = profileData.getOrDefault(
                    id,
                    getProfile(tables.get(0).getElementsByTag("tr"))
            );
            output.put("profile", profile);

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);
            Elements detailRaw = tables.get(2).getElementsByTag("tr");
            for (int i = 1; i < detailRaw.size(); i++) {
                JSONObject tmp = new JSONObject();
                detailJSON.put(tmp);

                Elements children = detailRaw.get(i).children();

                String semester = children.get(0).text().trim().split("學年")[0];
                if (children.get(1).text().trim().equals("第一學期")) {
                    semester += "-1";
                } else if (children.get(1).text().trim().equals("第二學期")) {
                    semester += "-2";
                }

                tmp.put("semester", semester);
                tmp.put("name", children.get(2).text().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static JSONObject getClassTable(final String responseContent, final String id) {
        JSONObject output = new JSONObject();
        String[] dayTitles = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五"};

        try {
            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");

            JSONObject profile = profileData.getOrDefault(
                    id,
                    getProfile(tables.get(0).getElementsByTag("tr"))
            );
            output.put("profile", profile);

            JSONObject detailJSON = new JSONObject();
            output.put("detail", detailJSON);
            Elements detailRaw = tables.get(2).getElementsByTag("tr");

            for (String i : dayTitles)
                detailJSON.put(i, new JSONObject());

            for (int i = 1; i < detailRaw.size() - 1; i++) {
                Elements children = detailRaw.get(i).children();

                for (int j = 0; j < 5; j++) {
                    /* [微處理機, (必), 王彥盛,, 資訊二甲] */
                    String[] class_split = children.get(j + 2).text().split(" ");

                    JSONObject curClass = new JSONObject("{\"empty\": false}");
                    JSONObject curDay = detailJSON.getJSONObject(dayTitles[j]);
                    curDay.put(String.valueOf(i), curClass);

                    if (class_split.length <= 1) {
                        curClass.put("empty", true);
                        continue;
                    }

                    curClass.put("name", class_split[0]);
                    curClass.put("require", class_split[1].equals("(必)"));
                    curClass.put("teacher", new JSONArray(class_split[2].split(",")));
                    curClass.put("place", class_split[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    public static JSONObject getHistoryScore(final String responseContent, final String id) {
        JSONObject output = new JSONObject();

        try {

            Document doc = Jsoup.parse(responseContent);
            Elements tables = doc.getElementsByTag("table");
//            System.out.println(doc);
//            System.out.println(tables);

            JSONObject profile = profileData.getOrDefault(
                    id,
                    getProfile(tables.get(0).getElementsByTag("tr"))
            );
            output.put("profile", profile);

            JSONArray detailJSON = new JSONArray();
            output.put("detail", detailJSON);


            Elements e = tables.get(2).getElementsByTag("tbody");

            System.out.println(e);
//            for (Element i : tables.get(2).getElementsByTag("tr")) {
//                System.out.println(i.getElementsByTag("td") + "\n\n ------------------------------- \n\n");
//            }


//            tables.get(2).getElementsByTag("tr").get(0).

//            Elements detailRaw = tables.get(2).getElementsByTag("tr");


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    private static int getInt(String inp) {
        if (inp.equals(""))
            return 0;
        return Integer.parseInt(inp);
    }

    private static String getTimeFormat(String time) {
        // `111年11月04日` after format -> `2023-11-04`
        return String.join("-", time.split("[年月日]"));
    }
}
