package baza;


import java.sql.Connection;
import java.sql.DriverManager;


public class KonekcijaBaze {

    
    public KonekcijaBaze() {
        
    }

    public Connection otvoriKonekciju() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/turnir_2026?useSSL=false&allowPublicKeyRetrieval=true", "root", "");
    }
}
