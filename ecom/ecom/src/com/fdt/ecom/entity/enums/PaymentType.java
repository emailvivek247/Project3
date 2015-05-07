package com.fdt.ecom.entity.enums;

public enum PaymentType {
    RECURRING {
        @Override
        public String toString() {
            return "RECURRING";
        }
    },
    OTC {
        @Override
        public String toString() {
            return "OTC";
        }
    },
    
    WEB {
        @Override
        public String toString() {
            return "WEB";
        }
    }, 
    
    PAYASUGO {
        @Override
        public String toString() {
            return "PAYASUGO";
        }
    }
    
      
    
    
}
