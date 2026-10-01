package baza;

import model.Korisnik;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UpitiKorisnik {

    private KonekcijaBaze konekcijaBaze;

    public UpitiKorisnik(KonekcijaBaze konekcijaBaze) {
        this.konekcijaBaze = konekcijaBaze;
    }

    public Korisnik nadjiPoKorisnickomImenu(String korisnickoIme) throws Exception {
        String sql = "SELECT korisnicko_ime, lozinka, ime, prezime, jmbg, email_adresa FROM korisnici WHERE korisnicko_ime = ?";
        try (Connection c = konekcijaBaze.otvoriKonekciju();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, korisnickoIme);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Korisnik k = new Korisnik();
                k.setKorisnickoIme(rs.getString("korisnicko_ime"));
                k.setLozinka(rs.getString("lozinka"));
                k.setIme(rs.getString("ime"));
                k.setPrezime(rs.getString("prezime"));
                k.setJmbg(rs.getString("jmbg"));
                k.setEmailAdresa(rs.getString("email_adresa"));
                return k;
            }
        }
    }

    public void sacuvaj(Korisnik k) throws Exception {
        String sql = "INSERT INTO korisnici(korisnicko_ime, lozinka, ime, prezime, jmbg, email_adresa) VALUES(?,?,?,?,?,?)";
        try (Connection c = konekcijaBaze.otvoriKonekciju();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k.getKorisnickoIme());
            ps.setString(2, k.getLozinka());
            ps.setString(3, k.getIme());
            ps.setString(4, k.getPrezime());
            ps.setString(5, k.getJmbg());
            ps.setString(6, k.getEmailAdresa());
            ps.executeUpdate();
        }
    }
}
