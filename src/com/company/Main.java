package com.company;

import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final int BATCH_SIZE = 2048; // bytes to keep in memo 2MB?
    public static HashMap<Character, Integer> letterCount = new HashMap<>();
    public static List<Pair<Character, Integer>> listSortedCount = new ArrayList<>();

    public static HashMap<Character, String> encodeMap = new HashMap<>();
    public static HashMap<String, Character> decodeMap = new HashMap<>();

    public static List<HuffmanTreeNode> leafNodes = new ArrayList<>();
    public static HuffmanTreeNode huffmanTree;


    public static void main(String... args) {

        readFileFillHashMap();
        buildHuffTree();
        walkTreePopulateAddress(huffmanTree, "");
        createMaps(huffmanTree);

        TreePrinter.printNode(huffmanTree);

        encodeFileTo("file", "encoded");
        decodeFileTo("encoded", "decoded");
    }

    private static void walkTreePopulateAddress(HuffmanTreeNode node, String address) {
        if (node == null) return;
        node.setAddress(address);
        walkTreePopulateAddress(node.getLeft(), address + "0");
        walkTreePopulateAddress(node.getRight(), address + "1");
    }

    private static void decodeFileTo(String encoded, String decoded) {
        String bytes = getBytes(encoded);
        writeDecodedBytesToFile(bytes, decoded);
    }

    private static void writeDecodedBytesToFile(String bytes, String decoded) {
        try (FileOutputStream fos = new FileOutputStream(decoded)) {
            String key = "";
            String message = "";
            for (int i = 0; i < bytes.length(); i++) {
                key += bytes.charAt(i);
                if (decodeMap.containsKey(key)) {
                    message += decodeMap.get(key);
                    key = "";
                }

                if (message.length() > BATCH_SIZE) { //batch
                    fos.write(message.getBytes());
                    fos.flush();
                    message = "";
                }
            }
            fos.write(message.getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getBytes(String encoded) {
        String bytes = "";

        try (FileInputStream fin = new FileInputStream(encoded)) {
            int c = fin.read();
            while (c != -1) {
                bytes += String.format("%7s", Integer.toBinaryString(c)).replace(' ', '0').substring(0);
                c = fin.read();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static void encodeFileTo(String input, String output) {
        String binary = binaryRepresentationFromFile(input);
        writeBinaryStringtoFile(binary, output);
    }

    private static String binaryRepresentationFromFile(String input) {
        String binaryRepresentation = "";
        try (FileInputStream fin = new FileInputStream(input)) {
            int c = fin.read();
            while (c != -1) {
                if (!encodeMap.containsKey((char) c)) {
                    System.out.println((char) c + "Key not found!");
                    break;
                }
                binaryRepresentation += encodeMap.get((char) c);
                c = fin.read();
            }

            System.out.println(binaryRepresentation);
            System.out.println(decompressString(binaryRepresentation));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return binaryRepresentation;
    }

    private static void writeBinaryStringtoFile(String binary, String output) {

        //convert to binary
        try (FileOutputStream fos = new FileOutputStream(output)) {
            List<String> byteChunks = Arrays.asList(binary.split("(?<=\\G.{7})"));

            for (String byteChunk : byteChunks) {
                Byte encodedByte = Byte.parseByte(byteChunk, 2);
                fos.write(encodedByte);
                //TODO: add batching here! :)
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void buildHuffTree() {

        PriorityQueue<HuffmanTreeNode> queue = new PriorityQueue<>(Comparator.comparing(HuffmanTreeNode::getCount));
        listSortedCount.forEach(pair -> {
            HuffmanTreeNode node = new HuffmanTreeNode(true, pair.getKey(), null, null, pair.getValue());
            leafNodes.add(node); // this is to keep track of the leaf nodes
            queue.add(node);
        });
        for (; ; ) {
            if (queue.size() < 2) break;
            HuffmanTreeNode left = queue.poll();
            left.setEncoding((byte) 0x0);
            HuffmanTreeNode right = queue.poll();
            right.setEncoding((byte) 0x1);
            queue.add(new HuffmanTreeNode(false, null, right, left, right.getCount() + left.getCount()));
        }
        huffmanTree = queue.peek();

    }

    private static void createMaps(HuffmanTreeNode node) {
        leafNodes.forEach(leaf -> {
            decodeMap.put(leaf.getAddress(), leaf.getCharacter());
            encodeMap.put(leaf.getCharacter(), leaf.getAddress());
        });

    }

    private static String decompressString(String s) {
        String message = "";
        String key = "";
        for (int i = 0; i < s.length(); i++) {
            key += Character.toString((char) s.charAt(i));
            if (decodeMap.containsKey(key)) {
                message += decodeMap.get(key);
                key = "";
                continue;
            }
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
    private String address = "";

    private HuffmanTreeNode right;
    private HuffmanTreeNode left;

    public Integer getCount() {
        return count;
    }

    public boolean isLeaf() {
        return leaf;

    }

    public String getAddress() {
        return address;
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

    public void setAddress(String s) {
        address = s;
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
