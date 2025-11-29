package hsmed;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Hashage {
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            byte[] saltAndHash = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
            System.arraycopy(hashedPassword, 0, saltAndHash, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(saltAndHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static boolean verifyPassword(String password, String hash) {
        try {
            if (hash == null || hash.trim().isEmpty()) {
                System.out.println("ERREUR: Hash vide ou null");
                return false;
            }
            
            byte[] saltAndHash = Base64.getDecoder().decode(hash);
            
            if (saltAndHash.length < 48) {
                System.out.println("ERREUR: Hash invalide - taille insuffisante: " + saltAndHash.length + " bytes");
                return false;
            }
            
            byte[] salt = new byte[16];
            System.arraycopy(saltAndHash, 0, salt, 0, 16);
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            for (int i = 0; i < hashedPassword.length; i++) {
                if (hashedPassword[i] != saltAndHash[16 + i]) {
                    return false;
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("ERREUR: Hash mal encodé en Base64: " + e.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("ERREUR: Algorithme SHA-256 non disponible: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("ERREUR inconnue lors de la vérification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void main(String[] args) {
        // Test :
        String mdpMedecin = "medecin123";
        String mdpSecretaire = "secretaire123";
        
        String hashMedecin = hashPassword(mdpMedecin);
        String hashSecretaire = hashPassword(mdpSecretaire);
        
        System.out.println("BDD :\n");
        
        System.out.println("Medecin");
        System.out.println("INSERT INTO utilisateur (matUtili, nomUtili, prenomUtili, mdpUtili) VALUES (10001, 'Kaci', 'Mohand Ameziane', '" + hashMedecin + "');");
        System.out.println("INSERT INTO medecin (matMed, specialite, adresseCabinet) VALUES (10001, 'Cardiologue', '20 Rue du congrès - Ouzellaguen');\n");
        
        System.out.println("Secretaire");
        System.out.println("INSERT INTO utilisateur (matUtili, nomUtili, prenomUtili, mdpUtili) VALUES (20001, 'Benfares', 'Lyna', '" + hashSecretaire + "');");
        System.out.println("INSERT INTO secretaire (matSec) VALUES (20001);\n");
        
        System.out.println("Test :\n");
        System.out.println("Médecin (medecin123): " + verifyPassword(mdpMedecin, hashMedecin));
        System.out.println("Secrétaire (secretaire123): " + verifyPassword(mdpSecretaire, hashSecretaire));
    }
}