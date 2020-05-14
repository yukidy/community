package com.mycode.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符常量-用于替换敏感词
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    /**
     * 初始化方法
     *  根据sensitive-words.txt文件的数据构造树形
     *  这个方法应该是在程序启动，首次调用这个工具时，自动初始化化好，初始化一次
     */
    // 当容器实例化这个Bean后，在调用完构造器之后，该方法自动被调用
    // bean合适被初始化？ 在服务启动时被初始化，所以在服务器启动时，该方法就会被调用
    @PostConstruct
    public void init () {
        try ( // 字节流自动关闭
              // 在classes之下获取文件，加载到的是字节流
              InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words");
              // 将字节流转换成字符流InputStreamReader，
              // 直接使用字符流也不太方便，将字符流转换成缓冲流，效率会更高
              BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            // 读取到的值-存入
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                // 实现：封装到addKeyWord()方法中
                this.addKeyWord(keyword);
            }

        } catch (IOException e) {
            logger.error("加载敏感文件失败:" + e.getMessage());
        }

    }

    /**
     * 将一个敏感词添加到前缀树中
     * @param keyword 敏感词
     */
    private void addKeyWord(String keyword) {

        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 尝试寻找是否已经有该子节点，如果没有，则新建
            TrieNode subNode = tempNode.getSubNodes(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                // 将子节点挂到当前节点之下
                tempNode.addSubNodes(c, subNode);
            }

            // 有，直接用该子节点
            // 指向子节点，进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                // 表示这是这个敏感词的最后一个字符，是敏感词的结束标记
                tempNode.setKeywordEnd(true);
            }

        }

    }


    /**
     * 过滤敏感词-算法
     *
     * @param text 未过滤的文本
     * @return 过滤后的文本
     */
    public String sensitivefilter (String text) {

        // 空值处理
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果，需要不断的追加字符
        // 最好用变长字符串，效率更高
        //StringBuffer:(jdk1.0),是线程安全的,要考虑线程安全问题。
        //StringBuilder:(jdk1.5),是线程不安全的,不需要考虑线程安全问题。
        //两者的区分：在不考虑线程安全的前提下,尽量使StringBuilder,效率高,速度快。
        StringBuilder stringBuilder = new StringBuilder();

        // 算法-重头到尾检测字符串
        while (position < text.length()) {

            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点，将此符号记入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    stringBuilder.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNodes(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                stringBuilder.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重写指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词，将begin ~ position 字符串替换掉
                stringBuilder.append(REPLACEMENT);
                // 进入下一位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一字符
                position++;
            }

        }

        // 将最后一批字符计入结果
        stringBuilder.append(text.substring(begin));

        return stringBuilder.toString();

    }

    // 判断是否为符号
    private boolean isSymbol (Character c) {
        // 判断该字符是否为合法的、普通的字符
        // 0x2E80 ~ 0x9FFF 是东亚范围内的文字，不认为它是符号
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    /**
     * 定义前缀树，树形结构
     */
    private class TrieNode {

        // 关键词结束标识 x
        private boolean isKeywordEnd = false;

        // 当前节点的子节点(key是下级字符，value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNodes (Character c, TrieNode subNode) {
            subNodes.put(c, subNode);
        }

        // 获取子节点 (通过key获取value)
        public TrieNode getSubNodes (Character c) {
            return subNodes.get(c);
        }
    }


}
