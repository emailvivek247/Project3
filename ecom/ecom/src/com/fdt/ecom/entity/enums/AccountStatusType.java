package com.fdt.ecom.entity.enums;

public enum AccountStatusType {
    ACTIVE {
        @Override
        public String toString() {
            return "ACTIVE";
        }
    },
    INACTIVE {
        @Override
        public String toString() {
            return "INACTIVE";
        }
    }
}
