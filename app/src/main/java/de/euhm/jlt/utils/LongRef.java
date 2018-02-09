/**
 * $Id: LongRef.java 45 2015-01-20 21:11:44Z hmueller $
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.euhm.jlt.utils;


/**
 * Class to get a reference to a long value
 * @author hmueller
 * @version $Rev: 45 $
 */
public class LongRef {

	// The value of the LongRef.
	public long value;
	
	/**
	 * constructor
	 */
	public LongRef(long value) {
		 this.value = value;
	}

	/**
	 * @return the value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(long value) {
		this.value = value;
	}

}
