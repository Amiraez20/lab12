<?php
include_once __DIR__.'/../repository/ICrud.php';
include_once __DIR__.'/../modele/Coordonnee.php';
include_once __DIR__.'/../db/BaseDonnees.php';

class GeoService implements ICrud {
    private $bd;
    public function __construct(){ $this->bd = new BaseDonnees(); }

    public function ajouter($coordonnee){
        $requete = "INSERT INTO position(latitude,longitude,date,imei)
                    VALUES (?,?,?,?)";
        $stmt = $this->bd->getPdo()->prepare($requete);
        $stmt->execute([
            $coordonnee->getLat(),
            $coordonnee->getLng(),
            $coordonnee->getHorodatage(),
            $coordonnee->getDeviceId()
        ]);
        return true;
    }
    public function recupererTout(){
        $stmt = $this->bd->getPdo()->prepare("SELECT * FROM position");
        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
    public function modifier($e){}
    public function supprimer($e){}
    public function trouverParId($id){}
}