package carihadis;

/**
 * Kelas untuk memproses kueri dari user
 * @author M. Fauzan Naufan
 */
public class ProsesKueri {
    
    /**
     * Fungsi untuk memproses kueri secara keseluruhan
     * Fungsi ini akan dipanggil oleh main program
     * Fungsi mengembalikan token-token dari kueri
     * @return
     */
    public String Proses() {
        String kueri = Baca();
        return Preproses(kueri);
    }
    
    /**
     * Fungsi untuk membaca kueri dari user
     * @return 
     */
    private String Baca() {
        return null;
    }
    
    /** Fungsi untuk melakukan preproses terhadap kueri
     * 
     * @param kueri
     * @return 
     */
    private String Preproses(String kueri) {
        return null;
    }
}
