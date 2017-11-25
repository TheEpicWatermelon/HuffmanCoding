package com.company;

//imports

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Huffman Decoder
 *
 * @author Sasha Maximovitch
 *         Version 5.0
 * @date 10/11/2017
 * Decodes a .huff file into any specified file
 */


public class HuffmanDecoder {

    // a variable to hold the location of where the table ends in the .huff file
    static int endTable = 0;

    public static void main(String[] args) throws IOException {

        // welcomes the user
        JOptionPane.showMessageDialog(null, "Welcome to the .huff decoder");

        // get the encoded file name from the user
        String fileName =  JOptionPane.showInputDialog(null, "Please input the name of the .huff file to be decoded (don't add the .huff extension)");

        // get the output file from the user
        String outFileName = JOptionPane.showInputDialog(null, "Now, please input the name of the file you want encode file to decode to (add extension)");

        // error if one or both filenames are not inputed
        if ( (outFileName == null) || (fileName == null) || (outFileName.equals("")) || (fileName.equals("")) ){
            JOptionPane.showMessageDialog(null, "EXITING: NO FILE INPUTTED");
            System.exit(1);
        }

        // start decompressing
        JOptionPane.showMessageDialog(null, "Starting to decompress the file.");

        // get time for when the code started
        double startTime = System.currentTimeMillis();

        // get an array of bytes for the whole file
        byte[] data = getByte(fileName);

        // create a table for each byte
        Map<String, Byte> table = getTable(data);

        // create an array list of the file without a table
        ArrayList<Byte> encodedByte = getEncoded(data);
        // an array list to hold the decoded bytes
        ArrayList<Byte> decodedByte = decode(encodedByte, table, (int) data[1]);

        //save the decoded bytes in the specified file
        saveToFile(decodedByte, outFileName);

        // get time for when the code ended
        double endTime = System.currentTimeMillis();

        // calculate and print out the total time of the program
        double totalTime = (endTime - startTime) / 1000.0;        JOptionPane.showMessageDialog(null, "Finished Decoding\nTime to compress: " + totalTime + " seconds");
    }// end main

    /**
     * saveToFile
     * saves all the decoded bytes into an output file
     * Version 1.0
     *
     * @param decoded  the decoded byte arraylist
     * @param fileName the name of the output file
     * @throws IOException
     */

    private static void saveToFile(ArrayList<Byte> decoded, String fileName) throws IOException {
        FileOutputStream output = null;
        // create an array of bytes that will hold the same bytes from the arraylist of bytes (do this because you cannot print out an arraylist)
        byte[] decodedArray = new byte[decoded.size()];
        for (int i = 0; i < decodedArray.length; i++) {
            decodedArray[i] = decoded.get(i);

        }

        // TEST - print out the length of both arraylist and array to see if they are equal
        //System.out.println("length of arrayList: " + decoded.size());
        //System.out.println("Length of array" + decodedArray.length);

        // print the decoded bytes to a specified file
        try {
            File file = new File(fileName);
            output = new FileOutputStream(file);
            output.write(decodedArray);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }// end saveToFile

    /**
     * decode
     * gets all the encoded bytes, decodes them and stores them in an array list
     * Version 3.0
     *
     * @param encoded
     * @param table
     * @param remainingZeros
     * @return
     */

    private static ArrayList<Byte> decode(ArrayList<Byte> encoded, Map<String, Byte> table, int remainingZeros) {
        // hold the resulting list of bytes
        ArrayList<Byte> result = new ArrayList<Byte>();
        // string builder that holds a code that is being built and checked
        StringBuilder tempString = new StringBuilder();
        // TEST - print out the size of the encoded file
        //System.out.println("SIZE: " + encoded.size() );
        // loop through all the encoded file and decode
        for (int i = 0; i < encoded.size(); i++) {
            // get the current byte
            byte currentByte = encoded.get(i);
            // if the current byte is not the last byte
            if (i != encoded.size() - 1) {
                // loop through the byte
                for (int j = 7; j >= 0; j--) {
                    // get a temporary byte, then and it with the current byte, return 0 or 1 as appropriate
                    byte tempByte = (byte) Math.pow(2, j);
                    if ((tempByte & currentByte) != 0) {
                        tempString.append("1");
                    } else {
                        tempString.append("0");
                    }
                    // checks if the current tempString represents a code for a byte
                    Byte b = table.get(tempString.toString());
                    // if it represents a byte, add that byte to the decoded arraylist and reset the tempstring
                    if (b != null) {
                        //System.out.println(tempString + "->" + b);
                        result.add(b);
                        tempString = new StringBuilder();
                    }
                }// else if it is the last byte of the array
            } else {
                // only read the remaining bits, skips the leading
                for (int j = 7 - remainingZeros; j >= 0; j--) {
                    // get a temporary byte, then and it with the current byte, return 0 or 1 as appropriate
                    byte tempByte = (byte) Math.pow(2, j);
                    if ((tempByte & currentByte) != 0) {
                        tempString.append("1");
                    } else {
                        tempString.append("0");
                    }
                    // checks if the current tempString represents a code for a byte
                    Byte b = table.get(tempString.toString());
                    // if it represents a byte, add that byte to decoded and reset tempString
                    if (b != null) {
                        //System.out.println(tempString + "->" + b);
                        result.add(b);
                        tempString.delete(0, tempString.length());
                    }
                }
            }
        }// return the decoded byte arraylist
        return result;
    }// end decode

    /**
     * getEncoded
     * get the content from the data array, this excludes the table
     * Version 1.0
     *
     * @param data - holds all the bytes from the encoded file
     * @return an array list of bytes that holds the content of the encoded file
     */

    private static ArrayList<Byte> getEncoded(byte[] data) {
        // hold the arraylist of bytes for content and add all the content
        ArrayList<Byte> encoded = new ArrayList<Byte>();
        for (int i = endTable; i < data.length; i++) {
            encoded.add(data[i]);

        }// return all the encoded content
        return encoded;
    }// end getEncoded

    /**
     * getCode
     * get the code of a byte to be used in the table for decoding
     * Version 3.0
     *
     * @param data          an array that holds all the information in the file
     * @param startPosition the position in data where the code starts
     * @param remainder     the remainder of the code, will need another byte to be read
     * @param numberOfBytes the total number of bytes the code is in
     * @return a string for the code of the byte
     */

    private static String getCode(byte[] data, int startPosition, int remainder, int numberOfBytes) {
        // string builder that holds the temporary code
        StringBuilder tempCode = new StringBuilder();
        // if there is more than one byte
        if (numberOfBytes > 1) {
            // loop through all the bytes
            for (int i = 0; i < numberOfBytes; i++) {
                // get the current byte
                byte currentByte = data[startPosition + i];
                // if this is the last byte to be read
                if (i == (numberOfBytes - 1)) {
                    // if the remainder is zero, change it to 8 so that it will read 8 bits
                    if (remainder == 0) {
                        remainder = 8;
                    }// loop through the array
                    for (int j = 7; j > 7 - remainder; j--) {
                        // temporary byte that will use and with the current byte to add to the code
                        byte temp = (byte) Math.pow(2, j);
                        if ((temp & currentByte) != 0) {
                            tempCode.append("1");
                        } else {
                            tempCode.append("0");
                        }
                    }// if it is not the last byte
                } else {
                    // loop through the whole byte
                    for (int j = 7; j >= 0; j--) {
                        // temporary byte that will use and with the current byte to add to the code
                        byte temp = (byte) Math.pow(2, j);
                        if ((temp & currentByte) != 0) {
                            tempCode.append("1");
                        } else {
                            tempCode.append("0");
                        }
                    }
                }

            }// if there is only one byte that holds the entire code
        } else {
            // if the remainder is zero, change it to 8 so it will read 8 bits
            if (remainder == 0) {
                remainder = 8;
            }
            // get the current byte
            byte currentByte = data[startPosition];
            // loop through the whole byte
            for (int i = remainder - 1; i >= 0; i--) {
                // temporary byte that will use and with the current byte to add to the code
                byte temp = (byte) Math.pow(2, i);
                if ((temp & currentByte) != 0) {
                    tempCode.append("1");
                } else {
                    tempCode.append("0");
                }
            }
        }// return the code for the byte
        return tempCode.toString();
    }// end getCode

    /**
     * getTable
     * returns a map of the table to be used when decoding
     * Version 5.0
     *
     * @param data an array of bytes that holds all the information in the encoded file
     * @return a map with a string code for a key, and a byte for the value
     */

    private static Map<String, Byte> getTable(byte[] data) {
        // get the length of the table, if its 0 then length has to be 256 (the maximum length)
        int lengthOfTable = (int) data[0];
        if (lengthOfTable == 0) {
            lengthOfTable = 256;
        }
        // create a map that will hold the table
        Map<String, Byte> tempTable = new HashMap();
        // start the pointer at two as it skips the length of the table and remaining zeros in data
        int pointer = 2;
        // while the table is not complete yet
        while (tempTable.size() < lengthOfTable) {
            // get the byte
            byte b = data[pointer];
            // get the length of the code at the next byte
            int lengthOfCode = (byte) data[pointer + 1];
            // get the number of bytes the code is in
            int numberOfBytes = lengthOfCode / 8;
            int remainder = lengthOfCode % 8;
            if (remainder != 0) {
                numberOfBytes += 1;
            }// increase the pointer by 2 to make it point at the beginning of the code
            pointer += 2;
            // get the code of the byte using the getCode method
            String code = getCode(data, pointer, remainder, numberOfBytes);
            // store the code and the byte it represents into the table
            tempTable.put(code, b);
            // TEST - print out the code and its byte that it represents
            //System.out.println(code+"->"+b);
            // increase the pointer by the length of the code of the current byte
            pointer += numberOfBytes;
        }
        // TEST - print the length of the table
        //System.out.println(lengthOfTable);
        // saves the last pointer as the end of the table, so that when an arraylist of all the content is made it will know when to start
        endTable = pointer;
        // return the table
        return tempTable;
    }// end getTable

    /**
     * getByte
     * get all the information from the encoded file and store it in a byte array
     * Version 1.0
     *
     * @param fileName the name of the encoded file
     * @return an array of bytes that stores all the information in the encoded file
     * @throws IOException
     */

    private static byte[] getByte(String fileName) throws IOException {
        // the array to hold all the bytes from the encoded file
        byte[] getBytes;
        InputStream input = null;
        // take all the bytes from the encoded file and save it to the byte array
        try {
            File file = new File(fileName);
            // set the size of the byte array to the length of the file
            getBytes = new byte[(int) file.length()];
            input = new FileInputStream(file);
            // get all the bytes from the file
            input.read(getBytes);
        } finally {
            if (input != null) {
                input.close();
            }
        }// return the array of bytes for all the information in the encoded file
        return getBytes;
    }// end getByte
}// end HuffmanDecoder
