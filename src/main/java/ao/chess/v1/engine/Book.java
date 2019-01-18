package ao.chess.v1.engine; /**
 *  Book class by Yves Catineau 
 */

import java.io.*;
import java.util.Random;
import java.util.zip.ZipInputStream;

public class Book {
	
	/** Constante pour definir le nombre maximal de variantes. */
	private static final int MAX_VARIANTS_LENGTH = 2324;
	/** Ce champ stocke le generateur de nombre aleatoire. */
	private static final Random rdm = new Random(System.currentTimeMillis());
	/** Ce champ stocke les variantes triees de la bibliotheque d'ouvertures. */
	private static String[] variantes;
	/** Ce champ stocke la taille de la bibliotheque d'ouverture. */
	private static int size;
	/** singleton book */
	static Book eInstance = null;
	
	
	
	/**
	 * Constructeur de folie de la classe Book
	 * */
	private Book() throws IOException {
		// This constructor is private in order to prevent object instanciation
	}
	
	
	
	// Creates only once the instance of the Object Singleton
	public static Book getInstance() throws IOException{
		if(eInstance == null){
			eInstance = new Book();
		}
		return eInstance;
	}
	
	
	
	/**
	 * Initialisation de la bibliotheque
	 * */
	public void readBook() {
		variantes = new String[MAX_VARIANTS_LENGTH];
		size = 0;
		
		BufferedReader fileReader = null;
        InputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        ZipInputStream zipInputStream = null;
    	boolean finish = false;
        try {
        		//ouverture du fichier en entree
            	fileInputStream = Book.class.getResourceAsStream("book.zip");
            	if(fileInputStream == null)
            	{
            		System.out.println("Error: book.zip was not found, make sure it is in the same directory as");
            		System.out.println("Book.class (or the executable). The book will not be used in this session");
            		return;
            	}
            	//ouverture du fichier dans le buffer
            	bufferedInputStream = new BufferedInputStream(fileInputStream);
            	//ouverture du fichier archive Zip en entree
            	zipInputStream = new ZipInputStream(bufferedInputStream);
         } 
         catch(Exception e) {
        	 	e.printStackTrace();
         }
        
         //positionner le stream sur l'entree suivante
         try {
            	zipInputStream.getNextEntry();
         }
         catch (Exception e) {
                e.printStackTrace();
         }
		
         //initialisation du reader du fichier contenu dans l'archive
         fileReader  = new BufferedReader(new InputStreamReader(zipInputStream)); 

         if (!finish) {
			BufferedReader br = new BufferedReader(fileReader);
	
			String line;
			try	{
				line = br.readLine();
			}
			catch (IOException e) {
				line = null;
			}
	
			//parcours de la bibliotheque et ajout dans le champ variants
			while (line != null) {
				variantes[size] = line;
				size++;
				try
				{
					line = br.readLine();
				}
				catch (IOException e)
				{
					line = null;
					finish = true;
				}
			}
		}
         
         //fermeture des streams
         try {
             fileReader.close();
             fileInputStream.close();
             bufferedInputStream.close();
             zipInputStream.close();
         	}
         catch (Exception e) {
             e.printStackTrace();
         }
		
	}
	
	
	/**
	 * Cette methode retourne le mouvement connu dans la bibliotheque.
	 * @param movesList Ce parametre stocke les coups joues.
	 * @return La methode retourne le deplacement suivant si present dans la bibliotheque d'ouverture.
	 */
	public static String getBestMove(String movesList) {

		//si la longueur de la chaine est superieure la variante
		//il n'y a rien faire : IA du moteur prendre la releve ;)
		if (movesList.length() > MAX_VARIANTS_LENGTH) {
			return "";
		}

		int i = 0;
		while (i < size && (!variantes[i].startsWith(movesList) || variantes[i].equals(movesList)))	{
			i++;
		}

		if (i >= size) {
			return "";
		}

		int j = i;
		while (j < size && variantes[j].startsWith(movesList))
			j++;

		int choice = i + rdm.nextInt(j - i);
		int lengthMoves = movesList.length();
		return variantes[choice].substring(lengthMoves,lengthMoves + 4);
	}
}
