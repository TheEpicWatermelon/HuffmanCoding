package com.company;

//imports

import java.io.*;
import java.util.*;


/**
 * Huffman Encoder
 *
 * @author Sasha Maximovitch
 *         Version 3.0
 * @date October 4th,2017
 * Uses Huffman coding to encode any file
 */


public class HuffmanEncoder {

    public static void main(String[] args) throws IOException {
        // check parameters
        if (args.length != 2) {
            System.err.println("Incorrect usage! HuffmanEncoder <file to encode> <output file>");
            System.exit(1);
        }

        double startTime = System.currentTimeMillis();

        // Files for input and output
        String fileName = "HarryPotter1.txt";
        //String fileName = "test.txt";
        String outFileName = args[1];
        System.out.println("Compressing "+fileName+" to "+outFileName);

        // HashMap of frequencies which stores bytes and nodes, gets the hashmap from the getFrequencies method
        Map<Byte, Node> frequencies = getFrequencies(fileName);

        // TEST - PRINT OUT ALL THE FREQUENCIES
        //System.out.println(frequencies);

        // Stores all the nodes from frequencies into an array list
        ArrayList<Node> nodes = new ArrayList<Node>(frequencies.values());
        // TEST - Print out nodes
        //System.out.println(nodes);

        // Get the top node by using the Arraylist of nodes in createTree
        Node topNode = createTree(nodes);

        // create an encoding table to be used when encoding the file
        Map<Byte, String> encodingTable = new HashMap<>();
        createEncodingTable(topNode, "", encodingTable);

        // TEST - Print out the encoding table
        System.out.println(encodingTable.size());
        System.out.println(encodingTable);

        // encode using the encoding table from the input file
        String encoded = encode(fileName, encodingTable);
        // TEST - print out the encoded text
        //System.out.println(encoded);

        // encodes the table to binary representation
        String encodedTable = decodingTableAsBinary(encodingTable,encoded);

        // saves the encoded table and text in the .huff file
        saveToFile(outFileName, encoded, encodedTable);
        double endTime = System.currentTimeMillis();

        double totalTime = (endTime - startTime) /1000.0;
        System.out.println(totalTime + " seconds");


        System.out.println("Finished Compressing");

        // just for testing, decode
        //String original = decode(encoded, decodingTable);
        //System.out.println(original);

    }// main method end

    /**
     * decodingTableAsBinary
     * Format:
     * Byte 1: Length of table(triplets)
     * Byte 2: The number of zeros in the last byte that needs to be skipped, because the file ended
     * Rest is: Byte, length of byte code, and the code of the byte.
     * Version 1.0
     *
     * @param table - encodeTable to be changed to binary
     * @return String representation of binary
     */

    private static String decodingTableAsBinary(Map<Byte, String> table, String encodedString) {

        // String that holds the binary representation of the variable to be returned
        StringBuilder result = new StringBuilder();

        // add length of the table(in pairs)
        result.append(padWithZeros(Integer.toBinaryString(table.size())));
        result.append(padWithZeros(Integer.toBinaryString(getRemainingZeros(encodedString))));

        // get a set of all the keys from the table
        Set<Byte> bytes = table.keySet();

        // add all the table elements to result in binary form, before the code, add the length of the code
        for (Byte b : bytes) {
            result.append(padWithZeros(Integer.toBinaryString(b)));
            result.append(padWithZeros(Integer.toBinaryString((table.get(b)).length())));
            result.append(padWithZeros(table.get(b)));
        }
        return result.toString();
    }// end decodeTableAsBinary

    /**
     * Gives back the total number of zeros at the end of the file to be skipped
     * Version 1.0
     * @param encoded string for the message
     * @return integer for the number of zeros to skip over at the end of the file
     */
    private static int getRemainingZeros(String encoded){
        // gets the remainder of the length of the encoded string by 8, since it will all be divided into bytes of 8
        int remainder = encoded.length() % 8;
        // returns 8 - remainder as that is the number of zeros at the end of the file that must be skipped
        return 8 - remainder;
    }// end getRemainingZeros

    /**
     * Pads given text with leading zeros up to total size of 8(To convert it to binary)
     * Version 1.0
     *
     * @param text - the text that needs to be converted to binary
     * @return String for the binary representation of text
     */
    private static String padWithZeros(String text) {
        int bytesSize = text.length() / 8;
        if ( (text.length() % 8) != 0) {
            bytesSize += 1;
        }
        if (bytesSize == 1){
            String result = "00000000" + text;
            return result.substring(result.length() - 8);
        }else {
            String result = text + "00000000";
            return result.substring(0,8*bytesSize);
        }
        // add text to 8 zeros and then substring the last 8 characters to get binary
    }// end padWithZeros

    /**
     * messageToBytes
     * Takes the encoded text and changes it all to bytes in a byte array
     * Version 1.0
     *
     * @param msg, the text that needs to be encoded to bytes
     * @return byte[], this will include the message in a byte array
     */

    private static byte[] messageToBytes(String msg) {
        // create a byte array to store each chunk of the message
        byte[] arr = new byte[getByteArraySize(msg)];
        // TEST - print out the length of the byte array
        //System.out.println(arr.length);

        // encode message, loop over the arr array
        for (int i = 0; i < arr.length; i++) {
            // creates a chunk of string which is of size 8
            String chunk = getChunk(msg, i);
            // TEST - Print out the chunck of string
            //System.out.println(chunk);
            // Take the chunck and convert it to a byte by using encodeToByte
            byte encodedByte = encodeToByte(chunk);
            // save that byte in the byte array
            arr[i] = encodedByte;

        }
        // return the byte array back
        return arr;
    }// messageToBytes end

    /**
     * saveToFile
     * stores the decoded table and encoded message in a specified .huff file
     * Version 1.0
     *
     * @param outFileName  - the name of the .huff file that will be the output for the encoded text
     * @param encoded      - the text that is encoded and ready to be put into the output file
     * @param encodedTable - the encoded table to be outputted to the file
     * @throws IOException
     */

    private static void saveToFile(String outFileName, String encoded, String encodedTable) throws IOException {
        // Change the encoded message into an array of bytes, 8 bits each
        byte[] arr = messageToBytes(encoded);
        byte[] table = messageToBytes(encodedTable);

        // output the the specified .huff file for the encoded text
        try (OutputStream outputStream = new FileOutputStream(outFileName)) {
            // output the whole byte array into the .huff file in outFileName
            outputStream.write(table);
            outputStream.write(arr);
        }// close the output to outFileName
    }// end of saveToFile

    /**
     * encodeToByte
     * takes a chunk of string which is of 8 length and change it to one byte, it returns that byte
     * Version 1.0
     *
     * @param chunk - the chunk of string that is 8 characters long, it will be converted to a byte
     * @return a byte holding the chunk
     */

    private static byte encodeToByte(String chunk) {
        // if the chunks length is less than 8, produce an error
        if (chunk.length() > 8) {
            System.out.println("*** Error: " + chunk);
        }

        // the end result of the byte is stored in result
        byte result = 0;
        // loops through the whole chunk one by one, it will loop 8 times
        for (int i = 0; i < chunk.length(); i++) {
            // takes the character at position i in chunck
            char c = chunk.charAt(i); // can only be 1 or 0
            // if the character is equal to one we put it in the byte, if it is zero we don't need to shift it since it is already there
            if (c == '1') {
                // creates a temporary byte variable which shifts a one to the left by the chunks length - i -1
                byte b = (byte) (1 << (chunk.length() - i - 1));
                // use or to put the b byte into the end result
                result = (byte) (result | b);
            }
            // TEST - print out the resulting byte
            //System.out.println(Integer.toBinaryString(result));
        }

        // return the final byte
        return result;
    }// end encodeToByte

    /**
     * getChunk
     * takes the encoded message and takes a chunk of 8 characters that was specified and returns it
     * Version 1.0
     *
     * @param encoded - the encoded text
     * @param i       - the chunk number that needs to be returned
     * @return - returns back a string that is a chunk of the encoded message (8 characters)
     */

    private static String getChunk(String encoded, int i) {

        // returns a substring out of the current chunk which is i * 8, 8 characters is one chunk, I specifies which chunk needs to be taken
        return encoded.substring(8 * i, Math.min(8 * i + 8, encoded.length()));// return back String either with 8 characters or the remainder
    }// end getChunk

    /**
     * getByteArraySize
     * takes an encoded text and finds out how many chunks of 8 are inside it, if there is a remainder, it adds one chunk because bytes must have 8 bits
     * Version 1.0
     *
     * @param encoded - the encoded text
     * @return - returns the number of chunks in the encoded message
     */

    private static int getByteArraySize(String encoded) {

        // check for how many sets of 8 are inside the encoded message and stores it
        int set8 = encoded.length() / 8;
        // stores the remainder using the length of the text and modulo 8
        int remainder = encoded.length() % 8;

        // if there is a remainder add one to the total set of 8
        if (remainder != 0) {
            // return the set of 8 plus one because of the remainder
            return set8 + 1;
        } else {// else if there is not remainder, return the total set of 8 only
            return set8;
        }
    }// end getByteArraySize

    /**
     * TEST - decode
     * a test method that decodes the encoded string
     * Version 1.0
     *
     * @param encoded       - the encoded String
     * @param decodingTable - the decoding table
     * @return - returns back the decoded text
     */

    private static String decode(String encoded, Map<String, Character> decodingTable) {
        // text which holds the decoded text
        String text = "";
        // temporary variable which holds a set of the encoded string to be decoded, will be reset once a character is found
        String temp = "";

        // loop through the whole encoded text
        for (int i = 0; i < encoded.length(); i++) {
            // add the current character into temp
            temp += encoded.substring(i, i + 1);

            // takes character from the decoding table, because temp will be a code, if temp is not a valid key, c will be null
            Character c = decodingTable.get(temp);

            // if c is not null
            if (c != null) {
                // add the character to the decoded text
                text += c;
                // reset the temp variable
                temp = "";
            }
        }
        // return the decoded text
        return text;
    }// end decode

    /**
     * encode
     * reads a specified file and uses the encoding table to encode each byte and returns the encoded string
     * Version 1.0
     *
     * @param fileName      - the file that will be read from
     * @param encodingTable - the encoding table with all the bytes with their matching codes
     * @return - returns back the encoded string
     * @throws IOException
     */

    private static String encode(String fileName, Map<Byte, String> encodingTable) throws IOException {

        // String that will hold the encoded string
        StringBuilder encode = new StringBuilder();

        // variable which holds an array of all the bytes from the input file
        byte[] getBytes;
        // creates an inputStream to be used to read the file
        InputStream inputStream = null;
        // try reading the specified inputFile and collecting all the bytes into an array
        try {
            // create a path to the input file
            File inputFile = new File(fileName);
            // set getBytes to the length of the file
            getBytes = new byte[(int) inputFile.length()];
            inputStream = new FileInputStream(inputFile);
            // read all the bytes in the file and store it into getBytes
            inputStream.read(getBytes);
        }finally {
            if (!(inputStream == null))
                inputStream.close();
        }

        //System.out.println(getBytes.length);

        // loop through getByte and encode each byte and add it to the encode string
        for (int i = 0; i < getBytes.length; i++) {
            byte tempByte = getBytes[i];
            //System.out.println(tempByte + "->" + encodingTable.get(tempByte));
            encode.append(encodingTable.get(tempByte));
        }
        // return the encoded string
        return encode.toString();
    }// end encode

    /**
     * getFrequencies
     * Reads a file and stores the bytes and nodes(frequency and characters) into a hashmap
     * Version 1.0
     *
     * @param fileName - name of the file that is to be read from
     * @return Hashmap array with Characters and Nodes
     * @throws IOException
     */

    private static Map<Byte, Node> getFrequencies(String fileName) throws IOException {
        // Creates a hashmap that has Characters as a key and Nodes as the value
        Map<Byte, Node> frequency = new HashMap<>();

        byte[] getBytes;
        InputStream inputStream = null;
        // try reading the specified inputFile and collecting all the bytes into an array
        try {
            File inputFile = new File(fileName);
            getBytes = new byte[(int) inputFile.length()];
            inputStream = new FileInputStream(inputFile);
            inputStream.read(getBytes);
        }finally {
            if (!(inputStream == null))
                inputStream.close();
        }

        // loop through all the bytes and add them to the table, or increase their frequency
        for (int i = 0; i < getBytes.length; i++) {
            // get the current byte in the getBytes array
            byte currentByte = getBytes[i];

            // creates a temporary node that stores a node from frequency of the current byte
            Node node = frequency.get(currentByte);

            // if the current byte does not have its own node, create one
            if (node == null){
                node = new Node();
                node.b = currentByte;
                node.frequency = 0;
            }

            // increase the frequency of the current byte's node by one
            node.frequency += 1;

            // update/add the byte and node to the frequency map
            frequency.put(currentByte, node);
        }

        // TEST - print out the frequency map
        //System.out.println(frequency);

        // returns the HashMap
        return frequency;
    }// getFrequencies ends

    /**
     * sortByFrequency
     * takes a List of nodes and sorts the list in order of the nodes frequency, from least to greatest
     * Version 1.0
     *
     * @param nodes - the array list of nodes
     */

    private static void sortByFrequency(ArrayList<Node> nodes) {
        // Use a comparater to sort the nodes
        nodes.sort(new Comparator<Node>() {
            // compare the frequency of two nodes and switch if the first is bigger than the second
            public int compare(Node o1, Node o2) {
                return o1.frequency - o2.frequency;
            }
        });
    }// sortByFrequency ends

    /**
     * createTree
     * takes the list of nodes and creates a tree and returns the top node back
     * Version 1.0
     *
     * @param nodes - the list of current nodes
     * @return - returns back the top node in the tree
     */

    private static Node createTree(ArrayList<Node> nodes) {
        // sorts all the nodes by lowest to greatest frequency
        sortByFrequency(nodes);
        // TEST - Print out the sorted nodes list
        //System.out.println(nodes);

        // store the size of the nodes list in count
        int count = nodes.size() - 1;
        // loop through the nodes original size
        for (int i = 0; i < count; i++) {
            // take the 2 nodes with the smallest frequency
            Node node1 = nodes.get(0);
            Node node2 = nodes.get(1);

            // create a new node
            Node node = new Node();

            // set the new nodes frequency as the sum of the frequencies of the 2 nodes under
            node.frequency = node1.frequency + node2.frequency;
            // set the left node as the lowest frequency node
            node.left = node1;
            // set the right node as the higher frequency node
            node.right = node2;

            // remove the 2 lowest frequency nodes that were used to create a new node
            nodes.remove(0);
            nodes.remove(0);
            // add the new node to the list of nodes
            nodes.add(node);

            // sort all the nodes again
            sortByFrequency(nodes);
            // TEST - print out the iteration and the list of nodes
            //System.out.println(i + " " + nodes);
        }

        // return the top node, which will be the only node in this list
        return nodes.get(0);
    }// end createTree

    /**
     * createEncodingTable
     * A recursive function which takes the top node and finds the leafs of the tree and creates codes for each character of the leaf
     *
     * @param node - the top node of the tree
     * @param code - the code for each character will be stored here recursively, string cannot be immolated so every time it goes back in the recursion the string goes back to the original
     * @param map  - the map that holds the character and its code
     */

    private static void createEncodingTable(Node node, String code, Map<Byte, String> map) {
        // if the current node has a character
        if (node.b != null) {
            // TEST - print out the character with its code
            //System.out.println(node.c+"="+code);
            // put the character and its code into the map
            map.put(node.b, code);
            // return back
            return;
        }

        // if there is a left node
        if (node.left != null) {
            // use recursion and set the node as the node.left of the current node, add 0 to the code (0 is for left in tree), and put in the same map as before
            createEncodingTable(node.left, code + "0", map);
        }

        // if there is a right node
        if (node.right != null) {
            // use recursion and set the node as the node.right of the current node, add 1 to the code (1 is for right in tree), and put in the same map as before
            createEncodingTable(node.right, code + "1", map);
        }
    }// end createEncodingTable
}// main class close