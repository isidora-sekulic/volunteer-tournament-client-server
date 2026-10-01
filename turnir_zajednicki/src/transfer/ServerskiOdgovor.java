package transfer;

import enums.StatusOdgovora;
import java.io.Serializable;


public class ServerskiOdgovor implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private StatusOdgovora status;
    private String poruka;
    private Object podaci;

    
    private byte[] prilogBajtovi;
    private String prilogNazivFajla;

    public ServerskiOdgovor() {
        
    }

    public ServerskiOdgovor(StatusOdgovora status, String poruka) {
        this.status = status;
        this.poruka = poruka;
    }

    public static ServerskiOdgovor uspeh(String poruka) {
        return new ServerskiOdgovor(StatusOdgovora.OK, poruka);
    }

    public static ServerskiOdgovor greska(String poruka) {
        return new ServerskiOdgovor(StatusOdgovora.ERROR, poruka);
    }

    public StatusOdgovora getStatus() {
        return status;
    }

    public void setStatus(StatusOdgovora status) {
        this.status = status;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public Object getPodaci() {
        return podaci;
    }

    public void setPodaci(Object podaci) {
        this.podaci = podaci;
    }

    public byte[] getPrilogBajtovi() {
        return prilogBajtovi;
    }

    public void setPrilogBajtovi(byte[] prilogBajtovi) {
        this.prilogBajtovi = prilogBajtovi;
    }

    public String getPrilogNazivFajla() {
        return prilogNazivFajla;
    }

    public void setPrilogNazivFajla(String prilogNazivFajla) {
        this.prilogNazivFajla = prilogNazivFajla;
    }

    public boolean isUspeh() {
        return status == StatusOdgovora.OK;
    }
}
