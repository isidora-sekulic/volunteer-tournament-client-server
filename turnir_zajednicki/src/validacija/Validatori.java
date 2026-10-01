package validacija;

import java.util.regex.Pattern;

public class Validatori {
    
    private Validatori() {
    }

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static boolean jePrazno(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean jeValidanJmbg(String jmbg) {
        if (jmbg == null) return false;
        String t = jmbg.trim();
        return t.matches("\\d{13}");
    }

    public static boolean jeValidanEmail(String email) {
        if (email == null) return false;
        return EMAIL.matcher(email.trim()).matches();
    }
}
