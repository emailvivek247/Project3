package com.fdt.ecom.entity.enums;

public enum SettlementStatusType {
    REFUNDED {
        @Override
        public String toString() {
            return "REFUNDED";
        }
    },
    UNSETTLED {
        @Override
        public String toString() {
            return "UNSETTLED";
        }
    },
    VOIDED {
        @Override
        public String toString() {
            return "VOIDED";
        }
    },
    SETTLED {
        @Override
        public String toString() {
            return "SETTLED";
        }
    }
}
