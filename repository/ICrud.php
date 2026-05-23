<?php
interface ICrud {
    public function ajouter($entite);
    public function modifier($entite);
    public function supprimer($entite);
    public function trouverParId($id);
    public function recupererTout();
}