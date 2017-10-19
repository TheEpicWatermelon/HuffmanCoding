package com.company;

//imports

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Huffman Decoder
 *
 * @author Israel Shpilman
 *         Version 3.0
 * @date 10/11/2017
 * Decodes a .huff file into a .txt file
 */


public class HuffmanDecoder {

    static int endTable = 0;

    public static void main(String[] args) throws IOException {
        // variables
        String fileName = "test.huff"; // imports the file
        System.out.println("File imported");
        byte[] data = getByte(fileName);// imports the file data into a byte array
        System.out.println("Array made");
        Map<String, Byte> table = new HashMap();// a map that stores the string code value of a character as a string and the character is taken from the file
        System.out.println("Map made");

        System.out.println(data[1]);
        table = getTable(data);
        System.out.println("got table");
        System.out.println(table);

        ArrayList<Byte> encodedByte = getEncoded(data);
        System.out.println("Got encodedBytes");
        ArrayList<Byte> decodedByte;
        int remainingZeros = (int)data[1];
        System.out.println("created decoded array");
        decodedByte = decode(encodedByte, table, remainingZeros);

        //Test outputs
        /*System.out.println(termCode);
		System.out.println(table.size());
		System.out.println(encodedText);
		System.out.println(decodedText);
		*/

        //saving decoded text to a file
        System.out.println("Start file output");
        saveToFile(decodedByte, "Decoded.png");
        System.out.println("End file output");


    }

    private static void saveToFile(ArrayList<Byte> decoded, String fileName) throws IOException {
        FileOutputStream output = null;
        byte[] decodedArray = new byte[decoded.size()];
        for (int i = 0; i < decodedArray.length; i++) {
            decodedArray[i] = decoded.get(i);

        }

        System.out.println("length of arrayList: " + decoded.size());
        System.out.println("Length of array" + decodedArray.length);

        try {
            File file = new File(fileName);
            output = new FileOutputStream(file);
            output.write(decodedArray);
        } finally {
            output.close();
        }


    }

    private static ArrayList<Byte> decode(ArrayList<Byte> encoded, Map<String, Byte> table, int remainingZeros) {
        ArrayList<Byte> result = new ArrayList<Byte>();
        StringBuilder tempString = new StringBuilder();
        System.out.println("SIZE: " + encoded.size() );
        for (int i = 0; i < encoded.size(); i++) {
            byte currentByte = encoded.get(i);
            if (i != encoded.size()-1) {
                for (int j = 7; j >= 0; j--) {
                    byte tempByte = (byte) Math.pow(2, j);
                    if ((tempByte & currentByte) != 0) {
                        tempString.append("1");
                    } else {
                        tempString.append("0");
                    }
                    Byte b = table.get(tempString.toString());
                    if (b != null) {
                        //System.out.println(tempString + "->" + b);
                        result.add(b);
                        tempString.delete(0,tempString.length());
                    }
                }
            }else {
                for (int j = 7-remainingZeros; j >= 0; j--){
                    byte tempByte = (byte) Math.pow(2, j);
                    if ((tempByte & currentByte) != 0) {
                        tempString.append("1");
                    } else {
                        tempString.append("0");
                    }
                    Byte b = table.get(tempString.toString());
                    if (b != null) {
                        //System.out.println(tempString + "->" + b);
                        result.add(b);
                        tempString.delete(0,tempString.length());
                    }
                }
            }
        }
        return result;
    }

    private static ArrayList<Byte> getEncoded(byte[] data) {
        ArrayList<Byte> encoded = new ArrayList<Byte>();
        for (int i = endTable; i < data.length; i++) {
            encoded.add(data[i]);

        }
        return encoded;
    }

    private static String getText(byte b) {
        String tempText = "";
        byte temp = 0;

        for (int i = 7; i >= 0; i--) {
            temp = (byte) Math.pow(2, i);
            if ((temp & b) != 0) {
                tempText += "1";
            } else {
                tempText += "0";
            }
        }
        return tempText;
    }

    private static String getCode(byte[] data, int startPosition, int remainder, int numberOfBytes) {
        StringBuilder tempCode = new StringBuilder();

        if (numberOfBytes > 1) {
            for (int i = 0; i < numberOfBytes; i++) {
                byte currentByte = data[startPosition + i];
                if (i == (numberOfBytes - 1)) {
                    if (remainder == 0){
                        remainder = 8;
                    }
                    for (int j = 7; j > 7 - remainder; j--) {
                        byte temp = (byte) Math.pow(2, j);
                        if ((temp & currentByte) != 0) {
                            tempCode.append("1");
                        } else {
                            tempCode.append("0");
                        }
                    }
                } else {
                    for (int j = 7; j >= 0; j--) {
                        byte temp = (byte) Math.pow(2, j);
                        if ((temp & currentByte) != 0) {
                            tempCode.append("1");
                        } else {
                            tempCode.append("0");
                        }
                    }
                }

            }
        }else {
            if (remainder == 0){
                remainder = 8;
            }
            byte currentByte = data[startPosition];
            for (int i = remainder - 1; i >= 0  ; i--) {
                byte temp = (byte) Math.pow(2,i);
                if ( (temp & currentByte) != 0){
                    tempCode.append("1");
                } else{
                    tempCode.append("0");
                }
            }
        }
        return tempCode.toString();
    }

    private static Map<String, Byte> getTable(byte[] data) {
        int lengthOfTable = (int)data[0];
        if (lengthOfTable == 0){
            lengthOfTable = 256;
        }
        System.out.println("l " + lengthOfTable);
        Map<String, Byte> tempTable = new HashMap();
        int pointer = 2;
        while (tempTable.size() < lengthOfTable) {
            byte b = data[pointer];
            int lengthOfCode = (byte) data[pointer + 1];
            int numberOfBytes = lengthOfCode / 8;
            int remainder = lengthOfCode % 8;
            if (remainder != 0){
                numberOfBytes += 1;
            }
            pointer += 2;
            String code = getCode(data, pointer, remainder, numberOfBytes);
            tempTable.put(code, b);
            //System.out.println(code+"->"+b);
            pointer += numberOfBytes;
        }
        //System.out.println(lengthOfTable);
        endTable = pointer;
        return tempTable;
    }

    private static byte[] getByte(String fileName) throws IOException {
        byte[] getBytes;
        InputStream input = null;
        try {
            File file = new File(fileName);
            getBytes = new byte[(int) file.length()];
            input = new FileInputStream(file);
            input.read(getBytes);
        } finally {
            input.close();
        }
        return getBytes;
    }
}