import java.util.ArrayList;
public class ArtisanService {

    ArrayList<Artisan> artisans = new ArrayList<>();
    
    public void addArtisan( Artisan artisan ){
        artisans.add(artisan);
    }

    public Artisan getArtisanById( int id ){

        for( int i = 0 ; i < artisans.size() ; i++ ){
            if( artisans.get(i).artisanId == id ){
                return artisans.get(i);
            }
        }
        
        return null;
    }
}
