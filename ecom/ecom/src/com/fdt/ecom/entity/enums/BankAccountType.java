package com.fdt.ecom.entity.enums;

public enum BankAccountType {
    SAVING {
        @Override
        public String toString() {
            return "S";
        }
    },
    CHECKING {
        @Override
        public String toString() {
            return "C";
        }
    }    
}
