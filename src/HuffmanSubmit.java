
/*
 * Joseph Hur
 * jhur3
 * Class ID: 67
 * Project 3
 */

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class HuffmanSubmit implements Huffman {
	private static final int R = 256;
	public static HashMap<String, String> freqMap = new HashMap<String, String>();
	public static HashMap<Character, Integer> charMap = new HashMap<Character, Integer>();

	public static class Node implements Comparable<Node> { // class for node for Huffman tree
		public char ch;
		public int freq;
		public Node left, right;

		Node(char ch, int freq, Node left, Node right) {
			this.ch = ch;
			this.freq = freq;
			this.left = left;
			this.right = right;
		}

		public boolean isLeaf() {// check if node is leaf
			assert ((left == null) && (right == null)) || ((left != null) && (right != null));
			return ((left == null) && (right == null));
		}

		public int compareTo(Node node) {
			return this.freq - node.freq;
		}
	}

	private static class HuffManComparator implements Comparator<Node> {
		@Override
		public int compare(Node node1, Node node2) {
			return node1.freq - node2.freq;
		}
	}

	public void makeFreqMap(Node root) {// creates freqmap with binary representation of chars
		if (!root.isLeaf()) {
			makeFreqMap(root.left);
			makeFreqMap(root.right);
		} else {
			String temp = Integer.toBinaryString(root.ch);
			while (temp.length() < 8) {
				temp = "0" + temp;
			}
			freqMap.put(temp, Integer.toString(root.freq));
		}
	}

	// uses freqMap to create freqFile
	public void makeFreqFile(HashMap<String, String> freqMap, String freqFile) throws IOException {
		BufferedWriter outputWriter = null;
		outputWriter = new BufferedWriter(new FileWriter(freqFile));
		for (String ch : freqMap.keySet()) {

			outputWriter.write(ch + ":" + freqMap.get(ch));
			outputWriter.newLine();
		}
		outputWriter.flush();
		outputWriter.close();
	}

	public void encode(String inputFile, String outputFile, String freqFile) {//encode method
		System.out.println("Attempting encoding...");
		BinaryIn BinIn = new BinaryIn(inputFile);
		String s = BinIn.readString();
		char[] input = s.toCharArray();

		int[] freq = new int[R];
		for (int i = 0; i < input.length; i++) {
			freq[input[i]]++;
		}

		Node root = buildTree(freq);
		makeFreqMap(root);
		try {
			makeFreqFile(freqMap, freqFile);
		} catch (IOException e) {
			System.out.println("Cannot make freqFile");
		}
		System.out.println("Successfully created freqFile...");

		String[] st = new String[R];
		buildCode(st, root, "");

		BinaryOut BinOut = new BinaryOut(outputFile);
		BinOut.write(input.length);

		for (int i = 0; i < input.length; i++) {
			String code = st[input[i]];
			for (int j = 0; j < code.length(); j++) {
				if (code.charAt(j) == '0') {
					BinOut.write(false);
				} else if (code.charAt(j) == '1') {
					BinOut.write(true);
				} else
					throw new IllegalStateException("Illegal state");
			}
		}
		BinOut.flush();
		BinOut.close();
		System.out.println("Successfully encoded : " + inputFile + " | Output file is : " + outputFile);
	}

	public static Node buildTree(int[] freq) {//builds huffman tree from int array of frequencies
		PriorityQueue<Node> pq = new PriorityQueue<Node>();
		for (char i = 0; i < R; i++) {
			if (freq[i] >= 1) {
				pq.add(new Node(i, freq[i], null, null));
			}
		}

		if (pq.size() == 1) {
			if (freq['\0'] == 0)
				pq.add(new Node('\0', 0, null, null));
			else
				pq.add(new Node('\1', 0, null, null));
		}

		while (pq.size() > 1) {
			Node left = pq.poll();
			Node right = pq.poll();
			Node parent = new Node('\0', left.freq + right.freq, left, right);
			pq.add(parent);
		}
		return pq.poll();

	}

	public static void buildCode(String[] st, Node x, String s) {//builds code for output writer
		if (!x.isLeaf()) {
			buildCode(st, x.left, s + '0');
			buildCode(st, x.right, s + '1');
		} else {
			st[x.ch] = s;
		}
	}

	public void decode(String inputFile, String outputFile, String freqFile) {//decode method
		System.out.println("Attempting decoding...");
		BinaryIn BinIn = new BinaryIn(inputFile);
		int length = BinIn.readInt();
		try {
			readFreq(freqFile);
		} catch (IOException e) {
			System.out.println("Error reading frequency file!");
		}
		System.out.println("Successfully read freqFile...");
		Node root = buildTree(charMap);
		BinaryOut BinOut = new BinaryOut(outputFile);
		for (int i = 0; i < length; i++) {
			Node x = root;
			while (!x.isLeaf()) {
				boolean bit = BinIn.readBoolean();
				if (bit) {
					x = x.right;
				} else {
					x = x.left;
				}
			}
			BinOut.write(x.ch, 8);
		}
		BinOut.flush();
		BinOut.close();
		System.out.println("Successfully decoded : " + inputFile + " | Output file is : " + outputFile);
	}

	public static void readFreq(String freqFile) throws IOException {//reads freqFile to create charMap
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(freqFile));
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split(":", 2);
			if (parts.length >= 2) {
				String keytemp = parts[0];
				String valuetemp = parts[1];
				char ch = '0';
				int value = Integer.parseInt(valuetemp);
				for (int i = 0; i < keytemp.length() / 8; i++) {
					int a = Integer.parseInt(keytemp.substring(8 * i, (i + 1) * 8), 2);
					ch = (char) (a);
				}
				charMap.put(ch, value);
			} else {
				System.out.println("ignoring line: " + line);
			}
		}
		reader.close();
	}

	public static Node buildTree(HashMap<Character, Integer> map) {//uses charMap to recreate Huffman tree
		final Queue<Node> nodeQueue = createNodeQueue(map);

		while (nodeQueue.size() > 1) {
			final Node node1 = nodeQueue.remove();
			final Node node2 = nodeQueue.remove();
			Node node = new Node('\0', node1.freq + node2.freq, node1, node2);
			nodeQueue.add(node);
		}

		return nodeQueue.remove();
	}

	public static Queue<Node> createNodeQueue(Map<Character, Integer> map) { //helper method to create pq for buildTree
		final Queue<Node> pq = new PriorityQueue<Node>(11, new HuffManComparator());
		for (Entry<Character, Integer> entry : map.entrySet()) {
			pq.add(new Node(entry.getKey(), entry.getValue(), null, null));
		}
		return pq;
	}

	public static void main(String[] args) {
		Huffman huffman = new HuffmanSubmit();
		huffman.encode("ur.jpg", "ur.enc", "freq.txt");
		huffman.decode("ur.enc", "ur_dec.jpg", "freq.txt");

		// After decoding, both ur.jpg and ur_dec.jpg should be the same.
		// On linux and mac, you can use `diff' command to check if they are the same.
	}

}
