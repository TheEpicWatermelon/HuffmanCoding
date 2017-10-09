package com.company;

//imports
import java.io.*;
import java.util.*;


/**
 * Huffman Encoder
 * Version 3.0
 * @author Sasha Maximovitch
 * @date October 4th,2017
 * Uses Huffman coding to encode ASCII based files
 */

// Main Class start
public class Main {
    // Tilda will be our terminating character
    static final char TERM_CHAR = '~';

    // main method start
    public static void main(String[] args) throws IOException {
        // Files for input and output, text.txt is for input and text.huff is the output
        String fileName = "test.txt";
        String outFileName = "text.huff";

        // HashMap of frequencies which stores characters and nodes, gets the hashmap from the getFrequencies method
        Map<Character, Node> frequencies = getFrequencies(fileName);

        // CREATE A TERMINATION CHARACTER WITH ITS OWN NODE
        Node term = new Node();
        term.c = TERM_CHAR;
        // Frequency one for the termination character
        term.frequency = 1;
        frequencies.put(TERM_CHAR, term);

        // TEST - PRINT OUT ALL THE FREQUENCIES
        //System.out.println(frequencies);

        // Stores all the nodes from frequencies into an array list
        ArrayList<Node> nodes = new ArrayList<Node>(frequencies.values());
        // TEST - Print out nodes
        //System.out.println(nodes);

        // Get the top node by using the Arraylist of nodes in createTree
        Node topNode = createTree(nodes);

        // create an encoding table to be used when encoding the file
        Map<Character,String> encodingTable = new HashMap<>();
        createEncodingTable(topNode, "", encodingTable);

        // TEST - Print out the encoding table
        //System.out.println(encodingTable);

        // encode using the encoding table from the input file
        String encoded = encode(fileName, encodingTable);
        // TEST - print out the encoded text
        //System.out.println(encoded);

        // create decodingTable that will be put in a text file for the decoder to read
        Map<String, Character> decodingTable = createDecodingTable(encodingTable);

        // saves the encoded message in the .huff file, and saves the decoding table in a text, all for the decoder to use
        saveToFile(outFileName,encoded, decodingTable);


        // just for testing, decode
        //String original = decode(encoded, decodingTable);
        //System.out.println(original);

    }// main method end

    /**
     * messageToBytes
     * Takes the encoded text and changes it all to bytes in a byte array
     * @author Sasha Maximovitch
     * Version 1.0
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
     * Takes the decoding table and outputs it into table.txt to be used by decoder, then it takes the encoded message and stores it in the .huff output file
     * @author Sasha Maximovitch
     * Version 1.0
     * @param outFileName - the name of the .huff file that will be the output for the encoded text
     * @param encoded - the text that is encoded and ready to be put into the output file
     * @param decodingTable - the decoding table that will be used in table.txt for the decoder to decode the encoded message
     * @throws IOException
     */

    private static void saveToFile(String outFileName, String encoded, Map<String, Character> decodingTable) throws IOException {
        // Change the encoded message into an array of bytes, 8 bits each
        byte[] arr = messageToBytes(encoded);

        // get a set off all the keys from the decoding table
        Set<String> tableKeys = decodingTable.keySet();
        // set a variable which will store the key for the termination character
        String termKey = "";

        // loop through all the keys and find which one stores the terminating character, then save the key
        for (String key: tableKeys){
            // if the key is storing the terminating character
            if (decodingTable.get(key).equals(TERM_CHAR)){
                // save the key into termKey
                termKey = key;
            }
        }

        // output the whole table into table.txt for decoding
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("table.txt"))){

            // the first thing in the file will always be the termination key with the tilda
            writer.write(termKey + "=" + "~");
            // create a new line in the file
            writer.newLine();

            // loop through all the keys
            for (String key: tableKeys){
                // if it finds the termination key then skip over it
                if (key.equals(termKey)){
                    continue;
                } else {
                    // else, write out the key with the character it is associated with, put an equal sign in between
                    writer.write(key + "=" + decodingTable.get(key));
                    // create a new line
                    writer.newLine();
                }
            }
        }// close the output to table.txt

        // output the the specified .huff file for the encoded text
        try(OutputStream outputStream = new FileOutputStream(outFileName)){
            // output the whole byte array into the .huff file in outFileName
            outputStream.write(arr);
        }// close the output to outFileName
    }// end of saveToFile

    /**
     * encodeToByte
     * takes a chunk of string which is of 8 length and change it to one byte, it returns that byte
     * @author Sasha Maximovitch
     * Version 1.0
     * @param chunk - the chunck of string that is 8 characters long, it will be converted to a byte
     * @return a byte holding the chunk
     */

    private static byte encodeToByte(String chunk) {
        // if the chuncks length is less than 8, produce an error
        if (chunk.length() > 8) {
            System.out.println("*** Error: "+chunk);
        }

        // the end result of the byte is stored in result
        byte result = 0;
        // loops through the whole chunk one by one, it will loop 8 times
        for(int i = 0; i < chunk.length(); i++) {
            // takes the character at position i in chunck
            char c = chunk.charAt(i); // can only be 1 or 0
            // if the character is equal to one we put it in the byte, if it is zero we don't need to shift it since it is already there
            if (c == '1') {
                // creates a temporary byte variable which shifts a one to the left by the chunks length - i -1
                byte b = (byte)(1 << (chunk.length() - i - 1));
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
     * getChunck
     * takes the encoded message and takes a chunk of 8 characters that was specified and returns it
     * @author Sasha Maximovitch
     * Version 1.0
     * @param encoded - the encoded text
     * @param i - the chunk number that needs to be returned
     * @return - returns back a string that is a chunk of the encoded message (8 characters)
     */

    private static String getChunk(String encoded, int i) {

        // returns a substring out of the current chunk which is i * 8, 8 characters is one chunk, I specifies which chunk needs to be taken
        return encoded.substring(8*i, Math.min(8*i+8, encoded.length()));// return back String either with 8 characters or the remainder
    }// end getChunk

    /**
     * getByteArraySize
     * takes an encoded text and finds out how many chunks of 8 are inside it, if there is a remainder, it adds one chunk because bytes must have 8 bits
     * @author Sasha Maximovitch
     * Version 1.0
     * @param encoded - the encoded text
     * @return - returns the number of chunks in the encoded message
     */

    private static int getByteArraySize(String encoded) {

        // check for how many sets of 8 are inside the encoded message and stores it
        int set8 = encoded.length()/8;
        // stores the remainder using the length of the text and modulo 8
        int remainder = encoded.length() % 8;

        // if there is a remainder add one to the total set of 8
        if (remainder != 0){
            // return the set of 8 plus one because of the remainder
            return set8 + 1;
        }else {// else if there is not remainder, return the total set of 8 only
            return set8;
        }
    }// end getByteArraySize

    /**
     * createDecodingTable
     * takes the encoding table and switches the keys to value and the values to keys to help with decoding
     * @author Sasha
     * Version 1.0
     * @param encodingTable - the encoding table with characters as keys and String as values
     * @return - returns a map that is the reverse of the encoding table
     */

    private static Map<String, Character> createDecodingTable(Map<Character, String> encodingTable){
        // create a new map for the decoding table
        Map<String, Character> decodingTable = new HashMap<>();

        // get all the keys from the encoding table
        Set<Character> keys = encodingTable.keySet();
        // loop through all the keys
        for(Character c: keys) {
            // set the decoding table by getting the value from each key in the encoding table and putting it as the key and putting the encoding key as the value in the decoding table
            decodingTable.put(encodingTable.get(c),c);
        }
        // TEST - prints out the whole decoding table
        //System.out.println(decodingTable);

        // return the decodingTable map
        return  decodingTable;
    }// end createDecodingTable

    /**
     * TEST - decode
     * a test method that decodes the encoded string
     * @author Sasha Maximovitch
     * Version 1.0
     * @param encoded - the encoded String
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
            temp += encoded.substring(i,i+1);

            // takes character from the decoding table, because temp will be a code, if temp is not a valid key, c will be null
            Character c = decodingTable.get(temp);

            // if c is not null
            if(c != null){
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
     * reads a specified file and uses the encoding table to encode each character and returns the encoded string
     * @author Sasha Maximovitch
     * Version 1.0
     * @param fileName - the file that will be read from
     * @param encodingTable - the encoding table with all the characters with their matching codes
     * @return - returns back the encoded string
     * @throws IOException
     */

    private static String encode(String fileName, Map<Character, String> encodingTable) throws IOException{

        // String that will hold the encoded string
        String encode = "";

        // read through the specified file
        try(BufferedReader inputStream = new BufferedReader(new FileReader(fileName))){

            // integer that will be used to store values from the file, will be -1 when the file ends
            int i;

            // while the file is not finsihed
            while((i = inputStream.read()) != -1){
                // store i as a char in the char value
                char c = (char)i;

                // use the encoding table to find the code for the current character and add it to the encode string
                encode += encodingTable.get(c);
            }
        }

        // add termination term to the encode string
        encode += encodingTable.get(TERM_CHAR);
        // return the encoded string
        return encode;
    }// end encode

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

        // TEST - print out the frequency map
        //System.out.println(frequency);

        // returns the HashMap
        return  frequency;
    }// getFrequencies ends

    /**
     * sortByFrequency
     * takes a List of nodes and sorts the list in order of the nodes frequency, from least to greatest
     * @author Sasha Maximovitch
     * Version 1.0
     * @param nodes - the array list of nodes
     */

    private static void sortByFrequency(ArrayList<Node> nodes){
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
     * @author Sasha Maximovitch
     * Version 1.0
     * @param nodes - the list of current nodes
     * @return - returns back the top node in the tree
     */

    private static Node createTree(ArrayList<Node> nodes){
        // sorts all the nodes by lowest to greatest frequency
        sortByFrequency(nodes);
        // TEST - Print out the sorted nodes list
        //System.out.println(nodes);

        // store the size of the nodes list in count
        int count = nodes.size()-1;
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
     * @param node - the top node of the tree
     * @param code - the code for each character will be stored here recursively, string cannot be immolated so every time it goes back in the recursion the string goes back to the original
     * @param map - the map that holds the character and its code
     */

    private static void createEncodingTable(Node node, String code, Map<Character, String> map){
        // if the current node has a character
        if  (node.c != null){
            // TEST - print out the character with its code
            //System.out.println(node.c+"="+code);
            // put the character and its code into the map
            map.put(node.c, code);
            // return back
            return;
        }

        // if there is a left node
        if (node.left != null){
            // use recursion and set the node as the node.left of the current node, add 0 to the code (0 is for left in tree), and put in the same map as before
            createEncodingTable(node.left, code+"0",map);
        }

        // if there is a right node
        if (node.right != null){
            // use recursion and set the node as the node.right of the current node, add 1 to the code (1 is for right in tree), and put in the same map as before
            createEncodingTable(node.right, code+"1",map);
        }
    }// end createEncodingTable
}// main class close