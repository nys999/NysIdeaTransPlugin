package com.nys.plugindemo.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.nys.plugindemo.action.DictPopUp;
import com.nys.plugindemo.utils.JedisBean;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @Author: nys
 * @ClassName: TopWordsDialog
 * @Date: 2024/4/2
 **/
public class TopWordsDialog extends DialogWrapper {
   private static String TOP_SET_NAME="";
   public TopWordsDialog(){
      super(true);
      this.setTitle("your top10 searched words");
      InputStream in = DictPopUp.class.getClassLoader().getResourceAsStream("nysTranslationConfig.properties");
//      InputStream in = ClassLoader.getSystemResourceAsStream("nysTranslationConfig.properties");
      Properties properties = new Properties();
      try {
         properties.load(in);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      TOP_SET_NAME=properties.getProperty("redis.zset.name");
      init();
   }
   @Nullable
   @Override
   protected JComponent createCenterPanel() {
      JPanel dialogPanel = new JPanel(new BorderLayout());
      Jedis jedis= JedisBean.getJedis();
      List<Tuple> topWordWithScores = jedis.zrangeWithScores(TOP_SET_NAME, 0, 10);
      Collections.reverse(topWordWithScores);
      JBList<Tuple> jbList = new JBList<>(topWordWithScores);
      dialogPanel.add(jbList);

      return dialogPanel;
   }
}
