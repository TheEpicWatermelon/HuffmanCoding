package com.company;

//imports
import java.io.*;
import java.util.*;


/**
 * Huffman Encoder
 * Version 2.0
 * @author Sasha Maximovitch
 * @date October 4th,2017
 * Uses Huffman coding to encode ASCII based files
 */

// Main Class start
public class Main {
    static final char TERM_CHAR = '~';

    // main method start
    public static void main(String[] args) throws IOException {
        String fileName = "test.txt";
        String outFileName = "text.huff";

        // HashMap of frequencies which stores characters and nodes, gets the hashmap from the getFrequencies method
        Map<Character, Node> frequencies = getFrequencies(fileName);
        // TEST - Print out frequencies
        Node term = new Node();
        term.c = TERM_CHAR;
        term.frequency = 1;
        frequencies.put(TERM_CHAR, term);

        System.out.println(frequencies);

        // Stores all the nodes from frequencies into an array list
        ArrayList<Node> nodes = new ArrayList<Node>(frequencies.values());
        // TEST - Print out nodes
        System.out.println(nodes);

        Node topNode = createTree(nodes);

        Map<Character,String> encodingTable = new HashMap<>();
        createEncodingTable(topNode, "", encodingTable);
        System.out.println(encodingTable);

        // encode
        String encoded = encode(fileName, encodingTable);
        System.out.println(encoded);

        Map<String, Character> decodingTable = createDecodingTable(encodingTable);
        byte[] decodingTableAsByte = tableToByte(decodingTable);

        //saveToFile(outFileName,encoded, decodingTableAsString);


        // just for testing, decode
        String original = decode(encoded, decodingTable);
        System.out.println(original);

    }// main method end

    // Encode decoding table into byte array
    // first byte always term character
    // then bytes in pair - code/char
    private static byte[] tableToByte(Map<String, Character> decodingTable) {
        byte[] result = new byte[2 * decodingTable.size() + 1];

        Set<String> keysTemp = decodingTable.keySet();
        int count = 1;
        for(String key: keysTemp){
            Character c = decodingTable.get(key);
            if (c == TERM_CHAR){
                result[0] = encodeToByte(Integer.toString(c, 2));
            }
            result[count] = encodeToByte(key);
            result[count+1] = encodeToByte(Integer.toString(c, 2));
            count += 2;
        }
        return result;
    }

    private static byte[] messageToBytes(String msg) {
        byte[] arr = new byte[getByteArraySize(msg)];
        System.out.println(arr.length);

        // encode message
        for (int i = 0; i < arr.length; i++) {
            String chunk = getChunk(msg, i);
            System.out.println(chunk);
            byte encodedByte = encodeToByte(chunk);
            arr[i] = encodedByte;

        }
        return arr;
    }

    private static void saveToFile(String outFileName, String encoded, String decodingTable) throws IOException {
        byte[] arr = messageToBytes(encoded);
        byte[] table = messageToBytes(decodingTable);

        try(OutputStream outputStream = new FileOutputStream(outFileName)){
            outputStream.write(arr);
            outputStream.write(table);
        }
    }

    private static byte encodeToByte(String chunk) {
        if (chunk.length() > 8) {
            System.out.println("*** Error: "+chunk);
        }

        byte result = 0;
        for(int i = 0; i < chunk.length(); i++) {
            char c = chunk.charAt(i); // can only be 1 or 0
            if (c == '1') {
                byte b = (byte)(1 << (chunk.length() - i - 1));
                result = (byte) (result | b);
            }
            //System.out.println(Integer.toBinaryString(result));
        }

        //byte value = Byte.parseByte(chunk,2);
        return result;
    }

    private static String getChunk(String encoded, int i) {

        // return back String either with 8 characters or the remainder
        return encoded.substring(8*i, Math.min(8*i+8, encoded.length()));
    }


    private static int getByteArraySize(String encoded) {

        int set8 = encoded.length()/8;
        int remainder = encoded.length() % 8;

        if (remainder != 0){
            return set8 + 1;
        }else {
            return set8;
        }
    }

    private static Map<String, Character> createDecodingTable(Map<Character, String> encodingTable){
        Map<String, Character> decodingTable = new HashMap<>();

        Set<Character> keys = encodingTable.keySet();
        for(Character c: keys) {
            decodingTable.put(encodingTable.get(c),c);
        }
        System.out.println(decodingTable);

        return  decodingTable;
    }

    private static String decode(String encoded, Map<String, Character> decodingTable) {
        String text = "";
        String temp = "";

        for (int i = 0; i < encoded.length(); i++) {
            temp += encoded.substring(i,i+1);

            Character c = decodingTable.get(temp);

            if(c != null){
                text += c;
                temp = "";
            }
        }
        return text;
    }

    private static String encode(String fileName, Map<Character, String> encodingTable) throws IOException{

        String encode = "";

        try(BufferedReader inputStream = new BufferedReader(new FileReader(fileName))){

            int i;

            while((i = inputStream.read()) != -1){
                char c = (char)i;

                encode += encodingTable.get(c);
            }
        }

        // add termination term
        encode += encodingTable.get(TERM_CHAR);
        return encode;

    }


    /**
     * getFrequencies
     * Reads a file and stores the characters and nodes(frequency and characters) into a hashmap
     * Version 1.0
     * @author Sasha Maximovitch
     * @param fileName - name of the file that is to be read from
     * @return Hashmap array with Characters and Nodes
     * @throws IOException
     */

    private static Map<Character, Node> getFrequencies(String fileName) throws IOException {
        // Creates a hashmap that has Characters as a key and Nodes as the value
        Map<Character,Node> frequency = new HashMap<>();

        // try reading the file that is selecte
        try(BufferedReader inputStream = new BufferedReader(new FileReader(fileName))) {

            // create an int to see if there is more lines left in the file
            int i;
            // while there is more lines left repeat
            while((i = inputStream.read()) != -1) {
                // changes the ASCII to an actual character
                char c = (char)i;

                // Creates a temporary node that stores a node from the frequency of the current character
                Node node = frequency.get(c);

                // if that character doesn't have a node create a new one
                if (node == null) {
                    // create a new node
                    node = new Node();
                    // set the character to that node as c
                    node.c = c;
                    // set the frequency of the new node as 0
                    node.frequency = 0;
                }// end while

                // increase the frequency of that character by one
                node.frequency += 1;
                // update/add the character and node to the frequency map
                frequency.put(c,node);
            }
        } // try

        //System.out.println(frequency);

        // returns the HashMap
        return  frequency;


    }// getFrequencies end

    private static void sortByFrequency(ArrayList<Node> nodes){
        nodes.sort(new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                return o1.frequency - o2.frequency;
            }
        });
    }

    private static Node createTree(ArrayList<Node> nodes){
        sortByFrequency(nodes);
        System.out.println(nodes);

        int count = nodes.size()-1;
        for (int i = 0; i < count; i++) {
            Node node1 = nodes.get(0);
            Node node2 = nodes.get(1);

            Node node = new Node();

            node.frequency = node1.frequency + node2.frequency;
            node.left = node1;
            node.right = node2;

            nodes.remove(0);
            nodes.remove(0);
            nodes.add(node);

            sortByFrequency(nodes);
            System.out.println(i + " " + nodes);
        }

        return nodes.get(0);
    }

    private static void createEncodingTable(Node node, String code, Map<Character, String> map){
        if  (node.c != null){
            //System.out.println(node.c+"="+code);
            map.put(node.c, code);
            return;
        }

        if (node.left != null){
            createEncodingTable(node.left, code+"0",map);
        }

        if (node.right != null){
            createEncodingTable(node.right, code+"1",map);
        }
    }
}// main class close