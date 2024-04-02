package com.nys.plugindemo.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nys.plugindemo.dialog.TopWordsDialog;


public class WordList extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        // 获取Redis中的TOP10单词并显示

        TopWordsDialog topWordsDialog = new TopWordsDialog();
        topWordsDialog.showAndGet();

    }
}
