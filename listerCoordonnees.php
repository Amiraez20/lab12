<?php
include_once __DIR__ . '/metier/GeoService.php';

header('Content-Type: application/json; charset=utf-8');
$svc = new GeoService();
echo json_encode(["positions" => $svc->recupererTout()]);