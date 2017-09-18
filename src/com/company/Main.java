package com.company;

import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    public static HashMap<Character, Integer> letterCount = new HashMap<>();
    public static List<Pair<Character, Integer>> listSortedCount = new ArrayList<>();
    public static HashMap<Byte, Character> encodingMap = new HashMap<>();
    public static HashMap<Character, Byte> charToEncodingMap = new HashMap<>();
    public static HuffmanTreeNode huffmanTree;


    public static void main(String... args) {

        readFileFillHashMap();
        buildHuffTree();

        TreePrinter.printNode(huffmanTree);

        encodeFileTo("file", "encoded");
        decodeFileTo("encoded", "decoded");
    }

    private static void decodeFileTo(String encoded, String decoded) {
        try (FileOutputStream fos = new FileOutputStream(decoded); FileInputStream fin = new FileInputStream(encoded)) {
            byte b;
            byte temp = 0x1;
            while ((b = (byte) fin.read()) != -1) {
                if (!encodingMap.containsKey(b & temp)) {
                    temp = ((byte) (b << 1));
                    continue;
                }
                fos.write(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void encodeFileTo(String input, String output) {
        try (FileOutputStream fos = new FileOutputStream(output); FileInputStream fin = new FileInputStream(input)) {
            int c;
            String binaryRepresentation = "";
            while ((c = (char) fin.read()) != -1) {
                if (!charToEncodingMap.containsKey((char) c)) {
                    System.out.println((char) c + "Key not found!");
                    break;
                }
                binaryRepresentation += Integer.toBinaryString(charToEncodingMap.get((char) c));
                fos.write(charToEncodingMap.get((char) c));
            }
            System.out.println(binaryRepresentation);
            System.out.println(decompressString(binaryRepresentation));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildHuffTree() {

        PriorityQueue<HuffmanTreeNode> queue = new PriorityQueue<>(Comparator.comparing(HuffmanTreeNode::getCount));
        listSortedCount.forEach(pair -> {
            queue.add(new HuffmanTreeNode(true, pair.getKey(), null, null, pair.getValue()));
        });
        for (; ;) {
            if (queue.size() < 2) break;
            HuffmanTreeNode left = queue.poll();
            left.setEncoding((byte) 0x0);
            HuffmanTreeNode right = queue.poll();
            right.setEncoding((byte) 0x1);
            queue.add(new HuffmanTreeNode(false, null, right, left, right.getCount() + left.getCount()));
        }
        huffmanTree = queue.peek();
        printEncoding((byte)0, queue.peek());
        System.out.println(encodingMap);

    }

    private static void printEncoding(Byte encoding, HuffmanTreeNode node) {
        if (encoding == null) {
            encoding = node.getEncoding();
        }
        if (node.isLeaf()) {
            encodingMap.put(encoding, node.getCharacter());
            charToEncodingMap.put(node.getCharacter(), encoding);
            System.out.println(Integer.toBinaryString(encoding) + node.getCharacter());
            return;
        }

        printEncoding((byte) (encoding << 1), node.getLeft());
        printEncoding((byte) ((encoding << 1) | 0x1), node.getRight());

    }

    private static String decompressString(String s) {
        String message = "";
        Byte incoming;
        Byte key = 0;
        for (int i = 0; i < s.length(); i++) {
            incoming = s.charAt(i) == '0' ? (byte)0 : (byte)1;
            key =(byte)(key | incoming);

            if (encodingMap.containsKey(key)) {
                message += encodingMap.get(key);
                key = 0;
                continue;
            }
            key = (byte)(key << 1);
        }
        return message;
    }

    private static void readFileFillHashMap() {
        try (FileInputStream fin = new FileInputStream("file")) {
            int c;
            while ((c = fin.read()) != -1) {
                letterCount.put((char) c, letterCount.getOrDefault((char) c, 0) + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Character> listofChars = new ArrayList<>(letterCount.keySet());
        List<Integer> listofIntegers = new ArrayList<>(letterCount.values());

        for (int i = 0; i < listofChars.size(); i++) {
            listSortedCount.add(new Pair<>(listofChars.get(i), listofIntegers.get(i)));
            Collections.sort(listSortedCount, (a, b) -> b.getValue().compareTo(a.getValue()));
        }
    }
}

class HuffmanTreeNode {

    private boolean leaf;
    private Character character;

    private HuffmanTreeNode right;
    private HuffmanTreeNode left;

    public Integer getCount() {
        return count;
    }

    public boolean isLeaf() {
        return leaf;

    }

    public byte getEncoding() {
        return encoding;
    }

    public Character getCharacter() {
        return character;
    }

    public HuffmanTreeNode getRight() {
        return right;
    }

    public HuffmanTreeNode getLeft() {
        return left;
    }

    private Integer count;
    private byte encoding;

    public void setEncoding(byte encoding) {
        this.encoding = encoding;
    }

    public HuffmanTreeNode(boolean leaf, Character character, HuffmanTreeNode right, HuffmanTreeNode left, Integer count) {
        this.leaf = leaf;
        this.character = character;
        this.right = right;
        this.left = left;
        this.count = count;
    }
}
