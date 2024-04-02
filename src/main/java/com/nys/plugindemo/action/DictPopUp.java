package com.nys.plugindemo.action;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.nys.plugindemo.utils.JedisBean;
import com.nys.plugindemo.utils.TransApi;
import groovy.json.StringEscapeUtils;
import redis.clients.jedis.Jedis;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DictPopUp extends AnAction {
    private static String APP_ID = "";
    private static String SECURITY_KEY = "";
    private static String TOP_SET_NAME="";
    static TransApi transApi;

    static {

        InputStream in = DictPopUp.class.getClassLoader().getResourceAsStream("nysTranslationConfig.properties");
//      InputStream in = ClassLoader.getSystemResourceAsStream("nysTranslationConfig.properties");
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        APP_ID=properties.getProperty("baidu.appid");
        SECURITY_KEY=properties.getProperty("baidu.sercuritykey");
        TOP_SET_NAME=properties.getProperty("redis.zset.name");
        transApi = new TransApi(APP_ID, SECURITY_KEY);
    }
    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        //获取editor
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        String s=selectionModel.getSelectedText();
        //
        String transResult = transApi.getTransResult(s, "en", "zh");
        //{"from":"en","to":"zh",
        // "trans_result":
        // [
        //  {"src":"<project version=\"4\">","dst":"<\u9879\u76ee\u7248\u672c=\u201c4\u201d>"},
        //  {"src":"<component name=\"ProjectRootManager\">","dst":"<component name=\u201cProjectRootManager\u201d>"},
        //  {"src":"<output url=\"file:\/\/$PROJECT_DIR$\/out\" \/>","dst":"<output url=\u201cfile:\/\/$PROJECT_DIR$\/out\u201d\/>"},
        //  {"src":"<\/component>","dst":"\u7ec4\u6210\u90e8\u5206"},
        //  {"src":"<\/project>","dst":"\u9879\u76ee"}
        // ]}
        // 拿到结果后，先解析成字符串
        JSONObject jsonObject = JSONObject.parseObject(transResult);
        String jsonResult = jsonObject.getString("trans_result");
        JSONArray jsonArray = JSONArray.parseArray(jsonResult);
        StringBuffer dst=new StringBuffer();
        for(int i=0;i<jsonArray.size();i++){
            String dst1 = jsonArray.getJSONObject(i).getString("dst");
            dst.append(dst1);
        }
        String res=dst.toString();
        res=convertUnicodeToCh(res);
        // 如果是单词，则计入
        if(isWord(s)){
            Jedis jedis= JedisBean.getJedis();
            s=s.toLowerCase();
            Double nysWordList = jedis.zscore(TOP_SET_NAME, s);
            if(nysWordList==null){
                jedis.zadd(TOP_SET_NAME,1.0,s);
            }else {
                jedis.zrem(TOP_SET_NAME,s);
                jedis.zadd(TOP_SET_NAME,nysWordList+1.0,s);
            }
        }
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel(res), BorderLayout.CENTER);
        JBPopup jbPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null).createPopup();
        jbPopup.showInBestPositionFor(editor);

    }

    private static boolean isWord(String s){
        s = s.trim();
        int length=s.length();
        for(int i=0;i<length;i++){
            char c=s.charAt(i);
            // 如果c既不是letter 又不是“-”
            if(!Character.isLetter(c) && c!='-'){
                return false;
            }
        }
        return true;
    }

    // 转为Cn
    private static String convertUnicodeToCh(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
        Matcher matcher = pattern.matcher(str);

        // 迭代，将str中的所有unicode转换为正常字符
        while (matcher.find()) {
            String unicodeFull = matcher.group(1); // 匹配出的每个字的unicode，比如\u67e5
            String unicodeNum = matcher.group(2); // 匹配出每个字的数字，比如\u67e5，会匹配出67e5

            // 将匹配出的数字按照16进制转换为10进制，转换为char类型，就是对应的正常字符了
            char singleChar = (char) Integer.parseInt(unicodeNum, 16);

            // 替换原始字符串中的unicode码
            str = str.replace(unicodeFull, singleChar + "");
        }
        return StringEscapeUtils.unescapeJava(str);
    }
}
