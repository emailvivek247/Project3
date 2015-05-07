package com.fdt.ecom.entity.enums;

public enum TransactionType {
	REFUND("REFUND"), CHARGE("CHARGE"), AUTHORIZE("AUTHORIZE"), CAPTURE("CAPTURE");

	private String code = null;

	TransactionType(String code) {
		this.code = code;
	}

	/**
	 * Return a string representation of this TransactionType code.
	 */
	@Override
	public String toString() {
		return code;
	}
}