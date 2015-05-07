package com.fdt.ecom.entity.enums;

public enum CardType {
    AMEX {
        @Override
        public String toString() {
            return "AMEX";
        }
    },
    DISCOVER {
        @Override
        public String toString() {
            return "DISCOVER";
        }
    },
    VISA {
        @Override
        public String toString() {
            return "VISA";
        }
    },
    MASTER {
        @Override
        public String toString() {
            return "MASTER";
        }
    },
    NA {
        @Override
        public String toString() {
            return "N/A";
        }
    }
}
