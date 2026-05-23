<?php
class Coordonnee {
    private $identifiant;
    private $lat;
    private $lng;
    private $horodatage;
    private $deviceId;

    function __construct($identifiant,$lat,$lng,$horodatage,$deviceId){
        $this->identifiant = $identifiant;
        $this->lat         = $lat;
        $this->lng         = $lng;
        $this->horodatage  = $horodatage;
        $this->deviceId    = $deviceId;
    }
    function getIdentifiant() { return $this->identifiant; }
    function getLat()         { return $this->lat; }
    function getLng()         { return $this->lng; }
    function getHorodatage()  { return $this->horodatage; }
    function getDeviceId()    { return $this->deviceId; }

    function setIdentifiant($v){ $this->identifiant=$v; }
    function setLat($v)        { $this->lat=$v; }
    function setLng($v)        { $this->lng=$v; }
    function setHorodatage($v) { $this->horodatage=$v; }
    function setDeviceId($v)   { $this->deviceId=$v; }
}