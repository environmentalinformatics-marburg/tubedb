package tsdb.testing;

//import javax.xml.bind.DatatypeConverter;

import tsdb.util.Util;

public class TestingBytesToHex {
	
	public static void main(String[] args) {
		byte[] bytes = new byte[]{0,-1,1,-100,100, -128, 127};
		
		//System.out.println(DatatypeConverter.printHexBinary(bytes));
		
		System.out.println(Util.bytesToHex(bytes));
	}

}
