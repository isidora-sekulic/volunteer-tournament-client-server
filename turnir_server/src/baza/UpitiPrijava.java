package baza;

import enums.Pozicija;
import enums.Smena;
import model.PrijavaVolontiranja;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpitiPrijava {

    private final KonekcijaBaze konekcijaBaze;

    public UpitiPrijava(KonekcijaBaze konekcijaBaze) {
        this.konekcijaBaze = konekcijaBaze;
    }

    public List<PrijavaVolontiranja> ucitajSve() throws Exception {
        String sql = "SELECT * FROM prijave_volontiranja";
        try (Connection c = konekcijaBaze.otvoriKonekciju();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PrijavaVolontiranja> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapiraj(rs));
            }
            return lista;
        }
    }

    public void sacuvaj(PrijavaVolontiranja p) throws Exception {
        String sql = "INSERT INTO prijave_volontiranja(id, korisnicko_ime, ime, prezime, jmbg, email_adresa, datum_volontiranja, smena, pozicija, kreirano, otkazano) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = konekcijaBaze.otvoriKonekciju();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getId().toString());
            ps.setString(2, p.getKorisnickoIme());
            ps.setString(3, p.getIme());
            ps.setString(4, p.getPrezime());
            ps.setString(5, p.getJmbg());
            ps.setString(6, p.getEmailAdresa());
            ps.setObject(7, p.getDatumVolontiranja());
            ps.setString(8, p.getSmena().name());
            ps.setString(9, p.getPozicija().name());
            ps.setObject(10, p.getKreirano());
            ps.setBoolean(11, p.isOtkazano());
            ps.executeUpdate();
        }
    }

    public boolean postaviOtkazano(UUID idPrijave, boolean otkazano) throws Exception {
        String sql = "UPDATE prijave_volontiranja SET otkazano=? WHERE id=?";
        try (Connection c = konekcijaBaze.otvoriKonekciju();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, otkazano);
            ps.setString(2, idPrijave.toString());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean izmeni(UUID idPrijave, LocalDate noviDatum, Smena novaSmena, Pozicija novaPozicija) throws Exception {
        String sql = "UPDATE prijave_volontiranja SET datum_volontiranja=?, smena=?, pozicija=? WHERE id=?";
        try (Connection c = konekcijaBaze.otvoriKonekciju();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, noviDatum);
            ps.setString(2, novaSmena.name());
            ps.setString(3, novaPozicija.name());
            ps.setString(4, idPrijave.toString());
            return ps.executeUpdate() == 1;
        }
    }

    private PrijavaVolontiranja mapiraj(ResultSet rs) throws Exception {
        PrijavaVolontiranja p = new PrijavaVolontiranja();
        p.setId(UUID.fromString(rs.getString("id")));
        p.setKorisnickoIme(rs.getString("korisnicko_ime"));
        p.setIme(rs.getString("ime"));
        p.setPrezime(rs.getString("prezime"));
        p.setJmbg(rs.getString("jmbg"));
        p.setEmailAdresa(rs.getString("email_adresa"));
        p.setDatumVolontiranja(rs.getObject("datum_volontiranja", LocalDate.class));
        p.setSmena(Smena.valueOf(rs.getString("smena")));
        p.setPozicija(Pozicija.valueOf(rs.getString("pozicija")));
        p.setKreirano(rs.getObject("kreirano", LocalDateTime.class));
        p.setOtkazano(rs.getBoolean("otkazano"));
        return p;
    }
}
