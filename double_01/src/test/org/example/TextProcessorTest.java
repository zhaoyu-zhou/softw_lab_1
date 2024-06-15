package org.example;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextProcessorTest {

    private static Graph graph;

    @BeforeAll
    static void setup() throws Exception {
        String filePath = "..//double_01//text.txt";
        String processedText = TextProcessor.processTextFile(filePath);
        Map<String, Map<String, Integer>> directedGraphData = TextProcessor.buildDirectedGraph(processedText);
        graph = TextProcessor.createGraph(directedGraphData);
    }

    @Test
    void testQueryBridgeWords_BothWordsExistWithBridgeWords() {
        // 创建一个Scanner模拟输入
        String input = "hello\nthis\n"; // 输入模拟，第一个单词为"hello"，第二个单词为"world"
        Scanner scanner = new Scanner(input);


        // 捕获System.out的输出
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out; // 保存原始的System.out
        System.setOut(new PrintStream(outContent));

        // 调用queryBridgeWords方法
        TextProcessor.queryBridgeWords(scanner, graph);

        // 还原System.out
        System.setOut(originalOut);

        // 获取输出并分割成行
        String output = outContent.toString().trim();
        String[] lines = output.split("\n");

        // 获取最后一行
        String lastLine = lines[lines.length - 1].trim();

        // Replace "bridge_word1, bridge_word2" with expected bridge words
        String expectedOutput = "桥接词：world";
        assertEquals(expectedOutput, lastLine);
    }


    @Test
    void testQueryBridgeWords_Word1ExistsWord2NotExist() {
        String input = "word1\nnonexistent\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);

        // Capture the output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        TextProcessor.queryBridgeWords(scanner, graph);

        String output = outContent.toString().trim();
        String[] lines = output.split("\n");
        String lastLine = lines[lines.length - 1].trim();

        String expectedOutput = "起始单词或目标单词不在图中。";
        assertEquals(expectedOutput, lastLine);
    }

    @Test
    void testQueryBridgeWords_Word1NotExistWord2Exists() {
        String input = "nonexistent\nword2\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);

        // Capture the output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        TextProcessor.queryBridgeWords(scanner, graph);

        String output = outContent.toString().trim();
        String[] lines = output.split("\n");
        String lastLine = lines[lines.length - 1].trim();

        String expectedOutput = "起始单词或目标单词不在图中。";
        assertEquals(expectedOutput, lastLine);
    }

    @Test
    void testQueryBridgeWords_BothWordsNotExist() {
        String input = "nonexistent1\nnonexistent2\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);

        // Capture the output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        TextProcessor.queryBridgeWords(scanner, graph);

        String output = outContent.toString().trim();
        String[] lines = output.split("\n");
        String lastLine = lines[lines.length - 1].trim();

        String expectedOutput = "起始单词或目标单词不在图中。";
        assertEquals(expectedOutput, lastLine);
    }

    @Test
    void testQueryBridgeWords_BothWordsExistWithoutBridgeWords() {
        String input = "hello\nworld\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);

        // Capture the output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        TextProcessor.queryBridgeWords(scanner, graph);

        String output = outContent.toString().trim();
        String[] lines = output.split("\n");
        String lastLine = lines[lines.length - 1].trim();

        String expectedOutput = "没有找到桥接词。";
        assertEquals(expectedOutput, lastLine);
    }
}