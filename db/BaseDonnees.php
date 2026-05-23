<?php
class BaseDonnees {
    private $pdo;
    public function __construct(){
        $serveur     = 'localhost';
        $base        = 'localisation';
        $utilisateur = 'root';
        $motDePasse  = '';
        try {
            $chaine    = "mysql:host=$serveur;dbname=$base;charset=utf8";
            $this->pdo = new PDO($chaine,$utilisateur,$motDePasse,[
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
            ]);
        } catch(Exception $ex){
            die('Connexion impossible : '.$ex->getMessage());
        }
    }
    public function getPdo(){ return $this->pdo; }
}