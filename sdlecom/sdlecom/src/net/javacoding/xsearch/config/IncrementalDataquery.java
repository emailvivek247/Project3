package net.javacoding.xsearch.config;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class IncrementalDataquery extends Dataquery {

    public IncrementalDataquery() {
        name = "Incremental";
    }
    
    public void setTimestamp(PreparedStatement ps, Timestamp ts) throws SQLException {
        if(this.sql!=null && this.sql.indexOf("?")>0) {
            int counter = 0;
            int x = -1;
            while((x=this.sql.indexOf("?", x+1))>=0) {
                counter++;
            }
            for(int i=0;i<counter;i++) {
                ps.setTimestamp(i+1, ts);
            }
        }
    }

}
