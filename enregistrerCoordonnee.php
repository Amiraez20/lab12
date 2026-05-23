<?php
header('Content-Type: application/json; charset=utf-8');
if($_SERVER["REQUEST_METHOD"] !== "POST"){
    http_response_code(405);
    echo json_encode(["succes"=>false,"message"=>"POST requis"]);
    exit;
}
include_once __DIR__.'/metier/GeoService.php';
include_once __DIR__.'/modele/Coordonnee.php';

$lat        = $_POST['latitude']  ?? null;
$lng        = $_POST['longitude'] ?? null;
$horodatage = $_POST['date']      ?? null;
$deviceId   = $_POST['imei']      ?? null;
$adresseIp  = $_SERVER['REMOTE_ADDR'];

if(!$lat||!$lng||!$horodatage||!$deviceId){
    http_response_code(400);
    echo json_encode(["succes"=>false,"message"=>"Params manquants","ip"=>$adresseIp]);
    exit;
}
try {
    $svc = new GeoService();
    $svc->ajouter(new Coordonnee(null,$lat,$lng,$horodatage,$deviceId));
    echo json_encode(["succes"=>true,"ip"=>$adresseIp]);
} catch(Exception $ex){
    http_response_code(500);
    echo json_encode(["succes"=>false,"message"=>$ex->getMessage(),"ip"=>$adresseIp]);
}