package zahtevi;

import java.io.Serializable;

public class ZahtevListaPrijava implements Serializable {
    private static final long serialVersionUID = 1L;

    private String jmbg;
    private String emailAdresa;

    public ZahtevListaPrijava() {
    }

    public ZahtevListaPrijava(String jmbg, String emailAdresa) {
        this.jmbg = jmbg;
        this.emailAdresa = emailAdresa;
    }

    public String getJmbg() {
        return jmbg;
    }

    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }

    public String getEmailAdresa() {
        return emailAdresa;
    }

    public void setEmailAdresa(String emailAdresa) {
        this.emailAdresa = emailAdresa;
    }
}
