package transfer;

import enums.Operacija;
import java.io.Serializable;


public class KlijentskiZahtev implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Operacija operacija;
    private Object parametar;

    public KlijentskiZahtev() {
    }

    public KlijentskiZahtev(Operacija operacija, Object parametar) {
        this.operacija = operacija;
        this.parametar = parametar;
    }

    public Operacija getOperacija() {
        return operacija;
    }

    public void setOperacija(Operacija operacija) {
        this.operacija = operacija;
    }

    public Object getPayload() {
        return parametar;
    }

    public void setPayload(Object payload) {
        this.parametar = payload;
    }
}
